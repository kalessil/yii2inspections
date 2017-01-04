package com.kalessil.phpStorm.yii2inspections.actors.upadateTranslations;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
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
    final private PsiFile target;

    UpdateTranslationsPatcher (@NotNull PsiFile target) {
        this.target = target;
    }

    boolean patch(@NotNull ConcurrentHashMap<String, ConcurrentHashMap<String, String>> translations) {
        /* drop the file if the category not used at all */
        final String category = this.target.getName().replaceAll("\\.php$", "");
        if (!translations.containsKey(category)) {
            this.target.delete();
            return true;
        }
        final ConcurrentHashMap<String, String> usedTranslations = translations.get(category);

        /* ignore file if its' structure is not as expected */
        final PhpReturn returnExpression = PsiTreeUtil.findChildOfType(this.target, PhpReturn.class);
        final PsiElement argument        = null == returnExpression ? null : returnExpression.getArgument();
        if (!(argument instanceof ArrayCreationExpression)) {
            return false;
        }

        /* collect translations covered by the file */
        final Set<String> exportedTranslations = new HashSet<>();
        for (ArrayHashElement item : ((ArrayCreationExpression) argument).getHashElements()) {
            final PhpPsiElement key = item.getKey();
            if (key instanceof StringLiteralExpression) {
                final String message = ((StringLiteralExpression) key).getContents();
                exportedTranslations.add(message);
            }
        }

        /* find missing translations */
        int missingTranslations = 0;
        for (String message : usedTranslations.keySet()) {
            /* TODO: add translation at the end */
            if (!exportedTranslations.contains(message)){
Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", "Missing: " + category + "|" + message, NotificationType.INFORMATION));
                ++missingTranslations;
            }
        }
        /* find unused translations */
        int unusedTranslations = 0;
        for (String message : exportedTranslations) {
            if (!usedTranslations.containsKey(message)) {
Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", "Unused: " + category + "|" + message, NotificationType.INFORMATION));
                ++unusedTranslations;
            }
            /* TODO: drop the translation */
        }

        return missingTranslations > 0 || unusedTranslations > 0;
    }
}
