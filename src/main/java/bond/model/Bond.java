package bond.model;

import java.time.LocalDate;

public record Bond(
    String isin,
    String issuer,
    double price,
    String currency,
    double priceEur,
    double priceChf,
    double couponPct,
    LocalDate maturity,
    double currentYieldPct,
    double totalYieldToMat,
    double currentYieldPctChf,
    double totalYieldToMatChf
) {}