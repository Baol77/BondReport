package bond.scoring;

import bond.model.Bond;
import bond.scrape.SovereignSpreadScraper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BondScoreEngine {

    public static final String INCOME = "INCOME";
    public static final String BALANCED = "BALANCED";
    public static final String GROWTH = "GROWTH";
    public static final String OPPORTUNISTIC = "OPPORTUNISTIC";

    public static final List<String> PROFILES = List.of(
        INCOME, BALANCED, GROWTH, OPPORTUNISTIC
    );

    private static final Map<String, ProfileParams> PROFILES_PARAMS = Map.of(
        INCOME, new ProfileParams(0.75, 1.3, 0.15, 1.0),
        BALANCED, new ProfileParams(0.55, 1.0, 0.30, 0.7),
        GROWTH, new ProfileParams(0.30, 0.7, 0.45, 0.4),
        OPPORTUNISTIC, new ProfileParams(0.20, 0.5, 0.60, 0.1)
    );

    private record ProfileParams(double alpha,
                                 double lambdaFactor,
                                 double capitalSensitivity,
                                 double riskAversion) {
    }

    // Logistic curve parameters (credit cliff protection)
    private static final double LOGISTIC_STEEPNESS = 10.0;
    private static final double LOGISTIC_MIDPOINT = 0.55;

    /* ========================================================= */

    public Map<String, Double> score(Bond bond,
                                     String reportCurrency,
                                     List<Double> marketCurrYields,
                                     List<Double> marketTotalYields,
                                     double lambdaBase,
                                     Map<String, Double> sovereignSpreads) {

        double currYield = reportCurrency.equals("CHF")
            ? bond.currentYieldPctChf()
            : bond.currentYieldPct();

        double totalYield = reportCurrency.equals("CHF")
            ? bond.totalYieldToMatChf()
            : bond.totalYieldToMat();

        double normC = MathLibrary.normWinsorized(currYield, marketCurrYields);
        double normT = MathLibrary.normWinsorized(totalYield, marketTotalYields);

        double capitalYield = Math.max(0, totalYield - currYield);
        double capitalWeight = totalYield > 0 ? capitalYield / totalYield : 0;

        // --- Credit quality from sovereign spread ---
        double spreadBps = SovereignSpreadScraper.getSpreadForIssuer(bond.issuer(), sovereignSpreads);
        double creditQuality = calculateCreditQualityFromSpread(spreadBps);

        Map<String, Double> scores = new LinkedHashMap<>();

        for (String profile : PROFILES) {
            ProfileParams p = PROFILES_PARAMS.get(profile);

            double baseScore = p.alpha * normC + (1 - p.alpha) * normT;
            double lambda = lambdaBase * p.lambdaFactor;

            // --- FX-credit wrong-way correlation ---
            double correlationFactor = calculateFxCreditCorrelation(creditQuality);

            double penalty = fxCapitalPenalty(
                bond.currency(),
                reportCurrency,
                yearsToMaturity(bond),
                capitalWeight,
                p.capitalSensitivity,
                lambda,
                correlationFactor
            );

            // --- Non-linear credit trust shaping ---
            double logisticQuality = applyLogisticTrust(creditQuality);
            double adjustedQuality = Math.pow(logisticQuality, p.riskAversion);

            double finalScore = Math.max(0, (baseScore - penalty) * adjustedQuality);
            scores.put(profile, finalScore);
        }

        return scores;
    }

    /* ========================================================= */
    /* CREDIT QUALITY */
    /* ========================================================= */

    /**
     * Converts sovereign spread (bps) into a smooth credit quality score ∈ [0.1, 0.95].
     * Convex decay: low spreads barely penalized, high spreads punished exponentially.
     */
    private static double calculateCreditQualityFromSpread(double spreadBps) {
        double x = Math.max(0, spreadBps);
        double quality = 0.95 * Math.exp(-x / 600.0);
        return Math.max(0.1, Math.min(0.95, quality));
    }

    /**
     * Logistic cliff protection against false bargains.
     */
    private static double applyLogisticTrust(double creditQuality) {
        return 1.0 / (1.0 + Math.exp(-LOGISTIC_STEEPNESS * (creditQuality - LOGISTIC_MIDPOINT)));
    }

    /**
     * FX-credit wrong-way risk amplification.
     * Lower credit quality → higher FX penalty.
     */
    private static double calculateFxCreditCorrelation(double creditQuality) {
        return 1.0 + Math.max(0, (1.0 - creditQuality) * 0.8);
    }

    /* ========================================================= */
    /* FX PENALTY */
    /* ========================================================= */

    private static double yearsToMaturity(Bond b) {
        double y = ChronoUnit.DAYS.between(LocalDate.now(), b.maturity()) / 365.25;
        return Math.max(0.1, y);
    }

    private static double fxCapitalPenalty(String bCurr,
                                           String rCurr,
                                           double years,
                                           double capitalWeight,
                                           double capitalSensitivity,
                                           double lambda,
                                           double correlationFactor) {
        if (bCurr.equals(rCurr)) return 0;

        double sigma = switch (bCurr + "_" + rCurr) {
            case "USD_EUR", "EUR_USD" -> 0.09;
            case "USD_CHF", "CHF_USD" -> 0.11;
            case "GBP_EUR" -> 0.10;
            case "SEK_EUR" -> 0.13;
            case "SEK_CHF" -> 0.15;
            case "EUR_CHF", "CHF_EUR" -> 0.07;
            default -> 0.12;
        };

        double riskSensitivity = 1 + (capitalWeight * capitalSensitivity);

        double basePenalty = lambda * (1 - Math.exp(-sigma * Math.sqrt(years) * riskSensitivity));
        return basePenalty * correlationFactor;
    }
}