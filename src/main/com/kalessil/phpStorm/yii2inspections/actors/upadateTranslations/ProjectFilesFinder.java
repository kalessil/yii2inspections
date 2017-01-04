package com.kalessil.phpStorm.yii2inspections.actors.upadateTranslations;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final class ProjectFilesFinder implements Iterator {
    private final LinkedList<PsiFileSystemItem> items = new LinkedList<>();

    ProjectFilesFinder(@NotNull Project project) {
        PsiDirectory root = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
        if (null != root) {
            Collections.addAll(this.items, root.getFiles());
            Collections.addAll(this.items, root.getSubdirectories());
        }
    }

    @Override
    public boolean hasNext() {
        while (this.items.size() > 0 && !(this.items.getFirst() instanceof PsiFile)) {
            final PsiFileSystemItem first = this.items.pollFirst();
            if (first instanceof PsiDirectory) {
                for (PsiFile file: ((PsiDirectory) first).getFiles()) {
                    this.items.add(0, file);
                }
                Collections.addAll(this.items, ((PsiDirectory) first).getSubdirectories());
            }
        }

        return this.items.size() > 0;
    }

    @Override
    public PsiFileSystemItem next() {
        return this.items.pollFirst();
    }
}