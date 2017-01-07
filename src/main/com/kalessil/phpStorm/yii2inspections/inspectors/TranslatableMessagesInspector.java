package com.kalessil.phpStorm.yii2inspections.inspectors;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.kalessil.phpStorm.yii2inspections.codeInsight.TranslationKeysIndexer;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class TranslatableMessagesInspector extends PhpInspection {
    private static final String messageNoTranslations = "The message doesn't have any translations or doesn't belong to the category";
    private static final String messageNonAscii       = "Usage of any characters out of ASCII range will cause translation problems.";

    @SuppressWarnings("CanBeFinal")
    static private Pattern nonAsciiCharsRegex = null;
    static {
        nonAsciiCharsRegex = Pattern.compile(".*[^\\u0000-\\u007F]+.*");
    }

    @NotNull
    public String getShortName() {
        return "TranslatableMessagesInspection";
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

                /* warn if non-ascii characters has been used */
                if (nonAsciiCharsRegex.matcher(message).matches()) {
                    holder.registerProblem(messageExpression, messageNonAscii, ProblemHighlightType.WEAK_WARNING);
                }

                /* prepare scope of index search */
                final Set<String> searchEntry = new HashSet<>(Collections.singletonList(message));
                GlobalSearchScope theScope = GlobalSearchScope.allScope(reference.getProject());
                theScope = GlobalSearchScope.getScopeRestrictedByFileTypes(theScope, PhpFileType.INSTANCE);

                /* search the index */
                final Set<VirtualFile> providers = new HashSet<>();
                final String expectedFileName    = category + ".php";
                FileBasedIndex.getInstance()
                    .getFilesWithKey(TranslationKeysIndexer.identity, searchEntry, virtualFile -> {
                        if (virtualFile.getName().equals(expectedFileName)) {
                            providers.add(virtualFile);
                        }

                        return true;
                    }, theScope);

                /* report found cases */
                if (0 == providers.size()) {
                    holder.registerProblem(messageExpression, messageNoTranslations, ProblemHighlightType.WEAK_WARNING);
                }
                providers.clear();
            }
        };
    }
}
