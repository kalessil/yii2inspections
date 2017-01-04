package com.kalessil.phpStorm.yii2inspections.actors;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

final public class UpdateTranslationsActor extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        /* TODO: implement */
    }

    @Override
    public void update(AnActionEvent event) {
        if (null != event.getProject()) {
            event.getPresentation().setEnabled(true);
            event.getPresentation().setVisible(true);
        }
    }
}
