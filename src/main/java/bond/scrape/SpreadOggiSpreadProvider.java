package bond.scrape;

import com.microsoft.playwright.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class SpreadOggiSpreadProvider implements ISovereignSpreadProvider {

    @Override
    public String name() {
        return "SpreadOggi";
    }

    @Override
    public Map<String, Double> fetchSpreads() throws Exception {
        Map<String, Double> spreads = new LinkedHashMap<>();

        try (Playwright pw = Playwright.create()) {
            Browser browser = pw.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();
            page.navigate("https://spreadoggi.it/");
            page.waitForSelector("#GridView1");

            Object result = page.evaluate("""
                () => $('#GridView1').DataTable().rows().data().toArray()
                  .map(r => ({
                    country: $('<div>').html(r[0]).text().trim(),
                    spread: parseFloat(r[2].replace(',', '.'))
                  }))
            """);

            var list = (java.util.List<?>) result;
            for (Object o : list) {
                var m = (Map<?, ?>) o;
                String country = (String) m.get("country");
                double spread = ((Number) m.get("spread")).doubleValue();
                String normalized = CountryNormalizer.normalize(country);
                if (!normalized.isEmpty() && Double.isFinite(spread)) {
                    spreads.put(normalized, spread);
                }
            }
        }

        return spreads;
    }
}