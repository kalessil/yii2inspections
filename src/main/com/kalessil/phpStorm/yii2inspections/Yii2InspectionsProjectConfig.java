package com.kalessil.phpStorm.yii2inspections;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class Yii2InspectionsProjectConfig implements Configurable {
    private final Project project;
    private final Yii2InspectionsProjectConfigPanel panel;

    public Yii2InspectionsProjectConfig(@NotNull Project project) {
        this(project, new Yii2InspectionsProjectConfigPanel(project));
    }

    public Yii2InspectionsProjectConfig(@NotNull Project project, @NotNull Yii2InspectionsProjectConfigPanel panel) {
        this.project = project;
        this.panel   = panel;
    }

    public String getDisplayName() {
        return "Yii2 Inspections";
    }

    public String getHelpTopic() {
        return null;
    }

    public boolean isModified() {
        /* TODO: implement */
        return false;
    }

    public void apply() throws ConfigurationException {
        /* TODO: implement */
    }

    public void reset() {
        /* TODO: implement */
    }

    public void disposeUIResources() {
        /* TODO: implement */
    }

    public JComponent createComponent() {
        reset();
        return panel;
    }
}
