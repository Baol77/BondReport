package bond.model;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class Bond {
    String isin;
    String issuer;
    double price;
    String currency;
    double priceEur;
    double couponPct;
    LocalDate maturity;
    double currentCoupon;
    double finalCapitalToMat;
    double cagr;

    public Bond(String isin, String issuer, double price, String currency,
                double priceEur, double couponPct,
                LocalDate maturity) {
        this.isin = isin;
        this.issuer = issuer;
        this.price = price;
        this.currency = currency;
        this.priceEur = priceEur;
        this.couponPct = couponPct;
        this.maturity = maturity;
        this.currentCoupon = couponPct * 100 / priceEur;
    }
}