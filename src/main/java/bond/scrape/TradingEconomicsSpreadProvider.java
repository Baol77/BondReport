package bond.scrape;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;

public class TradingEconomicsSpreadProvider implements ISovereignSpreadProvider {

    private static final String SOURCE_URL = "https://tradingeconomics.com/bonds";

    @Override
    public String name() {
        return "TradingEconomics";
    }

    @Override
    public Map<String, Double> fetchSpreads() throws Exception {
        Map<String, Double> spreads = new LinkedHashMap<>();

        Document doc = Jsoup.connect(SOURCE_URL)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            .timeout(15_000)
            .get();

        Elements rows = doc.selectXpath(
            "//table[.//th[contains(normalize-space(.), 'Europe')]]//tbody/tr"
        );

        if (rows.isEmpty()) return spreads;

        Double germanyYield = extractGermanyYield(rows);
        if (germanyYield == null) return spreads;

        for (Element row : rows) {
            String country = extractCountry(row);
            Double yield = extractYield(row);
            if (country == null || yield == null) continue;

            double spread = Math.max(0, yield - germanyYield) * 100;
            String normalized = CountryNormalizer.normalize(country);
            if (!normalized.isEmpty()) {
                spreads.put(normalized, spread);
            }
        }

        return spreads;
    }

    private static Double extractGermanyYield(Elements rows) {
        for (Element row : rows) {
            String country = extractCountry(row);
            if ("Germany".equalsIgnoreCase(country)) {
                return extractYield(row);
            }
        }
        return null;
    }

    private static String extractCountry(Element row) {
        Element cell = row.selectFirst(".datatable-item-first");
        return cell != null ? cell.text().trim() : null;
    }

    private static Double extractYield(Element row) {
        Element cell = row.selectFirst("td#p");
        if (cell == null) return null;
        double v = parseNumeric(cell.text());
        return Double.isFinite(v) && v > 0 ? v : null;
    }

    private static double parseNumeric(String text) {
        try {
            String cleaned = text.replaceAll("[^0-9.,]", "");
            return Double.parseDouble(cleaned.replace(",", "."));
        } catch (Exception e) {
            return Double.NaN;
        }
    }
}
