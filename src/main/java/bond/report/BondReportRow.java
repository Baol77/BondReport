package bond.report;

import bond.model.Bond;

import java.util.Map;

public record BondReportRow(
    Bond bond,
    Map<String, Double> scores   // INCOME → 0.742, BALANCED → 0.681, etc
) {}
