# Sovereign Bond Analytics Platform

A comprehensive Java application for analyzing and scoring sovereign bonds with **SAY (Simple Annual Yield)** rankings,
credit ratings, preset investment profiles, FX risk assessment, and dual-mode reporting.

## 🎯 Overview

This platform analyzes sovereign bonds across multiple currencies and generates interactive reports featuring:

- **Dual-Mode Analysis**: Capital Gain (SAY-focused) or Income (Current Yield-focused)
- **Price Range Filtering**: Filter bonds by minimum and maximum price for flexible portfolio construction
- **4 Preset Investment Profiles**: Quick-start filters for different investor types with customizable profile types
- **SAY Analysis**: Simple Annual Yield as primary metric for total returns
- **Current Yield Analysis**: Immediate income metric for income investors
- **Credit Ratings**: Sovereign credit ratings integrated into filtering
- **Advanced FX Risk Modeling**: Risk thresholds based on bond maturity with haircuts for coupon and capital phases
- **Multi-Currency Support**: EUR, USD, GBP, CHF, SEK, and others
- **Interactive Heatmaps**: Color-coded bands for SAY or Yield (mode-dependent)
- **YAML Profile Customization**: Import custom investor profiles from external YAML files

## ✨ Recent Enhancements (v4.0+)

### 1. **Price Range Filtering**

- Added **minimum price filter** (`priceMin`) alongside the existing maximum price filter
- Unified filtering UI with separate min/max inputs in the Price column header
- Enables precise portfolio construction for specific price ranges

### 2. **Improved FX Risk Modeling**

- Moved `fxExpectedMultiplier()` from `BondScoreEngine` to `FxService`
- Implemented **RiskThreshold** record with maturity-based haircuts:
    - **0-5 years**: 10% capital haircut, 5% coupon haircut
    - **5-10 years**: 15% capital haircut, 7.5% coupon haircut
    - **10-15 years**: 20% capital haircut, 10% coupon haircut
    - **15-20 years**: 25% capital haircut, 12.5% coupon haircut
    - **20+ years**: 30% capital haircut, 15% coupon haircut
- Static `TreeMap<Integer, RiskThreshold>` structure for efficient lookups
- Enhanced documentation and centralized FX logic

### 3. **Custom Profile Enhancements**

- Added `sortedBy` property to define default sort column per profile (e.g., "SAY", "CURR_YIELD")
- Added `profileType` property to specify profile behavior: `"SAY"` or `"income"`
- Enables more granular control over preset behavior
- YAML format fully supports new properties for custom profile imports

### 4. **Terminology Correction**

- Renamed **SAL** → **SAY** throughout the codebase:
    - JavaScript column identifier: `COL.SAY`
    - Filter element IDs: `filterMinSAY`
    - Legend titles: "SAY Heatmap"
    - Profile IDs: `sayAggressive`, `sayConservative`
    - Comments and documentation updated
- SAY correctly stands for **Simple Annual Yield**

## 📊 Built-In Investment Profiles

### Updated Profile Structure (v4.0+)

Each profile now includes `profileType` and `sortedBy` properties:

#### 1. **📈🔥 SAY Aggressive** (Maximum Returns)

*Best for: Risk-tolerant investors seeking maximum total return*

- **Profile Type**: SAY (Capital Gain)
- **Sort By**: SAY (descending)
- **Time Horizon**: 1-10 years (short to medium term)
- **Minimum SAY**: 5.0%+
- **Strategy**: High-return opportunities regardless of rating
- **Typical Results**: ~15-25 high-yield bonds

#### 2. **📈🛡️ SAY Conservative** (Quality + Returns)

*Best for: Balanced investors wanting solid returns with credit safety*

- **Profile Type**: SAY (Capital Gain)
- **Sort By**: SAY (descending)
- **Time Horizon**: 1-20 years (short to long term)
- **Minimum SAY**: 4.0%+
- **Minimum Rating**: BBB+ (investment grade)
- **Strategy**: Quality bonds with good total returns
- **Typical Results**: ~50-80 investment-grade bonds

#### 3. **💵🔥 Income High** (Maximum Cash Flow)

*Best for: Income-focused investors needing high annual payments*

