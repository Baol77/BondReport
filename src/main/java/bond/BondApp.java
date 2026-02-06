package bond;

import bond.calc.BondCalculator;
import bond.fx.FxService;
import bond.model.Bond;
import bond.report.BondReportRow;
import bond.report.HtmlReportWriter;
import bond.scoring.BondProfileManager;
import bond.scoring.IssuerManager;
import bond.scoring.MathLibrary;
import bond.scrape.BondScraper;
import bond.scoring.BondScoreEngine;
import bond.scrape.SovereignSpreadService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        // Load profiles
        BondProfileManager.load();

        // --- Load FX rates ---
        FxService fxService = new FxService();
        Map<String, Double> fx = fxService.loadFxRates();

        // --- Scrape bonds ---
        BondCalculator calculator = new BondCalculator();
        BondScraper scraper = new BondScraper(calculator);
        List<Bond> bonds = scraper.scrape(fx);
        bonds.removeIf(Objects::isNull);

        System.out.println("📊 Loaded " + bonds.size() + " bonds\n");

        // === NEW: Load sovereign spreads for dynamic trust ===
        System.out.println("📈 Loading sovereign spreads...");
        Map<String, Double> sovereignSpreads = SovereignSpreadService.fetchSpreads();
        System.out.println("✅ Spreads loaded: " + sovereignSpreads.size() + " countries\n");

        BondScoreEngine engine = new BondScoreEngine();

        // --- Generate reports ---
        List<BondReportRow> eurRows = buildRows(bonds, "EUR", engine, sovereignSpreads);
        List<BondReportRow> chfRows = buildRows(bonds, "CHF", engine, sovereignSpreads);

        HtmlReportWriter w = new HtmlReportWriter();
        w.writeEur(eurRows, "docs/eur/index.html");
        w.writeChf(chfRows, "docs/chf/index.html");

        // --- Handle unknown issuers alerting ---
        handleUnknownIssuers();

        System.out.println("\n✅ Reports generated:");
        System.out.println(" - docs/eur/index.html");
        System.out.println(" - docs/chf/index.html");
    }

    /* ========================================================= */

    /**
     * Handles detection and alerting of unknown issuers.
     */
    private static void handleUnknownIssuers() {
        try {
            Path alertPath = Paths.get("docs/alerts.txt");
            Set<String> unknowns = IssuerManager.getUnknownIssuers();

            if (!unknowns.isEmpty()) {
                List<String> lines = new ArrayList<>();
                lines.add("--- UNKNOWN ISSUERS REPORT ---");
                lines.add("Generated on: " + java.time.LocalDateTime.now());
                lines.add("");
                lines.addAll(unknowns);
                Files.write(alertPath, lines);

                System.out.println("\n⚠  " + unknowns.size() + " unknown issuers found. Check docs/alerts.txt");
            } else {
                Files.deleteIfExists(alertPath);
            }
        } catch (Exception e) {
            System.err.println("Could not manage alert file: " + e.getMessage());
        }
    }

    /* ========================================================= */

    /**
     * Builds report rows for a given reporting currency.
     *
     * @param bonds List of all bonds
     * @param reportCurrency EUR or CHF
     * @param engine Scoring engine
     * @param sovereignSpreads Map of country → spread (bps)
     * @return Sorted list of BondReportRows
     */
    private static List<BondReportRow> buildRows(List<Bond> bonds,
                                                 String reportCurrency,
                                                 BondScoreEngine engine,
                                                 Map<String, Double> sovereignSpreads) {

        // 1. Collect market yield distributions
        List<Double> marketCurr = bonds.stream()
            .map(b -> reportCurrency.equals("CHF")
                ? b.currentYieldPctChf()
                : b.currentYieldPct())
            .toList();

        List<Double> marketTot = bonds.stream()
            .map(b -> reportCurrency.equals("CHF")
                ? b.totalYieldToMatChf()
                : b.totalYieldToMat())
            .toList();

        // 2. Calculate lambdaBase from BALANCED profile distribution (60th percentile)
        List<Double> baseScores = bonds.stream()
            .map(b -> {
                double c = reportCurrency.equals("CHF")
                    ? b.currentYieldPctChf()
                    : b.currentYieldPct();
                double t = reportCurrency.equals("CHF")
                    ? b.totalYieldToMatChf()
                    : b.totalYieldToMat();

                double normC = MathLibrary.normWinsorized(c, marketCurr);
                double normT = MathLibrary.normWinsorized(t, marketTot);

                return 0.55 * normC + 0.45 * normT;
            })
            .sorted()
            .toList();

        double lambdaBase = calculateLambdaBase(baseScores);

        System.out.println("  - " + reportCurrency + " lambdaBase: " + String.format("%.4f", lambdaBase));

        // 3. Build rows with scores
        return bonds.stream()
            .map(b -> new BondReportRow(
                b,
                engine.score(b, reportCurrency, marketCurr, marketTot, lambdaBase, sovereignSpreads)
            ))
            .sorted((a, b) ->
                Double.compare(
                    b.scores().get(BondProfileManager.BALANCED),
                    a.scores().get(BondProfileManager.BALANCED)
                )
            )
            .toList();
    }

    /**
     * Calculates lambdaBase as the 60th percentile of balanced base scores.
     */
    private static double calculateLambdaBase(List<Double> sortedScores) {
        if (sortedScores.isEmpty()) {
            return 0.5;
        }
        int idx = (int) Math.floor(0.60 * (sortedScores.size() - 1));
        return sortedScores.get(idx);
    }
}