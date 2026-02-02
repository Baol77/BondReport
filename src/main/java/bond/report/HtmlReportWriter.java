package bond.report;

import bond.model.Bond;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.FileWriter;
import java.nio.file.Files;
import java.time.ZoneId;
import java.util.*;


public class HtmlReportWriter {

    private final Configuration cfg;

    public HtmlReportWriter() {
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassForTemplateLoading(getClass(), "/");
        cfg.setDefaultEncoding("UTF-8");
    }

    public void writeEur(List<Bond> bonds, String file) throws Exception {
        write(bonds, file, "EUR");
    }

    public void writeChf(List<Bond> bonds, String file) throws Exception {
        write(bonds, file, "CHF");
    }

    private void write(List<Bond> bonds, String file, String reportCurrency) throws Exception {
        Template t = cfg.getTemplate("bond-report.ftl");

        Map<String, Object> model = new HashMap<>();
        model.put("bonds", bonds);
        model.put("reportCurrency", reportCurrency);
        model.put("generatedAt",
            java.time.LocalDateTime.now(ZoneId.of("Europe/Zurich"))
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        // distinct currencies for dropdown
        List<String> currencies = bonds.stream()
            .map(Bond::currency)
            .distinct()
            .sorted()
            .toList();
        model.put("currencies", currencies);

        try (FileWriter w = new FileWriter(file)) {
            t.process(model, w);
        }
    }
}