package com.kalessil.phpStorm.yii2inspections.inspectors;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

final public class MissingTranslationsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.allowTreeAccessForAllFiles();
        myFixture.configureByFile("testData/fixtures/missing-translations.php");
        myFixture.configureByFile("testData/fixtures/missing-translations.html");
        myFixture.configureByFile("testData/fixtures/missing-translations.html.twig");
        myFixture.configureByFile("testData/fixtures/translations/en-US/missing.php");
        myFixture.enableInspections(MissingTranslationsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
