package com.kalessil.phpStorm.yii2inspections.inspectors.utils;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.PhpFileType;
import org.jetbrains.annotations.Nullable;

final public class TranslationProviderUtil {
    static public boolean isProvider(@Nullable PsiFile file) {
        //noinspection SimplifiableIfStatement - easier to read
        if (null == file || PhpFileType.INSTANCE != file.getFileType() || file.getName().equals("config.php")) {
            return false;
        }
        return file.getVirtualFile().getPath()
                .matches(".*/(translations|messages)/([a-zA-z]{2}(_[a-zA-z]{2})?)/[^/]+\\.php$");
    }
}
