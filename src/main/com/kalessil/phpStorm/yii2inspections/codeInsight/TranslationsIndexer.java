package com.kalessil.phpStorm.yii2inspections.codeInsight;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.psi.elements.*;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TranslationsIndexer extends FileBasedIndexExtension<String, Void> {
    public static final ID<String, Void> identity  = ID.create("com.kalessil.phpStorm.yii2inspections.translations");
    private final KeyDescriptor<String> descriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return identity;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return new DataIndexer<String, Void, FileContent>() {
            @NotNull
            @Override
            public Map<String, Void> map(@NotNull FileContent fileContent) {
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
                        final String message = ((StringLiteralExpression) key).getContents();
                        map.putIfAbsent(message, null);
                    }
                }

                return map;
            }
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
            final String fileName = file.getName();
            final String filePath = file.getCanonicalPath();
            if (null == filePath || !fileName.endsWith(".php") || fileName.equals("config.php") ) {
                return false;
            }


            return filePath.matches(".*/(translations|messages)/([a-zA-z]{2}(_[a-zA-z]{2})?)/[^/]+\\.php$");
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
