package com.kalessil.phpStorm.yii2inspections.utils;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    static public ExtractionResult process(@NotNull MethodReference reference) {
        return null;
    }

    public class ExtractionResult {
        @Nullable
        private StringLiteralExpression category;
        @Nullable
        private Map<StringLiteralExpression, PsiElement> messages;

        ExtractionResult(@NotNull StringLiteralExpression category, @NotNull Map<StringLiteralExpression, PsiElement> messages) {
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
