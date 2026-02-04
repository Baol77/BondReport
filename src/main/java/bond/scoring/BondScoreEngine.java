package bond.scoring;

import bond.model.Bond;

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
        INCOME, new ProfileParams(0.75, 1.2, 0.15, 1.0),
        BALANCED, new ProfileParams(0.55, 1.0, 0.30, 0.7),
        GROWTH, new ProfileParams(0.30, 0.7, 0.45, 0.4),
        OPPORTUNISTIC, new ProfileParams(0.20, 0.5, 0.60, 0.1)
    );

    private static final Map<String, Double> ISSUER_TRUST = Map.ofEntries(
        Map.entry("GERMANIA", 1.00),
        Map.entry("FINLANDIA", 1.00),
        Map.entry("OLANDA", 1.00),
        Map.entry("AUSTRIA", 1.00),

        Map.entry("FRANCIA", 0.97),
        Map.entry("BELGIO", 0.97),
        Map.entry("IRLANDA", 0.97),

        Map.entry("SPAGNA", 0.92),
        Map.entry("PORTOGALLO", 0.92),
        Map.entry("SLOVENIA", 0.92),
        Map.entry("ESTONIA", 0.92),

        Map.entry("ITALY", 0.85),
        Map.entry("REPUBLIC OF ITALY", 0.85),
        Map.entry("ITALY GREEN BOND", 0.85),
        Map.entry("ITALY GREEN", 0.85),
        Map.entry("ITALY FUTURA", 0.85),
        Map.entry("ITALY VALORE", 0.85),
        Map.entry("ITALY PIU'", 0.85),
        Map.entry("ITALY ITALIA", 0.85),
        Map.entry("ITALYI", 0.85),

        Map.entry("POLONIA", 0.85),

        Map.entry("UNGHERIA", 0.82),
        Map.entry("UNGHERIA GREEN BOND", 0.82),
        Map.entry("LITUANIA", 0.82),

        Map.entry("ROMANIA", 0.78),
        Map.entry("ROMANIA GREEN BOND", 0.78),
        Map.entry("BULGARIA", 0.78),

        Map.entry("GRECIA", 0.72),
        Map.entry("REPUBBLICA GRECA", 0.72),

        Map.entry("CIPRO", 0.70),

        Map.entry("TURCHIA", 0.65),

        Map.entry("REGNO UNITO", 0.97)
    );

    private record ProfileParams(double alpha,
                                 double lambdaFactor,
                                 double capitalSensitivity,
                                 double riskAversion) {
    }

    /* ========================================================= */

    public Map<String, Double> score(Bond bond,
                                     String reportCurrency,
                                     double minCurr, double maxCurr,
                                     double minTot, double maxTot,
                                     double lambdaBase) {

        double currYield = reportCurrency.equals("CHF")
            ? bond.currentYieldPctChf()
            : bond.currentYieldPct();

        double totalYield = reportCurrency.equals("CHF")
            ? bond.totalYieldToMatChf()
            : bond.totalYieldToMat();

        double normC = norm(currYield, minCurr, maxCurr);
        double normT = norm(totalYield, minTot, maxTot);

        double capitalYield = Math.max(0, totalYield - currYield);
        double capitalWeight = totalYield > 0 ? capitalYield / totalYield : 0;

        String issuer = bond.issuer().toUpperCase();
        double baseTrust = ISSUER_TRUST.getOrDefault(issuer, 0.8);

        Map<String, Double> scores = new LinkedHashMap<>();

        for (String profile : PROFILES) {
            ProfileParams p = PROFILES_PARAMS.get(profile);

            double baseScore = p.alpha * normC + (1 - p.alpha) * normT;
            double lambda = lambdaBase * p.lambdaFactor;

            double penalty = fxCapitalPenalty(
                bond.currency(),
                reportCurrency,
                yearsToMaturity(bond),
                capitalWeight,
                p.capitalSensitivity,
                lambda
            );

            double adjustedTrust = 1 - ((1 - baseTrust) * p.riskAversion);
            double finalScore = Math.max(0, (baseScore - penalty) * adjustedTrust);

            scores.put(profile, finalScore);
        }

        return scores;
    }

    /* ---------------- helpers ---------------- */

    private static double yearsToMaturity(Bond b) {
        double y = ChronoUnit.DAYS.between(LocalDate.now(), b.maturity()) / 365.25;
        return Math.max(0.1, y); // évite sqrt(0) / maturité passée
    }

    private static double norm(double v, double min, double max) {
        if (max == min) return 1;
        return Math.max(0, Math.min(1, (v - min) / (max - min)));
    }

    private static double fxCapitalPenalty(String bCurr, String rCurr,
                                           double years,
                                           double capitalWeight,
                                           double capitalSensitivity,
                                           double lambda) {
        if (bCurr.equals(rCurr)) return 0;

        double sigma = switch (bCurr + "_" + rCurr) {
            case "USD_EUR", "EUR_USD" -> 0.09;
            case "USD_CHF", "CHF_USD" -> 0.11;
            case "GBP_EUR" -> 0.10;
            //case "GBP_CHF" -> 0.12;
            case "SEK_EUR" -> 0.13;
            case "SEK_CHF" -> 0.15;
            case "EUR_CHF", "CHF_EUR" -> 0.07;
            default -> 0.12;
        };

        double riskSensitivity = 1 + (capitalWeight * capitalSensitivity);
        return lambda * (1 - Math.exp(-sigma * Math.sqrt(years) * riskSensitivity));
    }
}
