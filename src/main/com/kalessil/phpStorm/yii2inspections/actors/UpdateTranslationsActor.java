package com.kalessil.phpStorm.yii2inspections.actors;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.MethodReference;

import java.util.Collection;

final public class UpdateTranslationsActor extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        /* consume event */
        final Project   project = event.getProject();
        final PsiElement target = CommonDataKeys.PSI_ELEMENT.getData(event.getDataContext());
        if (null == project || null == target) {
            return;
        }

        /* determine the action target */
        PsiFile file           = null;
        PsiDirectory directory = null;
        if (target instanceof PsiDirectoryContainer) {
            directory = ((PsiDirectoryContainer) target).getDirectories()[0];
        } else if (target instanceof PsiDirectory) {
            directory = (PsiDirectory )target;
        } else {
            file = target.getContainingFile();
            if (file != null) {
                directory = file.getContainingDirectory();
            }
        }
        if (null == file && null == directory) {
            return;
        }

        // PsiManager.getInstance(project).findFile();
        // final VirtualFile[] files = (VirtualFile[]) CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(event.getDataContext());
        final PsiDirectory root = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
        if (null != root) {
            int hits = 0;

            Collection<MethodReference> calls = PsiTreeUtil.findChildrenOfType(root, MethodReference.class);
            for (MethodReference call : calls) {
                final String methodName = call.getName();
                if (null != methodName && methodName.equals("t")) {
                    ++hits;
                }
            }
            calls.clear();

//            String group = "Yii2 Inspections";
//            Notification count = new Notification(group, group, "Hits: " + hits, NotificationType.INFORMATION);
//            Notifications.Bus.notify(count);
        }

//        String group = "Yii2 Inspections";
//        Notification fdebug = new Notification(group, group, null == file ? "?" : file.getVirtualFile().getCanonicalPath(), NotificationType.INFORMATION);
//        Notifications.Bus.notify(fdebug);
//        Notification ddebug = new Notification(group, group, null == directory ? "?" : directory.getVirtualFile().getCanonicalPath(), NotificationType.INFORMATION);
//        Notifications.Bus.notify(ddebug);
    }

    @Override
    public void update(AnActionEvent event) {
        if (null != event.getProject()) {
            event.getPresentation().setEnabled(true);
            event.getPresentation().setVisible(true);
        }
    }
}
