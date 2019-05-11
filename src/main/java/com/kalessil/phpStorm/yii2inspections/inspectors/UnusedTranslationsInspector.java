package com.kalessil.phpStorm.yii2inspections.inspectors;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.jetbrains.php.util.PhpStringUtil;
import com.jetbrains.twig.TwigFileType;
import com.kalessil.phpStorm.yii2inspections.codeInsight.TranslationCallsIndexer;
import com.kalessil.phpStorm.yii2inspections.inspectors.utils.TranslationProviderUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class UnusedTranslationsInspector extends PhpInspection {
    private static final String messagePattern = "This translation seems to be not used";

    @NotNull
    public String getShortName() {
        return "UnusedTranslationsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            @Override
            public void visitPhpArrayCreationExpression(ArrayCreationExpression expression) {
                final PsiFile file = expression.getContainingFile();
                if (!TranslationProviderUtil.isProvider(file)) {
                    return;
                }

                /* prepare scope of index search */
                GlobalSearchScope theScope = GlobalSearchScope.allScope(expression.getProject());
                theScope = GlobalSearchScope.getScopeRestrictedByFileTypes(theScope, PhpFileType.INSTANCE, HtmlFileType.INSTANCE, TwigFileType.INSTANCE);

                /* iterate defined translations and report unused */
                final String searchPrefix = file.getName().replaceAll("\\.php$", "|");
                final Project project     = expression.getProject();
                for (ArrayHashElement pair : expression.getHashElements()) {
                    final PhpPsiElement key = pair.getKey();
                    if (!(key instanceof StringLiteralExpression)) {
                        continue;
                    }

                    final StringLiteralExpression literal = (StringLiteralExpression) key;
                    final String messageToFind            = PhpStringUtil.unescapeText(literal.getContents(), literal.isSingleQuote());
                    final String regularEntry             = searchPrefix + messageToFind;

                    final Set<String> usages
                        = new HashSet<>(FileBasedIndex.getInstance().getAllKeys(TranslationCallsIndexer.identity, project));
                    boolean found = usages.contains(regularEntry);
                    if (!found && usages.size() > 0) {
                        final String slashedCategoryEntry = "/" + regularEntry;
                        for (String usage : usages) {
                            if (usage.endsWith(slashedCategoryEntry)) {
                                found = true;
                                break;
                            }
                        }
                    }
                    usages.clear();

                    if (!found) {
                        holder.registerProblem(pair, messagePattern, ProblemHighlightType.LIKE_UNUSED_SYMBOL, new TheLocalFix());
                    }
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Remove unused translations";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof ArrayHashElement) {
                PsiElement next = expression.getNextSibling();
                if (next instanceof PsiWhiteSpace) {
                    next.delete();
                }
                next = expression.getNextSibling();
                if (null != next && PhpTokenTypes.opCOMMA == next.getNode().getElementType()) {
                    next.delete();
                }
                next = expression.getNextSibling();
                if (next instanceof PsiWhiteSpace) {
                    next.delete();
                }

                expression.delete();
            }
        }
    }
}
