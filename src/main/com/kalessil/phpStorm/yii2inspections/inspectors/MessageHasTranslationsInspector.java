package com.kalessil.phpStorm.yii2inspections.inspectors;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.kalessil.phpStorm.yii2inspections.codeInsight.TranslationsIndexer;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
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

public class MessageHasTranslationsInspector extends PhpInspection {
    private static final String messagePattern = "The message doesn't have any translations or doesn't belong to the category";

    @NotNull
    public String getShortName() {
        return "MessageHasTranslationsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(MethodReference reference) {
                /* check general call structure */
                final PsiElement[] params = reference.getParameters();
                final String methodName   = reference.getName();
                if (null == methodName || params.length < 2 || !methodName.equals("t")) {
                    return;
                }

                /* validate provided arguments */
                if (!(params[0] instanceof StringLiteralExpression) || !(params[1] instanceof StringLiteralExpression)) {
                    return;
                }
                StringLiteralExpression categoryExpression = (StringLiteralExpression) params[0];
                StringLiteralExpression messageExpression  = (StringLiteralExpression) params[1];
                if (null != categoryExpression.getFirstPsiChild() || null != messageExpression.getFirstPsiChild()) {
                    return;
                }
                final String category = categoryExpression.getContents();
                final String message  = messageExpression.getContents();
                if (StringUtils.isEmpty(category) || StringUtils.isEmpty(message)) {
                    return;
                }

                /* prepare scope of index search */
                final Set<String> searchEntry = new HashSet<>(Collections.singletonList(message));
                GlobalSearchScope theScope = GlobalSearchScope.allScope(reference.getProject());
                theScope = GlobalSearchScope.getScopeRestrictedByFileTypes(theScope, PhpFileType.INSTANCE);

                /* search the index */
                final Set<VirtualFile> providers = new HashSet<>();
                final String expectedFileName    = category + ".php";
                FileBasedIndexImpl.getInstance()
                    .getFilesWithKey(TranslationsIndexer.identity, searchEntry, virtualFile -> {
                        if (virtualFile.getName().equals(expectedFileName)) {
                            providers.add(virtualFile);
                        }

                        return false;
                    }, theScope);

                /* report found cases */
                if (0 == providers.size()) {
                    holder.registerProblem(messageExpression, messagePattern, ProblemHighlightType.GENERIC_ERROR);
                }
                providers.clear();
            }
        };
    }
}
