package com.xml2style.converters;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.xml2style.XML2StyleDialog;

import java.awt.*;

/**
 * Created by GacaPC on 9.2.2016..
 */
public class ConverterXml2Style extends ConverterMain {

    private com.intellij.openapi.editor.Document mDocument;
    private VirtualFile styleVirtualFile;

    public ConverterXml2Style(XML2StyleDialog dialog) {
        super(dialog);

        // Instantiate
        PsiFile psiFile = getDialog().getPsiFile();

        // Get current file
        mDocument = psiFile.getViewProvider().getDocument();
        VirtualFile parent = psiFile.getVirtualFile().getParent();

        // Get needed style file
        styleVirtualFile = parent.findChild("values").findChild("styles.xml");
    }

    @Override
    public void convert() {

        // Convert
        String[] result = xml2Style(mSelectedText);
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
                mDocument.setText(documentText.replace(mSelectedText, styleNameToReplace));
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

        // Check if divided
        if (!isDivided()) {
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
        return new StringBuilder().append(" style=\"@style/").append(isDivided() ? "" : parent + ".").append(style).append("\"").toString();
    }


    public boolean isDivided() {
        return getDialog().isDivided();
    }
}
