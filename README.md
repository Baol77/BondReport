# Sovereign Bond Analytics & Scoring System 📈

A professional-grade Java application designed to scrape, analyze, and score European sovereign bonds. The system fetches real-time data, applies advanced financial modeling to account for FX risk and credit trust, and generates interactive HTML reports.

## 🏗 System Architecture

The project follows a clean, modular architecture:
- **`bond.scrape`**: Real-time data retrieval using Jsoup.
- **`bond.fx`**: Daily exchange rate integration via ECB API.
- **`bond.scoring`**: The core logic engine applying risk-adjusted algorithms.
- **`bond.report`**: Dashboard generation using FreeMarker templates.

---

## 🧠 The Scoring Logic & Formulas

The `BondScoreEngine` evaluates bonds using a multi-factor approach. Here are the key formulas integrated into the code:

### 1. Market Normalization
To compare bonds fairly, yields are normalized between 0 and 1 based on the current market universe (Min/Max):
$$Norm(x) = \frac{x - Min_{market}}{Max_{market} - Min_{market}}$$

### 2. The $\lambda$ (Lambda) Parameter: FX Risk Gravity
The $\lambda$ parameter represents the **intensity of the FX penalty**. It acts as a scaling factor:
- If $\lambda = 0$: No currency penalty is applied.
- If $\lambda$ is high: Foreign bonds are heavily penalized, favoring local currency bonds.

**Dynamic Calculation:** In `BondApp.java`, $\lambda$ is automatically calibrated at **80% of the market's average base score**. This ensures the penalty is always proportional to the yields currently available on the market.

#### A. Calibration of $\lambda_{base}$
In `BondApp.java`, the system calculates a global $\lambda_{base}$ representing 80% of the market's average "Balanced" score:
$$\lambda_{base} = 0.8 \cdot \text{Average}\left( 0.55 \cdot Norm_{Current} + 0.45 \cdot Norm_{Total} \right)$$

#### B. Profile Scaling
This base value is then adjusted by a `lambdaFactor` specific to each investor profile:
$$\lambda_{final} = \lambda_{base} \cdot Factor_{profile}$$
*(e.g., 1.2 for INCOME, 0.5 for OPPORTUNISTIC)*

### 3. Base Profile Score
Each investor profile (Income, Balanced, Growth) uses a weight $\alpha$ to balance Current Yield vs. Total Yield:
$$Score_{base} = (\alpha \cdot Norm_{Current}) + ((1 - \alpha) \cdot Norm_{Total})$$

### 4. Dynamic FX Penalty
For bonds not denominated in the reporting currency, a penalty is applied based on the **Square Root of Time** rule and historical volatility ($\sigma$):
$$Penalty_{FX} = \lambda \cdot (1 - e^{-\sigma \cdot \sqrt{T} \cdot Sensitivity})$$
*Where:*
- $\sigma$: Annualized volatility of the currency pair (e.g., 0.09 for USD/EUR).
- $T$: Years to maturity.
- $Sensitivity$: Increases if the yield is heavily dependent on capital gains ($1 + CapitalWeight \cdot Sensitivity_{profile}$).



### t. Elastic Trust Adjustment
The issuer's credit quality (Trust) is adjusted based on the investor's **Risk Aversion** ($RA$):
$$Score_{final} = (Score_{base} - Penalty_{FX}) \cdot [1 - ((1 - Trust_{issuer}) \cdot RA_{profile})]$$
- **High Risk Aversion (Income)**: The full credit penalty is applied.
- **Low Risk Aversion (Opportunistic)**: The credit penalty is nearly ignored to highlight raw yield.

---

## 👤 Investor Profiles (Technical Parameters)

Each profile in the engine is defined by four distinct parameters that control its behavior:

| Profile | $\alpha$ (Income Weight) | $Factor_{\lambda}$ (FX Sensitivity) | $Sensitivity_{cap}$ (Cap. Risk) | $RA$ (Risk Aversion) | Objective |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **INCOME** | 0.75 | 1.2 | 0.15 | 1.0 | Immediate cash flow; maximum credit/FX safety. |
| **BALANCED** | 0.55 | 1.0 | 0.30 | 0.7 | Standard total return with moderate protection. |
| **GROWTH** | 0.30 | 0.7 | 0.45 | 0.4 | Capital gains from discounted bonds; lower safety. |
| **OPPORTUNISTIC**| 0.20 | 0.5 | 0.60 | 0.1 | Maximum raw yield; ignores credit/FX penalties. |



---

## 🛠 Prerequisites & Setup

1. **Java 17+** is required.
2. **Dependencies**: Jsoup (Scraping) and FreeMarker (Templating).
3. **Run the Application**:
   ```bash
   java bond.BondApp