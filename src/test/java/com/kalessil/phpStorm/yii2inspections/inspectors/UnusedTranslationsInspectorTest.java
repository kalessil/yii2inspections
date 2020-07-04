package com.kalessil.phpStorm.yii2inspections.inspectors;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

final public class UnusedTranslationsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.allowTreeAccessForAllFiles();
        myFixture.configureByFile("testData/fixtures/unused-translations.php");
        myFixture.configureByFile("testData/fixtures/unused-translations.html");
        myFixture.configureByFile("testData/fixtures/unused-translations.html.twig");
        myFixture.configureByFile("testData/fixtures/translations/en-US/unused.php");
        myFixture.enableInspections(new UnusedTranslationsInspector());
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile(
                "testData/fixtures/translations/en-US/unused.php",
                "testData/fixtures/translations/en-US/unused.fixed.php",
                false
        );
    }
}
