package bond.calc;

import bond.model.Bond;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BondCalculator {

    private static final double IT_TAX = 0.875;

    private double calculateTotalAmountToMaturity(
        double couponPct,
        double price,
        double priceRef,
        double yearsToMaturity)
    {
        // Change rate
        double rate = price / priceRef;
        // How much I invest
        double investedAmount = rate * 1000;
        // How many bonds I buy for that price
        double nBonds = investedAmount / price;
        // Gain from bonds + capital gain (or loss if nominal value < 100)
        double YieldToMRef = nBonds * couponPct * yearsToMaturity + nBonds * 100;
        // Convert to refence currency
        return YieldToMRef / rate;
    }

    public Bond buildBond(String isin, String issuer, double price, double priceEur, double priceChf,
                          double couponPct, LocalDate maturity, String currency) {

        double years = Math.floor(ChronoUnit.DAYS.between(LocalDate.now(), maturity) / 365.25);
        if (years <= 1) return null;

        double currentYield = couponPct * 100 / priceEur;
        double YieldToM = calculateTotalAmountToMaturity(couponPct, price, priceEur, years);

        double currentYieldChf =  couponPct * 100 / priceChf;
        double YieldToMChf = YieldToM; // se il tasso di cambio rimane costante tra l'acquisto e la scadenza, l'effetto della valuta si annulla

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
            round(YieldToM, 0),
            round(currentYieldChf, 2),
            round(YieldToMChf, 0)
        );
    }

    private static double round(double v, int d) {
        double m = Math.pow(10, d);
        return Math.round(v * m) / m;
    }
}