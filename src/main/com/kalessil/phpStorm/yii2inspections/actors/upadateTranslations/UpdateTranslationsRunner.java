package com.kalessil.phpStorm.yii2inspections.actors.upadateTranslations;

import com.intellij.codeInsight.actions.AbstractLayoutCodeProcessor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.IncorrectOperationException;
import com.kalessil.phpStorm.yii2inspections.actors.upadateTranslations.registry.UsedTranslationsRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class UpdateTranslationsRunner extends AbstractLayoutCodeProcessor {
    @Nullable
    private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> discovered = null;
    private boolean discoveringFinished = false;
    private boolean startNotified       = false;

    public UpdateTranslationsRunner(Project project, PsiDirectory directory, boolean b) {
        super(project, directory, b, "Updating Yii2 translations", "Update Yii2 translations", false);
    }

    public UpdateTranslationsRunner(Project project, PsiFile file) {
        super(project, file, "Updating Yii2 translations", "Update Yii2 translations", false);
    }

    @Nullable
    private Runnable createRunner(@NotNull Project project, @NotNull PsiFile file) {
        /* we might already collect translations or project is not valid */
        final PsiDirectory root = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
        if (null == root) {
            return null;
        }
        if (!this.startNotified) {
            this.startNotified = true;
Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", "Scanning for used translations", NotificationType.INFORMATION));
        }

        return () -> {
            if (null == this.discovered) {
                /* exclusively do scanning */
                this.discovered = new ConcurrentHashMap<>();

                /* do scanning itself */
                this.discovered = new UsedTranslationsRegistry(project).populate();
                discoveringFinished = true;
            }

            try {
                while (!discoveringFinished) {
                    wait(100);
                }
            } catch (InterruptedException interrupted) {
                final String group   = "Yii2 Inspections";
                final String message = "Translations update has been interrupted";
                Notifications.Bus.notify(new Notification(group, group, message, NotificationType.ERROR));
            }

            if (new UpdateTranslationsPatcher(file).patch(this.discovered)) {
                final String group   = "Yii2 Inspections";
                final String message = "Needs check: " + file.getVirtualFile().getCanonicalPath();
                Notifications.Bus.notify(new Notification(group, group, message, NotificationType.INFORMATION));
            }
        };
    }

    @NotNull
    @Override
    protected FutureTask<Boolean> prepareTask(@NotNull PsiFile file, boolean b) throws IncorrectOperationException {
        Runnable defaultRunner = EmptyRunnable.getInstance();
        Runnable runner        = this.createRunner(file.getProject(), file);

        return new FutureTask(null == runner ? defaultRunner : runner, true);
    }
}
