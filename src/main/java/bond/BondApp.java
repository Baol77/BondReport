package bond;

import bond.calc.BondCalculator;
import bond.fx.FxService;
import bond.model.Bond;
import bond.report.HtmlReportWriter;
import bond.scrape.BondScraper;
import bond.scoring.BondScoreEngine;

import java.util.*;

/**
 * Main application class.
 * Enhancements:
 * - Loads sovereign spreads for dynamic trust calculation
 * - Generates scoring reports in EUR and CHF
 * - Tracks unknown issuers and generates alerts
 */
public class BondApp {

    public static void main(String[] args) throws Exception {
        System.out.println("🚀 Starting Sovereign Bond Analytics...\n");

        // --- Load FX rates ---
        FxService fxService = FxService.getInstance();
        Map<String, Double> fx = fxService.loadFxRates();

        // --- Scrape bonds ---
        BondCalculator calculator = new BondCalculator();
        BondScraper scraper = new BondScraper(calculator);
        List<Bond> bonds = scraper.scrape(fx);
        bonds.removeIf(Objects::isNull);

        System.out.println("📊 Loaded " + bonds.size() + " bonds\n");

        BondScoreEngine engine = new BondScoreEngine();
        engine.estimateFinalCapitalAtMaturity(bonds, "EUR");

        HtmlReportWriter w = new HtmlReportWriter();
        w.writeEur(bonds, "docs/eur/index.html");

        System.out.println("\n✅ Reports generated:");
        System.out.println(" - docs/eur/index.html");
    }
}