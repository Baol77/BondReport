package bond.model;

import java.time.LocalDate;

public record Bond(
    String isin,
    String issuer,
    double priceEur,
    double couponPct,
    LocalDate maturity,
    String currency,
    double currentYieldPct,
    double totalYieldPctToMaturity
) {}