package bond.scrape;

import bond.calc.BondCalculator;
import bond.model.Bond;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.util.*;

/**
 * Scraper service to extract European bond data from SimpleToolsForInvestors.
 * It parses the HTML yield table and transforms raw rows into Bond objects.
 */
public class BondScraper {

    /**
     * Target URL for European Sovereign Bond monitor.
     */
    private static final String SOURCE =
        "https://www.simpletoolsforinvestors.eu/monitor_info.php?monitor=europa&yieldtype=G&timescale=DUR";

    private final BondCalculator calculator;

    public BondScraper(BondCalculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Connects to the source, parses the table, and filters bonds based on specific criteria.
     * * @param fx Map of exchange rates (Currency -> Rate) used for EUR conversion.
     *
     * @return A list of valid, non-zero coupon bonds.
     * @throws Exception If connection or parsing fails.
     */
    public List<Bond> scrape(Map<String, Double> fx) throws Exception {
        List<Bond> list = new ArrayList<>();

        // Fetch HTML document with browser-like headers to avoid blocking
        Document doc = Jsoup.connect(SOURCE)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            .referrer("https://google.com")
            .timeout(30_000)
            .get();

        Element table = doc.select("#YieldTable").first();
        if (table == null) return list;

        Elements rows = table.select("tr");
        // Extract headers to map column names to indices dynamically
        List<String> headers = rows.get(0).select("th").eachText();

        for (int i = 1; i < rows.size(); i++) {
            Elements td = rows.get(i).select("td");
            if (td.size() != headers.size()) continue;

            // Map cell values to their header names for easier access
            Map<String, String> r = new HashMap<>();
            for (int j = 0; j < headers.size(); j++) {
                r.put(headers.get(j), td.get(j).text());
            }

            try {
                String d = r.get("Descrizione");
                String isin = r.get("Codice ISIN");

                // Avoid buy constraints of high quantities
                int lottoMinimo = Integer.parseInt(r.get("Lotto minimo"));
                if (lottoMinimo > 5000) continue;

                // Clean Issuer: Extract text before the first digit (usually the date/coupon start)
                // Example: "ITALY 4.5% 2026" -> "ITALY"
                String issuer = d.split("\\d", 2)[0].trim();
                issuer = issuer.replace("BTP", "ITALY"); // Normalize Italian Bonds

                // Parse Coupon: Find the '%' and look back to find the numeric value
                int pct = d.indexOf('%');
                double coupon = Double.parseDouble(
                    d.substring(d.lastIndexOf(' ', pct) + 1, pct).replace(',', '.')
                );

                // Skip zero-coupon bonds as they follow different yield logic
                if (coupon == 0) continue;

                LocalDate maturity = LocalDate.parse(r.get("Data scadenza"));

                // Filter: Skip specific Nordic currencies if not relevant for current analysis
                String ccy = r.get("Divisa");
                if (ccy.equals("NOK") || ccy.equals("SEK")) continue;

                double price = parse(r.get("Prezzo di riferimento"));

                // Convert Price to EUR using the provided FX rates
                double priceEur = price / fx.getOrDefault(ccy, 1.0);

                // Build and add the bond using the calculator helper
                list.add(calculator.buildBond(
                    isin, issuer, price, ccy, priceEur, coupon, maturity
                ));
            } catch (Exception ignored) {
                // Ignore individual row failures to continue processing the rest of the table
            }
        }
        return list;
    }

    /**
     * Sanitizes numeric strings by replacing commas and removing currency symbols.
     * * @param s Raw string from the table (e.g., "102,45 €")
     *
     * @return Parsed double
     */
    private static double parse(String s) {
        return Double.parseDouble(s.replace(",", ".").replace("€", "").trim());
    }
}