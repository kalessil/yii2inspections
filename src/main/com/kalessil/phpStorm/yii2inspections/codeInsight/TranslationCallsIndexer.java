package com.kalessil.phpStorm.yii2inspections.codeInsight;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.util.PhpStringUtil;
import com.jetbrains.twig.TwigFileType;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This file is part of the Yii2 Inspections package.
 *
 * Author: Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class TranslationCallsIndexer extends FileBasedIndexExtension<String, Void> {
    public static final ID<String, Void> identity  = ID.create("com.kalessil.phpStorm.yii2inspections.translation_usages");
    private final KeyDescriptor<String> descriptor = new EnumeratorStringDescriptor();

    final static private Pattern regexTwigTranslateFilter;
    static {
        regexTwigTranslateFilter
            = Pattern.compile(".*\\W((\\'[^\\']+\\')|(\\\"[^\\\"]+\\\"))\\|(t|translate)(\\(((\\'[^\\']+\\')|(\\\"[^\\\"]+\\\")))?\\W.*");
    }

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

            final FileType fileType = fileContent.getFileType();
            if (PhpFileType.INSTANCE == fileType) {
                final Collection<MethodReference> calls = PsiTreeUtil.findChildrenOfType(fileContent.getPsiFile(), MethodReference.class);
                for (MethodReference call : calls) {
                    final PsiElement[] params = call.getParameters();
                    final String methodName   = call.getName();
                    if (null == methodName || params.length < 2 || !methodName.equals("t")) {
                        continue;
                    }

                    /* TODO: resolve params as string literals */

                    /* extract contained texts from message and category literals */
                    String category       = null;
                    String message        = null;
                    boolean isSingleQuote = true;
                    if (params[0] instanceof StringLiteralExpression) {
                        final StringLiteralExpression categoryLiteral = (StringLiteralExpression) params[0];
                        if (null == categoryLiteral.getFirstPsiChild()) {
                            category = categoryLiteral.getContents();
                        }
                    }
                    if (params[1] instanceof StringLiteralExpression) {
                        final StringLiteralExpression messageLiteral = (StringLiteralExpression) params[1];
                        if (null == messageLiteral.getFirstPsiChild()) {
                            message       = messageLiteral.getContents();
                            isSingleQuote = messageLiteral.isSingleQuote();
                        }
                    }

                    /* register usage */
                    if (null != category && null != message) {
                        map.putIfAbsent(category + "|" + PhpStringUtil.unescapeText(message, isSingleQuote), null);
                    }
                }
                calls.clear();
            }

            /* extract translations from twig-constructs */
            if (HtmlFileType.INSTANCE == fileType || TwigFileType.INSTANCE == fileType) {
                final Matcher regexMatcher = regexTwigTranslateFilter.matcher(fileContent.getPsiFile().getText());
                while (regexMatcher.find()) {
                    final String twigMessageExpression = regexMatcher.group(1);
                    final String twigGroupExpression   = null == regexMatcher.group(6) ? "'site'" : regexMatcher.group(6);
                    if (twigMessageExpression.length() > 2 && twigGroupExpression.length() > 2) {
                        final String message        = twigMessageExpression.substring(1, twigMessageExpression.length() - 1);
                        final String category       = twigGroupExpression.substring(1, twigGroupExpression.length() - 1);
                        final boolean isSingleQuote = '\'' == twigMessageExpression.charAt(0);
                        map.putIfAbsent(category + "|" + PhpStringUtil.unescapeText(message, isSingleQuote), null);
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
    public DataExternalizer<Void> getValueExternalizer() {
        return ScalarIndexExtension.VOID_DATA_EXTERNALIZER;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> {
            final FileType fileType = file.getFileType();
            if (PhpFileType.INSTANCE == fileType) {
                return !file.getPath().matches(".*/(translations|messages)/([a-zA-z]{2}(-[a-zA-z]{2})?)/[^/]+\\.php$");
            }

            return HtmlFileType.INSTANCE == fileType || TwigFileType.INSTANCE == fileType;
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
