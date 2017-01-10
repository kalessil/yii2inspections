package com.kalessil.phpStorm.yii2inspections.codeInsight;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class TranslateMessageAutocompleteContributor extends CompletionContributor {
    public TranslateMessageAutocompleteContributor() {
        final CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                final PsiElement target = completionParameters.getOriginalPosition();
                if (null == target || !(target.getParent() instanceof StringLiteralExpression)) {
                    return;
                }

                final StringLiteralExpression literal = (StringLiteralExpression) target.getParent();
                completionResultSet.addElement(LookupElementBuilder.create("Hello").withIcon(PhpIcons.ADVICE_ICON));
                Notifications.Bus.notify(new Notification("-", "-", "Adviced 'hello' for: " + literal.getContents(), NotificationType.INFORMATION));
            }
        };
        final ElementPattern<PsiElement> filter = PlatformPatterns.psiElement(PsiElement.class)
                .withParent(PlatformPatterns.psiElement(StringLiteralExpression.class)
                    .withParent(PlatformPatterns.psiElement(PhpElementTypes.PARAMETER_LIST)
                        .withParent(PlatformPatterns.psiElement(PhpElementTypes.METHOD_REFERENCE))))
                .withLanguage(PhpLanguage.INSTANCE);

        extend(CompletionType.BASIC, filter, provider);
    }
}