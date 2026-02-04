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

### 2. Base Profile Score
Each investor profile (Income, Balanced, Growth) uses a weight $\alpha$ to balance Current Yield vs. Total Yield:
$$Score_{base} = (\alpha \cdot Norm_{Current}) + ((1 - \alpha) \cdot Norm_{Total})$$



### 3. Dynamic FX Penalty
For bonds not denominated in the reporting currency, a penalty is applied based on the **Square Root of Time** rule and historical volatility ($\sigma$):
$$Penalty_{FX} = \lambda \cdot (1 - e^{-\sigma \cdot \sqrt{T} \cdot Sensitivity})$$
*Where:*
- $\sigma$: Annualized volatility of the currency pair (e.g., 0.09 for USD/EUR).
- $T$: Years to maturity.
- $Sensitivity$: Increases if the yield is heavily dependent on capital gains ($1 + CapitalWeight \cdot Sensitivity_{profile}$).



### 4. Elastic Trust Adjustment
The issuer's credit quality (Trust) is adjusted based on the investor's **Risk Aversion** ($RA$):
$$Score_{final} = (Score_{base} - Penalty_{FX}) \cdot [1 - ((1 - Trust_{issuer}) \cdot RA_{profile})]$$
- **High Risk Aversion (Income)**: The full credit penalty is applied.
- **Low Risk Aversion (Opportunistic)**: The credit penalty is nearly ignored to highlight raw yield.

---

## 👤 Investor Profiles

| Profile | $\alpha$ (Income Weight) | Risk Aversion | Objective |
| :--- | :--- | :--- | :--- |
| **INCOME** | 0.75 | 1.0 | High immediate cash flow, low risk tolerance. |
| **BALANCED** | 0.55 | 0.7 | Balanced total return with moderate risk. |
| **GROWTH** | 0.30 | 0.4 | Capital appreciation from discounted bonds. |
| **OPPORTUNISTIC**| 0.20 | 0.1 | Maximum yield, accepting high credit/FX risk. |



---

## 🛠 Prerequisites & Setup

1. **Java 17+** is required.
2. **Dependencies**: Jsoup (Scraping) and FreeMarker (Templating).
3. **Run the Application**:
   ```bash
   java bond.BondApp