package com.kalessil.phpStorm.yii2inspections.codeInsight;

import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ThreeState;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class TranslateMessageAutocompleteConfidence extends CompletionConfidence {

    @NotNull
    @Override
    public ThreeState shouldSkipAutopopup(@NotNull PsiElement contextElement, @NotNull PsiFile psiFile, int offset) {
        final PsiElement context = contextElement.getContext();
        if (context instanceof StringLiteralExpression) {
            return ThreeState.NO;
        }

        return ThreeState.UNSURE;
    }
}
