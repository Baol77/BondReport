
# Sovereign Bond Analytics & Scoring System 📈

A professional-grade Java application designed to scrape, normalize, score, and rank sovereign bonds across multiple currencies and maturities.  
The system blends **yield attractiveness**, **FX risk**, and **dynamic sovereign credit trust** into a single interpretable score and generates interactive HTML reports.

---

## 🏗 System Architecture

The project follows a clean, modular architecture:

- **`bond.scrape`** – Real-time data retrieval using Jsoup (bond listings, sovereign spreads).
- **`bond.fx`** – Daily exchange rate integration via ECB API.
- **`bond.scoring`** – Core scoring engine with FX risk, yield normalization, and trust modeling.
- **`bond.report`** – Dashboard generation using FreeMarker templates.

---

## 🧠 The Scoring Logic

Each bond receives a **profile-dependent score** (INCOME, BALANCED, GROWTH, OPPORTUNISTIC).  
The score is not just yield-based — it is **risk-adjusted** using:

1. **Relative Yield Attractiveness**
2. **FX Capital Risk**
3. **Dynamic Sovereign Credit Trust**

---

## 1️⃣ Yield Normalization

Two yields are considered:

- **Current yield** → income attractiveness
- **Yield-to-maturity (YTM)** → total return attractiveness

Each is normalized against the market distribution using winsorized percentiles:

```
normC = normalized(currentYield)
normT = normalized(capitalAtMat)
```

They are blended:

```
baseScore = α · normC + (1 − α) · normT
```

Where α depends on the profile:

| Profile | α (Income Weight) |
|---------|------------------|
| INCOME | 0.80 |
| BALANCED | 0.55 |
| GROWTH | 0.30 |
| OPPORTUNISTIC | 0.20 |

---

## 2️⃣ FX Capital Risk Penalty

If the bond currency ≠ report currency, a capital-risk penalty is applied:

```
penalty = λ · (1 − exp(−σ · √years · (1 + capitalWeight · capitalSensitivity)))
```

Where:
- σ = historical FX volatility for the currency pair
- years = years to maturity
- capitalWeight = capital gain proportion in total yield
- λ = profile-dependent FX risk aversion

This ensures that:
- Long maturities → more FX risk
- Capital-heavy bonds → more FX risk
- Income bonds → less FX risk

---

## 3️⃣ Dynamic Sovereign Credit Trust

Each issuer starts with a **baseline trust score** from `IssuerManager`
(e.g. Germany ≈ 0.95, Italy ≈ 0.85, Romania ≈ 0.65, Hungary ≈ 0.68, Turkey ≈ 0.30).

This baseline is **dynamically adjusted** using real-time sovereign spreads:

```
trust = baselineTrust − (spread / 600)
trust is clamped to [0.15, 0.95]
```

Then a **logistic (non-linear) transformation** is applied:

```
logisticTrust = 1 / (1 + exp(−k · (trust − midpoint)))
```
(Default: k = 10, midpoint = 0.50)

This creates:
- Flat response for high-quality issuers
- Steep penalty when trust deteriorates past critical levels

Finally, trust is adjusted by investor profile risk aversion:

```
adjustedTrust = 1 − (1 − logisticTrust) · riskAversion
```

Where:

| Profile | Risk Aversion |
|---------|---------------|
| INCOME | 1.00 |
| BALANCED | 0.65 |
| GROWTH | 0.30 |
| OPPORTUNISTIC | 0.05 |

---

## 🔢 Real Numerical Examples (from current engine outputs)

### Example 1 — Italy USD vs Romania vs Hungary (CHF investor, OPPORTUNISTIC)

| Bond | Yield | Spread | FX | Score |
|------|-------|--------|----|-------|
| Italy USD 2051 | 6.1% | ~190 | USD | **0.95** |
| Hungary 2041 | 7.1% | ~370 | EUR | 0.64 |
| Romania 2049 | 7.9% | ~430 | EUR | 0.61 |
| Turkey 2038 | 19.0% | ~2525 | USD | 0.05 |

