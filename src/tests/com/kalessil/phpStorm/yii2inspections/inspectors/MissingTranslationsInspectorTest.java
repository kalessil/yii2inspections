package com.kalessil.phpStorm.yii2inspections.inspectors;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

final public class MissingTranslationsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/missing-translations.php");
        myFixture.enableInspections(MissingPropertyAnnotationsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
