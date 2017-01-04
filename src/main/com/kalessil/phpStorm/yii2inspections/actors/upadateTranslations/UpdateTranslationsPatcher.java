package com.kalessil.phpStorm.yii2inspections.actors.upadateTranslations;

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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

final public class UpdateTranslationsPatcher {
    final private PsiFile target;

    UpdateTranslationsPatcher (@NotNull PsiFile target) {
        this.target = target;
    }

    public boolean patch(@NotNull ConcurrentHashMap<String, ConcurrentHashMap<String, String>> translations) {
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
        final List<String> exportedTranslations = new ArrayList<>();
        for (ArrayHashElement item : ((ArrayCreationExpression) argument).getHashElements()) {
            final PhpPsiElement keyContainer = item.getKey();
            final PhpPsiElement key          = null == keyContainer ? null : keyContainer.getFirstPsiChild();
            if (key instanceof StringLiteralExpression) {
                final String message = ((StringLiteralExpression) key).getContents();
                exportedTranslations.add(message);
            }
        }

        /* find missing translations */
        int missingTranslations = 0;
        for (String message : usedTranslations.keySet()) {
            /* TODO: add translation at the end */
Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", "Missing: " + message, NotificationType.INFORMATION));
            missingTranslations += exportedTranslations.contains(message) ? 0 : 1;
        }
        /* find unused translations */
        int unusedTranslations = 0;
        for (String message : exportedTranslations) {
            unusedTranslations += usedTranslations.containsKey(message) ? 0 : 1;
Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", "Unused: " + message, NotificationType.INFORMATION));
            /* TODO: drop the translation */
        }

        return missingTranslations > 0 || unusedTranslations > 0;
    }
}
