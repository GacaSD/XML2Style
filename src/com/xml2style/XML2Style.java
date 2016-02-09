package com.xml2style;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiFile;

/**
 * Created by GacaPC on 3.8.2015..
 */
public class XML2Style extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        XML2StyleDialog dialog = new XML2StyleDialog(e);
        dialog.setCrossClosesWindow(true);
        dialog.show();
    }

    @Override
    public void update(AnActionEvent e) {

        // Check if XML file
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (psiFile != null && psiFile.getFileType().getName().equals("XML")) {
            e.getPresentation().setEnabled(true);
        } else {
            e.getPresentation().setEnabled(false);
        }
    }
}
