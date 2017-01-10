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
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.jetbrains.php.util.PhpStringUtil;
import com.kalessil.phpStorm.yii2inspections.codeInsight.TranslationKeysIndexer;
import com.kalessil.phpStorm.yii2inspections.inspectors.utils.StringLiteralExtractUtil;
import com.kalessil.phpStorm.yii2inspections.utils.TranslationCallsProcessUtil;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;
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
    private static final String messageInjection      = "Parametrized message should be used instead, e.g.: Yii::t('app', 'Token is: {token}', ['token' => 'value'])";

    final static private Pattern nonAsciiCharsRegex;
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
                /* ensure that it's a target call; category is not empty and has no injections; we have messages */
                TranslationCallsProcessUtil.ProcessingResult extracted = TranslationCallsProcessUtil.process(reference, true);
                if (null == extracted) {
                    return;
                }

                /* iterate found translations and validate correctness */
                final String expectedFileName                           = extracted.getCategory().getContents() + ".php";
                final Map<StringLiteralExpression, PsiElement> messages = extracted.getMessages();
                for (StringLiteralExpression literal : messages.keySet()) {
                    /* only quotes, no content presented */
                    if (literal.getTextLength() <= 2) {
                        continue;
                    }

                    final String message             = literal.getContents();
                    final PsiElement reportingTarget = messages.get(literal);

                    /* warn injections are presented and skip further processing */
                    if (REPORT_INJECTIONS && null != literal.getFirstPsiChild()) {
                        holder.registerProblem(reportingTarget, messageInjection, ProblemHighlightType.WEAK_WARNING);
                        continue;
                    }
                    /* warn if non-ascii characters has been used */
                    if (REPORT_NONASCII_CHARACTERS && nonAsciiCharsRegex.matcher(message).matches()) {
                        holder.registerProblem(reportingTarget, messageNonAscii, ProblemHighlightType.WEAK_WARNING);
                    }

                    /* warn if the message is have no translations in the group */
                    final Set<String> searchEntry
                        = new HashSet<>(Collections.singletonList(PhpStringUtil.unescapeText(message, literal.isSingleQuote())));
                    GlobalSearchScope theScope = GlobalSearchScope.allScope(reference.getProject());
                    theScope                   = GlobalSearchScope.getScopeRestrictedByFileTypes(theScope, PhpFileType.INSTANCE);
                    final Set<VirtualFile> providers = new HashSet<>();
                    FileBasedIndex.getInstance()
                            .getFilesWithKey(TranslationKeysIndexer.identity, searchEntry, virtualFile -> {
                                if (virtualFile.getName().equals(expectedFileName)) {
                                    providers.add(virtualFile);
                                }

                                return true;
                            }, theScope);

                    /* report found cases */
                    if (0 == providers.size()) {
                        holder.registerProblem(reportingTarget, messageNoTranslations, ProblemHighlightType.WEAK_WARNING);
                    }
                    providers.clear();
                }

                extracted.dispose();
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
