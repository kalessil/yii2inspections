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

final public class TranslationKeysIndexer extends FileBasedIndexExtension<String, Void> {
    public static final ID<String, Void> identity  = ID.create("com.kalessil.phpStorm.yii2inspections.translation_keys");
    private final KeyDescriptor<String> descriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return identity;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return fileContent -> {
            final Map<String, Void> map = new THashMap<>();

            /* ignore file if its' structure is not as expected */
            final PhpReturn returnExpression = PsiTreeUtil.findChildOfType(fileContent.getPsiFile(), PhpReturn.class);
            final PsiElement argument        = null == returnExpression ? null : returnExpression.getArgument();
            if (!(argument instanceof ArrayCreationExpression)) {
                return map;
            }

            /* extract translations from the file */
            for (ArrayHashElement item : ((ArrayCreationExpression) argument).getHashElements()) {
                final PhpPsiElement key = item.getKey();
                if (key instanceof StringLiteralExpression) {
                    final StringLiteralExpression literal = (StringLiteralExpression) key;
                    final String message                  = literal.getContents();
                    final boolean isSingleQuote           = literal.isSingleQuote();
                    map.putIfAbsent(PhpStringUtil.unescapeText(message, isSingleQuote), null);
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
    public DataExternalizer<Void> getValueExternalizer() {
        return ScalarIndexExtension.VOID_DATA_EXTERNALIZER;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> {
            //noinspection SimplifiableIfStatement - better readability
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
        return 3;
    }
}
