package com.kalessil.phpStorm.yii2inspections.codeInsight;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.util.PhpStringUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class TranslationKeysIndexer extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> identity = ID.create("com.kalessil.phpStorm.yii2inspections.translation_keys");
    private final KeyDescriptor<String> descriptor  = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, String> getName() {
        return identity;
    }

    @NotNull
    @Override
    public DataIndexer<String, String, FileContent> getIndexer() {
        return fileContent -> {
            final Map<String, String> map      = new THashMap<>();
            final PhpReturn returnExpression = PsiTreeUtil.findChildOfType(fileContent.getPsiFile(), PhpReturn.class);
            if (returnExpression != null) {
                final PsiElement argument = returnExpression.getArgument();
                if (argument instanceof ArrayCreationExpression) {
                    final String category = argument.getContainingFile().getName().replaceAll("\\.php$", "");
                    for (final ArrayHashElement item : ((ArrayCreationExpression) argument).getHashElements()) {
                        final PhpPsiElement key = item.getKey();
                        if (key instanceof StringLiteralExpression) {
                            final StringLiteralExpression literal = (StringLiteralExpression) key;
                            final String message                  = literal.getContents();
                            if (!message.isEmpty()) {
                                map.putIfAbsent(PhpStringUtil.unescapeText(message, literal.isSingleQuote()), category);
                            }
                        }
                    }
                }
            }
            return map;
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return this.descriptor;
    }

    @NotNull
    @Override
    public DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> {
            if (PhpFileType.INSTANCE != file.getFileType() || file.getName().equals("config.php")) {
                return false;
            }

            return file.getPath().matches(".*/(translations|messages)/([a-zA-z]{2}(-[a-zA-z]{2})?)/[^/]+\\.php$");
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 4;
    }
}
