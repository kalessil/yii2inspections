package com.kalessil.phpStorm.yii2inspections.codeInsight;

import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ThreeState;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class TranslateMessageAutocompleteConfidence extends CompletionConfidence {

    @NotNull
    @Override
    public ThreeState shouldSkipAutopopup(@NotNull PsiElement contextElement, @NotNull PsiFile psiFile, int offset) {
        if (!(psiFile instanceof PhpFile) || !(contextElement.getParent() instanceof StringLiteralExpression)) {
            //Notifications.Bus.notify(new Notification("-", "-", "unsure", NotificationType.INFORMATION));
            return ThreeState.UNSURE;
        }

        //Notifications.Bus.notify(new Notification("-", "-", "popup", NotificationType.INFORMATION));
        return ThreeState.NO;
    }
}
