package com.kalessil.phpStorm.yii2inspections.inspectors.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.Nullable;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class NamedElementUtil {

    /** returns name identifier, which is valid for reporting */
    @Nullable
    static public PsiElement getNameIdentifier(@Nullable PsiNameIdentifierOwner element) {
        if (null != element) {
            PsiElement id          = element.getNameIdentifier();
            boolean isIdReportable = null != id && id.getTextLength() > 0;

            return isIdReportable ? id : null;
        }

        return null;
    }

}
