package bond.calc;

import bond.model.Bond;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BondCalculator {

    private static final double IT_TAX = 0.875;

    public Bond buildBond(String isin, String issuer, double price, String currency, double priceEur,
                          double couponPct, LocalDate maturity) {

        double years = Math.floor(ChronoUnit.DAYS.between(LocalDate.now(), maturity) / 365.25);
        if (years <= 1) return null;

        return new Bond(
            isin,
            issuer,
            price,
            currency,
            round(priceEur, 2),
            couponPct,
            maturity
        );
    }

    private static double round(double v, int d) {
        double m = Math.pow(10, d);
        return Math.round(v * m) / m;
    }
}