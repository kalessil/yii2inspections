package com.kalessil.phpStorm.yii2inspections.actors.upadateTranslations;

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

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
        return false;
//        /* find missing translations */
//        int missingTranslations    = 0;
//        int presentedTranslations = 0;
//        for (String message : usedTranslations.keySet()) {
//            /* TODO: add translation at the end */
//            /* TODO: delayed processing for missing - craft cms can */
//            if (!exportedTranslations.contains(message)){
//                ++missingTranslations;
//            } else {
//                ++presentedTranslations;
//            }
//        }
//        if (missingTranslations > 0) {
//            final String message = target.getVirtualFile().getCanonicalPath() + ": presented " + presentedTranslations + " missing " + missingTranslations;
//            Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", message, NotificationType.INFORMATION));
//        }
//        /* find unused translations */
//        int unusedTranslationsCount = 0;
//        int usedTranslationsCount   = 0;
//        for (String message : exportedTranslations) {
//            if (!usedTranslations.containsKey(message)) {
//                ++unusedTranslationsCount;
//            } else {
//                ++usedTranslationsCount;
//            }
//            /* TODO: drop the translation */
//        }
//        if (unusedTranslationsCount > 0) {
//            final String message = target.getVirtualFile().getCanonicalPath() + ": used " + usedTranslationsCount + " unused " + unusedTranslationsCount;
//            Notifications.Bus.notify(new Notification("Yii2 Inspections", "Yii2 Inspections", message, NotificationType.INFORMATION));
//        }
//
//        return missingTranslations > 0 || unusedTranslationsCount > 0;
    }
}
