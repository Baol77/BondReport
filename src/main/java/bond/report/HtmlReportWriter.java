package bond.report;

import bond.config.BondProfilesConfig;
import bond.model.Bond;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
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

    public void writeEur(List<Bond> bonds, String file) throws Exception {
        write(bonds, file, "EUR");
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
            .map(Bond::getCurrency)
            .distinct()
            .sorted()
            .toList();
        model.put("currencies", currencies);

        // Load profiles
        BondProfilesConfig cfg = BondProfilesConfig.load();
        model.put("presets", cfg.getProfiles());

        try (FileWriter w = new FileWriter(file)) {
            t.process(model, w);
        }
    }
}