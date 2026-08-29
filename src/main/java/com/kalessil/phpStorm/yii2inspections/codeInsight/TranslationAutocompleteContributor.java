package com.kalessil.phpStorm.yii2inspections.codeInsight;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.yii2inspections.inspectors.utils.StringLiteralExtractUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class TranslationAutocompleteContributor extends CompletionContributor {
    public TranslationAutocompleteContributor() {
        final CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(
                    @NotNull CompletionParameters completionParameters,
                    @NotNull ProcessingContext processingContext,
                    @NotNull CompletionResultSet completionResultSet
            ) {
                /* validate the autocompletion target */
                final PsiElement target = completionParameters.getOriginalPosition();
                if (target == null || !(target.getParent() instanceof StringLiteralExpression parameter)) {
                    return;
                }

                /* suggest only to target code structure */
                final PsiElement context = parameter.getParent().getParent();
                if (!(context instanceof FunctionReference reference)) {
                    return;
                }

                // validate if valid autocompletion target
                final String name            = reference.getName();
                final PsiElement[] arguments = reference.getParameters();
                int categoryPosition         = -1;
                int messagePosition          = -1;
                boolean isTarget             = false;
                if (reference instanceof MethodReference) {
                    if ("t".equals(name) || "registerTranslations".equals(name) || "prep".equals(name)) {
                        isTarget         = true;
                        categoryPosition = 0;
                        messagePosition  = 1;
                    } else if ("translate".equals(name)) {
                        // CraftCms\Cms\Support\Facades\I18N::translate(), CraftCms\Cms\Translation\I18N::translate(), Yiisoft\Translator\Translator::translate()
                        isTarget         = true;
                        categoryPosition = 2;
                        messagePosition  = 0;
                    }
                } else {
                    if ("t".equals(name)) {
                        // CraftCms\Cms\t()
                        isTarget         = true;
                        categoryPosition = 2;
                        messagePosition  = 0;
                    }
                }
                if (arguments.length == 0 || ! isTarget ) {
                    return;
                }

                /* generate proposals */
                final boolean autocompleteCategory = arguments.length > categoryPosition && arguments[categoryPosition] == parameter;
                final boolean autocompleteMessage  = arguments.length > messagePosition && arguments[messagePosition] == parameter;
                if (autocompleteCategory || autocompleteMessage) {
                    StringLiteralExpression categoryLiteral = null;
                    if (autocompleteMessage) {
                        categoryLiteral = StringLiteralExtractUtil.resolveAsStringLiteral(arguments[categoryPosition], true);
                        if (categoryLiteral == null) {
                            return;
                        }
                    }

                    /* extract uniques messages for faster processing */
                    final FileBasedIndex index    = FileBasedIndex.getInstance();
                    final Project project         = target.getProject();
                    final GlobalSearchScope scope = GlobalSearchScope.allScope(project);

                    final Set<String> suggestions = new HashSet<>();
                    final String category         = autocompleteMessage ? categoryLiteral.getContents() : "";
                    for (final String candidate : index.getAllKeys(TranslationKeysIndexer.identity, project)) {
                        if (autocompleteCategory) {
                            suggestions.addAll(index.getValues(TranslationKeysIndexer.identity, candidate, scope));
                        } else if (!category.isEmpty()) {
                            final List<String> categories = index.getValues(TranslationKeysIndexer.identity, candidate, scope);
                            if (categories.contains(category)) {
                                suggestions.add(candidate);
                            }
                        }
                    }

                    if (!suggestions.isEmpty()) {
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