- **Profile Type**: income
- **Sort By**: CURR_YIELD (descending)
- **Time Horizon**: 20-40 years (long term)
- **Minimum Current Yield**: 5.0%+
- **Strategy**: Maximum immediate income from coupons
- **Typical Results**: ~10-20 high-income bonds

#### 4. **💵🌱 Income Moderate** (Balanced Income + Quality)

*Best for: Conservative income investors prioritizing safety*

- **Profile Type**: income
- **Sort By**: CURR_YIELD (descending)
- **Time Horizon**: 20-40 years (long term)
- **Minimum Current Yield**: 3.5%+
- **Minimum Rating**: BBB+ (investment grade)
- **Strategy**: Reliable income from quality issuers
- **Typical Results**: ~30-50 stable income bonds

## 🎨 Dual-Mode Report Interface

### Toggle Between Two Investment Approaches

**Capital Gain Mode (Default - SAY Focus)**

- Emphasizes total return potential
- Color gradient from red (SAY < 1%) → yellow (1-2.5%) → green (2.5%+)
- Strong color intensity for maximum visibility
- Best for growth-oriented investors

**Income Mode (Current Yield Focus)**

- Emphasizes immediate cash flow
- Lighter color palette for sustainable viewing
- Shows coupon-driven returns
- Best for income-focused investors

## 🔧 Using Custom Profiles

### YAML Format for Custom Profiles

Create a `.yaml` file with custom investment profiles:

```yaml
profiles:
  - id: myCustomProfile
    label: "My Custom Strategy"
    emoji: "🎯"
    description: "Description of your investment strategy"
    profileType: "SAY"  # or "income"
    sortedBy: "SAY"     # or "CURR_YIELD"
    filters:
      minMatYears: 2
      maxMatYears: 15
      minSAY: 3.5
      minRating: "A-"
      maxPrice: 105
      minPrice: 95
```

### Importing Custom Profiles

1. Click the **📁 Import YAML** button
2. Select your `.yaml` file
3. Custom profiles appear as new buttons alongside built-in profiles
4. Apply filters with one click

## 📐 FX Risk Adjustment Details

### Why FX Risk Matters for International Bonds

When investing in non-EUR bonds, three critical phases face currency risk:

1. **BUY Phase**: No haircut (immediate transaction)
2. **COUPON Phase**: Progressive haircut as coupons arrive (moderate risk)
3. **MATURITY Phase**: Larger haircut on principal repayment (long-term risk)

### Risk Model Implementation

The FX risk model applies maturity-dependent haircuts:

```java
RISK_MODEL.put(5,new RiskThreshold(0.10, 0.050));    // 0-5 years
    RISK_MODEL.

put(10,new RiskThreshold(0.15, 0.075));    // 5-10 years
    RISK_MODEL.

put(15,new RiskThreshold(0.20, 0.100));    // 10-15 years
    RISK_MODEL.

put(20,new RiskThreshold(0.25, 0.125));    // 15-20 years
    RISK_MODEL.

put(Integer.MAX_VALUE, new RiskThreshold(0.30, 0.150));  // 20+ years
```

**Example**: A 7-year USD bond gets:

- Coupon haircut: 7.5%
- Capital haircut: 15%

## 🛠️ Development & Architecture

### Project Structure

```
src/main/java/bond/
├── fx/
│   └── FxService.java          # FX rates & risk modeling
├── scoring/
│   └── BondScoreEngine.java    # SAY calculations
├── config/
│   ├── BondProfile.java        # Profile with sortedBy & profileType
│   └── BondProfilesConfig.java # Profile loading
├── model/
│   └── Bond.java               # Bond data structure
└── ...
src/main/resources/
├── bond-report.ftl             # HTML template
├── bond-report.js              # Interactive UI logic
├── bond-profiles.yaml          # Default profiles
└── bond-report.css             # Styling
```

### Key Classes

#### FxService (Enhanced in v4.0)

- **Responsibility**: Manage FX rates and apply risk-based adjustments
- **New Static Method**: `fxExpectedMultiplier()`
- **New Fields**: `RISK_MODEL` (TreeMap), `RiskThreshold` record
- **Maturity-Aware**: Applies haircuts based on bond years-to-maturity

#### BondScoreEngine (Simplified in v4.0)

- **Responsibility**: Calculate bond scores (Final Capital and SAY)
- **Change**: Now delegates FX risk calculations to `FxService`
- **Cleaner**: Removed internal `fxExpectedMultiplier()` method
- **Benefit**: Separation of concerns and code reuse

