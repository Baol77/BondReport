# Sovereign Bond Analytics Platform

A comprehensive Java application for analyzing and scoring sovereign bonds with **SAY (Simple Annual Yield)** rankings,
credit ratings, preset investment profiles, FX risk assessment, dual-mode reporting, and **integrated portfolio analysis**.

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
- **🎯 Portfolio Analyzer**: Client-side portfolio management with real-time statistics and CSV import/export

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

### 5. **🎯 Portfolio Analyzer (NEW - v5.0)**

A powerful client-side tool for building and analyzing custom bond portfolios with comprehensive real-time statistics.

#### Key Features

- **🔍 Search & Add**: Find bonds by ISIN and add them to your portfolio
- **💰 Flexible Input**: Enter quantity OR € amount (auto-calculates the other)
- **🔄 Draggable Modal**: Move the portfolio window aside to see the bond table while building
- **✏️ Editable Quantities**: Click Qty in portfolio table to adjust quantities in real-time
- **💾 Persistent Storage**: Data saved in browser's localStorage, survives page refresh
- **✅ Price Tracking**: When reimporting a saved portfolio, automatically updates prices to current market values and shows which bonds moved
- **📥 Export Portfolio**: Save your portfolio as CSV for backup or sharing
- **📤 Import Portfolio**: Load saved portfolios with automatic market price updates

#### Portfolio Table (11 Columns - v5.0)

Each bond in your portfolio shows:

| Column | Description | Notes |
|--------|-------------|-------|
| ISIN | Bond identifier | - |
| Issuer | Bond issuer name | - |
| Currency | Original currency | USD, GBP, EUR, CHF, SEK, etc. |
| Price | Price in EUR | - |
| Qty | Quantity (EDITABLE) | Click to change, updates instantly |
| Investment | Total € amount | Price × Quantity |
| SAY | Simple Annual Yield % | - |
| Curr. Yield | Current Yield % | Income yield |
| Rating | Credit rating | AAA, AA+, BBB+, etc. |
| Maturity | Bond maturity date | When capital is repaid |
| Action | Delete bond | ❌ Delete button |

#### Portfolio Statistics Dashboard (v5.0)

**Financial Metrics (8 metrics):**
- Total Investment (€) - Total portfolio value
- Avg Price (€) - Average € price per unit
- Weighted SAY (%) - Portfolio's total return
- Weighted Yield (%) - Portfolio's income return
- Avg Coupon (%) - Weighted average coupon rate
- Bond Count - Number of different bonds
- Avg Risk (Maturity) - Weighted years to maturity
- **Weighted Rating** - Weighted average credit rating

**Currency Breakdown (NEW - v5.0):**
- Shows portfolio allocation by currency
- Displays % of total investment per currency
- Shows € amount invested in each currency
- Perfect for FX risk analysis

#### Example: Multi-Currency Portfolio Analysis

**Portfolio Table Example Row:**
```
XS1313004928 | VOLKSWAG | EUR | €96.50 | 10 | €965.00 | 4.32% | 2.35% | BBB+ | 2025-04-15 | ❌
```

**Statistics Dashboard Shows:**
```
Total Investment: €10,500.00
Weighted SAY: 4.32%
Weighted Rating: BBB+ (Investment Grade)
Avg Risk: 7.71 years (Medium-term duration)

Currency Breakdown:
EUR    65.2%  €6,820.00
USD    25.8%  €2,700.00
GBP     8.9%    €930.00
```

#### How to Use Portfolio Analyzer

1. **Open Portfolio**: Click the **🎯 Portfolio Analysis** button in the controls
2. **Search for Bond**:
    - Type ISIN code (e.g., `US0378331005`)
    - Click 🔍 Search or press Enter
3. **Add to Portfolio**:
    - Enter Quantity (how many units) OR € Amount
    - Click ➕ Add to Portfolio
4. **Review Portfolio**:
    - See all added bonds with full details (currency, rating, maturity, yield)
    - View real-time statistics below
5. **Manage Quantities**:
    - Click on Qty field to edit
    - Statistics update instantly
    - Perfect for "what-if" analysis
6. **Manage Portfolio**:
    - Delete individual bonds with ❌ Delete button
    - Export portfolio with 📥 Export CSV
    - Import previously saved portfolios with 📤 Import CSV
    - Clear entire portfolio with 🗑️ Clear Portfolio

#### Portfolio Import with Automatic Price Updates

When you reimport a saved portfolio CSV file:

1. **Quantities are preserved** - keeps your original investment amounts
2. **Prices are updated** - uses current market values from the bond table
3. **Statistics recalculate** - all metrics based on new prices
4. **Changes shown** - alert displays which bonds moved and by how much
5. **Weighted metrics update** - rating, maturity, currency breakdown all recalculate

**Example Import Alert**:
```
Imported 2 bonds!

📊 Price Updates (Market has changed):
XS1313004928: €96.50 → €98.75 (+€2.25)
US0378331005: €105.20 → €103.80 (-€1.40)
```

Perfect for tracking portfolio performance over time!

#### CSV Format

