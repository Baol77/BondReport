package bond.scoring;

import lombok.Data;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.*;

public final class IssuerManager {

    private static double DEFAULT_TRUST;
    private static double NO_TRUST;
    private static List<TrustRule> RULES = List.of();

    private static final Set<String> UNKNOWN_ISSUERS =
        Collections.synchronizedSet(new TreeSet<>());

    private IssuerManager() {}

    /* ===================================================== */

    public static void load() {
        if (!RULES.isEmpty()) return;

        InputStream in = IssuerManager.class
            .getClassLoader()
            .getResourceAsStream("issuers.yaml");

        if (in == null) {
            throw new IllegalStateException("issuers.yaml not found in classpath");
        }

        LoaderOptions options = new LoaderOptions();
        Constructor ctor = new Constructor(IssuerConfig.class, options);
        Yaml yaml = new Yaml(ctor);

        IssuerConfig cfg = yaml.load(in);
        DEFAULT_TRUST = cfg.getDefaultTrust();
        NO_TRUST = cfg.getNoTrust();
        RULES = List.copyOf(cfg.getRules());
    }

    /* ===================================================== */

    public static List<TrustRule> getTrustRules() {
        if (RULES.isEmpty()) load();
        return RULES;
    }

    public static double getTrustScore(String issuerName) {
        if (issuerName == null || issuerName.isBlank()) {
            return NO_TRUST;
        }

        if (RULES.isEmpty()) load();

        String normalized = issuerName.toUpperCase();

        for (TrustRule rule : RULES) {
            for (String k : rule.getKeywords()) {
                if (normalized.contains(k)) {
                    return rule.getScore();
                }
            }
        }

        UNKNOWN_ISSUERS.add(issuerName);
        return DEFAULT_TRUST;
    }

    public static Set<String> getUnknownIssuers() {
        return UNKNOWN_ISSUERS;
    }

    /* ===================================================== */
    /* YAML binding classes */
    /* ===================================================== */

    @Data
    public static class TrustRule {
        private List<String> keywords;
        private double score;
        private double spreadBps;
    }

    @Data
    public static class IssuerConfig {
        private double defaultTrust;
        private double noTrust;
        private List<TrustRule> rules;
    }
}