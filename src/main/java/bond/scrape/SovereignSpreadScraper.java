package bond.scrape;

import bond.scoring.IssuerManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Scrapes sovereign bond spreads from external sources.
 * Uses Trading Economics as primary source, with hardcoded fallback for common countries.
 * <p>
 * Maps issuer names to countries using IssuerManager's keyword matching,
 * ensuring consistency with the main trust scoring system.
 */
public class SovereignSpreadScraper {

    /**
     * Fetches spreads indexed by country keywords (matching IssuerManager format).
     * Falls back to hardcoded values if scraping fails.
     *
     * @return Map of country keyword -> spread in basis points
     */
    public static Map<String, Double> fetchSpreads() {
        Map<String, Double> spreads = new HashMap<>();

        // Try to scrape from Trading Economics
        try {
            spreads = scrapeTradingEconomics();
            if (!spreads.isEmpty()) {
                System.out.println("✅ Spreads loaded from Trading Economics");
                return spreads;
            }
        } catch (Exception e) {
            System.err.println("⚠ Could not scrape Trading Economics: " + e.getMessage());
        }

        // Fallback: use hardcoded values
        System.out.println("Using fallback spreads (hardcoded)");
        //Iteration on RULES and for reach TrustRule we map all the keywords to the associated spread
        return IssuerManager.getTrustRules().stream()
            .flatMap(rule -> rule.keywords().stream()
                .map(k -> Map.entry(k.toUpperCase(), rule.spreadBps())))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (existing, replacement) -> existing // In case of duplicates we keep the first
            ));
    }

    /**
     * Retrieves the spread for a specific issuer.
     * First attempts to match the issuer name to a country using IssuerManager's logic,
     * then looks up the spread for that country.
     *
     * @param issuerName Name of the issuer (e.g., "ITALIA", "TURCHIA")
     * @param spreadsMap Map of country keywords to spreads
     * @return Spread in basis points, or 150 bps (default risky) if not found
     */
    public static double getSpreadForIssuer(String issuerName, Map<String, Double> spreadsMap) {
        if (issuerName == null || issuerName.isBlank()) {
            return 150.0;
        }

        String normalized = issuerName.toUpperCase().trim();

        // First, try direct lookup in spreads map
        if (spreadsMap.containsKey(normalized)) {
            return spreadsMap.get(normalized);
        }

        // Second, use IssuerManager's trust scoring to infer the country
        // Higher trust = lower spread (rough inverse relationship)
        double trust = IssuerManager.getTrustScore(issuerName);

        // Map trust score to approximate spread
        // This ensures consistency: if IssuerManager recognizes it, we can estimate its spread
        return trustToSpread(trust);
    }

    /**
     * Converts a trust score to an estimated spread.
     * Inverse relationship: high trust → low spread, low trust → high spread.
     *
     * @param trust Trust score from IssuerManager (0.65 to 1.00)
     * @return Estimated spread in basis points
     */
    private static double trustToSpread(double trust) {
        // Linear inverse mapping:
        // trust = 1.00 → spread = 0 bps
        // trust = 0.95 → spread = 50 bps
        // trust = 0.85 → spread = 150 bps
        // trust = 0.70 → spread = 300 bps
        // trust = 0.65 → spread = 450 bps

        double spread = (1.0 - trust) * 1000.0;
        return Math.max(0, Math.min(500.0, spread));
    }

    /**
     * Scrapes sovereign bond spreads from Trading Economics website.
     * Looks for table rows with country name and spread values.
     * Returns map keyed by country keywords matching IssuerManager format.
     */
    private static Map<String, Double> scrapeTradingEconomics() throws IOException {
        Map<String, Double> spreads = new HashMap<>();

        // Connect to Trading Economics bonds page
        Document doc = Jsoup.connect("https://tradingeconomics.com/bonds")
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .timeout(15000)
            .get();

        // Look for table rows
        Elements rows = doc.select("table tbody tr");

        if (rows.isEmpty()) {
            // Fallback selector if table structure is different
            rows = doc.select("tr");
        }

        for (Element row : rows) {
            Elements cells = row.select("td");
            if (cells.size() < 2) continue;

            String countryText = cells.get(0).text().trim();
            String spreadText = cells.get(1).text().trim();

            // Try to extract spread value (format: "123 bps" or "1.23%")
            double spread = parseSpreadValue(spreadText);

            if (spread >= 0 && !countryText.isEmpty()) {
                // Normalize country name to match COUNTRY_SPREADS keys
                String normalizedCountry = normalizeCountryName(countryText);
                if (!normalizedCountry.isEmpty()) {
                    spreads.put(normalizedCountry, spread);
                }
            }
        }

        return spreads;
    }

    /**
     * Parses a spread value from various formats.
     * Examples: "285 bps", "2.85%", "285", "2.85"
     *
     * @return spread in basis points, or -1 if parsing fails
     */
    private static double parseSpreadValue(String text) {
        try {
            String cleaned = text.replaceAll("[^0-9.,]", "");
            if (cleaned.isEmpty()) return -1;

            double value = Double.parseDouble(cleaned.replace(",", "."));

            // If value is small (< 50), assume it's in percent and convert to bps
            if (value < 50 && text.contains("%")) {
                value = value * 100;
            }

            return Math.max(0, value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Normalizes country names from external sources to match IssuerManager keywords.
     * Example: "Italy" → "ITALY", "Germany" → "DEUTSCHLAND" or "GERMANY"
     */
    private static String normalizeCountryName(String rawCountry) {
        String upper = rawCountry.toUpperCase().trim();

        // Map common names to IssuerManager keywords
        return switch (upper) {
            case "ITALY", "REPUBBLICA ITALIANA", "ITALIA" -> "ITALY";
            case "GERMANY", "DEUTSCHLAND", "DEUTSCHLAND BUNDESREPUBLIK" -> "GERMANY";
            case "FRANCE", "FRANCIA" -> "FRANCE";
            case "SPAIN", "SPAGNA" -> "SPAIN";
            case "PORTUGAL", "PORTOGALLO" -> "PORTUGAL";
            case "GREECE", "GRECIA", "REPUBBLICA GRECA" -> "GREECE";
            case "IRELAND", "IRLANDA" -> "IRELAND";
            case "NETHERLANDS", "OLANDA", "PAESI BASSI" -> "NETHERLANDS";
            case "BELGIUM", "BELGIO" -> "BELGIUM";
            case "AUSTRIA" -> "AUSTRIA";
            case "FINLAND", "FINLANDIA" -> "FINLAND";
            case "SWEDEN", "SVEZIA" -> "SWEDEN";
            case "NORWAY", "NORVEGIA" -> "NORWAY";
            case "UNITED KINGDOM", "UK", "GRAN BRETAGNA", "REGNO UNITO" -> "UNITED KINGDOM";
            case "ROMANIA", "RUMANIA" -> "ROMANIA";
            case "POLAND", "POLONIA" -> "POLAND";
            case "HUNGARY", "UNGHERIA" -> "HUNGARY";
            case "BULGARIA" -> "BULGARIA";
            case "CROATIA", "CROAZIA" -> "CROATIA";
            case "SLOVENIA" -> "SLOVENIA";
            case "ESTONIA" -> "ESTONIA";
            case "LATVIA", "LETTONIA" -> "LATVIA";
            case "LITHUANIA", "LITUANIA" -> "LITHUANIA";
            case "CYPRUS", "CIPRO" -> "CYPRUS";
            case "TURKEY", "TURCHIA", "TURCHIA REPUBBLICA" -> "TURKEY";
            case "CZECH", "CZECHIA", "CECHIA" -> "";  // Not in IssuerManager
            default -> "";
        };
    }
}