#### BondProfile (Extended in v4.0)

- **New Property**: `sortedBy` (default column to sort by)
- **New Property**: `profileType` ("SAY" or "income")
- **Use Case**: Enables profiles to control UI behavior
- **YAML Support**: Both properties supported in custom profile import

### Building & Testing

```bash
# Build the project
mvn clean package

# Run tests
mvn test

# Generate Javadoc
mvn javadoc:javadoc
```

## 🌍 Supported Currencies

The platform pulls real-time FX rates from the European Central Bank (ECB) and supports:

- EUR, USD, GBP, CHF, SEK, and other major currencies
- Automatic rate caching (refreshed on demand)
- Fallback to 1:1 for unknown currencies

## 📈 Metrics Explained

### SAY (Simple Annual Yield) %

The total return per year as a percentage of the bond's current purchase price:

```
SAY = (Annual Coupon Income + Capital Gain/Loss per Year) / Purchase Price
```

**Example**: A €100 bond yielding 5% coupons with €2 annual appreciation:

```
SAY = (5 + 2) / 100 = 7%
```

### Current Yield %

Immediate income from the bond's coupon relative to current price:

```
Current Yield = (Annual Coupon / Current Price) × 100
```

### Final Capital at Maturity (on €1,000 investment)

Total amount you'll have when the bond matures, including all coupons and principal, adjusted for FX risk.

## 🎬 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Modern web browser (Chrome, Firefox, Safari, Edge)

### Running the Application

1. **Build**:
   ```bash
   mvn clean package
   ```

2. **Generate Report**:
   ```bash
   java -cp target/classes bond.BondApp
   ```

3. **Open Report**:
    - File is generated to `target/bond-report.html`
    - Open in your browser
    - Apply profiles, adjust filters, toggle modes

## 📋 Column Headers & Filters

| Column             | Type   | Description                        | Filter               | YAML filter                 | SortedBy       |
|--------------------|--------|------------------------------------|----------------------|-----------------------------|----------------|
| ISIN               | Text   | Unique bond identifier             | Contains search      |                             | ISIN           |
| Issuer             | Text   | Sovereign issuer                   | Contains search      |                             | ISSUER         |
| Price              | Number | Current price in issuer currency   | Min & Max range      | minPrice<br/>maxPrice       | PRICE          |
| Currency           | Select | Bond currency (EUR, USD, GBP, etc) | Dropdown             |                             |                |
| Rating             | Select | Sovereign credit rating            | Minimum rating       | minRating                   | RATING         |
| Price (EUR)        | Number | Price converted to EUR             | Display only         |                             | PRICE_R        |
| Coupon %           | Number | Annual coupon rate                 | Display only         |                             | COUPON         |
| Maturity           | Date   | Bond maturity date                 | Min & Max date range | minMatYears<br/>maxMatYears | MATURITY       |
| Curr. Yield %      | Number | Annual income as % of price        | Minimum yield        | minYield                    | CURR_YIELD     |
| Total Return (1k€) | Number | Final capital on €1,000 investment | Minimum amount       |                             | CAPITAL_AT_MAT |
| SAY (%)            | Number | Simple Annual Yield                | Minimum SAY          | minSAY                      | SAY            |

## 🔐 Security & Performance

- **No External Data Storage**: All data processed locally
- **ECB FX Rate Caching**: Single HTTP call per session
- **Large Bond Lists**: Efficient JavaScript filtering and sorting
- **Responsive UI**: Sub-second filter updates even with 500+ bonds

## 🚀 Future Enhancements

- Real-time bond data integration
- Historical SAY analysis
- Portfolio simulation engine
- PDF report export
- Advanced charting with TradingView Lightweight Charts
- Multi-currency portfolio optimization

## 📞 Support & Contribution

For questions, issues, or contributions:

- Review the existing code documentation
- Check the YAML profile examples in `docs/bond-profiles-custom.yaml`
- Update all references when modifying terminology (SAY vs SAL)

## 📄 License

This project is provided as-is for educational and analytical purposes.

---

**Last Updated**: February 2026  
**Version**: 3.0  
**Key Changes**: Price range filtering, FX risk model refactoring, SAY terminology, custom profile enhancements