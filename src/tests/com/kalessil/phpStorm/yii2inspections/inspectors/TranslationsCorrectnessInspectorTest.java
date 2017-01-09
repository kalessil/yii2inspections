package com.kalessil.phpStorm.yii2inspections.inspectors;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

final public class TranslationsCorrectnessInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/translation-correctness.php");
        myFixture.enableInspections(TranslationsCorrectnessInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
