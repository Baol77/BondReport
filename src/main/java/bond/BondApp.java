package bond;

import bond.calc.BondCalculator;
import bond.fx.FxService;
import bond.model.Bond;
import bond.report.BondReportRow;
import bond.report.HtmlReportWriter;
import bond.scoring.IssuerManager;
import bond.scrape.BondScraper;
import bond.scoring.BondScoreEngine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BondApp {

    public static void main(String[] args) throws Exception {
        FxService fxService = new FxService();
        Map<String, Double> fx = fxService.loadFxRates();

        BondCalculator calculator = new BondCalculator();
        BondScraper scraper = new BondScraper(calculator);

        List<Bond> bonds = scraper.scrape(fx);
        bonds.removeIf(Objects::isNull);

        BondScoreEngine engine = new BondScoreEngine();

        List<BondReportRow> eurRows = buildRows(bonds, "EUR", engine);
        List<BondReportRow> chfRows = buildRows(bonds, "CHF", engine);

        HtmlReportWriter w = new HtmlReportWriter();
        w.writeEur(eurRows, "docs/eur/index.html");
        w.writeChf(chfRows, "docs/chf/index.html");

        // --- NOUVELLE LOGIQUE D'ALERTE ---
        handleUnknownIssuers();

        System.out.println("✅ Reports generated:");
        System.out.println(" - docs/eur/index.html");
        System.out.println(" - docs/chf/index.html");
    }

    /* -------------------------------------------------------- */

    private static void handleUnknownIssuers() {
        try {
            Path alertPath = Paths.get("docs/alerts.txt");
            Set<String> unknowns = IssuerManager.getUnknownIssuers();

            if (!unknowns.isEmpty()) {
                // On écrit les émetteurs inconnus (un par ligne)
                List<String> lines = new ArrayList<>();
                lines.add("--- UNKNOWN ISSUERS REPORT ---");
                lines.add("Generated on: " + java.time.LocalDateTime.now());
                lines.add("");
                lines.addAll(unknowns); // Ajoute tous les noms du Set
                Files.write(alertPath, lines);

                System.out.println("⚠️ " + unknowns.size() + " unknown issuers found. Check docs/alerts.txt");
            } else {
                // Si tout est reconnu, on s'assure que l'ancien fichier est supprimé
                Files.deleteIfExists(alertPath);
            }
        } catch (Exception e) {
            System.err.println("❌ Could not manage alert file: " + e.getMessage());
        }
    }

    private static List<BondReportRow> buildRows(List<Bond> bonds,
                                                 String reportCurrency,
                                                 BondScoreEngine engine) {

        // 1. min / max sur la bonne devise
        double minCurr = bonds.stream()
            .mapToDouble(b -> reportCurrency.equals("CHF")
                ? b.currentYieldPctChf()
                : b.currentYieldPct())
            .min().orElse(0);

        double maxCurr = bonds.stream()
            .mapToDouble(b -> reportCurrency.equals("CHF")
                ? b.currentYieldPctChf()
                : b.currentYieldPct())
            .max().orElse(1);

        double minTot = bonds.stream()
            .mapToDouble(b -> reportCurrency.equals("CHF")
                ? b.totalYieldToMatChf()
                : b.totalYieldToMat())
            .min().orElse(0);

        double maxTot = bonds.stream()
            .mapToDouble(b -> reportCurrency.equals("CHF")
                ? b.totalYieldToMatChf()
                : b.totalYieldToMat())
            .max().orElse(1);

        // 2. lambda dynamique = 80% de la moyenne des baseScores BALANCED
        double lambdaBase = bonds.stream()
            .mapToDouble(b -> {
                double c = reportCurrency.equals("CHF")
                    ? b.currentYieldPctChf()
                    : b.currentYieldPct();
                double t = reportCurrency.equals("CHF")
                    ? b.totalYieldToMatChf()
                    : b.totalYieldToMat();
                double normC = (maxCurr == minCurr) ? 1 : (c - minCurr) / (maxCurr - minCurr);
                double normT = (maxTot == minTot) ? 1 : (t - minTot) / (maxTot - minTot);
                return 0.55 * normC + 0.45 * normT; // BALANCED brut
            })
            .average()
            .orElse(0.5) * 0.8;

        // 3. construction des rows
        return bonds.stream()
            .map(b -> new BondReportRow(
                b,
                engine.score(b, reportCurrency, minCurr, maxCurr, minTot, maxTot, lambdaBase)
            ))
            .sorted((a, b) ->
                Double.compare(
                    b.scores().get(BondScoreEngine.BALANCED),
                    a.scores().get(BondScoreEngine.BALANCED)
                )
            )
            .toList();
    }
}
