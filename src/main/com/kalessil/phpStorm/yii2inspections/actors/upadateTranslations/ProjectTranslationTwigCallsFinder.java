package com.kalessil.phpStorm.yii2inspections.actors.upadateTranslations;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

final public class ProjectTranslationTwigCallsFinder {
    final private PsiFile file;

    ProjectTranslationTwigCallsFinder(@NotNull PsiFile file) {
        this.file = file;
    }

    void find(ConcurrentHashMap<String, ConcurrentHashMap<String, String>> storage) {
        // t/translate are twig filters, see craft/app/etc/templating/twigextensions/CraftTwigExtension.php:59
        final String regex = ".*\\W((\\'[^\\']+\\')|(\\\"[^\\\"]+\\\"))\\|(t|translate)\\W.*";
    }
}
