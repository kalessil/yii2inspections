package com.kalessil.phpStorm.yii2inspections.actors.upadateTranslations.registry;

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final class ProjectTranslationTwigCallsFinder {
    final private PsiFile file;

    @SuppressWarnings("CanBeFinal")
    static private Pattern regexTwigTranslateFilter = null;
    static {
        regexTwigTranslateFilter = Pattern.compile(".*\\W((\\'[^\\']+\\')|(\\\"[^\\\"]+\\\"))\\|(t|translate)\\W.*");
    }

    ProjectTranslationTwigCallsFinder(@NotNull PsiFile file) {
        this.file = file;
    }

    void find(ConcurrentHashMap<String, ConcurrentHashMap<String, String>> storage) {
        // t/translate are twig filters, see craft/app/etc/templating/twigextensions/CraftTwigExtension.php:59
        final String category = "craft";
        if (!storage.containsKey(category)) {
            storage.putIfAbsent(category, new ConcurrentHashMap<>());
        }
        final ConcurrentHashMap<String, String> translationsHolder = storage.get(category);

        final Matcher regexMatcher = regexTwigTranslateFilter.matcher(this.file.getText());
        while (regexMatcher.find()) {
            final String expression = regexMatcher.group(1);
            if (expression.length() > 2) {
                final String message = expression.substring(1, expression.length() - 1);
                translationsHolder.putIfAbsent(message, message);
            }
        }
    }
}
