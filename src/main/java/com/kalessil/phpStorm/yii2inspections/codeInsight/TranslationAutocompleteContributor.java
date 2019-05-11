package com.kalessil.phpStorm.yii2inspections.codeInsight;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.yii2inspections.inspectors.utils.StringLiteralExtractUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TranslationAutocompleteContributor extends CompletionContributor {
    public TranslationAutocompleteContributor() {
        final CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(
                    @NotNull CompletionParameters completionParameters,
                    ProcessingContext processingContext,
                    @NotNull CompletionResultSet completionResultSet
            ) {
                /* validate the autocompletion target */
                final PsiElement target = completionParameters.getOriginalPosition();
                if (target == null || !(target.getParent() instanceof StringLiteralExpression)) {
                    return;
                }

                /* suggest only to target code structure */
                final StringLiteralExpression parameter = (StringLiteralExpression) target.getParent();
                final PsiElement context                = parameter.getParent().getParent();
                if (!(context instanceof MethodReference)) {
                    return;
                }

                final MethodReference reference         = (MethodReference) context;
                final String name                       = reference.getName();
                final PsiElement[] arguments            = reference.getParameters();
                if (name == null || arguments.length == 0 || (!name.equals("t") && !name.equals("registerTranslations"))) {
                    return;
                }

                /* generate proposals */
                final boolean autocompleteCategory = arguments[0] == parameter;
                final boolean autocompleteMessage  = arguments.length > 1 && arguments[1] == parameter;
                if (autocompleteCategory || autocompleteMessage) {
                    StringLiteralExpression category = null;
                    if (autocompleteMessage) {
                        category = StringLiteralExtractUtil.resolveAsStringLiteral(arguments[0], true);
                        if (category == null) {
                            return;
                        }
                    }

                    /* extract uniques messages for faster processing */
                    final Set<String> suggestions = new HashSet<>();
                    final Set<String> messages
                        = new HashSet<>(FileBasedIndex.getInstance().getAllKeys(TranslationCallsIndexer.identity, target.getProject()));

                    final String prefix = autocompleteMessage ? category.getContents() + "|" : null;
                    for (final String prefixedMessage : messages) {
                        if (autocompleteCategory) {
                            suggestions.add(prefixedMessage.substring(0, prefixedMessage.indexOf('|')));
                        } else if (prefixedMessage.startsWith(prefix)) {
                            suggestions.add(prefixedMessage.substring(prefixedMessage.indexOf('|') + 1));
                        }
                    }
                    messages.clear();

                    if (!suggestions.isEmpty()){
                        final List<String> sortedSuggestions = new ArrayList<>(suggestions);
                        Collections.sort(sortedSuggestions);
                        for (final String suggestion : sortedSuggestions) {
                            completionResultSet.addElement(LookupElementBuilder.create(suggestion));
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
        final PsiElement candidate = position instanceof StringLiteralExpression ? position : position.getParent();
        return candidate instanceof StringLiteralExpression && candidate.getParent() instanceof ParameterList;
    }
}
