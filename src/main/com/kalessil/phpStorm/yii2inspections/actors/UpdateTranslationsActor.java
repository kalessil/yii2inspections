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
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDirectoryContainer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

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

        /* run scan-update process in background */
        new UpdateTranslationsRunner(project).run();
        Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", "Started updating translations", NotificationType.INFORMATION));
    }

    @Override
    public void update(AnActionEvent event) {
        if (null != event.getProject()) {
            event.getPresentation().setEnabled(true);
            event.getPresentation().setVisible(true);
        }
    }
}
