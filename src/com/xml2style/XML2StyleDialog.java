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

    // Dialog view instances
    private JPanel contentPane;
    private JButton btnConvert;
    private JButton btnClose;
    private JTextArea txtInput;
    private JTextArea txtOutput;
    private JTextField textStyleName;
    private JButton destroyStyleButton;
    private JCheckBox splitFromParentCheckBox;

    // Dialog main instances
    private Editor mEditor;
    private String mSelectedText;
    private PsiFile mPsiFile;

    public XML2StyleDialog(AnActionEvent event) {
        super(null);
        setModal(true);
        getRootPane().setDefaultButton(btnConvert);
        setTitle("XML2Style");

        // Get data
        mEditor = event.getData(PlatformDataKeys.EDITOR);

        // Get document
        if (mEditor != null) {
            mSelectedText = mEditor.getSelectionModel().getSelectedText();

            // Get selected text from current file
            txtInput.setText(mSelectedText);

            // Get file and parent
            try {
                mPsiFile = event.getData(LangDataKeys.PSI_FILE);

                // Is from layout folder
                VirtualFile parent = mPsiFile.getVirtualFile().getParent();
                if (parent.equals("layout")) {

                } else if (parent.equals("values")) {

                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        // Enable buttons
        if (txtInput.getText().contains("<item name=")) {
            destroyStyleButton.addActionListener(e -> onDestroy());
            btnConvert.setEnabled(false);
        } else {
            btnConvert.addActionListener(e -> onConvert());
            destroyStyleButton.setEnabled(false);
        }

        // Set actions to the buttons
        btnClose.addActionListener(e -> onCancel());

        // Init
        init();
    }

    private void onConvert() {

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

    }

    /**
     * Return true if parent and style will be divided. False otherwise.
     */
    public boolean isDivided() {
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

    public PsiFile getPsiFile() {
        return mPsiFile;
    }
}