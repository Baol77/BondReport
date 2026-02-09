package bond.scoring;

import bond.model.Bond;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static bond.fx.FxService.getExchangeRate;


/**
 * Engine for calculating bond scores, CAGR, and final capital at maturity.
 * <p>
 * ═══════════════════════════════════════════════════════════════════════
 * CRITICAL CURRENCY CONVENTIONS (DO NOT CHANGE)
 * ═══════════════════════════════════════════════════════════════════════
 * <p>
 * 1. BOND PRICE CURRENCY
 *    ✓ bond.getPrice() is ALWAYS expressed in the bond's currency
 *    ✓ Example: EUR bond priced at 105 → 105 EUR
 *    ✓ Example: CHF bond priced at 105 → 105 CHF
 * <p>
 * 2. EXCHANGE RATE CONVENTION
 *    ✓ getExchangeRate(from, to) returns: 1 from_currency = X to_currency
 *    ✓ Example: getExchangeRate("EUR", "CHF") = 1.05 means 1 EUR = 1.05 CHF
 * <p>
 * 3. FINAL CALCULATIONS CURRENCY
 *    ✓ All results (FinalCapitalToMat) are expressed in reportCurrency
 *    ✓ Example: reportCurrency="EUR" → result in EUR
 * <p>
 * 4. FX VOLATILITY APPLICATION
 *    ✓ Sigma is used to adjust returns for currency uncertainty
 *    ✓ Applied as downside scenario (95% CI lower bound)
 *    ✓ Affects both FinalCapitalToMat and CAGR
 * <p>
 * ═══════════════════════════════════════════════════════════════════════
 */
public class BondScoreEngine {

    /**
     * Calculate the expected final value of a bond investment.
     *
     * @param bond              The bond to evaluate
     * @param maturityYears     Time to maturity in years
     * @param eurToBondCurrency Exchange rate (1 EUR = ? bond currency)
     * @return Final amount in EUR at maturity (before FX downside)
     */
    private double calculateExpectedFinalValue(Bond bond, double maturityYears, double eurToBondCurrency) {
        double referenceInvestment = 1000.0;

        // Step 1: Convert 1000 EUR to bond currency
        double capitalInBondCurrency = referenceInvestment * eurToBondCurrency;

        // Step 2: Calculate number of bonds we can buy
        // bond.getPrice() is in bond currency (e.g., 105 CHF)
        double numberOfBonds = capitalInBondCurrency / bond.getPrice();

        // Step 3: Calculate coupons and return of capital
        double annualCoupon = numberOfBonds * bond.getCouponPct();
        double totalCoupons = annualCoupon * maturityYears;
        double principalRepayment = numberOfBonds * 100.0;  // Par value is always 100

        // Step 4: Final amount in bond currency
        double finalAmountInBondCurrency = totalCoupons + principalRepayment;

        // Step 5: Convert back to EUR
        double bondCurrencyToEur = 1.0 / eurToBondCurrency;

        return finalAmountInBondCurrency * bondCurrencyToEur;
    }

    /**
     * Apply FX downside (worst-case scenario) to the final capital at maturity.
     * <p>
     * LOGIC:
     * - If bond is in same currency as report: sigma = 0, no downside
     * - If bond is in different currency: apply 95% CI lower bound downside
     * - Downside multiplier = 1 / (1 + 1.96 * sigma_at_maturity)
     *
     * @param originalCapital Amount in EUR (before FX downside)
     * @param yearsToMaturity Time horizon for volatility scaling
     * @param bondCurrency    Currency of the bond
     * @param reportCurrency  Reporting currency
     * @return Amount in EUR after FX downside
     */
    private double applyFxDownside(double originalCapital, double yearsToMaturity, String bondCurrency, String reportCurrency) {
        // Get annual FX volatility
        double sigmaAnnual = getSigma(bondCurrency, reportCurrency);

        // No FX risk if same currency
        if (sigmaAnnual == 0) {
            return originalCapital;  // No downside applied
        }

        // Scale volatility to maturity horizon
        double sigmaAtMaturity = sigmaAnnual * Math.sqrt(yearsToMaturity);
        double sigmaCapped = Math.min(sigmaAtMaturity, 0.35);

        // Calculate 95% CI lower bound
        double downsideMultiplier = 1.0 / (1.0 + 1.96 * sigmaCapped);

        return originalCapital * downsideMultiplier;
    }

