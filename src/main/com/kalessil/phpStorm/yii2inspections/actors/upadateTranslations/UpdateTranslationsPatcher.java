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
        /* if category is not used, create an empty container and make it empty later */
        String category = this.target.getName().replaceAll("\\.php$", "");
        category = category.matches("[a-zA-z]{2}(_[a-zA-z]{2})?") ? "craft" : category;
        final ConcurrentHashMap<String, String> usedTranslations =
                translations.containsKey(category) ? translations.get(category) : new ConcurrentHashMap<>();

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
        int missingTranslations    = 0;
        int presentedTranslations = 0;
        for (String message : usedTranslations.keySet()) {
            /* TODO: add translation at the end */
            /* TODO: delayed processing for missing - craft cms can */
            if (!exportedTranslations.contains(message)){
                ++missingTranslations;
            } else {
                ++presentedTranslations;
            }
        }
        if (missingTranslations > 0) {
            final String message = target.getVirtualFile().getCanonicalPath() + ": presented " + presentedTranslations + " missing " + missingTranslations;
            Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", message, NotificationType.INFORMATION));
        }
        /* find unused translations */
        int unusedTranslationsCount = 0;
        int usedTranslationsCount   = 0;
        for (String message : exportedTranslations) {
            if (!usedTranslations.containsKey(message)) {
                ++unusedTranslationsCount;
            } else {
                ++usedTranslationsCount;
            }
            /* TODO: drop the translation */
        }
        if (unusedTranslationsCount > 0) {
            final String message = target.getVirtualFile().getCanonicalPath() + ": used " + usedTranslationsCount + " unused " + unusedTranslationsCount;
            Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", message, NotificationType.INFORMATION));
        }

        return missingTranslations > 0 || unusedTranslationsCount > 0;
    }
}
