package bond.scoring;

import lombok.Data;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public final class BondProfileManager {

    public static final String BALANCED = "BALANCED";
    private static List<BondProfile> profiles = List.of();

    private BondProfileManager() {}

    public static void load() {
        if (!profiles.isEmpty()) return;

        InputStream in = BondProfileManager.class
            .getClassLoader()
            .getResourceAsStream("profiles.yaml");

        if (in == null) {
            throw new IllegalStateException("profiles.yaml not found in classpath");
        }

        LoaderOptions options = new LoaderOptions();
        Constructor ctor = new Constructor(ProfilesWrapper.class, options);
        Yaml yaml = new Yaml(ctor);

        ProfilesWrapper wrapper = yaml.load(in);
        profiles = Collections.unmodifiableList(wrapper.getProfiles());
    }

    public static List<BondProfile> all() {
        if (profiles.isEmpty()) load();
        return profiles;
    }

    /* ===================================================== */

    @Data
    public static class BondProfile {
        private String name;
        private double alpha;
        private double lambdaFactor;
        private double capitalSensitivity;
        private double riskAversion;
    }

    @Data
    public static class ProfilesWrapper {
        private List<BondProfile> profiles;
    }
}