package com.kalessil.phpStorm.yii2inspections.utils;

import org.jetbrains.annotations.Nullable;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class StringUtils {
    public static @Nullable String uncapitalize(@Nullable String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }
        return Character.toLowerCase(string.charAt(0)) + string.substring(1);
    }

    public static int countMatches(@Nullable String string, @Nullable String substring) {
        if (string == null || string.isEmpty() || substring == null || substring.isEmpty()) {
            return 0;
        }

        int count = 0;
        int index = 0;
        while ((index = string.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length(); // Move past the match to avoid infinite loop
        }
        return count;
    }
}
