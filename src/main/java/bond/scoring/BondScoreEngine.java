package bond.scoring;

import bond.fx.FxService;
import bond.model.Bond;

import java.util.List;

/**
 * Engine for calculating bond performance scores.
 * Computes final capital and simple annual yield (SAL) considering
 * a degraded foreign exchange (FX) risk scenario.
 */
public class BondScoreEngine {

    /** Theoretical amount invested to normalize final capital calculations. */
    private static final double INIT_INVESTMENT_EUR = 1000.0;

    /** Investment phases differently impacted by currency volatility. */
    enum FxPhase {
        BUY,      // Time of purchase (Current SPOT rate)
        COUPON,   // Interest reception (Medium term)
        MATURITY  // Capital repayment (Long term)
    }

    /**
     * Calculates and updates scores (Final Capital and SAL) for each bond.
     *
     * @param bonds          The list of bonds to process.
     * @param reportCurrency The investor's reference currency (e.g., EUR).
     */
    public void calculateBondScores(List<Bond> bonds, String reportCurrency) {
        for (Bond bond : bonds) {
            // --- 1. Determine FX rates with safety margins ---
            double fxInitial = fxExpectedMultiplier(bond.getCurrency(), reportCurrency, FxPhase.BUY);
            double fxCurrent = fxExpectedMultiplier(bond.getCurrency(), reportCurrency, FxPhase.COUPON);
            double fxFuture = fxExpectedMultiplier(bond.getCurrency(), reportCurrency, FxPhase.MATURITY);

            // --- 2. Calculate projected final capital ---
            // Calculate how many securities are bought with 1000€ (Price * Initial Exchange Rate)
            double bondNbr = INIT_INVESTMENT_EUR / (fxInitial * bond.getPrice());

            // Cumulative coupon income converted to EUR with a moderate FX penalty
            double capitalFromBondNbrEUR = bondNbr * bond.getCouponPct() * bond.getYearsToMaturity() * fxCurrent;

            // Redemption value (assuming 100 par) converted to EUR with a strong FX penalty
            double capitalGainEUR = 100 * bondNbr * fxFuture;

            bond.setFinalCapitalToMat(capitalFromBondNbrEUR + capitalGainEUR);

            // --- 3. Calculate Simple Annual Yield (SAL %) ---
            // Coupon yield relative to purchase price
            double annualCouponDev = 100 * bond.getCouponPct() / bond.getPrice();
            double annualCouponEUR = annualCouponDev * fxCurrent;

            // Capital performance (gain or loss) linearized per year, adjusted for FX risk
            double finalCapitalEUR = (100 * fxFuture - bond.getPrice() * fxInitial) / bond.getYearsToMaturity();

            // Total annual yield calculation relative to initial entry cost
            double simpleAnnualYield = 100 * (annualCouponEUR + finalCapitalEUR) / (bond.getPrice() * fxInitial);

            bond.setSimpleAnnualYield(simpleAnnualYield);
        }
    }

    /**
     * Applies a "Stress Test" logic to exchange rates.
     * The further the horizon (Maturity), the stronger the applied penalty
     * to simulate a depreciation of the bond's currency.
     *
     * @param bondCurrency   Issuer's currency.
     * @param reportCurrency Investor's currency.
     * @param fxPhase        Target investment phase.
     * @return The exchange rate adjusted by a risk coefficient.
     */
    private double fxExpectedMultiplier(String bondCurrency, String reportCurrency, FxPhase fxPhase) {
        // No FX risk if currencies are identical
        if (bondCurrency.equalsIgnoreCase(reportCurrency)) return 1.0;

        double currentFx = FxService.getExchangeRate(reportCurrency, bondCurrency);

        return switch (fxPhase) {
            case BUY -> currentFx;           // Current rate for purchase
            case COUPON -> currentFx * 0.90; // Pessimistic scenario: 10% loss on coupons received
            default -> currentFx * 0.80;     // Pessimistic scenario: 20% loss on final capital
        };
    }
}