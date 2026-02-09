package bond.calc;

import bond.model.Bond;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BondCalculator {

    public Bond buildBond(
        String isin,
        String issuer,
        double price,
        String currency,
        double priceEur,
        double couponPct,
        LocalDate maturity) {

        double years = Math.floor(ChronoUnit.DAYS.between(LocalDate.now(), maturity) / 365.25);
        if (years <= 1) return null;

        return new Bond(
            isin,
            issuer,
            price,
            currency,
            roundTo2Decimals(priceEur),
            couponPct,
            maturity
        );
    }

    private static double roundTo2Decimals(double v) {
        double m = Math.pow(10, 2);
        return Math.round(v * m) / m;
    }
}