package bond.report;

import bond.model.Bond;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.FileWriter;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlReportWriter {

    private final Configuration cfg;

    public HtmlReportWriter() {
        cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassForTemplateLoading(getClass(), "/");
        cfg.setDefaultEncoding("UTF-8");
    }

    public void writeEur(List<BondReportRow> rows, String file) throws Exception {
        write(rows, file, "EUR");
    }

    public void writeChf(List<BondReportRow> rows, String file) throws Exception {
        write(rows, file, "CHF");
    }

    private void write(List<BondReportRow> rows, String file, String reportCurrency) throws Exception {
        Template t = cfg.getTemplate("bond-report.ftl");

        Map<String, Object> model = new HashMap<>();
        model.put("rows", rows);
        model.put("reportCurrency", reportCurrency);
        model.put("generatedAt",
            java.time.LocalDateTime.now(ZoneId.of("Europe/Zurich"))
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        // distinct currencies for dropdown
        List<String> currencies = rows.stream()
            .map(r -> r.bond().currency())
            .distinct()
            .sorted()
            .toList();
        model.put("currencies", currencies);

        try (FileWriter w = new FileWriter(file)) {
            t.process(model, w);
        }
    }
}