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
        regexTwigTranslateFilter
            = Pattern.compile(".*\\W((\\'[^\\']+\\')|(\\\"[^\\\"]+\\\"))\\|(t|translate)(\\(((\\'[^\\']+\\')|(\\\"[^\\\"]+\\\")))?\\W.*");
    }

    ProjectTranslationTwigCallsFinder(@NotNull PsiFile file) {
        this.file = file;
    }

    void find(ConcurrentHashMap<String, ConcurrentHashMap<String, String>> storage) {
        final Matcher regexMatcher = regexTwigTranslateFilter.matcher(this.file.getText());
        while (regexMatcher.find()) {
            final String messageExpression = regexMatcher.group(1);
            final String groupExpression   = null == regexMatcher.group(6) ? "'site'" : regexMatcher.group(6);
            if (messageExpression.length() > 2 && groupExpression.length() > 2) {
                final String message = messageExpression.substring(1, messageExpression.length() - 1);
                final String group   = groupExpression.substring(1, groupExpression.length() - 1);

                 if (!storage.containsKey(group)) {
                    storage.putIfAbsent(group, new ConcurrentHashMap<>());
                }
                storage.get(group).putIfAbsent(message, message);
            }
        }
    }
}
