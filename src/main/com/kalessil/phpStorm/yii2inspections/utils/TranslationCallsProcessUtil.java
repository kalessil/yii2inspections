package com.kalessil.phpStorm.yii2inspections.utils;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.yii2inspections.inspectors.utils.StringLiteralExtractUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class TranslationCallsProcessUtil {

    @Nullable
    static public ProcessingResult process(@NotNull MethodReference reference) {
        final String name         = reference.getName();
        final PsiElement[] params = reference.getParameters();
        if (null == name || params.length < 2 || (!name.equals("t") && !(name.equals("registerTranslations")))) {
            return null;
        }

        /* category needs to be resolved and without any injections */
        final StringLiteralExpression categoryLiteral = StringLiteralExtractUtil.resolveAsStringLiteral(params[0]);
        if (null == categoryLiteral || null != categoryLiteral.getFirstPsiChild()) {
            return null;
        }

        final Map<StringLiteralExpression, PsiElement> messages = new HashMap<>();
        if (name.equals("t")) {
            /* 2nd argument expected to be a string literal (possible with injections) */
            final StringLiteralExpression messageLiteral = StringLiteralExtractUtil.resolveAsStringLiteral(params[1]);
            if (null != messageLiteral) {
                messages.put(messageLiteral, params[1]);
            }
        }

        if (name.equals("registerTranslations") && params[1] instanceof ArrayCreationExpression) {
            /* 2nd argument expected to be an inline array with string literal (possible with injections) */
            for (PsiElement child : params[1].getChildren()) {
                final PsiElement literalCandidate            = child.getFirstChild();
                final StringLiteralExpression messageLiteral = StringLiteralExtractUtil.resolveAsStringLiteral(literalCandidate);
                if (null != messageLiteral) {
                    messages.put(messageLiteral, literalCandidate);
                }
            }
        }

        return 0 == messages.size() ? null : new ProcessingResult(categoryLiteral, messages);
    }

    static public class ProcessingResult {
        @Nullable
        private StringLiteralExpression category;
        @Nullable
        private Map<StringLiteralExpression, PsiElement> messages;

        ProcessingResult(@NotNull StringLiteralExpression category, @NotNull Map<StringLiteralExpression, PsiElement> messages) {
            this.category = category;
            this.messages = messages;
        }

        @NotNull
        public StringLiteralExpression getCategory() {
            if (null == this.category) {
                throw new RuntimeException("The object has been disposed already");
            }

            return this.category;
        }

        @NotNull
        public Map<StringLiteralExpression, PsiElement> getMessages() {
            if (null == this.messages) {
                throw new RuntimeException("The object has been disposed already");
            }

            return this.messages;
        }

        public void dispose() {
            if (null != this.messages) {
                this.messages.clear();
            }

            this.category = null;
            this.messages = null;
        }
    }
}
