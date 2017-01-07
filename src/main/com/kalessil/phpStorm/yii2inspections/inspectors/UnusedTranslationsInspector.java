package com.kalessil.phpStorm.yii2inspections.inspectors;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.kalessil.phpStorm.yii2inspections.codeInsight.TranslationCallsIndexer;
import com.kalessil.phpStorm.yii2inspections.codeInsight.TranslationKeysIndexer;
import com.kalessil.phpStorm.yii2inspections.inspectors.utils.TranslationProviderUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
                theScope = GlobalSearchScope.getScopeRestrictedByFileTypes(theScope, PhpFileType.INSTANCE);

                /* iterate defined translations and report unused */
                final String searchPrefix = file.getName().replaceAll("\\.php$", "|");
                for (ArrayHashElement pair : expression.getHashElements()) {
                    final PhpPsiElement key = pair.getKey();
                    if (!(key instanceof StringLiteralExpression)) {
                        continue;
                    }

                    final String messageToFind       = ((StringLiteralExpression) key).getContents();
                    final Set<VirtualFile> consumers = new HashSet<>();

                    final Set<String> entry = new HashSet<>(Collections.singletonList(searchPrefix + messageToFind));
                    FileBasedIndexImpl.getInstance()
                        .getFilesWithKey(TranslationCallsIndexer.identity, entry, virtualFile -> {
                            consumers.add(virtualFile);
                            return true;
                        }, theScope);

                    if (0 == consumers.size()) {
                        holder.registerProblem(pair, messagePattern, ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                    }
                    consumers.clear();
                }
            }
        };
    }
}
