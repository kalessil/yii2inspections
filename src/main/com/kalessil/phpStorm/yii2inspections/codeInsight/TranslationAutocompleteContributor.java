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
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TranslationAutocompleteContributor extends CompletionContributor {
    public TranslationAutocompleteContributor() {
        final CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                /* validate the autocompletion target */
                final PsiElement target = completionParameters.getOriginalPosition();
                if (null == target || !(target.getParent() instanceof StringLiteralExpression)) {
                    return;
                }

                /* autocomplete only categories only when they are empty */
                final StringLiteralExpression parameter = (StringLiteralExpression) target.getParent();
                if (parameter.getContents().length() != 0) {
                    return;
                }
                /* suggest only to target code structure */
                final MethodReference reference = (MethodReference) parameter.getParent().getParent();
                final String name               = reference.getName();
                final PsiElement[] params       = reference.getParameters();
                if (null == name || params.length < 2 || (!name.equals("t") && !(name.equals("registerTranslations")))) {
                    return;
                }

                /* generate proposals */
                final boolean autocompleteCategory = params[0] == parameter;
                final boolean autocompleteMessage  = params[1] == parameter;
                if (autocompleteCategory || autocompleteMessage) {
                    /* extract uniques messages for faster processing */
                    final Set<String> suggestions = new HashSet<>();
                    final Set<String> messages
                        = new HashSet<>(FileBasedIndex.getInstance().getAllKeys(TranslationCallsIndexer.identity, target.getProject()));

                    final String prefix
                        = params[0] instanceof StringLiteralExpression ? ((StringLiteralExpression) params[0]).getContents() + "|" : "|";
                    for (String prefixedMessage : messages) {
                        if (autocompleteCategory) {
                            suggestions.add(prefixedMessage.substring(0, prefixedMessage.indexOf('|')));
                            continue;
                        }

                        if (prefixedMessage.startsWith(prefix)) {
                            suggestions.add(prefixedMessage.replace(prefix, ""));
                        }
                    }
                    messages.clear();

                    if (suggestions.size() > 0){
                        final List<String> sortedSuggestions = new ArrayList<>(suggestions);
                        Collections.sort(sortedSuggestions);
                        for (String category : sortedSuggestions) {
                            completionResultSet.addElement(LookupElementBuilder.create(category));
                        }

                        suggestions.clear();
                        sortedSuggestions.clear();
                    }
                }
            }
        };
        final ElementPattern<PsiElement> filter = PlatformPatterns.psiElement(PsiElement.class)
                .withParent(PlatformPatterns.psiElement(StringLiteralExpression.class)
                    .withParent(PlatformPatterns.psiElement(PhpElementTypes.PARAMETER_LIST)
                        .withParent(PlatformPatterns.psiElement(PhpElementTypes.METHOD_REFERENCE))))
                .withLanguage(PhpLanguage.INSTANCE);

        extend(CompletionType.BASIC, filter, provider);
    }

    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        /* validate the autocompletion target */
//        if (!(position.getParent() instanceof StringLiteralExpression)) {
//            return false;
//        }

        return true;
    }
}
