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
import com.jetbrains.php.util.PhpStringUtil;
import com.kalessil.phpStorm.yii2inspections.codeInsight.TranslationKeysIndexer;
import com.kalessil.phpStorm.yii2inspections.inspectors.utils.StringLiteralExtractUtil;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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

final public class TranslationsCorrectnessInspector extends PhpInspection {
    // configuration flags automatically saved by IDE
    @SuppressWarnings("WeakerAccess")
    public boolean REPORT_NONASCII_CHARACTERS = true;
    @SuppressWarnings("WeakerAccess")
    public boolean REPORT_INJECTIONS = true;

    private static final String messageNoTranslations = "The message doesn't have any translations or doesn't belong to the category";
    private static final String messageNonAscii       = "Usage of any characters out of ASCII range is not recommended.";
    private static final String messageInjections     = "Parametrized message should be used instead, e.g.: Yii::t('app', 'Token is: {token}', ['token' => 'value'])";

    @SuppressWarnings("CanBeFinal")
    static private Pattern nonAsciiCharsRegex = null;
    static {
        nonAsciiCharsRegex = Pattern.compile(".*[^\\u0000-\\u007F]+.*");
    }

    @NotNull
    public String getShortName() {
        return "TranslationsCorrectnessInspection";
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
                StringLiteralExpression categoryExpression = StringLiteralExtractUtil.resolveAsStringLiteral(params[0]);
                StringLiteralExpression messageExpression  = StringLiteralExtractUtil.resolveAsStringLiteral(params[1]);
                if (
                    null == categoryExpression || null != categoryExpression.getFirstPsiChild() ||
                    null == messageExpression  || null != messageExpression.getFirstPsiChild()
                ) {
                    if (REPORT_INJECTIONS && null != messageExpression && null != messageExpression.getFirstPsiChild()) {
                        holder.registerProblem(params[1], messageInjections, ProblemHighlightType.WEAK_WARNING);
                    }

                    return;
                }
                final String category       = categoryExpression.getContents();
                final String message        = messageExpression.getContents();
                final boolean isSingleQuote = messageExpression.isSingleQuote();
                if (StringUtils.isEmpty(category) || StringUtils.isEmpty(message)) {
                    return;
                }

                /* warn if non-ascii characters has been used */
                if (REPORT_NONASCII_CHARACTERS && nonAsciiCharsRegex.matcher(message).matches()) {
                    holder.registerProblem(params[1], messageNonAscii, ProblemHighlightType.WEAK_WARNING);
                }

                /* prepare scope of index search */
                final Set<String> searchEntry
                        = new HashSet<>(Collections.singletonList(PhpStringUtil.unescapeText(message, isSingleQuote)));
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
                    holder.registerProblem(params[1], messageNoTranslations, ProblemHighlightType.WEAK_WARNING);
                }
                providers.clear();
            }
        };
    }

    public JComponent createOptionsPanel() {
        return (new TranslationsCorrectnessInspector.OptionsPanel()).getComponent();
    }

    private class OptionsPanel {
        final private JPanel optionsPanel;

        final private JCheckBox reportNonAsciiCodes;
        final private JCheckBox reportInjections;

        OptionsPanel() {
            optionsPanel = new JPanel();
            optionsPanel.setLayout(new MigLayout());

            reportNonAsciiCodes = new JCheckBox("Report non-ASCII characters usage", REPORT_NONASCII_CHARACTERS);
            reportNonAsciiCodes.addChangeListener(e -> REPORT_NONASCII_CHARACTERS = reportNonAsciiCodes.isSelected());
            optionsPanel.add(reportNonAsciiCodes, "wrap");

            reportInjections = new JCheckBox("Suggest using parametrised messages", REPORT_INJECTIONS);
            reportInjections.addChangeListener(e -> REPORT_INJECTIONS = reportInjections.isSelected());
            optionsPanel.add(reportInjections, "wrap");
        }

        JPanel getComponent() {
            return optionsPanel;
        }
    }
}
