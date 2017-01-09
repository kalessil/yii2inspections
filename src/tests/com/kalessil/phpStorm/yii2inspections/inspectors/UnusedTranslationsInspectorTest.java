package com.kalessil.phpStorm.yii2inspections.inspectors;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

final public class UnusedTranslationsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.allowTreeAccessForAllFiles();
        myFixture.configureByFile("fixtures/unused-translations.php");
        myFixture.configureByFile("fixtures/translations/en-US/unused.php");
        myFixture.enableInspections(UnusedTranslationsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
