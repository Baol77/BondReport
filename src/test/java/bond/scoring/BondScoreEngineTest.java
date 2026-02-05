package bond.scoring;

import org.junit.Test;

import static org.junit.Assert.*;

public class BondScoreEngineTest {

    // ---------------------------------------------------
    // 1. Credit quality must decay as spreads increase
    // ---------------------------------------------------
    @Test
    public void testCreditQualityDecay() {
        double low = BondScoreEngine.calculateCreditQualityFromSpread(50);
        double mid = BondScoreEngine.calculateCreditQualityFromSpread(200);
        double high = BondScoreEngine.calculateCreditQualityFromSpread(500);

        assertTrue(low > mid);
        assertTrue(mid > high);

        // Decay should accelerate in stressed zone (convexity)
        assertTrue((low - mid) < (mid - high));
    }

    // ---------------------------------------------------
    // 2. Logistic trust must collapse below midpoint
    // ---------------------------------------------------
    @Test
    public void testLogisticTrustCliff() {
        double above = BondScoreEngine.applyLogisticTrust(0.75);
        double mid = BondScoreEngine.applyLogisticTrust(0.55);
        double below = BondScoreEngine.applyLogisticTrust(0.35);

        assertTrue(above > mid);
        assertTrue(mid > below);

        // Cliff effect: drop below midpoint must be steep
        assertTrue((mid - below) > 0.25);
    }

    // ---------------------------------------------------
    // 3. FX penalty must increase when credit weakens
    // ---------------------------------------------------
    @Test
    public void testFxPenaltyAmplification() {
        double strongCredit = BondScoreEngine.calculateFxCreditCorrelation(0.9);
        double weakCredit = BondScoreEngine.calculateFxCreditCorrelation(0.4);

        assertTrue(weakCredit > strongCredit);
    }

    // ---------------------------------------------------
    // 4. FX penalty must increase with maturity
    // ---------------------------------------------------
    @Test
    public void testFxPenaltyIncreasesWithTime() {
        double shortTerm = BondScoreEngine.fxCapitalPenalty(
            "USD", "EUR", 2, 0.4, 0.3, 1.0, 1.0);

        double longTerm = BondScoreEngine.fxCapitalPenalty(
            "USD", "EUR", 10, 0.4, 0.3, 1.0, 1.0);

        assertTrue(longTerm > shortTerm);
    }

    // ---------------------------------------------------
    // 5. OPPORTUNISTIC must penalize credit less than INCOME
    // ---------------------------------------------------
    @Test
    public void testProfileDifferentiationOnFinalScore() {
        double baseScore = 0.7;
        double penalty = 0.2;
        double creditQuality = 0.5;

        double logistic = BondScoreEngine.applyLogisticTrust(creditQuality);

        double incomeFinal = Math.max(0, (baseScore - penalty) * Math.pow(logistic, 1.0));
        double oppsFinal   = Math.max(0, (baseScore - penalty) * Math.pow(logistic, 0.1));

        assertTrue(oppsFinal > incomeFinal);
    }
}
