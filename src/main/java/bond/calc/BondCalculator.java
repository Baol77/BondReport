package bond.calc;

import bond.model.Bond;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BondCalculator {

    private static final double IT_TAX = 0.875;

    public Bond buildBond(String isin, String issuer,double price, double priceEur,  double priceChf,
                          double couponPct, LocalDate maturity, String currency) {

        double years = ChronoUnit.DAYS.between(LocalDate.now(), maturity) / 365.25;
        if (years <= 5) return null;

        double currentYield = 100 * couponPct / priceEur;
        double Yield1000ToM = 10 * currentYield * years; // 1000 EUR investment

        double currentYieldChf = 100 * couponPct / priceChf;
        double Yield1000ToMChf = 10 * currentYieldChf * years; // 1000 CHF investment

        return new Bond(
            isin,
            issuer,
            price,
            currency,
            round(priceEur, 2),
            round(priceChf, 2),
            couponPct,
            maturity,
            round(currentYield, 2),
            round(Yield1000ToM, 0),
            round(currentYieldChf, 2),
            round(Yield1000ToMChf, 0)
        );
    }

    private static double round(double v, int d) {
        double m = Math.pow(10, d);
        return Math.round(v * m) / m;
    }
}