    /**
     * Calculate CAGR (Compound Annual Growth Rate) including FX downside.
     * <p>
     * FORMULA:
     * CAGR = (currentYield + capitalGainCAGR) * fxDownsideMultiplier
     * <p>
     * Where:
     * - currentYield = coupon / price (annual income as % of invested capital)
     * - capitalGainCAGR = (100 / price)^(1/years) - 1 (return from price appreciation to par)
     * - fxDownsideMultiplier = 1 / (1 + 1.96 * sigma_at_maturity) (FX risk adjustment)
     *
     * @param bond             The bond to evaluate
     * @param yearsToMaturity  Time to maturity
     * @param reportCurrency   Reporting currency for FX risk assessment
     * @return CAGR in percentage (%)
     */
    public double calculateCAGR(Bond bond, double yearsToMaturity, String reportCurrency) {
        if (yearsToMaturity <= 0) {
            return 0;
        }

        double price = bond.getPrice();
        double coupon = bond.getCouponPct();

        // Component 1: Income from coupons
        double currentYield = coupon / price;

        // Component 2: Capital gain from par convergence
        double capitalGainCAGR = Math.pow(100.0 / price, 1.0 / yearsToMaturity) - 1.0;

        // Component 3: Total return before FX adjustment
        double totalCAGR = currentYield + capitalGainCAGR;

        // Component 4: Apply FX downside
        double sigmaAnnual = getSigma(bond.getCurrency(), reportCurrency);
        if (sigmaAnnual > 0) {
            double sigmaAtMaturity = sigmaAnnual * Math.sqrt(yearsToMaturity);
            double sigmaCapped = Math.min(sigmaAtMaturity, 0.35);
            double fxDownsideMultiplier = 1.0 / (1.0 + 1.96 * sigmaCapped);
            totalCAGR *= fxDownsideMultiplier;
        }

        return totalCAGR * 100.0;  // Convert to percentage
    }

    /**
     * Main entry point: Calculate final capital and CAGR for all bonds.
     *
     * @param bonds          List of bonds to evaluate
     * @param reportCurrency Currency for reporting (e.g., "EUR")
     */
    public void estimateFinalCapitalAtMaturity(List<Bond> bonds, String reportCurrency) {
        for (Bond bond : bonds) {
            double yearsToMaturity = yearsToMaturity(bond);

            // Get exchange rate: 1 EUR = ? bond_currency
            double eurToBondCurrency = getExchangeRate(bond.getCurrency(), reportCurrency);

            // STEP 1: Calculate expected final value (in report currency, before FX downside)
            double expectedCapital = calculateExpectedFinalValue(
                bond,
                // EUR investment
                yearsToMaturity,
                eurToBondCurrency
            );

            // STEP 2: Apply FX downside (95% CI lower bound)
            double finalCapitalWithDownside = applyFxDownside(
                expectedCapital,
                yearsToMaturity,
                bond.getCurrency(),
                reportCurrency
            );

            bond.setFinalCapitalToMat(finalCapitalWithDownside);

            // STEP 3: Calculate CAGR (uses internal downside calculation)
            double cagr = calculateCAGR(bond, yearsToMaturity, reportCurrency);
            bond.setCagr(cagr);
        }
    }

    /* ========================================================= */
    /* UTILITY METHODS */
    /* ========================================================= */

    /**
     * Calculate years from today to bond maturity.
     *
     * @param bond The bond
     * @return Years to maturity (minimum 0.1)
     */
    private static double yearsToMaturity(Bond bond) {
        double years = ChronoUnit.DAYS.between(LocalDate.now(), bond.getMaturity()) / 365.25;
        return Math.max(0.1, years);
    }

    /**
     * Get annual FX volatility for a currency pair.
     * <p>
     * CONVENTION: Returns volatility regardless of pair order
     *
     * @param currency1     First currency
     * @param currency2     Second currency
     * @return Annual volatility (0 if same currency, 0.07-0.15 for different pairs)
     */
    static double getSigma(String currency1, String currency2) {
        if (currency1.equals(currency2)) {
            return 0.0;  // No FX risk
        }

        String normalizedPair = normalizeCurrencyPair(currency1, currency2);

        return switch (normalizedPair) {
            case "CHF_EUR" -> 0.07;
            case "CHF_GBP", "GBP_USD" -> 0.10;
            case "CHF_USD" -> 0.11;
            case "EUR_GBP" -> 0.08;
            case "EUR_USD" -> 0.09;
            case "EUR_SEK" -> 0.13;
            case "CHF_SEK" -> 0.15;
            case "USD_SEK" -> 0.14;
            default -> 0.12;  // Default for unknown pairs
        };
    }

    /**
     * Normalize currency pair to alphabetical order for lookup.
     *
     * @param curr1 First currency
     * @param curr2 Second currency
     * @return Normalized pair (e.g., "CHF_EUR")
     */
    private static String normalizeCurrencyPair(String curr1, String curr2) {
        if (curr1.compareTo(curr2) < 0) {
            return curr1 + "_" + curr2;
        } else {
            return curr2 + "_" + curr1;
        }
    }
}