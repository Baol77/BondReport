package bond.scoring;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Investor profile defining yield preference, FX risk tolerance,
 * capital sensitivity, and credit risk aversion.
 */
@Getter
@RequiredArgsConstructor
public enum BondProfile {

    INCOME("INCOME", 0.80, 1.50, 0.05, 1.00),
    BALANCED("BALANCED", 0.55, 0.80, 0.35, 0.65),
    GROWTH("GROWTH", 0.30, 0.40, 0.55, 0.30),
    OPPORTUNISTIC("OPPORTUNISTIC", 0.20, 0.20, 0.75, 0.05);

    private final String label;
    private final double alpha;
    private final double lambdaFactor;
    private final double capitalSensitivity;
    private final double riskAversion;

    public static List<BondProfile> ordered() {
        return List.of(INCOME, BALANCED, GROWTH, OPPORTUNISTIC);
    }

    public static BondProfile fromLabel(String label) {
        return valueOf(label.toUpperCase());
    }
}
