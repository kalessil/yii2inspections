package com.kalessil.phpStorm.yii2inspections.actors;

import com.intellij.codeInsight.actions.AbstractLayoutCodeProcessor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.stream.IntStream;

class UpdateTranslationsRunner extends AbstractLayoutCodeProcessor {
    public UpdateTranslationsRunner(Project project) {
        super(project, "Update Yii2 translations", "Updating Yii2 translations", false);
    }

    @NotNull
    @Override
    protected FutureTask<Boolean> prepareTask(@NotNull PsiFile psiFile, boolean b) throws IncorrectOperationException {
        Runnable runner = EmptyRunnable.getInstance();

        // PsiManager.getInstance(project).findFile();
        // final VirtualFile[] files = (VirtualFile[]) CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(event.getDataContext());

        /* iterate files and scan them in threads */
        final Project project   = psiFile.getProject();
        final PsiDirectory root = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
        if (null != root) {
            final PsiFile[] files = root.getFiles();

            /* filter out PHP-files before invoking any threads */
            final List<PsiFile> phpFiles = new ArrayList<>();
            for (PsiFile file : files) {
                if (file.getName().endsWith(".php")) {
                    phpFiles.add(file);
                }
            }

            /* override future runnable with real work when needed */
            if (phpFiles.size() > 0) {
                runner = () -> {
                    final List<Runnable> threads                     = new ArrayList<>();
                    final ThreadGroup scanners                       = new ThreadGroup("Find t-methods invocation");
                    final ConcurrentHashMap<Integer, Integer> counts = new ConcurrentHashMap<>();

                    for (PsiFile file : phpFiles) {
                        /* tweak scope with variable we want to be accessible for threads */
                        final PsiFile assignedFile = file;
                        final int threadId         = threads.size();
                        final Thread runnerThread = new Thread(scanners, () -> {
                            counts.put(threadId, 0);

                            Collection<MethodReference> calls = PsiTreeUtil.findChildrenOfType(assignedFile, MethodReference.class);
                            for (MethodReference call : calls) {
                                final PsiElement[] params = call.getParameters();
                                final String methodName   = call.getName();
                                if (null == methodName || params.length < 2 || !methodName.equals("t")) {
                                    continue;
                                }

                                counts.put(threadId, 1 + counts.get(threadId));
                            }
                            calls.clear();
                        });

                        /* join the group and run in background */
                        threads.add(runnerThread);
                        runnerThread.run();
                    }

                    /* wait for all threads */
                    try {
                        scanners.wait();
                    } catch (InterruptedException interrupted) {
                        Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", "Interrupted", NotificationType.ERROR));
                    }

                    /* report count - for debug purposes */
                    int hits = 0;
                    for (Integer count : counts.values()) {
                        hits += count;
                    }
                    Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", "Scanning finished: found t-usages " + hits, NotificationType.INFORMATION));
                };
            }
        }

        return new FutureTask(runner, true);
    }
}
