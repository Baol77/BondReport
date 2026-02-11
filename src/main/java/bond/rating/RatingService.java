package bond.rating;

import bond.scrape.CountryNormalizer;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to map COUNTRIES to sovereign ratings.
 * <p>
 * ⚠️ IMPORTANT: The rating is based on the COUNTRY (issuer), NOT the currency!
 * <p>
 * Strategy:
 * 1. Normalize the country name using CountryNormalizer.normalize()
 * 2. Look up the normalized name in COUNTRY_TO_RATING
 * 3. If not found, return "BBB" by default
 * <p>
 * Example:
 * issuer = "ITALY" → normalize() → "ITALIA" → "BBB"
 * issuer = "ITALIA" → normalize() → "ITALIA" → "BBB"
 * issuer = "REPUBBLICA ITALIANA" → normalize() → "ITALIA" → "BBB"
 * issuer = "GERMANY" → normalize() → "GERMANIA" → "AAA"
 * issuer = "FRANCE" → normalize() → "FRANCIA" → "AA"
 * <p>
 * Sovereign ratings are sourced from providers such as Trading Economics,
 * Moody's, S&P, and Fitch (data updated as of February 2026).
 */
public class RatingService {

    private static final Map<String, String> COUNTRY_TO_RATING = new HashMap<>();

    static {
        // Mapping of normalized names to sovereign ratings
        // Keys: Use names returned by CountryNormalizer.normalize()

        // AAA
        COUNTRY_TO_RATING.put("GERMANIA", "AAA");      // Germany
        COUNTRY_TO_RATING.put("OLANDA", "AAA");        // Netherlands
        COUNTRY_TO_RATING.put("SVIZZERA", "AAA");      // Switzerland
        COUNTRY_TO_RATING.put("NORVEGIA", "AAA");      // Norway
        COUNTRY_TO_RATING.put("DANIMARCA", "AAA");     // Denmark
        COUNTRY_TO_RATING.put("LUSSEMBURGO", "AAA");   // Luxembourg

        // AA+
        COUNTRY_TO_RATING.put("USA", "AA+");           // United States
        COUNTRY_TO_RATING.put("AUSTRALIA", "AA+");
        COUNTRY_TO_RATING.put("CANADA", "AA+");
        COUNTRY_TO_RATING.put("AUSTRIA", "AA+");
        COUNTRY_TO_RATING.put("SVEZIA", "AA+");        // Sweden
        COUNTRY_TO_RATING.put("FINLANDIA", "AA+");     // Finland

        // AA
        COUNTRY_TO_RATING.put("FRANCIA", "AA");        // France
        COUNTRY_TO_RATING.put("REGNO UNITO", "AA");    // United Kingdom
        COUNTRY_TO_RATING.put("BELGIO", "AA");         // Belgium

        // A+
        COUNTRY_TO_RATING.put("GIAPPONE", "A+");       // Japan
        COUNTRY_TO_RATING.put("IRLANDA", "A+");        // Ireland

        // A
        COUNTRY_TO_RATING.put("SPAGNA", "A");          // Spain
        COUNTRY_TO_RATING.put("REPUBBLICA CECA", "AA"); // Czech Republic
        COUNTRY_TO_RATING.put("POLONIA", "A");         // Poland
        COUNTRY_TO_RATING.put("SLOVENIA", "A+");       // Slovenia
        COUNTRY_TO_RATING.put("SLOVACCHIA", "A");      // Slovakia
        COUNTRY_TO_RATING.put("CILE", "A");            // Chile
        COUNTRY_TO_RATING.put("LITUANIA", "A");        // Lithuania

        // A-
        COUNTRY_TO_RATING.put("CIPRO", "A-");          // Cyprus
        COUNTRY_TO_RATING.put("PORTOGALLO", "A-");     // Portugal
        COUNTRY_TO_RATING.put("LETTONIA", "A-");       // Latvia

        // AA-
        COUNTRY_TO_RATING.put("ESTONIA", "AA-");       // Estonia

        // BBB (Investment Grade - Minimum for profiles)
        COUNTRY_TO_RATING.put("ITALIA", "BBB");        // Italy ⭐
        COUNTRY_TO_RATING.put("UNGHERIA", "BBB");      // Hungary
        COUNTRY_TO_RATING.put("ROMANIA", "BBB");       // Romania
        COUNTRY_TO_RATING.put("BULGARIA", "BBB");      // Bulgaria
        COUNTRY_TO_RATING.put("MESSICO", "BBB");       // Mexico
        COUNTRY_TO_RATING.put("INDIA", "BBB-");        // India

        // BBB-
        COUNTRY_TO_RATING.put("CROAZIA", "BBB-");      // Croatia

        // BB+ (Speculative - Likely filtered)
        COUNTRY_TO_RATING.put("GRECIA", "BB+");        // Greece

        // BB
        COUNTRY_TO_RATING.put("SUDAFRICA", "BB");      // South Africa

        // B+
        COUNTRY_TO_RATING.put("TURCHIA", "B+");        // Turkey

        // B
        COUNTRY_TO_RATING.put("RUSSIA", "B");          // Russia (sanctions)

        // BB-
        COUNTRY_TO_RATING.put("BRASILE", "BB-");       // Brazil

        // CCC
        COUNTRY_TO_RATING.put("ARGENTINA", "CCC");     // Argentina
    }

