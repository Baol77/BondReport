# Sovereign Bond Analytics Platform

A comprehensive Java application for analyzing and scoring sovereign bonds with **SAL (Simple Annual Yield)** rankings, credit ratings, preset investment profiles, FX risk assessment, and dual-mode reporting.

## 🎯 Overview

This platform analyzes sovereign bonds across multiple currencies and generates interactive reports featuring:
- **Dual-Mode Analysis**: Capital Gain (SAL-focused) or Income (Current Yield-focused)
- **4 Preset Investment Profiles**: Quick-start filters for different investor types
- **SAL Analysis**: Simple Annual Yield as primary metric for total returns
- **Current Yield Analysis**: Immediate income metric for income investors
- **Credit Ratings**: Sovereign credit ratings integrated into filtering
- **FX Risk Modeling**: Progressive downside scenarios (10% coupons, 20% maturity)
- **Multi-Currency Support**: EUR, USD, GBP, CHF, SEK, and others
- **Interactive Heatmaps**: Color-coded bands for SAL or Yield (mode-dependent)

## 📊 Quick Start: Investment Profiles

### 4 Built-In Profiles (One-Click Filtering)

Click a button to apply pre-configured filters optimized for each strategy:

#### 1. **📈🔥 SAL Aggressive** (Maximum Returns)
*Best for: Risk-tolerant investors seeking maximum total return*
- **Time Horizon**: 1-10 years (short to medium term)
- **Minimum SAL**: 5.0%+
- **Strategy**: High-return opportunities regardless of rating
- **Typical Results**: ~15-25 high-yield bonds

#### 2. **📈🛡️ SAL Conservative** (Quality + Returns)
*Best for: Balanced investors wanting solid returns with credit safety*
- **Time Horizon**: 1-20 years (short to long term)
- **Minimum SAL**: 4.0%+
- **Minimum Rating**: BBB+ (investment grade)
- **Strategy**: Quality bonds with good total returns
- **Typical Results**: ~50-80 investment-grade bonds

#### 3. **💵🔥 Income High** (Maximum Cash Flow)
*Best for: Income-focused investors needing high annual payments*
- **Time Horizon**: 20-40 years (long term)
- **Minimum Current Yield**: 5.0%+
- **Strategy**: Maximum immediate income from coupons
- **Typical Results**: ~10-20 high-income bonds

#### 4. **💵🌱 Income Moderate** (Balanced Income + Quality)
*Best for: Conservative income investors prioritizing safety*
- **Time Horizon**: 20-40 years (long term)
- **Minimum Current Yield**: 3.5%+
- **Minimum Rating**: BBB+ (investment grade)
- **Strategy**: Reliable income from quality issuers
- **Typical Results**: ~30-50 stable income bonds

---

## 🎨 Dual-Mode Report Interface

### Toggle Between Two Investment Approaches

**Capital Gain Mode (Default)**
```
Primary metric: SAL % (total annualized return)
Sort by: SAL descending (best total returns first)
Heatmap: SAL bands (red < 2% → bright green > 6%)
Presets: SAL Aggressive, SAL Conservative
Use when: Maximizing total returns (coupons + capital gains)
```

**Income Mode**
```
Primary metric: Current Yield % (annual coupon income)
Sort by: Current Yield descending (highest income first)
Heatmap: Yield bands (red < 3% → bright green > 6.5%)
Presets: Income High, Income Moderate
Use when: Need steady cash flow from coupon payments
```

**User Experience:**
- Toggle between "📈 Capital Gain" and "💵 Income" modes
- Preset buttons adapt to active mode
- Heatmap colors update dynamically
- All filters and sorting reconfigure instantly

---

## 🎯 Key Metrics Explained

### 1. **SAL (Simple Annual Yield)** - Total Return Metric

SAL measures your **total annualized return** including both coupon income and capital gains:

```
Formula: SAL = [(Annual Coupon Income + Annual Capital Gain) / Initial Investment] × 100

Components:
  Annual Coupon Income (EUR) = (Coupon % / Price) × 100 × FX_coupon
  Annual Capital Gain (EUR) = [(100 - Price × FX_initial) / Years] × FX_maturity
  Initial Investment = Price × FX_initial

Example:
  Bond Price: €95.00 (in EUR)
  Coupon: 5.0% annually
  Maturity: 15 years
  FX rates: Initial=1.0, Coupon=0.9, Maturity=0.8
  
  Annual Coupon Income = (5.0 / 95.00) × 100 × 0.9 = 4.74%
  Annual Capital Gain = [(100 - 95×1.0) / 15] × 0.8 = 0.27%
  SAL = (4.74 + 0.27) = 5.01%
```

**Key Characteristics:**
- **Linear approximation**: Simple average, not compounded (CAGR)
- **All-in metric**: Combines income + appreciation
- **FX-adjusted**: Accounts for currency risk
- **Annualized**: Comparable across different maturities

**Your Data Distribution:**
- Minimum SAL: ~0.5% (low-coupon foreign currency bonds)
- Maximum SAL: ~8.0% (high-coupon emerging market bonds)
- Average SAL: ~4.2%
- Median SAL: ~4.0%

### 2. **Current Yield** - Income Metric

Current Yield measures your **annual coupon income** as a percentage of purchase price:

```
Formula: Current Yield % = (Coupon % / Price) × 100

Example:
  Bond Price: €100
  Annual Coupon: 5.0%
  Current Yield: (5.0 / 100) × 100 = 5.0%
  Annual Income per €1,000: €50
```

**Key Characteristics:**
- **Income-only**: Ignores capital gains/losses
- **Immediate**: Shows current cash flow
- **Simple**: Easy to understand and compare
- **No FX adjustment**: Based on purchase price in original currency

**Your Data Distribution:**
- Minimum Yield: ~0.3% (zero-coupon or ultra-long bonds)
- Maximum Yield: ~8.0% (high-coupon premium bonds)
- Average Yield: ~3.5%
- Median Yield: ~3.2%

### 3. **Final Capital at Maturity**

Total value at maturity (in EUR) from a **€1,000 initial investment**:

```
Formula: Final Capital = Total Coupons (EUR) + Redemption Value (EUR)

Components:
  Number of Bonds = €1,000 / (Price × FX_initial)
  Total Coupons = Number of Bonds × Coupon % × Years × FX_coupon
  Redemption Value = Number of Bonds × 100 (par) × FX_maturity

Example:
  Investment: €1,000
  Bond Price: €95.00
  Coupon: 5.0% annually
  Years: 15
  FX: Initial=1.0, Coupon=0.9, Maturity=0.8
  
  Number of Bonds = 1,000 / (95 × 1.0) = 10.526
  Total Coupons = 10.526 × 5.0 × 15 × 0.9 = €710.53
  Redemption = 10.526 × 100 × 0.8 = €842.11
  Final Capital = €710.53 + €842.11 = €1,552.64
```

---

## 🎨 Color-Coded Heatmaps

### SAL Heatmap (Capital Gain Mode)

```
🔴 RED         (< 2.0%)   - Very poor return (avoid)
🟡 YELLOW      (2.0-3.5%) - Below average (consider alternatives)
🟢 GREEN       (3.5-5.0%) - Good return (market standard)
🟢🟢 DARK GREEN (5.0-6.5%) - Excellent return (strong value)
⭐ BRIGHT GREEN (> 6.5%)   - Outstanding (top performers)
```

### Current Yield Heatmap (Income Mode)

```
🔴 RED         (< 3.0%)   - Too low (worse than risk-free)
🟡 YELLOW      (3.0-4.5%) - Acceptable (moderate income)
🟢 GREEN       (4.5-5.5%) - Good (solid income stream)
🟢🟢 DARK GREEN (5.5-6.5%) - Excellent (high income)
⭐ BRIGHT GREEN (> 6.5%)   - Outstanding (premium income)
```

---

## 🔍 Calculation Methodology

### SAL Calculation Step-by-Step

