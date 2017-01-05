package com.kalessil.phpStorm.yii2inspections.actors.upadateTranslations.registry;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

public class ProjectTranslationProvidersFinder {
    final private PsiFile file;

    ProjectTranslationProvidersFinder(@NotNull PsiFile file) {
        this.file = file;
    }

    void find(ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<PsiFile, PsiFile>>> storage) {
        /* ignore file if its' structure is not as expected */
        final PhpReturn returnExpression = PsiTreeUtil.findChildOfType(this.file, PhpReturn.class);
        final PsiElement argument        = null == returnExpression ? null : returnExpression.getArgument();
        if (!(argument instanceof ArrayCreationExpression)) {
            return;
        }

        /* prepare containers */
        final String category = this.file.getName().replaceAll("\\.php$", "");
        if (!storage.containsKey(category)) {
            storage.putIfAbsent(category, new ConcurrentHashMap<>());
        }
        final ConcurrentHashMap<String, ConcurrentHashMap<PsiFile, PsiFile>> translations = storage.get(category);

        /* extract translations from the file */
        for (ArrayHashElement item : ((ArrayCreationExpression) argument).getHashElements()) {
            final PhpPsiElement key = item.getKey();
            if (key instanceof StringLiteralExpression) {
                final String message = ((StringLiteralExpression) key).getContents();
                if (!translations.containsKey(message)) {
                    translations.putIfAbsent(message, new ConcurrentHashMap<>());
                }
                translations.get(message).putIfAbsent(this.file, this.file);
            }
        }
    }
}
