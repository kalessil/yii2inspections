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
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.psi.PhpFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;

class UpdateTranslationsRunner extends AbstractLayoutCodeProcessor {
    /* category =>  [ message-message, ... ] */
    @Nullable
    ConcurrentHashMap<String, ConcurrentHashMap<String, String>> discovered = null;
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
                Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", "Find used translations", NotificationType.INFORMATION));

                /* filter out PHP-files for searching t-calls before invoking any threads */
                final List<PsiFile> phpFiles = new ArrayList<>();
                for (PsiFile file : PsiTreeUtil.findChildrenOfType(root, PsiFile.class)) {
                    if (file instanceof PhpFile) {
                        phpFiles.add(file);
                    }
                }
                Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", "Files in project " + phpFiles.size(), NotificationType.INFORMATION));
                if (0 == phpFiles.size()) {
                    discoveringFinished = true;
                    return;
                }

                /* iterate files and run individual scanned withing scanners group */
                final ThreadGroup scanners = new ThreadGroup("Find t-methods invocations");
                for (PsiFile file : phpFiles) {
                    final PsiFile theAssignedFile = file;
                    final Thread runnerThread  = new Thread(scanners, () -> {
                        new UsedTranslationsFinder(theAssignedFile).find(discovered);
                    });
                    runnerThread.run();
                }

                /* wait for all threads to finish */
                try {
                    while (scanners.activeCount() > 0) {
                        wait(300);
                    }
                } catch (InterruptedException interrupted) {
                    Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", "Interrupted", NotificationType.ERROR));
                }
                discoveringFinished = true;

                /* report count - for debug purposes */
                int hits = 0;
                for (ConcurrentHashMap<String, String> categoryTranslations : this.discovered.values()) {
                    hits += categoryTranslations.size();
                }
                Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", "Scanning finished: found t-usages " + hits, NotificationType.INFORMATION));
            }

            /* TODO: fix requested file but wait for discoveringFinished being true */
        };
    }

    @NotNull
    @Override
    protected FutureTask<Boolean> prepareTask(@NotNull PsiFile psiFile, boolean b) throws IncorrectOperationException {
Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", "Processing " + psiFile.getName(), NotificationType.INFORMATION));

        Runnable defaultRunner = EmptyRunnable.getInstance();
        Runnable runner        = this.createRunner(psiFile.getProject());

        return new FutureTask(null == runner ? defaultRunner : runner, true);
    }
}
