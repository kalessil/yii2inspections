package com.kalessil.phpStorm.yii2inspections.inspectors.utils;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;

final public class InheritanceChainExtractUtil {
    @NotNull
    public static Set<PhpClass> collect(@NotNull PhpClass clazz) {
        final Set<PhpClass> processedItems = new HashSet<>();

        if (clazz.isInterface()) {
            processInterface(clazz, processedItems);
        } else {
            processClass(clazz, processedItems);
        }

        return processedItems;
    }

    private static void processClass(@NotNull PhpClass clazz, @NotNull Set<PhpClass> processed) {
        if (clazz.isInterface()) {
            throw new InvalidParameterException("Interface shall not be provided");
        }
        processed.add(clazz);

        /* re-delegate interface handling */
        for (PhpClass anInterface : clazz.getImplementedInterfaces()) {
            processInterface(anInterface, processed);
        }

        /* handle parent class */
        if (null != clazz.getSuperClass()) {
            processClass(clazz.getSuperClass(), processed);
        }
    }

    private static void processInterface(@NotNull PhpClass clazz, @NotNull Set<PhpClass> processed) {
        if (!clazz.isInterface()) {
            throw new InvalidParameterException("Class shall not be provided");
        }

        if (processed.add(clazz)) {
            for (PhpClass parentInterface : clazz.getImplementedInterfaces()) {
                processInterface(parentInterface, processed);
            }
        }
    }
}