```java
// From BondScoreEngine.calculateBondScores()

1. Get FX rates for three phases:
   fxInitial = fxExpectedMultiplier(currency, EUR, BUY)      // 1.00 × spot
   fxCoupon  = fxExpectedMultiplier(currency, EUR, COUPON)   // 0.90 × spot
   fxMaturity = fxExpectedMultiplier(currency, EUR, MATURITY) // 0.80 × spot

2. Calculate number of bonds from €1,000 investment:
   bondNbr = 1000 / (fxInitial × price)

3. Calculate total coupons in EUR over lifetime:
   capitalFromCoupons = bondNbr × couponPct × yearsToMaturity × fxCoupon

4. Calculate redemption value in EUR:
   capitalGain = 100 × bondNbr × fxMaturity

5. Set final capital:
   finalCapitalToMat = capitalFromCoupons + capitalGain

6. Calculate annual coupon yield in EUR:
   annualCouponDev = 100 × couponPct / price
   annualCouponEUR = annualCouponDev × fxCoupon

7. Calculate annual capital gain in EUR:
   finalCapitalEUR = (100 × fxMaturity - price × fxInitial) / yearsToMaturity

8. Calculate SAL:
   SAL = 100 × (annualCouponEUR + finalCapitalEUR) / (price × fxInitial)
```

### FX Risk Phases

```
Phase 1 - BUY (t=0):
  Multiplier: 1.00 (current spot rate)
  Logic: You know exact FX rate today
  
Phase 2 - COUPON (ongoing):
  Multiplier: 0.90 (10% conservative downside)
  Logic: Moderate FX uncertainty for periodic payments
  
Phase 3 - MATURITY (t=T):
  Multiplier: 0.80 (20% conservative downside)
  Logic: Maximum FX uncertainty for long-term value
```

**Example Impact:**
```
USD Bond (same coupon/maturity as EUR bond):
  Base SAL (no FX): 5.0%
  After coupon discount: 4.5%
  After maturity discount: ~3.8%
  Net FX penalty: -1.2 percentage points
```

---

## 🏗️ Architecture

```
bond/
├── BondApp.java              # Main entry point
├── model/
│   └── Bond.java            # Bond data model (SAL + Yield + Rating)
├── scrape/
│   ├── BondScraper.java     # CSV parsing & bond loading
│   └── CountryNormalizer.java # Country name normalization
├── calc/
│   └── BondCalculator.java  # Financial calculations
├── fx/
│   └── FxService.java       # FX rates & risk multipliers
├── rating/
│   └── RatingService.java   # Sovereign credit ratings
├── scoring/
│   └── BondScoreEngine.java # SAL, Yield, Final Capital calculation
├── config/
│   ├── BondProfile.java     # Profile data structure
│   └── BondProfilesConfig.java # YAML profile loader
└── report/
    └── HtmlReportWriter.java # Interactive HTML report generation

resources/
├── bond-profiles.yaml        # 4 preset investment profiles
├── bond-report.ftl          # FreeMarker HTML template
├── bond-report.css          # Styling
└── bond-report.js           # Interactive features
```

---

## 🏆 Credit Ratings Integration

### Rating Scale

```
AAA  - Highest quality, minimal credit risk
AA+  - Very high quality
AA   - High quality
AA-  - High quality
A+   - Upper-medium grade
A    - Medium grade
A-   - Medium grade
BBB+ - Lower-medium grade (lowest investment grade)
BBB  - Lower-medium grade
BBB- - Lower-medium grade
BB+  - Speculative (highest non-investment grade)
BB   - Speculative
... and lower
```

### How Ratings Affect Profiles

**SAL Conservative & Income Moderate:**
- Require **BBB+ minimum** (investment grade floor)
- Filters out speculative-grade bonds
- Prioritizes capital preservation

**SAL Aggressive & Income High:**
- **No rating filter** (accepts all grades)
- Allows higher-risk, higher-return opportunities
- User assumes credit risk for better yields

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- Internet connection (for FX rates)

### Installation

