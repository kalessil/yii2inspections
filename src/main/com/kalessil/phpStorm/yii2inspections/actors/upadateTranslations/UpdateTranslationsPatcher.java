package com.kalessil.phpStorm.yii2inspections.actors.upadateTranslations;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final class UpdateTranslationsPatcher {
    final private PsiFile file;

    UpdateTranslationsPatcher (@NotNull PsiFile file) {
        this.file = file;
    }

    boolean patch(
            @NotNull ConcurrentHashMap<String, ConcurrentHashMap<String, String>> usages,
            @NotNull ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<PsiFile, PsiFile>>> definitions
    ) {
        final String category = this.file.getName().replaceAll("\\.php$", "");
        final String filePath = this.file.getVirtualFile().getCanonicalPath();

        final Set<String> ownMessages = new HashSet<>();
        if (definitions.containsKey(category)) { // TODO: what if not?
            final ConcurrentHashMap<String, ConcurrentHashMap<PsiFile, PsiFile>> messages = definitions.get(category);
            for (String message : messages.keySet()) {
                if (messages.get(message).contains(this.file)) {
                    ownMessages.add(message);
                }
            }
        }

        if (usages.containsKey(category)) { // TODO: what if not?
            final ConcurrentHashMap<String, String> usedCategoryMessages = usages.get(category);

            /* find unused */
            int unusedTranslationsCount = 0;
            int usedTranslationsCount   = 0;
            for (String message : ownMessages) {
                if (!usedCategoryMessages.contains(message)) {
                    ++unusedTranslationsCount;
                } else {
                    ++usedTranslationsCount;
                }
            }
            if (unusedTranslationsCount > 0) {
                final String group   = "Yii2 Inspections";
                final String message = filePath + ": used " + usedTranslationsCount + " unused " + unusedTranslationsCount;
                Notifications.Bus.notify(new Notification(group, group, message, NotificationType.INFORMATION), this.file.getProject());
            }

            /* find missing: TODO: category should present on one directory only for fixing it */
            int missingTranslations   = 0;
            int presentedTranslations = 0;
            for (String message : usedCategoryMessages.keySet()) {
                if (!ownMessages.contains(message)) {
                    ++missingTranslations;
                } else {
                    ++presentedTranslations;
                }
            }
            if (missingTranslations > 0) {
                final String group   = "Yii2 Inspections";
                final String message = filePath + ": presented " + presentedTranslations + " missing " + missingTranslations;
                Notifications.Bus.notify(new Notification(group, group, message, NotificationType.INFORMATION), this.file.getProject());
            }

            return missingTranslations > 0 || unusedTranslationsCount > 0;
        }

        return false;
    }
}
