package bond.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class BondProfile {
    String id;
    String label;
    String emoji;
    String description;
    Map<String, Object> filters;

    public BondProfile() {
    }
}
