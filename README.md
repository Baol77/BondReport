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
To compare bonds fairly while being robust to outliers, yields are normalized using the 5th and 95th percentiles of the market distribution:

$$\text{Norm}_{\text{Winsorized}}(x) = \max \left( 0, \min \left( 1, \frac{x - Q_{5}}{Q_{95} - Q_{5}} \right) \right)$$

### 2. The $\lambda$ (Lambda) Parameter: FX Risk Gravity
The $\lambda$ parameter represents the **intensity of the FX penalty**. It acts as a scaling factor:
- If $\lambda = 0$: No currency penalty is applied.
- If $\lambda$ is high: Foreign bonds are heavily penalized, favoring local currency bonds.

#### A. Calibration of $\lambda_{base}$
In `BondApp.java`, the system computes λ_base as the **60th percentile of the market BALANCED base score distribution**:

$$
\lambda_{base} = Q_{60\%}\left( 0.55 \cdot Norm(CurrentYield) + 0.45 \cdot Norm(TotalYield) \right)
$$

#### B. Profile Scaling
This base value is then adjusted by a `lambdaFactor` specific to each investor profile:
$$\lambda_{final} = \lambda_{base} \cdot Factor_{profile}$$
*(e.g., 1.3 for INCOME, 0.5 for OPPORTUNISTIC)*


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

## 👤 Investor Profiles (Technical Parameters)

Each profile in the engine is defined by four distinct parameters that control its behavior:

| Profile | $\alpha$ (Income Weight) | $Factor_{\lambda}$ (FX Sensitivity) | $Sensitivity_{cap}$ (Cap. Risk) | $RA$ (Risk Aversion) | Objective |
| :--- | :--- |:------------------------------------| :--- | :--- | :--- |
| **INCOME** | 0.75 | 1.3                                 | 0.15 | 1.0 | Immediate cash flow; maximum credit/FX safety. |
| **BALANCED** | 0.55 | 1.0                                 | 0.30 | 0.7 | Standard total return with moderate protection. |
| **GROWTH** | 0.30 | 0.7                                 | 0.45 | 0.4 | Capital gains from discounted bonds; lower safety. |
| **OPPORTUNISTIC**| 0.20 | 0.5                                 | 0.60 | 0.1 | Maximum raw yield; ignores credit/FX penalties. |

### 🔹 Column Definitions

| Column | Meaning | Financial Interpretation | Effect in the Model |
|--------|---------|--------------------------|---------------------|
| **Profile** | Investment style name | Defines the investor’s objective (income, balance, growth, speculation) | Selects the parameter set used during scoring |
| **α (Income Weight)** | Weight of current yield vs total yield | Higher = prefers coupons; lower = prefers discounted bonds with capital gains | Used in base score: `α·Norm(Current) + (1−α)·Norm(Total)` |
| **$Factor_{\lambda}$ (FX Sensitivity)** | Multiplier applied to λ (FX penalty strength) | Higher = strong aversion to currency risk | Scales FX penalty: `λ_final = λ_base × Factorₗ` |
| **$Sensitivity_{cap}$ (Capital Risk)** | Sensitivity to capital-gain-driven returns | Higher = dislikes bonds whose return depends on price appreciation | Amplifies FX penalty when capital gains dominate |
| **$RA$ (Risk Aversion)** | Credit risk aversion coefficient | Higher = strongly penalizes weaker sovereign issuers | Adjusts issuer trust: `(1 − ((1 − Trust) × RA))` |

---

### ⚠️ Automated Alerting System (CI/CD)
The system is designed to "self-heal" and notify developers when new, unrecognized issuers appear in the data feed:

1. **Detection:** During the scoring process, any issuer not matching the internal rules is assigned a default trust score ($0.80$) and added to the `UNKNOWN_ISSUERS` set.
2. **Reporting:** If unknown issuers are detected, the system generates a `docs/alerts.txt` file.
3. **CI Integration:** The GitHub Actions workflow triggers a **Warning** in the build summary and logs the missing issuers in the console.
4. **Direct Access:** You can check the current status of unrecognized issuers here:  
   👉 **[Current Unknown Issuers List](https://baol77.github.io/BondReport/alerts.txt)** *(Note: This link returns a 404 if the database is 100% up to date).*
5. **Resolution:** Adding the missing keywords to `IssuerManager.java` and pushing the change will automatically resolve the alert and remove the file in the next build.

---

## 🛠 Prerequisites & Setup

1. **Java 17+** is required.
2. **Dependencies**: Jsoup (Scraping) and FreeMarker (Templating).
3. **Run the Application**:
   ```bash
   java bond.BondApp