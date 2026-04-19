package com.example.agentframework.bkt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BKTModelTest {

    @Test
    void testInitialParameters() {
        BKTModel model = new BKTModel(0.3, 0.1, 0.2, 0.1);
        assertEquals(0.3, model.getPL0(), 0.001);
        assertEquals(0.1, model.getPT(), 0.001);
        assertEquals(0.2, model.getPG(), 0.001);
        assertEquals(0.1, model.getPS(), 0.001);
        assertEquals(0.3, model.getPL(), 0.001);
    }

    @Test
    void testUpdateAfterCorrectAnswer() {
        BKTModel model = new BKTModel(0.3, 0.1, 0.2, 0.1);
        double updatedPL = model.update(true);
        assertTrue(updatedPL > 0.3, "PL should increase after correct answer");
        assertTrue(updatedPL <= 1.0, "PL should not exceed 1.0");
    }

    @Test
    void testUpdateAfterIncorrectAnswer() {
        BKTModel model = new BKTModel(0.8, 0.1, 0.2, 0.1);
        double updatedPL = model.update(false);
        assertTrue(updatedPL < 0.8, "PL should decrease after incorrect answer");
        assertTrue(updatedPL >= 0.0, "PL should not go below 0.0");
    }

    @Test
    void testConsecutiveCorrectAnswers() {
        BKTModel model = new BKTModel(0.2, 0.1, 0.2, 0.1);
        double pl = model.getPL();
        for (int i = 0; i < 10; i++) {
            pl = model.update(true);
        }
        assertTrue(pl > 0.9, "PL should approach 1.0 after many correct answers, got: " + pl);
    }

    @Test
    void testConsecutiveIncorrectAnswers() {
        BKTModel model = new BKTModel(0.5, 0.1, 0.2, 0.1);
        double pl = model.getPL();
        for (int i = 0; i < 10; i++) {
            pl = model.update(false);
        }
        assertTrue(pl < 0.3, "PL should decrease significantly after many incorrect answers, got: " + pl);
    }

    @Test
    void testPredictCorrect() {
        BKTModel model = new BKTModel(0.8, 0.1, 0.2, 0.1);
        double pCorrect = model.predict(true);
        assertTrue(pCorrect > 0.5, "Predicted correct probability should be high for high PL");
        assertTrue(pCorrect <= 1.0, "Predicted probability should not exceed 1.0");
    }

    @Test
    void testPredictIncorrect() {
        BKTModel model = new BKTModel(0.2, 0.1, 0.2, 0.1);
        double pIncorrect = model.predict(false);
        assertTrue(pIncorrect > 0.3, "Predicted incorrect probability should be high for low PL");
    }

    @Test
    void testGetMasteryLabel() {
        BKTModel mastered = new BKTModel(0.95, 0.1, 0.2, 0.1);
        mastered.update(true);
        assertEquals("精通", mastered.getMasteryLabel());

        BKTModel learning = new BKTModel(0.3, 0.1, 0.2, 0.1);
        String label = learning.getMasteryLabel();
        assertTrue(label.equals("学习中") || label.equals("初步了解") || label.equals("未掌握"));
    }

    @Test
    void testEstimateProblemsToMastery() {
        BKTModel model = new BKTModel(0.7, 0.1, 0.2, 0.1);
        int problems = model.estimateProblemsToMastery(0.95);
        assertTrue(problems > 0, "Should need some problems to reach mastery");
        assertTrue(problems < 100, "Should not need an unreasonable number of problems");
    }

    @Test
    void testCopy() {
        BKTModel original = new BKTModel(0.5, 0.15, 0.25, 0.12);
        original.update(true);
        BKTModel copy = original.copy();

        assertEquals(original.getPL(), copy.getPL(), 0.001);
        assertEquals(original.getPT(), copy.getPT(), 0.001);
        assertEquals(original.getPG(), copy.getPG(), 0.001);
        assertEquals(original.getPS(), copy.getPS(), 0.001);

        copy.update(false);
        assertNotEquals(original.getPL(), copy.getPL(), "Copy should be independent");
    }

    @Test
    void testBoundaryProtection() {
        BKTModel model = new BKTModel(0.99, 0.1, 0.01, 0.01);
        double pl = model.update(true);
        assertTrue(pl <= 1.0, "PL should not exceed 1.0");
        assertTrue(pl >= 0.0, "PL should not go below 0.0");
    }

    @Test
    void testZeroGuessAndSlip() {
        BKTModel model = new BKTModel(0.5, 0.1, 0.0, 0.0);
        double plAfterCorrect = model.update(true);
        assertTrue(plAfterCorrect > 0.5, "With zero guess/slip, correct answer should strongly increase PL");

        BKTModel model2 = new BKTModel(0.5, 0.1, 0.0, 0.0);
        double plAfterIncorrect = model2.update(false);
        assertTrue(plAfterIncorrect < 0.5, "With zero guess/slip, incorrect answer should strongly decrease PL");
    }
}