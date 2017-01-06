package com.kalessil.phpStorm.yii2inspections.actors.upadateTranslations.registry;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class ProvidedTranslationsRegistry {
    @Nullable
    private Project project = null;

    @Nullable
    private ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<PsiFile, PsiFile>>> translations = null;

    public ProvidedTranslationsRegistry(@NotNull Project project) {
        this.project = project;
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<PsiFile, PsiFile>>> populate() {
        PsiDirectory root =
            null == this.project ? null : PsiManager.getInstance(this.project).findDirectory(this.project.getBaseDir());
        if (null == root) {
            return null;
        }

        this.translations       = new ConcurrentHashMap<>();
        int countProcessedFiles = 0;

        ThreadGroup findTranslationsWorkers = new ThreadGroup("Find translations");
        ProjectFilesFinder files            = new ProjectFilesFinder(project);
        while (files.hasNext()) {
            final PsiFile theFile = (PsiFile) files.next();
            final String filePath = theFile.getVirtualFile().getCanonicalPath();
            if (null == filePath) {
                continue;
            }

            final String fileName       = theFile.getName();
            final boolean isTranslation = fileName.endsWith(".php") && !fileName.matches("config.php")
                    && filePath.matches(".*/(translations|messages)/([a-zA-z]{2}(_[a-zA-z]{2})?)/[^/]+\\.php$");
            if (!isTranslation) {
                continue;
            }

            final Thread runnerThread = new Thread(findTranslationsWorkers,
                () -> {
                    new ProjectTranslationProvidersFinder(theFile).find(this.translations);
                });
            runnerThread.run();

            ++countProcessedFiles;
        }

        try {
            while (findTranslationsWorkers.activeCount() > 0) {
                wait(100);
            }
        } catch (InterruptedException interrupted) {
            final String group   = "Yii2 Inspections";
            final String message = "Existing translations scan has been interrupted";
            Notifications.Bus.notify(new Notification(group, group, message, NotificationType.ERROR), this.project);
        }

        /* TODO: remove this debug */
        int countTranslations = 0;
        for (ConcurrentHashMap<String, ConcurrentHashMap<PsiFile, PsiFile>> translationGroup : this.translations.values()) {
            countTranslations += translationGroup.size();
        }
        final String group   = "Yii2 Inspections";
        final String message = "Translations: files " + countProcessedFiles + " groups " + this.translations.size() + " messages " + countTranslations;
        Notifications.Bus.notify(new Notification(group, group, message, NotificationType.INFORMATION), this.project);

        return this.translations;
    }
}
