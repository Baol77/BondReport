package bond.scrape;

import bond.scoring.IssuerManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Sovereign spread resolver with multi-source merge:
 * 1) SpreadOggi (primary)
 * 2) TradingEconomics (fill gaps)
 * 3) IssuerManager fallback (last resort)
 */
public class SovereignSpreadService {

    private static final double DEFAULT_FALLBACK_SPREAD = 180.0;

    private static final ISovereignSpreadProvider PRIMARY = new SpreadOggiSpreadProvider();
    private static final ISovereignSpreadProvider SECONDARY = new TradingEconomicsSpreadProvider();

    /* ========================================================= */
    /* PUBLIC API */
    /* ========================================================= */

    public static Map<String, Double> fetchSpreads() {

        Map<String, Double> spreads = new LinkedHashMap<>();

        // 1️⃣ Primary source (SpreadOggi)
        try {
            Map<String, Double> primary = PRIMARY.fetchSpreads();
            if (!primary.isEmpty()) {
                spreads.putAll(primary);
                System.out.println("✅ Loaded " + primary.size() + " spreads from " + PRIMARY.name());
            } else {
                System.err.println("⚠ " + PRIMARY.name() + " returned empty");
            }
        } catch (Exception e) {
            System.err.println("⚠ " + PRIMARY.name() + " failed: " + e.getMessage());
        }

        // 2️⃣ Secondary source (TradingEconomics) → fill gaps only
        try {
            Map<String, Double> secondary = SECONDARY.fetchSpreads();
            if (!secondary.isEmpty()) {
                int filled = 0;
                for (var e : secondary.entrySet()) {
                    if (!spreads.containsKey(e.getKey())) {
                        spreads.put(e.getKey(), e.getValue());
                        filled++;
                    }
                }
                System.out.println("➕ Filled " + filled + " spreads from " + SECONDARY.name());
            } else {
                System.err.println("⚠ " + SECONDARY.name() + " returned empty");
            }
        } catch (Exception e) {
            System.err.println("⚠ " + SECONDARY.name() + " failed: " + e.getMessage());
        }

        // 3️⃣ Total failure → IssuerManager fallback
        if (spreads.isEmpty()) {
            System.out.println("ℹ Using IssuerManager fallback spreads");
            return IssuerManager.getTrustRules().stream()
                .flatMap(rule -> rule.getKeywords().stream()
                    .map(k -> Map.entry(k.toUpperCase(), rule.getSpreadBps())))
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (a, b) -> a,
                    LinkedHashMap::new
                ));
        }

        return spreads;
    }

    /* ========================================================= */
    /* LOOKUP API */
    /* ========================================================= */

    public static double getSpreadForIssuer(String issuerName, Map<String, Double> spreadsMap) {
        if (issuerName == null || issuerName.isBlank()) {
            return DEFAULT_FALLBACK_SPREAD;
        }

        String normalized = CountryNormalizer.normalize(issuerName.toUpperCase().trim());

        // 1️⃣ Direct hit
        Double direct = spreadsMap.get(normalized);
        if (direct != null && Double.isFinite(direct)) {
            return direct;
        }

        // 2️⃣ Trust → synthetic spread
        double trust = IssuerManager.getTrustScore(issuerName);
        if (Double.isFinite(trust) && trust > 0) {
            return trustToSpread(trust);
        }

        // 3️⃣ Neutral fallback
        System.err.println("⚠ Missing sovereign spread mapping for issuer: " + issuerName);
        return DEFAULT_FALLBACK_SPREAD;
    }

    /* ========================================================= */
    /* FALLBACK LOGIC */
    /* ========================================================= */

    private static double trustToSpread(double trust) {
        trust = Math.max(0.6, Math.min(1.0, trust));
        double x = 1.0 - trust;
        double spread = 25 + 600 * x * x;
        return Math.max(20, Math.min(600, spread));
    }
}