**Export**: Creates a CSV with this structure:
```
ISIN,Issuer,Price EUR,Quantity,Investment EUR,SAY %,Current Yield %,Coupon %,Rating,Currency,Maturity
XS1313004928,"VOLKSWAG",96.50,10,965.00,4.32,2.35,3.50,BBB+,EUR,2025-04-15
US0378331005,"USA",98.00,10,980.00,3.75,1.90,2.75,AA+,USD,2030-05-15
```

**Import**: Accepts the same CSV format. The tool:
- Matches bonds by ISIN
- Restores quantities from CSV
- Updates all prices from current bond table
- Recalculates statistics automatically
- Shows which prices changed

## 📊 Built-In Investment Profiles

### Updated Profile Structure (v4.0+)

These 6 profiles represent the standard strategic configurations for the platform:

### 1. 📈🔥 SAY Aggressive
*Best for: Risk-tolerant investors seeking maximum total return via capital gains.*
- **Profile Type**: `SAY` | **Sort By**: `SAY`
- **Filters**: 1-10y maturity, Min 5.0% SAY, Max Price 95.
- **Strategy**: Focuses on "Pull to Par" effects where bonds at a discount provide high annual returns.

### 2. ⚖️🛡️ Balanced Core
*Best for: Balanced investors seeking solid returns with investment-grade safety.*
- **Profile Type**: `SAY` | **Sort By**: `SAY`
- **Filters**: 1-20y maturity, Min 4.5% SAY, Min Rating `BBB+`.
- **Strategy**: Targets the "sweet spot" of reliable issuers with competitive total yields.

### 3. 💵🔥 Max Income
*Best for: Investors needing maximum immediate cash flow.*
- **Profile Type**: `income` | **Sort By**: `CURR_YIELD`
- **Filters**: >15y maturity, Min 6.0% Current Yield, Max Price 110.
- **Strategy**: Prioritizes high coupons while capping prices to prevent overpaying for premiums.

### 4. 📉🚀 Deep Discount
*Best for: Long-term capital building and tax-efficient growth.*
- **Profile Type**: `SAY` | **Sort By**: `SAY`
- **Filters**: 3-20y maturity, Max Price 90, Min Rating `BBB`.
- **Strategy**: Targets bonds trading significantly below par for guaranteed capital appreciation at maturity.

### 5. 🅿️🛡️ Cash Parking
*Best for: Ultra-short term liquidity management (Bank Account Alternative).*
- **Profile Type**: `SAY` | **Sort By**: `MATURITY`
- **Filters**: <1.5y maturity, Min 3.5% Yield, Min Rating `BBB+`.
- **Strategy**: Minimizes interest-rate risk while seeking a safe return on idle cash.

### 6. 🏰🛡️ AAA/AA Fortress
*Best for: Wealth preservation and hedging against market volatility.*
- **Profile Type**: `income` | **Sort By**: `RATING`
- **Filters**: 5-30y maturity, Min 3.0% SAY, Min Rating `AA-`.
- **Strategy**: Only the highest quality sovereign debt to provide a "flight to quality" hedge.

---
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
RISK_MODEL.put(10,new RiskThreshold(0.15, 0.075));   // 5-10 years
RISK_MODEL.put(15,new RiskThreshold(0.20, 0.100));   // 10-15 years
RISK_MODEL.put(20,new RiskThreshold(0.25, 0.125));   // 15-20 years
RISK_MODEL.put(Integer.MAX_VALUE, new RiskThreshold(0.30, 0.150));  // 20+ years
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
src/main/resources/templates/
├── bond-report.ftl             # FreeMarker HTML template
├── bond-report.js              # Interactive UI logic
├── portfolio-analyzer-v2.js    # Portfolio Analyzer tool (NEW)
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

### Weighted SAY / Weighted Current Yield (Portfolio)

For portfolios with multiple bonds, the weighted average metric accounting for investment amount in each bond:

```
Weighted SAY = Σ(SAY × Investment Amount) / Total Investment
```

This shows your portfolio's average return, weighted by how much you invested in each bond.

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
    - File is generated to `docs/eur/index.html` (for GitHub Pages)
    - Open in your browser
    - Apply profiles, adjust filters, toggle modes
    - Use Portfolio Analyzer to build custom portfolios

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
- **Portfolio Data**: Stored in browser's localStorage only (never sent to server)

## 🚀 Future Enhancements

- Real-time bond data integration
- Historical SAY analysis
- Advanced portfolio simulation engine
- PDF report export
- Advanced charting with TradingView Lightweight Charts
- Multi-currency portfolio optimization
- Server-side portfolio persistence
- Collaborative portfolio sharing

## 📞 Support & Contribution

For questions, issues, or contributions:

- Review the existing code documentation
- Check the YAML profile examples in `docs/bond-profiles-custom.yaml`
- Update all references when modifying terminology (SAY vs SAL)
- Portfolio Analyzer is fully client-side; check browser console (F12) for debugging

## 📄 License

This project is provided as-is for educational and analytical purposes.

---

**Last Updated**: February 2026  
**Version**: 5.0  
**Key Changes**: Portfolio Analyzer v5.0 - Editable quantities, currency tracking, weighted rating, currency breakdown, comprehensive portfolio analytics