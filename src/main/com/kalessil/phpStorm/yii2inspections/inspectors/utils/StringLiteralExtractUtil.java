package com.kalessil.phpStorm.yii2inspections.inspectors.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

final public class StringLiteralExtractUtil {
    @Nullable
    private static PsiElement getExpressionTroughParenthesis(@Nullable PsiElement expression) {
        if (!(expression instanceof ParenthesizedExpression)) {
            return expression;
        }

        PsiElement innerExpression = ((ParenthesizedExpression) expression).getArgument();
        while (innerExpression instanceof ParenthesizedExpression) {
            innerExpression = ((ParenthesizedExpression) innerExpression).getArgument();
        }

        return innerExpression;
    }

    @Nullable
    private static Function getScope(@NotNull PsiElement expression) {
        PsiElement parent = expression.getParent();
        while (null != parent && !(parent instanceof PhpFile)) {
            if (parent instanceof Function) {
                return (Function) parent;
            }

            parent = parent.getParent();
        }

        return null;
    }

    @Nullable
    public static StringLiteralExpression resolveAsStringLiteral(@Nullable PsiElement expression) {
        if (null == expression) {
            return null;
        }
        expression = getExpressionTroughParenthesis(expression);

        if (expression instanceof StringLiteralExpression) {
            return (StringLiteralExpression) expression;
        }

        if (expression instanceof FieldReference || expression instanceof ClassConstantReference) {
            final Field fieldOrConstant = (Field) ((MemberReference) expression).resolve();
            if (null != fieldOrConstant && fieldOrConstant.getDefaultValue() instanceof StringLiteralExpression) {
                return (StringLiteralExpression) fieldOrConstant.getDefaultValue();
            }
        }

        if (expression instanceof Variable) {
            final String variable = ((Variable) expression).getName();
            if (!StringUtil.isEmpty(variable)) {
                final Function scope = getScope(expression);
                if (null != scope) {
                    final Set<AssignmentExpression> matched = new HashSet<>();

                    Collection<AssignmentExpression> assignments
                            = PsiTreeUtil.findChildrenOfType(scope, AssignmentExpression.class);
                    /* collect self-assignments as well */
                    for (AssignmentExpression assignment : assignments) {
                        if (assignment.getVariable() instanceof Variable && assignment.getValue() instanceof StringLiteralExpression) {
                            final String name = assignment.getVariable().getName();
                            if (!StringUtil.isEmpty(name) && name.equals(variable)) {
                                matched.add(assignment);
                            }
                        }
                    }
                    assignments.clear();

                    if (matched.size() == 1) {
                        StringLiteralExpression result = (StringLiteralExpression) matched.iterator().next().getValue();

                        matched.clear();
                        return result;
                    }
                    matched.clear();
                }
            }
        }

        return null;
    }
}
