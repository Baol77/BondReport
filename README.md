# Sovereign Bond Analytics Platform

A comprehensive Java application for analyzing and scoring sovereign bonds with FX risk assessment, generating detailed reports with downside scenarios.

## 🎯 Overview

This platform analyzes a portfolio of sovereign bonds across multiple currencies and generates scoring reports that account for:
- **FX Risk Modeling**: Currency volatility based on historical data (σ_annual × √T)
- **Downside Scenarios**: Worst-case analysis (95% CI lower bound)
- **Coupon Reinvestment**: Conservative model without reinvestment assumptions
- **Multi-Currency Support**: EUR, USD, GBP, CHF, SEK, and others

## 📊 Key Features

### 1. **Bond Data Scraping**
- Loads sovereign bond data from CSV files
- Extracts: ISIN, Issuer, Price, Currency, Coupon, Maturity
- Automatically normalizes country names and currency codes
- Handles multiple currency formats (e.g., "Currency All EUR GBP SEK USD")

### 2. **FX Risk Assessment**
Calculates currency risk using the **Geometric Brownian Motion model**:
```
σ_total = σ_annual × √T
Downside = Value / (1 + 1.96 × σ_total)
```

**Volatility Matrix (Annual):**

| Currency Pair | Volatility |
|---|---|
| EUR/CHF | 7% |
| EUR/USD | 9% |
| EUR/GBP | 8% |
| EUR/SEK | 13% |
| USD/CHF | 11% |
| USD/SEK | 14% |
| CHF/SEK | 15% |
| Other | 12% (default) |

### 3. **Final Capital Valuation**
Conservative model:
- **No reinvestment** of coupons (downside assumption)
- **Coupon calculation**: Coupon = (Annual Coupon % / 100) × Nominal × Years
- **Final value** = Coupons + Principal
- **Downside application**: Value × 1/(1 + 1.96×σ_total)

### 4. **HTML Reports**
Generates sortable, interactive reports with:
- ISIN, Issuer, Coupon, Maturity, Price
- Final Capital to Maturity (downside adjusted)
- Sortable columns (default: Final Capital descending)
- Color-coded maturity categories
- Summary statistics

## 🏗️ Architecture

```
bond/
├── BondApp.java              # Main entry point
├── model/
│   └── Bond.java            # Bond data model
├── scrape/
│   ├── BondScraper.java     # CSV parsing & loading
│   └── CountryNormalizer.java # Country name normalization
├── calc/
│   └── BondCalculator.java  # Coupon & price calculations
├── fx/
│   └── FxService.java       # FX rates (Yahoo Finance)
├── scoring/
│   └── BondScoreEngine.java # FX risk & downside calculation
└── report/
    └── HtmlReportWriter.java # HTML report generation
```

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven
- Internet connection (for FX rates)

### Running the Application
```bash
mvn clean package
java -cp target/bond-analytics.jar bond.BondApp
```

### Output
- Reports generated in `docs/eur/index.html`
- Bond data loaded from CSV source files

## 📈 Report Data

### Sample Report Insights (EUR Report)
**Portfolio Summary:**
- **Total Bonds**: 261
- **Currency Distribution**: Predominantly EUR (94%), some USD (6%)
- **Maturity Range**: 2031 - 2055
- **Average Coupon**: 2.5%

**Top Bonds by Final Capital (Downside):**
1. **FRANCIA (FR0014004J31)**: 2,812 EUR - 30-year ultra-long duration
2. **FRANCIA (FR0013480613)**: 2,708 EUR - Long-dated 0.75% coupon
3. **ROMANIA (XS2109813142)**: 2,667 EUR - Higher yield compensates FX risk

**Risk Observations:**
- EUR bonds: No FX downside applied (same currency)
- USD bonds: ~10-20% downside applied depending on maturity (10% annual vol)
- Longer maturities show higher FX risk (σ grows with √T)

## 🔍 FX Risk Methodology

### Model Assumptions
1. **Geometric Brownian Motion**: Currency follows GBM without drift (martingale)
2. **Volatility Scaling**: σ(T) = σ_annual × √(T years)
3. **95% Confidence Interval**: ±1.96σ bounds
4. **Cap at 35%**: Prevents unrealistic extreme scenarios for very long maturities

### Example: USD Bond with 10-Year Maturity
```
Volatility @ 10 years: 9% × √10 = 28.5%
Downside FX movement: -1.96 × 28.5% = -55.9%

Base case (Martingala): Value_base
Downside scenario: Value_base / (1 + 0.559) = 0.64 × Value_base
```

