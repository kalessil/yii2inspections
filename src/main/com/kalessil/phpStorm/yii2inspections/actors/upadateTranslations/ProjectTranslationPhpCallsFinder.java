package com.kalessil.phpStorm.yii2inspections.actors.upadateTranslations;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final class ProjectTranslationPhpCallsFinder {
    final private PsiFile file;

    ProjectTranslationPhpCallsFinder(@NotNull PsiFile file) {
        this.file = file;
    }

    void find(ConcurrentHashMap<String, ConcurrentHashMap<String, String>> storage) {
        final Collection<MethodReference> calls = PsiTreeUtil.findChildrenOfType(this.file, MethodReference.class);
        for (MethodReference call : calls) {
            final PsiElement[] params = call.getParameters();
            final String methodName   = call.getName();
            if (null == methodName || params.length < 2 || !methodName.equals("t")) {
                continue;
            }

            /* sort ot which params are category and message */
            String category               = null;
            String message                = null;
            PsiElement categoryExpression = params[0];
            PsiElement messageExpression  = params[1];

            /* TODO: resolve params as string literals */
            /* extract contained texts from message and category literals */
            if (categoryExpression instanceof StringLiteralExpression) {
                final StringLiteralExpression categoryLiteral = (StringLiteralExpression) categoryExpression;
                if (null == categoryLiteral.getFirstPsiChild()) {
                    category = categoryLiteral.getContents();
                }
            }
            if (messageExpression instanceof StringLiteralExpression) {
                final StringLiteralExpression messageLiteral = (StringLiteralExpression) messageExpression;
                if (null == messageLiteral.getFirstPsiChild()) {
                    message = messageLiteral.getContents();
                }
            }
            if (null == category || null == message) {
                continue;
            }

            /* create category translations holder if needed and store the message */
            if (!storage.containsKey(category)) {
                storage.putIfAbsent(category, new ConcurrentHashMap<>());
            }
            final ConcurrentHashMap<String, String> translationsHolder = storage.get(category);
            translationsHolder.putIfAbsent(message, message);
        }
        calls.clear();
    }
}