```bash
# Clone repository
git clone <repository-url>
cd BondReport

# Build with Maven
mvn clean package

# Run application
java -jar target/bond-report-1.1.jar
```

### Output

Reports are generated in:
```
docs/eur/index.html    # EUR-based analysis
docs/index.html        # Landing page
```

Open `docs/eur/index.html` in your browser to view the interactive report.

---

## 📊 Report Features

### Interactive Controls

1. **Mode Toggle**: Switch between Capital Gain (SAL) and Income (Yield) analysis
2. **Preset Buttons**: One-click application of investment profiles
3. **Column Sorting**: Click any header to sort (ascending/descending)
4. **Column Filters**: Type in filter boxes below headers
5. **CSV Export**: Download filtered results
6. **Dynamic Heatmap**: Colors update based on active mode

### Column Headers

| Column | Description |
|--------|-------------|
| **ISIN** | International Securities ID |
| **Issuer** | Country/entity name |
| **Price** | Market price in original currency |
| **Currency** | Bond denomination (EUR, USD, etc.) |
| **Price (EUR)** | Price converted to EUR |
| **Coupon %** | Annual coupon rate |
| **Maturity** | Redemption date |
| **Years** | Years to maturity |
| **Rating** | Sovereign credit rating |
| **Current Yield %** | Annual income / Price |
| **Final Capital** | Total value at maturity (€1,000 investment) |
| **SAL %** | Simple Annual Yield (total return) |

---

## 🛠️ Configuration

### Customizing Profiles (Two Methods)

#### Method 1: Edit YAML Configuration File (Permanent)

Edit `src/main/resources/bond-profiles.yaml` and rebuild:

```yaml
profiles:
  - id: customProfile
    label: Custom Strategy
    emoji: "🎯"
    description: Your custom filter description
    filters:
      minPrice: 90          # Optional: minimum price
      maxPrice: 110         # Optional: maximum price
      minMatYears: 5        # Optional: minimum years to maturity
      maxMatYears: 25       # Optional: maximum years to maturity
      minSAL: 4.5          # Optional: minimum SAL %
      minYield: 3.0        # Optional: minimum Current Yield %
      minRating: A-        # Optional: minimum credit rating
```

Then rebuild with `mvn clean package`.

#### Method 2: Import YAML at Runtime (Dynamic)

Create a custom profiles YAML file and import it directly in the browser:

**Step 1: Create your custom YAML file** (e.g., `my-profiles.yaml`):

```yaml
profiles:
  - id: ultraShortHigh
    label: Ultra Short High
    emoji: "⚡💰"
    description: Ultra short-term (1-3y) bonds with very high yield (6%+)
    filters:
      minMatYears: 1
      maxMatYears: 3
      minYield: 6.0
      
  - id: longQuality
    label: Long Quality
    emoji: "🏛️📈"
    description: Long-term (25-40y) investment-grade with 4%+ yield
    filters:
      minMatYears: 25
      maxMatYears: 40
      minYield: 4.0
      minRating: A-
```

**Step 2: Import in the web interface**:

1. Open the generated HTML report (`docs/eur/index.html`)
2. Click the **"📁 Import YAML"** button
3. Select your custom YAML file
4. Custom profiles appear with orange/dashed borders
5. Click any profile to apply its filters (example here: [custom profile](./docs/bond-profiles-custom.yaml))

**Advantages**:
- ✅ No rebuild required
- ✅ Easy to share profiles with colleagues
- ✅ Quick experimentation with different strategies
- ✅ Profiles persist until page reload
- ✅ Can re-import to update

**Available Filter Options**:
- `minPrice` / `maxPrice`: Price range in bond currency
- `minMatYears` / `maxMatYears`: Years to maturity (from today)
- `minSAL`: Minimum Simple Annual Yield (%)
- `minYield`: Minimum Current Yield (%)
- `minCapitalAtMat`: Minimum final capital at maturity (EUR)
- `minRating`: Minimum credit rating (AAA, AA+, AA, AA-, A+, A, A-, BBB+, BBB, etc.)

