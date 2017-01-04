package com.kalessil.phpStorm.yii2inspections.actors;

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

final class UpdateTranslationsRunner extends AbstractLayoutCodeProcessor {
    @Nullable
    private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> discovered = null;
    private boolean discoveringFinished = false;

    public UpdateTranslationsRunner(Project project, PsiDirectory directory, boolean b) {
        super(project, directory, b, "Updating Yii2 translations", "Update Yii2 translations", false);
    }

    public UpdateTranslationsRunner(Project project, PsiFile file) {
        super(project, file, "Updating Yii2 translations", "Update Yii2 translations", false);
    }

    @Nullable
    private Runnable createRunner(@NotNull Project project) {
        /* we might already collect translations or project is not valid */
        final PsiDirectory root = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
        if (null == root) {
            return null;
        }

        return () -> {
            if (null == discovered) {
                discovered = new ConcurrentHashMap<>();

                /* iterate files and run individual scanned withing scanners group */
                final ThreadGroup scanners     = new ThreadGroup("Find t-methods invocations");
                final ProjectFilesFinder files = new ProjectFilesFinder(project);
                while (files.hasNext()) {
                    final PsiFile theAssignedFile = (PsiFile) files.next();
                    if (!theAssignedFile.getName().endsWith(".php")) {
                        continue;
                    }

                    final Thread runnerThread = new Thread(scanners,
                            () -> new ProjectTranslationCallsFinder(theAssignedFile).find(discovered));
                    runnerThread.run();
                }

                /* wait for all threads to finish */
                try {
                    while (scanners.activeCount() > 0) {
                        wait(100);
                    }
                } catch (InterruptedException interrupted) {
Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", "Scan interrupted", NotificationType.ERROR));
                }
                discoveringFinished = true;
            }

            try {
                while (!discoveringFinished) {
                    wait(100);
                }
            } catch (InterruptedException interrupted) {
Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", "Update interrupted", NotificationType.ERROR));
            }
            /* TODO: fix requested file but wait for discoveringFinished being true */
        };
    }

    @NotNull
    @Override
    protected FutureTask<Boolean> prepareTask(@NotNull PsiFile psiFile, boolean b) throws IncorrectOperationException {
        Runnable defaultRunner = EmptyRunnable.getInstance();
        Runnable runner        = this.createRunner(psiFile.getProject());

        return new FutureTask(null == runner ? defaultRunner : runner, true);
    }
}
