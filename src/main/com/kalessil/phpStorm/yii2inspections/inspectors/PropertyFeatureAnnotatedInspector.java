package com.kalessil.phpStorm.yii2inspections.inspectors;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.kalessil.phpStorm.yii2inspections.inspectors.utils.InheritanceChainExtractUtil;
import com.kalessil.phpStorm.yii2inspections.inspectors.utils.NamedElementUtil;
import org.apache.commons.lang.StringUtils;
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

final public class PropertyFeatureAnnotatedInspector extends PhpInspection {
    private static final String messagePattern = "'%p%': properties needs to be annotated";

    @NotNull
    public String getShortName() {
        return "PropertyFeatureAnnotatedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                /* check only regular named classes */
                final PsiElement nameNode = NamedElementUtil.getNameIdentifier(clazz);
                if (null == nameNode) {
                    return;
                }

                /* check if the class inherited from yii\base\Object */
                boolean supportsPropertyFeature = false;
                final Set<PhpClass> parents     = InheritanceChainExtractUtil.collect(clazz);
                if (parents.size() > 0) {
                    for (PhpClass parent : parents) {
                        if (parent.getFQN().equals("\\yii\\base\\Object")) {
                            supportsPropertyFeature = true;
                            break;
                        }
                    }

                    parents.clear();
                }
                if (!supportsPropertyFeature) {
                    return;
                }

                /* iterate get methods, find matching set methods */
                final Set<String> props = this.findPropertyCandidates(clazz);
                if (props.size() > 0) {
                    final String message = messagePattern.replace("%p%", String.join("', '", props));
                    holder.registerProblem(nameNode, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(props));
                }
            }

            @NotNull
            private Set<String> findPropertyCandidates(@NotNull PhpClass clazz) {
                final Set<String> properties = new HashSet<>();

                /* extract methods and operate on name-methods relations */
                final Method[] methods = clazz.getOwnMethods();
                if (null == methods || 0 == methods.length) {
                    return properties;
                }
                final Map<String, Method> mappedMethods = new HashMap<>();
                for (Method method : methods) {
                    mappedMethods.put(method.getName(), method);
                }

                /* process extracted methods*/
                for (String getterCandidate : mappedMethods.keySet()) {
                    /* check only get */
                    if (!getterCandidate.startsWith("get")) {
                        continue;
                    }
                    final String setter = getterCandidate.replaceAll("^get", "set");
                    if (!mappedMethods.containsKey(setter)) {
                        continue;
                    }

                    /* additional check: methods should not be static */
                    if (mappedMethods.get(setter).isStatic() || mappedMethods.get(getterCandidate).isStatic()) {
                        continue;
                    }

                    /* store property as required */
                    properties.add(StringUtils.uncapitalize(getterCandidate.replaceAll("^get", "")));
                }

                /* exclude annotated properties: lazy bulk operation */
                if (properties.size() > 0) {
                    final Collection<Field> fields = clazz.getFields();
                    for (Field candidate : fields) {
                        /* do not process constants and static fields */
                        if (candidate.isConstant() || candidate.getModifier().isStatic()) {
                            continue;
                        }

                        properties.remove(candidate.getName());
                    }
                    fields.clear();
                }

                return properties;
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private Set<String> properties;

        TheLocalFix(@NotNull Set<String> properties) {
            super();
            this.properties = properties;
        }

        @NotNull
        @Override
        public String getName() {
            return "Annotate properties";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression     = descriptor.getPsiElement();
            final PsiElement clazzCandidate = null == expression ? null : expression.getParent();
            if (clazzCandidate instanceof PhpClass) {
                final PhpClass clazz   = (PhpClass) clazzCandidate;
                PhpPsiElement previous = clazz.getPrevPsiSibling();

                /* create PhpDoc if it's missing */

                if (previous instanceof PhpDocComment) {
                    /* reassemble for processing */
                    final LinkedList<String> lines = new LinkedList<>(Arrays.asList(previous.getText().split("\\n")));
                    final String lastLine          = lines.pollLast();

                    /* inject properties definition */
                    final String pattern  = lastLine.replaceAll("[\\s/]+$", " ");
                    for (String propertyName : this.properties) {
                        lines.add(pattern + "@property mixed $" + propertyName);
                    }
                    this.properties.clear();

                    /* generate a new node and replace the old one */
                    lines.add(lastLine);
                    final String newContent = String.join("\n", lines);
                    lines.clear();
                    PsiElement newBlock = PhpPsiElementFactory.createFromText(project, PhpDocComment.class, newContent);
                    if (null != newBlock) {
                        previous.replace(newBlock);
                    }
                }
            }
        }
    }
}
