package com.kalessil.phpStorm.yii2inspections.utils;

import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class MessagesPresentationUtil {
    static public String prefixWithYii(@NotNull String message) {
        return "[Yii2] " + message;
    }
}
