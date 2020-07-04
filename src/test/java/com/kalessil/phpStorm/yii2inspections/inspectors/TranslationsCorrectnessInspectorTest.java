package com.kalessil.phpStorm.yii2inspections.inspectors;

final public class TranslationsCorrectnessInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.allowTreeAccessForAllFiles();
        myFixture.configureByFile("testData/fixtures/translations/en-US/app.php");
        myFixture.configureByFile("testData/fixtures/translation-correctness.php");
        myFixture.enableInspections(new TranslationsCorrectnessInspector());
        myFixture.testHighlighting(true, false, true);
    }
}
