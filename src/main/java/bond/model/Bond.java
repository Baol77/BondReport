package bond.model;

import java.time.LocalDate;

public record Bond(
    String isin,
    String issuer,
    double price,
    String currency,
    double priceEur,
    double couponPct,
    LocalDate maturity,
    double currentYieldPct,
    double totalYieldPctToMaturity
) {}