**Example Template**: See `custom-profiles-example.yaml` for a complete template with 6 example profiles.

### Adjusting FX Risk

Edit `BondScoreEngine.fxExpectedMultiplier()`:

```java
return switch (fxPhase) {
    case BUY -> currentFx;
    case COUPON -> currentFx * 0.90;  // Adjust downside %
    case MATURITY -> currentFx * 0.80; // Adjust downside %
};
```

---

## 🚨 Limitations & Assumptions

### Model Assumptions

✅ **What This Model Does:**
- Accurate SAL calculation (linear total return)
- Conservative FX risk estimation
- Credit rating integration
- Clear separation of income vs total return strategies
- Transparent calculations

❌ **What This Model Doesn't Include:**
- **Coupon Reinvestment**: Assumes coupons held as cash
- **Compounding Effects**: Uses linear SAL, not geometric CAGR
- **Dynamic FX Volatility**: Fixed downside percentages
- **Credit Risk Events**: No default/restructuring modeling
- **Interest Rate Risk**: Assumes hold-to-maturity
- **Tax Considerations**: Pre-tax calculations
- **Transaction Costs**: No bid-ask spreads or fees
- **Liquidity Risk**: Assumes all bonds are equally liquid

### Key Simplifications

1. **Par Redemption**: All bonds assumed to redeem at 100% of face value
2. **Fixed FX Downside**: 10% for coupons, 20% for maturity
3. **€1,000 Base**: All calculations normalized to same investment
4. **Annual Coupons**: Assumes annual payment frequency
5. **Credit Stability**: Ratings assumed constant until maturity

### When to Use This Model

✅ **Good for:**
- Comparing bonds across maturities and currencies
- Quick screening of investment opportunities
- Understanding total return vs income trade-offs
- Conservative FX risk assessment
- Educational/illustrative analysis

⚠️ **Not sufficient for:**
- Precise portfolio optimization
- Professional fund management
- Derivative pricing
- Mark-to-market valuations
- Tax-optimized strategies

---

## 🔗 Technical Details

### Key Methods

**BondScoreEngine.calculateBondScores()**
- Calculates SAL, Final Capital, and related metrics
- Applies FX risk at three phases
- Normalizes all bonds to €1,000 investment

**FxService.getExchangeRate()**
- Fetches current spot rates
- Returns rate for converting from quote to base currency

**RatingService.getRating()**
- Maps country names to sovereign credit ratings
- Returns standardized rating strings

**BondProfilesConfig.loadProfiles()**
- Loads preset profiles from YAML
- Provides filter criteria for quick screening

---

## 📞 Support & Troubleshooting

### Common Issues

**Problem**: No bonds appear in report
- Check CSV file format matches expected headers
- Verify maturity dates are in the future
- Ensure price and coupon values are numeric

**Problem**: FX rates seem wrong
- Check internet connection
- Verify FxService API is accessible
- Review currency codes match ISO standards

**Problem**: Ratings not appearing
- Ensure RatingService has mapping for issuer country
- Check country name normalization

**Problem**: Preset filters return no results
- Adjust filter thresholds in YAML
- Check if bonds in dataset meet criteria
- Review actual SAL/Yield distribution

---

## 📄 License

See LICENSE file.

---

## 📝 Version History

**Version 1.1** (Current)
- SAL-based total return analysis
- 4 preset investment profiles (SAL Aggressive/Conservative, Income High/Moderate)
- Dual-mode reporting (SAL vs Yield)
- Credit rating integration with BBB+ filter option
- Progressive FX risk model (10%/20% downside)
- Interactive HTML reports with dynamic heatmaps

**Key Metrics:**
- **Primary**: SAL % (Simple Annual Yield)
- **Secondary**: Current Yield %
- **Tertiary**: Final Capital at Maturity (EUR)

---

**Last Updated**: February 2025  
**Status**: Production Ready  
**Key Innovation**: Simple, transparent total return metric (SAL) combining coupon income and capital appreciation with conservative FX risk modeling