package com.kalessil.phpStorm.yii2inspections.inspectors;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

final public class MissingPropertyAnnotationsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatternsComplimentaryApproach() {
        MissingPropertyAnnotationsInspector inspector = new MissingPropertyAnnotationsInspector();
        inspector.REQUIRE_BOTH_GETTER_SETTER          = true;
        myFixture.configureByFile("testData/fixtures/property-tags-complimentary.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/property-tags-complimentary.fixed.php");
    }
    public void testIfFindsAllPatternsPartialApproach() {
        myFixture.configureByFile("testData/fixtures/property-tags-partial.php");
        myFixture.enableInspections(new MissingPropertyAnnotationsInspector());
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/property-tags-partial.fixed.php");
    }
}