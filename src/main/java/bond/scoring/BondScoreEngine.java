package bond.scoring;

import bond.fx.FxService;
import bond.model.Bond;
import lombok.SneakyThrows;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static bond.fx.FxService.getExchangeRate;


public class BondScoreEngine {

    private void expectedFinalValue(Bond bond, double investment, double maturityYears, double initialFxRate) {
        double capitalX = investment * initialFxRate;
        double nominal = capitalX / (bond.getPrice() / 100.0);

        // Cedole al rendimento TOTALE (include spread)
        double coupon = nominal * (bond.getCouponPct() / 100.0);
        double totalCoupons = coupon * maturityYears;

        double finalAmount = totalCoupons + nominal;

        bond.setFinalCapitalToMat(finalAmount);
    }

    public void estimateFinalCapitalAtMaturity(List<Bond> bonds, String reportCurrency) {
        for (Bond bond : bonds) {
            double maturityYears = yearsToMaturity(bond);
            double rate = getExchangeRate(bond.getCurrency(), reportCurrency);

            expectedFinalValue(bond, 1000, maturityYears, rate);

            // Apply downside value to bond
            applyFxDownside(bond, reportCurrency);
        }
    }

    /**
     * Evaluates a bond and modifies the parameters finalCapitalToMat and finalCapitalToMatChf
     * with the worst-case scenario (downside) based on FX risk.
     * <p>
     * LOGIC:
     * - If bond is in EUR and report is EUR: no FX risk, value remains unchanged (NULL)
     * - If bond is in different currency: applies downside scenario (95% CI lower bound)
     * - Downside = value / (1 + 1.96 * sigma_total)
     */
    private void applyFxDownside(Bond bond, String reportCurrency) {
        // FX parameters
        double yearsToMat = yearsToMaturity(bond);
        double fxSigmaAnnual = getSigma(bond.getCurrency(), reportCurrency);

        // If same currency, no FX risk
        if (fxSigmaAnnual == 0) {
            // Nothing to modify, values remain unchanged
            return;
        }

        // Calculate volatility at maturity
        double fxSigmaAtMaturity = fxSigmaAnnual * Math.sqrt(yearsToMat);
        double fxSigmaCapped = Math.min(fxSigmaAtMaturity, 0.35);

        // Downside scenario (95% CI lower)
        double downsideMultiplier = 1.0 / (1 + 1.96 * fxSigmaCapped);

        // Modify parameters with worst-case scenario
        double originalFinalCapital = bond.getFinalCapitalToMat();

        double finalValue = originalFinalCapital * downsideMultiplier;
        bond.setFinalCapitalToMat(finalValue);
    }

    /* ========================================================= */
    /* UTILITY METHODS */
    /* ========================================================= */

    /**
     * Calculates the years to maturity of a bond
     */
    private static double yearsToMaturity(Bond b) {
        double y = ChronoUnit.DAYS.between(LocalDate.now(), b.getMaturity()) / 365.25;
        return Math.max(0.1, y);
    }

    /**
     * Retrieves annual FX volatility for a currency pair.
     * Returns 0 if currencies are the same (no FX risk).
     */
    static double getSigma(String bondCurrency, String reportCurrency) {
        if (bondCurrency.equals(reportCurrency)) return 0;

        String key = normalizePair(bondCurrency, reportCurrency);

        double sigma = switch (key) {
            case "CHF_EUR" -> 0.07;
            case "CHF_GBP" -> 0.10;
            case "CHF_USD" -> 0.11;
            case "EUR_GBP" -> 0.08;
            case "EUR_USD" -> 0.09;
            case "GBP_USD" -> 0.10;
            case "EUR_SEK" -> 0.13;
            case "CHF_SEK" -> 0.15;
            case "USD_SEK" -> 0.14;
            default -> 0.12;
        };

        return sigma;
    }

    /**
     * Normalizes a currency pair (alphabetical order)
     */
    private static String normalizePair(String curr1, String curr2) {
        if (curr1.compareTo(curr2) < 0) {
            return curr1 + "_" + curr2;
        } else {
            return curr2 + "_" + curr1;
        }
    }
}