    /**
     * Retrieves the sovereign rating for a given country/issuer.
     * <p>
     * Strategy:
     * 1. Normalize country name with CountryNormalizer
     * 2. Search in COUNTRY_TO_RATING HashMap
     * 3. If not found, return "BBB" as default
     *
     * @param issuer The name of the country/issuer (e.g. "ITALY", "ITALIA", "REPUBLIC OF ITALY")
     * @return The rating (e.g. "AAA", "AA", "BBB") or "BBB" if not found
     */
    public static String getRatingForIssuer(String issuer) {
        if (issuer == null || issuer.isEmpty()) {
            return "BBB";  // Default: investment grade
        }

        // STEP 1: Normalize country name using CountryNormalizer
        String normalizedCountry = CountryNormalizer.normalize(issuer);

        // If normalization fails (returns ""), log warning and return BBB
        if (normalizedCountry.isEmpty()) {
            System.out.println("⚠️ WARNING: Country name not recognized for issuer: " + issuer + " → defaulting to BBB");
            return "BBB";
        }

        // STEP 2: Search HashMap using the normalized name
        String rating = COUNTRY_TO_RATING.get(normalizedCountry);

        if (rating != null) {
            return rating;
        }

        // If normalization succeeded but no rating is mapped, log warning and return BBB
        System.out.println("⚠️ WARNING: Rating not found for country: " + normalizedCountry + " → defaulting to BBB");
        return "BBB";
    }

    /**
     * Verifies if a rating meets the minimum requirement.
     *
     * @param actualRating  The current rating (e.g. "AA")
     * @param minimumRating The minimum required rating (e.g. "BBB")
     * @return true if actualRating >= minimumRating in quality
     */
    public static boolean meetsRatingRequirement(String actualRating, String minimumRating) {
        return compareRatings(actualRating, minimumRating) >= 0;
    }

    /**
     * Compares two ratings.
     *
     * @param rating1 First rating
     * @param rating2 Second rating
     * @return > 0 if rating1 > rating2 in quality
     * = 0 if rating1 == rating2
     * < 0 if rating1 < rating2 in quality
     */
    public static int compareRatings(String rating1, String rating2) {
        int rank1 = getRatingRank(rating1);
        int rank2 = getRatingRank(rating2);
        return rank1 - rank2; // Positive result means rating1 is higher quality
    }

    /**
     * Maps ratings to a numerical rank for comparison.
     * Higher value = Higher quality (AAA is the highest).
     */
    private static int getRatingRank(String rating) {
        if (rating == null || rating.isEmpty()) {
            return 2; // Default to BBB rank
        }

        String r = rating.toUpperCase().trim();

        return switch (r) {
            case "AAA" -> 10;
            case "AA+" -> 9;
            case "AA" -> 8;
            case "AA-" -> 7;
            case "A+" -> 6;
            case "A" -> 5;
            case "A-" -> 4;
            case "BBB+" -> 3;
            case "BBB" -> 2;
            case "BBB-" -> 1;
            case "BB+" -> 0;
            case "BB" -> -1;
            case "BB-" -> -2;
            case "B+" -> -3;
            case "B" -> -4;
            case "B-" -> -5;
            case "CCC" -> -6;
            case "CC" -> -7;
            case "C" -> -8;
            case "D" -> -9;
            default -> 2; // Default to BBB rank
        };
    }
}