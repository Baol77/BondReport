package bond.scoring;

import bond.fx.FxService;
import bond.model.Bond;

import java.util.List;

/**
 * Engine for calculating bond scores, CAGR, and final capital at maturity.
 * <p>
 * Conversion Logic:
 * 1. Initial Investment (e.g., 1000 EUR) -> Converted to Bond Currency (e.g., USD) at SPOT rate.
 * 2. Coupons and Redemption -> Calculated in Bond Currency.
 * 3. Conversion back to EUR -> Applying a FX "downside" multiplier (volatility) based on time to maturity.
 */
public class BondScoreEngine {

    private static final double INIT_INVESTMENT_EUR = 1000.0;

    enum FxPhase {
        BUY,
        COUPON,
        MATURITY
    }

    public void calculateBondScores(List<Bond> bonds, String reportCurrency) {
        for (Bond bond : bonds) {
            // Exchange rate when the bond is bought
            double fxInitial = fxExpectedMultiplier(bond.getCurrency(), reportCurrency, FxPhase.BUY);
            // Exchange rate when the coupon is obtained
            double fxCurrent = fxExpectedMultiplier(bond.getCurrency(), reportCurrency, FxPhase.COUPON);
            // Exchange rate when the bond expires
            double fxFuture = fxExpectedMultiplier(bond.getCurrency(), reportCurrency, FxPhase.MATURITY);

            // Calculate final capital
            double bondNbr = INIT_INVESTMENT_EUR / (fxInitial * bond.getPrice());
            double capitalFromBondNbrEUR = bondNbr * bond.getCouponPct() * bond.getYearsToMaturity() * fxCurrent;
            double capitalGainEUR = 100 * bondNbr * fxFuture;
            bond.setFinalCapitalToMat(capitalFromBondNbrEUR + capitalGainEUR);

            // Calculate
            double annualCouponDev = 100 * bond.getCouponPct() / bond.getPrice();
            double annualCouponEUR = annualCouponDev * fxCurrent;
            double finalCapitalEUR = (100 * fxFuture - bond.getPrice() * fxInitial) / bond.getYearsToMaturity();
            double simpleAnnualYield = 100 * (annualCouponEUR + finalCapitalEUR) / (bond.getPrice() * fxInitial);
            bond.setSimpleAnnualYield(simpleAnnualYield);
        }
    }

    /* ========================================================= */
    /* FX RISK LOGIC                                             */
    /* ========================================================= */
    private double fxExpectedMultiplier(String bondCurrency, String reportCurrency, FxPhase fxPhase) {
        if (bondCurrency.equalsIgnoreCase(reportCurrency)) return 1.0;

        double currentFx = FxService.getExchangeRate(reportCurrency, bondCurrency);

        return switch (fxPhase) {
            case BUY -> currentFx;
            case COUPON -> currentFx * 0.90; // loss 10%
            default -> currentFx * 0.80; // loss 20%
        };
    }

    /* ========================================================= */
    /* UTILITIES                                                 */
    /* ========================================================= */
}