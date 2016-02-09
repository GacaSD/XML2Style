package com.xml2style;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class XML2StyleDialog extends DialogWrapper {
    private JPanel contentPane;
    private JButton btnConvert;
    private JButton btnClose;
    private JTextArea txtInput;
    private JTextArea txtOutput;
    private JTextField textStyleName;
    private JButton destroyStyleButton;
    private JCheckBox splitFromParentCheckBox;
    private com.intellij.openapi.editor.Document mDocument;
    private VirtualFile styleVirtualFile;
    private Editor mEditor;

    public XML2StyleDialog(AnActionEvent event) {
        super(null);
        setModal(true);
        getRootPane().setDefaultButton(btnConvert);
        setTitle("XML2Style");

        // Get data
        mEditor = event.getData(PlatformDataKeys.EDITOR);

        // Get document
        if (mEditor != null) {

            // Get file and parent
            try {
                PsiFile data = event.getData(LangDataKeys.PSI_FILE);
                mDocument = data.getViewProvider().getDocument();

                // Is from layout folder
                VirtualFile parent = data.getVirtualFile().getParent();
                if (parent.equals("layout")) {
                    styleVirtualFile = parent.findChild("values").findChild("styles.xml");
                } else if (parent.equals("values")){

                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // Set actions to the buttons
            btnClose.addActionListener(e -> onCancel());

            // Get selected text from current file
            txtInput.setText(mEditor.getSelectionModel().getSelectedText());
        }

        // Enable buttons
        if (txtInput.getText().contains("<item name=")) {
            destroyStyleButton.addActionListener(e -> onDestroy());
            btnConvert.setEnabled(false);
        } else {
            btnConvert.addActionListener(e -> onConvert());
            destroyStyleButton.setEnabled(false);
        }

        // Init
        init();
    }

    private void onConvert() {

        // Convert
        String inputText = txtInput.getText();
        String[] result = xml2Style(inputText);
        String styleNameToReplace = result[0];
        String parentName = result[1];
        String convertedText = result[2];

        // Show converted text
        txtOutput.setText(convertedText);

        // Find indexToPutStyle of selected text
        String styleGivenName = textStyleName.getText();
        if (mDocument != null && !styleGivenName.equals("")) {

            // Replace text with style
            String documentText = mDocument.getText();
            ApplicationManager.getApplication().runWriteAction(() -> {
                mDocument.setText(documentText.replace(inputText, styleNameToReplace));
            });

            // Add style in style.xml file
            Document styleDocument = FileDocumentManager.getInstance().getDocument(styleVirtualFile);
            final int indexToPutStyle = getProperIndexForStyle(styleDocument.getText(), parentName);

            // Put as new style
            new WriteCommandAction(mEditor.getProject()) {
                @Override
                protected void run(Result result) throws Throwable {
                    styleDocument.insertString(indexToPutStyle, "\n\n" + convertedText + "\n");
                }
            }.execute();
        } else {
            textStyleName.setBackground(Color.decode("#fa8888"));
        }
    }

    private int getProperIndexForStyle(String allStyleText, String parentName) {

        // Find parent style
        String termForParent = "name=\"" + parentName + "\"";

        // Check parent
        int indexToPutStyle = allStyleText.indexOf("</resources>");
        int newIndex = allStyleText.indexOf(termForParent);

        // Check indexToPutStyle
        String parentNameToFind = parentName;
        while (newIndex == -1) {
            if (parentNameToFind.contains(".")) {
                newIndex = allStyleText.indexOf(parentNameToFind);
                parentNameToFind = parentNameToFind.substring(0, parentNameToFind.lastIndexOf("."));
            } else {
                newIndex = allStyleText.indexOf(parentNameToFind);
                break;
            }
        }
        if (newIndex != -1) {
            String endStyle = "</style>";
            indexToPutStyle = allStyleText.indexOf(endStyle, newIndex) + endStyle.length();
        }
        return indexToPutStyle;
    }

    private void onDestroy() {

        // Convert
        String inputText = txtInput.getText();
        xml2Style(inputText);
    }

    /**
     * Convert xml text to style
     *
     * @param input Input text
     * @return Text to show
     */
    private String[] xml2Style(String input) {
        StringBuilder builder = new StringBuilder();
        String[] lines = input.split("\n");
        String style = "", parent = "";
        for (String line : lines) {
            line = line.trim();

            // Skip id
            if (!line.startsWith("android:id") && !line.equals("")) {
                if (line.startsWith("style=")) {

                    // Check if to split parent from style
                    parent = line.replace("style=\"@style/", "").replace("\"", "");
                    style = textStyleName.getText().trim();
                } else {
                    builder.append("\t");
                    builder.append("\t");
                    builder.append("<item name=\"");
                    System.out.println(line);
                    int firstPart = line.indexOf("=\"");
                    builder.append(line.substring(0, firstPart));
                    builder.append("\">");
                    builder.append(line.substring(firstPart + 2, line.indexOf("\"", firstPart + 2)));
                    builder.append("</item>");
                    builder.append("\n");
                }
            }
        }

        // Create final style
        StringBuilder finalS = new StringBuilder();
        finalS.append("\t");
        finalS.append("<style name=\"");

        // Check if splited
        if (!isSplited()) {
            finalS.append(parent);
            finalS.append(".");
            finalS.append(style);
        } else {
            finalS.append(style);
            finalS.append("\"");
            finalS.append(" parent=\"");
            finalS.append(parent);
        }
        finalS.append("\"");
        finalS.append(">");
        finalS.append("\n");
        finalS.append(builder);
        finalS.append("\t");
        finalS.append("</style>");
        return new String[]{getStyleName(parent, style), parent, finalS.toString()};
    }

    private String getStyleName(String parent, String style) {
        return new StringBuilder().append(" style=\"@style/").append(isSplited() ? "" : parent + ".").append(style).append("\"").toString();
    }

    private boolean isSplited() {
        return splitFromParentCheckBox.isSelected();
    }

    /**
     * Convert style format to XML format
     *
     * @param input Input to convert.
     * @return Converted text.
     */
    private String style2XML(String input) {
        StringBuilder builder = new StringBuilder();
        String[] lines = input.split("\n");
        for (String line : lines) {
            line = line.trim();

            // Check if style
            if (line.startsWith("<style")) {
                builder.append("style=\"@style/");

                // Name
                int indexOfNameFirst = line.indexOf("\"") + 1;
                int indexOfNameLast = line.indexOf("\"", indexOfNameFirst);
                String name = line.substring(indexOfNameFirst, indexOfNameLast);

                // Parent
                if (line.contains("parent=\"")) {
                    int indexOfParentFirst = line.indexOf("\"", indexOfNameLast + 1) + 1;
                    int indexOfParentLast = line.indexOf("\"", indexOfParentFirst);
                    String parent = line.substring(indexOfParentFirst, indexOfParentLast);
                    builder.append(parent);
                    builder.append(".");
                }
                builder.append(name);
                builder.append("\"");
                builder.append("\n");
            } else if (line.startsWith("<item name")) {
                builder.append(line.replace("<item name=\"", "").replace("</item>", "\"").replace("\">", "=\""));
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private void onCancel() {
        dispose();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

}