### Conservative Approach
- Uses **worst-case scenario (downside)** in final capital
- Does **not** reinvest coupons (assumes cash accumulation)
- Applied only for bonds in different currencies than report currency
- EUR-denominated bonds have 0% FX downside when reporting in EUR

## 📊 Data Quality Notes

### Verified Data Points
✅ All ISINs properly formatted
✅ 261 bonds across 20+ countries
✅ Coupon range: 0.10% - 6.75%
✅ Maturity range: 2031 - 2055
✅ Price range: 39.34 - 117.68

### Minor Data Observations
- Some issuers use "ITALYi" or "ITALY Plus" (data entry variants)
- Mostly EUR-denominated (246 bonds), with 15 USD bonds
- Concentration in France (30+), Italy (25+), and Spain (15+)
- Spread over investment-grade sovereigns in Eurozone + extended

## 🛠️ Configuration

### FX Volatility Adjustment
Edit `BondScoreEngine.getSigma()` to update historical volatility estimates:
```java
double sigma = switch (key) {
    case "EUR_USD" -> 0.09;  // Adjust based on new data
    // ...
};
```

### Report Currency
Change in `BondApp.java`:
```java
engine.estimateFinalCapitalAtMaturity(bonds, "CHF");  // Switch to CHF
w.writeEur(bonds, "docs/chf/index.html");
```

## 📈 Interpretation Guidelines

### Final Capital to Maturity (Downside Adjusted)
This represents the **worst-case scenario** for an investment of EUR 1,000:
- Includes full coupon accumulation over the bond's life
- Applies 95% confidence interval FX downside (only for foreign currency bonds)
- Conservative estimate (no coupon reinvestment)
- Useful for portfolio stress testing

### When to Use
✅ Portfolio risk assessment
✅ Conservative investor planning
✅ Scenario analysis (compare base vs downside)
✅ Currency exposure analysis

## 🔄 Calculation Logic

### Step-by-Step Example: USD Bond
**Input:**
- Investment: 1,000 EUR
- Price: 105.50 USD
- Coupon: 7.25%
- Maturity: 2038 (10 years from now)
- Currency: USD

**Processing:**
```
1. Convert EUR → USD: 1,000 × 1.18 (EUR/USD rate) = 1,180 USD
2. Calculate nominal: 1,180 / 1.0550 = 1,118.86 USD of par
3. Calculate coupons: 1,118.86 × 7.25% × 10 = 811.14 USD
4. Add principal: 811.14 + 1,118.86 = 1,930.00 USD
5. FX volatility @ 10yr: 9% × √10 = 28.5%
6. Downside multiplier: 1 / (1 + 1.96×0.285) = 0.638
7. Final value (downside): 1,930 × 0.638 = 1,231 EUR
```

## 🚨 Limitations & Caveats

1. **No Coupon Reinvestment**: Assumes coupons are held in cash (conservative)
2. **Static FX Volatility**: Uses historical averages, doesn't model changing vol
3. **No Credit Risk**: Assumes sovereigns always repay at par
4. **No Interest Rate Risk**: Bond prices assume held to maturity
5. **Single-Period Model**: Treats all FX moves as happening at maturity
6. **95% CI Cap**: Caps volatility at 35% to avoid unrealistic scenarios

For more sophisticated analysis:
- Add coupon reinvestment rates by term
- Include dynamic volatility models (GARCH)
- Model credit spreads and default probabilities
- Use full term structure of FX forward rates

## 📝 Report Navigation

**Column Headers:**
- **ISIN**: Bond identifier
- **Issuer**: Country or issuer name
- **Price**: Market price in original currency
- **Currency**: Denomination (EUR, USD, GBP, etc.)
- **Price (EUR)**: Converted to EUR at current rates
- **Coupon %**: Annual coupon rate
- **Maturity**: Redemption date
- **Current Coupon %**: Yield-to-price ratio
- **Tot. Capital to Maturity**: Final value with FX downside applied

**Sorting:**
- Default: Sorted by "Tot. Capital to Maturity" (descending)
- Click headers to re-sort
- Useful for identifying highest-return opportunities (downside)

## 🔗 Related Documentation

- **FX Risk Model**: See `BondScoreEngine.java` (lines 45-80)
- **CSV Parsing**: See `BondScraper.java`
- **HTML Generation**: See `HtmlReportWriter.java`
- **FX Rates**: See `FxService.java` (Yahoo Finance integration)

## 📞 Support

For questions or issues:
1. Check the `docs/` directory for generated reports
2. Review `BondScoreEngine` for FX calculation logic
3. Verify FX rates in `FxService` are up-to-date
4. Ensure input CSV has correct column headers

## 📄 License

See LICENSE file.

---

**Version**: 1.0  
**Last Updated**: 2025-02-08  
**Status**: Production Ready
