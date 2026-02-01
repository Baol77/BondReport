package bond.report;

import bond.model.Bond;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlReportWriter {

    private final Configuration cfg;

    public HtmlReportWriter() {
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassLoaderForTemplateLoading(
            getClass().getClassLoader(), "/");
        cfg.setDefaultEncoding("UTF-8");
    }

    public void write(List<Bond> bonds) throws Exception {
        Template template = cfg.getTemplate("bond-report.ftl");

        Path out = Path.of("docs/index.html");
        Files.createDirectories(out.getParent());

        Map<String, Object> model = new HashMap<>();
        model.put("bonds", bonds);
        model.put("currencies", bonds.stream()
            .map(Bond::currency)
            .distinct()
            .sorted()
            .toList());

        try (Writer w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            template.process(model, w);
        }

        System.out.println("✅ Report written to " + out.toAbsolutePath());
    }
}