package com.kalessil.phpStorm.yii2inspections.actors;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class UsedTranslationsFinder {
    final private PsiFile file;

    UsedTranslationsFinder(@NotNull PsiFile file) {
        this.file = file;
    }

    public void find(ConcurrentHashMap<String, ConcurrentHashMap<String, String>> storage) {
        final Collection<MethodReference> calls = PsiTreeUtil.findChildrenOfType(this.file, MethodReference.class);
        for (MethodReference call : calls) {
            final PsiElement[] params = call.getParameters();
            final String methodName   = call.getName();
            if (null == methodName || params.length < 2 || !methodName.equals("t")) {
                continue;
            }

            /* TODO: resolve the method, resolve params as string literals */

            if (params[0] instanceof StringLiteralExpression && params[1] instanceof StringLiteralExpression) {
                final StringLiteralExpression categoryExpression = (StringLiteralExpression) params[0];
                final StringLiteralExpression messageExpression  = (StringLiteralExpression) params[1];
                /* ignore strings with any injections */
                if (null != categoryExpression.getFirstPsiChild() || null != messageExpression.getFirstPsiChild()) {
                    continue;
                }

                /* create category translations holder if needed */
                final String category = categoryExpression.getContents();
                if (!storage.containsKey(category)) {
                    storage.putIfAbsent(category, new ConcurrentHashMap<>());
                }

                /* store the message */
                final ConcurrentHashMap<String, String> translationsHolder = storage.get(category);
                final String message                                       = messageExpression.getContents();
                translationsHolder.putIfAbsent(message, message);
            }
        }
        calls.clear();
    }
}