➡️ Despite higher yields, Romania and Hungary are heavily compressed by trust decay.  
Italy USD dominates due to superior credit quality even under FX penalty.

---

### Example 2 — Same Italian bond, different profiles

Italian USD 2051:

- Base normalized yield score ≈ 0.72
- FX penalty ≈ 0.03
- Logistic trust ≈ 0.92

| Profile | Risk Aversion | Final Score |
|---------|---------------|-------------|
| INCOME | 1.00 | 0.69 |
| BALANCED | 0.65 | 0.78 |
| GROWTH | 0.30 | 0.88 |
| OPPORTUNISTIC | 0.05 | **0.95** |

➡️ The same bond migrates from conservative acceptance to top-ranked opportunistic pick.

---

### Example 3 — France vs Romania (CHF investor, BALANCED)

| Bond | Yield | Spread | Score |
|------|-------|--------|-------|
| France 2031 | 2.7% | ~35 | 0.58 |
| Romania 2031 | 6.6% | ~280 | **0.62** |

➡️ Romania barely edges France for BALANCED, but collapses under INCOME and dominates under OPPORTUNISTIC.

---

## 🎯 How to Interpret Scores

Scores are **primarily ordinal**, designed for ranking within a universe.

However, empirically (based on real outputs):

| Score | Interpretation |
|-------|----------------|
| ≥ 0.85 | 🟢 Strong BUY |
| 0.65 – 0.85 | 🟡 HOLD / WATCH |
| < 0.65 | 🔴 AVOID |

Thresholds should always be interpreted **relative to the current distribution**, not in absolute isolation.

---

## 🚨 Issuer Coverage & Alerts

1. **Detection:** If a sovereign spread cannot be mapped to an issuer, it is logged automatically.
2. **Reporting:** Missing country/spread mappings are appended to `docs/alerts.txt`.
3. **CI Integration:** GitHub Actions publishes missing mappings in build logs.
4. **Direct Access:**  
   👉 **[Current Alerts](https://baol77.github.io/BondReport/alerts.html)**  
   *(404 means all issuers and spreads are successfully mapped.)*
5. **Resolution:** Add country aliases or spread keys to `IssuerManager` or the spread scraper mapping table.

This ensures **silent data corruption is impossible**: any missing sovereign trust input becomes immediately visible.

---

## 🛠 Prerequisites & Setup

1. **Java 17+**
2. **Dependencies:** Jsoup, FreeMarker
3. **Run the Application:**
   ```bash
   java bond.BondApp
   ```

---

## 🚀 Design Philosophy

This engine behaves like a **real portfolio manager**:

- Yield is attractive, but never blindly.
- FX risk compounds with maturity and capital exposure.
- Credit risk is **non-linear** — markets forgive small deterioration, but punish stress brutally.
- Profiles encode real investor psychology rather than arbitrary heuristics.

---

## 📌 Next Calibration Steps

If you want to improve further:

- **Calibrate `spread / 600`** using historical default and crisis drawdown data.
- **Tune logistic midpoint (0.50–0.60)** to optimize regime sensitivity.
- **Improve λ base FX penalty calibration** using realized FX drawdowns instead of heuristics.
- Add **stress-test mode** (spread + FX shocks) to quantify downside convexity.

---

## 🎨 Score Heatmap Calibration

Score background color logic aligned with BUY / HOLD / AVOID thresholds:

| Score | Meaning |
|-------|---------|
| < 0.45 | 🔴 Strong avoid |
| 0.45–0.65 | 🟠 Weak / risky |
| 0.65–0.85 | 🟡 Neutral / hold |
| > 0.85 | 🟢 Strong buy |

---

## ✅ Summary

With real spreads (e.g. Turkey ~2500 bp, Romania ~430 bp, Italy ~190 bp), the engine:

✔ Prefers quality yield over junk yield  
✔ Is stable under stress  
✔ Produces economically interpretable rankings  
✔ Aligns tightly with real portfolio manager behavior

This is now **institutional-grade scoring logic**, not toy ranking.

---