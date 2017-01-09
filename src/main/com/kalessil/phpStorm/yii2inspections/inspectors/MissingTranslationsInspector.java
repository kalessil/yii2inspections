package com.kalessil.phpStorm.yii2inspections.inspectors;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.jetbrains.php.util.PhpStringUtil;
import com.kalessil.phpStorm.yii2inspections.codeInsight.TranslationCallsIndexer;
import com.kalessil.phpStorm.yii2inspections.inspectors.utils.TranslationProviderUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
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

final public class MissingTranslationsInspector extends PhpInspection {
    private static final String messagePattern = "Some message are missing: %c% in total";

    @NotNull
    public String getShortName() {
        return "MissingTranslationsInspection";
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

                /* extract own messages */
                final Set<String> providedMessages = new HashSet<>();
                for (ArrayHashElement pair : expression.getHashElements()) {
                    final PhpPsiElement key = pair.getKey();
                    if (key instanceof StringLiteralExpression) {
                        final StringLiteralExpression literal = (StringLiteralExpression) key;
                        final String message                  = literal.getContents();
                        final boolean isSingleQuote           = literal.isSingleQuote();
                        providedMessages.add(PhpStringUtil.unescapeText(message, isSingleQuote));
                    }
                }
                if (0 == providedMessages.size()) {
                    return;
                }

                /* search globally for missing translations */
                final Collection<String> usages = FileBasedIndex.getInstance()
                        .getAllKeys(TranslationCallsIndexer.identity, expression.getProject());
                if (usages.size() > 0) {
                    final Set<String> missing = new HashSet<>();

                    final String searchPrefix = file.getName().replaceAll("\\.php$", "|");
                    for (String usage : usages) {
                        if (!usage.startsWith(searchPrefix)) {
                            continue;
                        }

                        final String message = usage.replace(searchPrefix, "");
                        if (!providedMessages.contains(message)) {
                            missing.add(message);
                        }
                    }
                    usages.clear();

                    if (missing.size() > 0) {
                        final String message = messagePattern.replace("%c%", String.valueOf(missing.size()));
                        holder.registerProblem(expression.getFirstChild(), message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(missing));
                    }
                }

                providedMessages.clear();
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private Set<String> missing;

        TheLocalFix(@NotNull Set<String> missing) {
            super();
            this.missing = missing;
        }

        @NotNull
        @Override
        public String getName() {
            return "Add missing translations";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (null != expression && expression.getParent() instanceof ArrayCreationExpression) {
                final ArrayCreationExpression container = (ArrayCreationExpression) expression.getParent();
                final PsiElement closingBracket         = container.getLastChild();
                if (null == closingBracket) {
                    return;
                }

                for (String translation : missing) {
                    /* reach or create a comma */
                    PsiElement last = closingBracket.getPrevSibling();
                    if (last instanceof PsiWhiteSpace) {
                        last = last.getPrevSibling();
                    }
                    if (null != last && PhpTokenTypes.opCOMMA != last.getNode().getElementType()) {
                        last.getParent().addAfter(PhpPsiElementFactory.createComma(project), last);
                        last = last.getNextSibling();
                    }

                    /* add a new translation */
                    final String escaped                = PhpStringUtil.escapeText(translation, true, '\n', '\t');
                    final String pairPattern            = "array('%s%' => '%s%')".replace("%s%", escaped).replace("%s%", escaped);
                    final ArrayCreationExpression array = PhpPsiElementFactory.createFromText(project, ArrayCreationExpression.class, pairPattern);
                    final PhpPsiElement pair            = null == array ? null : array.getHashElements().iterator().next();
                    final PsiWhiteSpace space           = PhpPsiElementFactory.createFromText(project, PsiWhiteSpace.class, " ");
                    if (null == last || null == pair || null == space) {
                        continue;
                    }
                    last.getParent().addAfter(pair, last);
                    last.getParent().addAfter(space, last);
                }
            }
        }
    }
}
