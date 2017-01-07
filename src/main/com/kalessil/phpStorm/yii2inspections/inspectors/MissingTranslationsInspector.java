package com.kalessil.phpStorm.yii2inspections.inspectors;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
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
                        providedMessages.add(((StringLiteralExpression) key).getContents());
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
                        holder.registerProblem(expression.getFirstChild(), message, ProblemHighlightType.WEAK_WARNING);
                    }
                }

                providedMessages.clear();
            }
        };
    }
}
