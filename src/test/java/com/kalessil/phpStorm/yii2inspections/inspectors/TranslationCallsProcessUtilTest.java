package com.kalessil.phpStorm.yii2inspections.inspectors;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.kalessil.phpStorm.yii2inspections.utils.TranslationCallsProcessUtil;

import java.util.HashMap;
import java.util.Map;

public class TranslationCallsProcessUtilTest extends PhpCodeInsightFixtureTestCase {
    public void testSingleMessageExtraction() {
        Project project = myFixture.getProject();

        Map<String, Integer> patterns = new HashMap<>();
        patterns.put("Yii::t('yii', \"$x\")", 1);
        patterns.put("Yii::t('yii', 'message')", 1);
        patterns.put("Yii::t('yii', \"message\")", 1);
        patterns.put("$view->registerTranslations('craft', ['message', 'message']);", 2);

        for (String pattern : patterns.keySet()) {
            MethodReference call = PhpPsiElementFactory.createFromText(project, MethodReference.class, pattern);
            assertNotNull(pattern + ": incorrect pattern", call);

            TranslationCallsProcessUtil.ProcessingResult messages = TranslationCallsProcessUtil.process(call, false);
            assertNotNull(pattern + ": not processed correctly", messages);

            assertTrue(messages.getMessages().size() == patterns.get(pattern));
            messages.dispose();
        }

        patterns.clear();
        patterns.put("Yii::t(\"$x\", 'message')", -1);
        patterns.put("Yii::t('', 'message')", -1);
        patterns.put("Yii::t($x, 'message')", -1);
        patterns.put("Yii::t('yii', '')", -1);
        patterns.put("$view->registerTranslations(\"$x\", ['message']);", -1);
        patterns.put("$view->registerTranslations($x, ['message']);", -1);
        patterns.put("$view->registerTranslations('craft', []);", -1);
        patterns.put("$view->registerTranslations('craft', ['', '']);", -1);
        for (String pattern : patterns.keySet()) {
            MethodReference call = PhpPsiElementFactory.createFromText(project, MethodReference.class, pattern);
            assertNotNull(pattern + ": incorrect pattern", call);

            TranslationCallsProcessUtil.ProcessingResult messages = TranslationCallsProcessUtil.process(call, false);
            assertNull(pattern + ": should be processed with null result", messages);
        }
    }
}
