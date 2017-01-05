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
            final Set<String> unusedMessages = new HashSet<>();
            for (String message : ownMessages) {
                if (!usedCategoryMessages.contains(message)) {
                    unusedMessages.add(message);
                }
            }
            if (unusedMessages.size() > 0) {
                final String group   = "Yii2 Inspections";
                final String message = filePath + ": unused " + unusedMessages.size() + " of " + ownMessages.size();
                Notifications.Bus.notify(new Notification(group, group, message, NotificationType.INFORMATION), this.file.getProject());
            }

            /* find missing: TODO: category should present on one directory only for fixing it */
            final Set<String> missingMessages = new HashSet<>();
            for (String message : usedCategoryMessages.keySet()) {
                if (!ownMessages.contains(message)) {
                    missingMessages.add(message);
                }
            }
            if (missingMessages.size() > 0) {
                final String group   = "Yii2 Inspections";
                final String message = filePath + ": missing " + missingMessages.size() + " of used " + usedCategoryMessages.size();
                Notifications.Bus.notify(new Notification(group, group, message, NotificationType.INFORMATION), this.file.getProject());
            }

            /* cleanup and report back to managing code */
            final boolean hasDefects = unusedMessages.size() > 0 || missingMessages.size() > 0;
            unusedMessages.clear();
            missingMessages.clear();

            return hasDefects;
        }

        return false;
    }
}
