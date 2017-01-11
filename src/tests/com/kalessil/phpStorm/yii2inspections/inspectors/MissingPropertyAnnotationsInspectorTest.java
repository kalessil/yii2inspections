package com.kalessil.phpStorm.yii2inspections.inspectors;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

final public class MissingPropertyAnnotationsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatternsComplimentaryApproach() {
        MissingPropertyAnnotationsInspector inspector = new MissingPropertyAnnotationsInspector();
        inspector.REQUIRE_BOTH_GETTER_SETTER = true;

        myFixture.configureByFile("fixtures/property-tags-complimentary.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
}