package bond.scrape;

import bond.calc.BondCalculator;
import bond.model.Bond;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.util.*;

public class BondScraper {

    private static final String SOURCE =
        "https://www.simpletoolsforinvestors.eu/monitor_info.php?monitor=europa&yieldtype=G&timescale=DUR";

    private final BondCalculator calculator;

    public BondScraper(BondCalculator calculator) {
        this.calculator = calculator;
    }

    public List<Bond> scrape(Map<String, Double> fx) throws Exception {
        List<Bond> list = new ArrayList<>();

        Document doc = Jsoup.connect(SOURCE)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            .referrer("https://google.com")
            .timeout(30_000)
            .get();

        Element table = doc.select("#YieldTable").first();
        Elements rows = table.select("tr");

        List<String> headers = rows.get(0).select("th").eachText();

        for (int i = 1; i < rows.size(); i++) {
            Elements td = rows.get(i).select("td");
            if (td.size() != headers.size()) continue;

            Map<String, String> r = new HashMap<>();
            for (int j = 0; j < headers.size(); j++) {
                r.put(headers.get(j), td.get(j).text());
            }

            try {
                String d = r.get("Descrizione");

                String isin = r.get("Codice ISIN");

                String issuer = d.split("\\d", 2)[0].trim(); // REPUBLIC OF ...01/01/1111
                issuer = issuer.replace("BTP", "ITALY");

                int pct = d.indexOf('%');

                double coupon = Double.parseDouble(
                    d.substring(d.lastIndexOf(' ', pct) + 1, pct).replace(',', '.')
                );
                if (coupon == 0) continue; // avoid 0% Bonds

                LocalDate maturity = LocalDate.parse(r.get("Data scadenza"));

                String ccy = r.get("Divisa");
                if (ccy.equals("NOK") || ccy.equals("SEK")) continue;

                double price = parse(r.get("Prezzo di riferimento"));

                double priceEur = price / fx.getOrDefault(ccy, 1.0);

                list.add(calculator.buildBond(
                    isin, issuer, price, ccy, priceEur, coupon, maturity
                ));
            } catch (Exception ignored) {
            }
        }
        return list;
    }

    private static double parse(String s) {
        return Double.parseDouble(s.replace(",", ".").replace("€", "").trim());
    }
}