package com.kalessil.phpStorm.yii2inspections.inspectors;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
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

final public class MissingPropertyAnnotationsInspector extends PhpInspection {
    private static final String messagePattern = "'%p%': properties needs to be annotated";

    @NotNull
    public String getShortName() {
        return "MissingPropertyAnnotationsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            @Override
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
                final Map<String, String> props = this.findPropertyCandidates(clazz);
                if (props.size() > 0) {
                    final String message = messagePattern.replace("%p%", String.join("', '", props.keySet()));
                    holder.registerProblem(nameNode, message, ProblemHighlightType.WEAK_WARNING, new TheLocalFix(props));
                }
            }

            @NotNull
            private Map<String, String> findPropertyCandidates(@NotNull PhpClass clazz) {
                final Map<String, String> properties = new HashMap<>();

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
                for (String getter : mappedMethods.keySet()) {
                    /* ensure getter has a complimentary setter */
                    if (getter.length() <= 3 || !getter.startsWith("get")) {
                        continue;
                    }
                    final String setter = getter.replaceAll("^get", "set");
                    if (!mappedMethods.containsKey(setter)) {
                        continue;
                    }

                    /* additional checks: get has no parameters, complimentary methods should not be static */
                    final Method getterMethod = mappedMethods.get(getter);
                    final Method setterMethod = mappedMethods.get(setter);
                    if (setterMethod.isStatic() || getterMethod.isStatic() || 0 != getterMethod.getParameters().length) {
                        continue;
                    }

                    /* store property and it's types */
                    final Set<String> propertyTypes = new HashSet<>();

                    propertyTypes.addAll(getterMethod.getType().filterUnknown().getTypes());
                    final Parameter[] setterParams = setterMethod.getParameters();
                    if (setterParams.length > 0) {
                        propertyTypes.addAll(setterParams[0].getType().filterUnknown().getTypes());
                    }
                    final String typesAsString = propertyTypes.isEmpty() ? "mixed" : String.join("|", propertyTypes);

                    properties.put(StringUtils.uncapitalize(getter.replaceAll("^get", "")), typesAsString);
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
        final private Map<String, String> properties;

        TheLocalFix(@NotNull Map<String, String> properties) {
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

                /* inject new DocBlock before the class if needed */
                if (!(previous instanceof PhpDocComment)) {
                    /* injection marker needed due to psi-tree structure for NS-ed and not NS-ed classes */
                    final PsiElement injectionMarker;
                    if (null == previous && clazz.getParent() instanceof GroupStatement) {
                        previous        = ((GroupStatement) clazz.getParent()).getPrevPsiSibling();
                        injectionMarker = previous instanceof PhpDocComment ? null : clazz.getParent();
                    } else {
                        injectionMarker = clazz;
                    }

                    PsiElement block = PhpPsiElementFactory.createFromText(project, PhpDocComment.class, "/**\n */\n");
                    if (null != injectionMarker && null != block) {
                        injectionMarker.getParent().addBefore(block, injectionMarker);
                        previous = clazz.getPrevPsiSibling();
                    }
                }

                /* perform injection into the DocBlock */
                if (previous instanceof PhpDocComment) {
                    /* reassemble for processing */
                    final LinkedList<String> lines = new LinkedList<>(Arrays.asList(previous.getText().split("\\n")));

                    /* check if we have already properties */
                    int injectionIndex = 0;
                    for (int i = lines.size() - 1; i > 0; --i) {
                        if (lines.get(i).contains("@property")) {
                            injectionIndex = i;
                            break;
                        }
                    }

                    /* inject properties definition */
                    final String pattern  = lines.peekLast().replaceAll("[\\s/]+$", " ");
                    if (0 == injectionIndex) {
                        lines.add(lines.size() - 1, pattern);
                    }
                    for (String propertyName : this.properties.keySet()) {
                        final String types   = this.properties.get(propertyName);
                        final String newLine = pattern + "@property " + types + " $" + propertyName;

                        lines.add((injectionIndex > 0 ? injectionIndex : lines.size() - 1), newLine);
                    }
                    this.properties.clear();

                    /* generate a new node and replace the old one */
                    final String newContent = String.join("\n", lines);
                    PsiElement newBlock = PhpPsiElementFactory.createFromText(project, PhpDocComment.class, newContent);
                    if (null != newBlock) {
                        previous.replace(newBlock);
                    }
                    lines.clear();
                }
            }
        }
    }
}
