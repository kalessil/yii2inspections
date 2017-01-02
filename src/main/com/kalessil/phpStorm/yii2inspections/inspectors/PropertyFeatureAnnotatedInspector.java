package com.kalessil.phpStorm.yii2inspections.inspectors;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.kalessil.phpStorm.yii2inspections.inspectors.utils.InheritanceChainExtractUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @NotNull
    public String getShortName() {
        return "PropertyFeatureAnnotatedInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                boolean supportsPropertyFeature = false;

                /* check if the class inherited from yii\base\Object */
                final Set<PhpClass> parents = InheritanceChainExtractUtil.collect(clazz);
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
                holder.registerProblem(clazz.getNameIdentifier(), props.toString(), ProblemHighlightType.WEAK_WARNING);
                props.clear();
            }

            @NotNull
            private Set<String> findPropertyCandidates(@NotNull PhpClass clazz) {
                final Set<String> props = new HashSet<>();

                /* extract methods and operate on name-methods relations */
                final Collection<Method> methods = clazz.getMethods();
                if (null == methods || 0 == methods.size()) {
                    return props;
                }
                final Map<String, Method> mappedMethods = new HashMap<>();
                for (Method method : methods) {
                    mappedMethods.put(method.getName(), method);
                }
                methods.clear();

                /* process extracted methods*/
                for (String methodName : mappedMethods.keySet()) {
                    /* check only get */
                    if (!methodName.startsWith("get")) {
                        continue;
                    }
                    final String setter = methodName.replaceAll("^get", "set");
                    if (!mappedMethods.containsKey(setter)) {
                        continue;
                    }

                    /* put the property, keep original naming */
                    props.add(methodName.replaceAll("^get", ""));
                }

                return props;
            }
        };
    }
}
