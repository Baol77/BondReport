package bond;

import bond.calc.BondCalculator;
import bond.fx.FxService;
import bond.model.Bond;
import bond.report.HtmlReportWriter;
import bond.scrape.BondScraper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BondApp {

    public static void main(String[] args) throws Exception {
        FxService fxService = new FxService();
        Map<String, Double> fx = fxService.loadFxRates();

        BondCalculator calculator = new BondCalculator();
        BondScraper scraper = new BondScraper(calculator);

        List<Bond> bonds = scraper.scrape(fx);
        bonds.removeIf(Objects::isNull);
        bonds.sort(Comparator.comparingDouble(Bond::currentYieldPct).reversed());

        new HtmlReportWriter().write(bonds);

        System.out.println("✅ Report generated: bond_ytm_report.html");
    }
}