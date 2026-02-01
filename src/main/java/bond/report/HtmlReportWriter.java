package bond.report;

import bond.model.Bond;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlReportWriter {

    private final Configuration cfg;

    public HtmlReportWriter() {
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "/");
        cfg.setDefaultEncoding("UTF-8");
    }

    public void write(List<Bond> bonds, String file) throws Exception {
        Template template = cfg.getTemplate("bond-report.ftl");

        Map<String, Object> model = new HashMap<>();
        model.put("bonds", bonds);

        try (Writer out = new OutputStreamWriter(
            new FileOutputStream(file), StandardCharsets.UTF_8)) {
            template.process(model, out);
        }
    }
}