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

### 1️⃣ Yield Normalization

Two yields are considered:

- **Current yield** → income attractiveness
- **Yield-to-maturity (YTM)** → total return attractiveness

Each is normalized against the market distribution using winsorized percentiles:

```
normC = normalized(currentYield)
normT = normalized(totalYield)
```

They are blended:

```
baseScore = α · normC + (1 − α) · normT
```

Where α depends on the profile:

| Profile | α (Income Weight) |
|---------|------------------|
| INCOME | 0.75 |
| BALANCED | 0.55 |
| GROWTH | 0.30 |
| OPPORTUNISTIC | 0.20 |

---

### 2️⃣ FX Capital Risk Penalty

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

### 3️⃣ Dynamic Sovereign Credit Trust

Each issuer starts with a **baseline trust score** from `IssuerManager` (e.g., Germany ≈ 0.95, Italy ≈ 0.85, Romania ≈ 0.65).

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
| INCOME | 1.0 |
| BALANCED | 0.7 |
| GROWTH | 0.4 |
| OPPORTUNISTIC | 0.1 |

---

## 🔢 Real Numerical Examples

### Example 1 — German Bund vs Italian BTP (EUR investor)

| Bond | Curr Yield | YTM | Spread | Maturity |
|------|------------|-----|--------|----------|
| Germany 2032 | 2.2% | 2.3% | 0 bp | 7y |
| Italy 2032 | 3.6% | 3.9% | 160 bp | 7y |

Assume market normalization gives:

```
Germany: normC = 0.35, normT = 0.40
Italy:   normC = 0.70, normT = 0.75
```

#### BALANCED profile

```
baseScore_DE = 0.55·0.35 + 0.45·0.40 = 0.372
baseScore_IT = 0.55·0.70 + 0.45·0.75 = 0.722
```

FX penalty = 0 (EUR investor).

Trust calculation:
```
Germany: trust ≈ 0.95 → logistic ≈ 0.98 → adjustedTrust ≈ 0.986
Italy:   trust ≈ 0.85 − 160/600 ≈ 0.58 → logistic ≈ 0.69 → adjustedTrust ≈ 0.783
```

Final scores:
```
Germany: 0.372 · 0.986 ≈ 0.367
Italy:   0.722 · 0.783 ≈ 0.565
```

➡️ Italy still ranks higher due to yield, but the credit risk meaningfully compresses the advantage.

---

### Example 2 — Romania vs France (CHF investor)

| Bond | Curr Yield | YTM | Spread | Maturity | Currency |
|------|------------|-----|--------|----------|----------|
| France 2031 | 2.5% | 2.7% | 35 bp | 6y | EUR |
| Romania 2031 | 6.2% | 6.6% | 280 bp | 6y | EUR |

Assume normalization:

```
France:  normC = 0.45, normT = 0.50
Romania: normC = 0.92, normT = 0.95
```

Capital weight ≈ 0.40, σ(EUR/CHF)=0.07, λ(BALANCED)=1.0.

FX penalty ≈ 0.06.

Trust:
```
France: 0.90 − 35/600 ≈ 0.84 → logistic ≈ 0.86 → adjustedTrust ≈ 0.90
Romania: 0.65 − 280/600 ≈ 0.18 → logistic ≈ 0.13 → adjustedTrust ≈ 0.39
```

Final scores:
```
France:  (0.55·0.45 + 0.45·0.50 − 0.06) · 0.90 ≈ 0.32
Romania: (0.55·0.92 + 0.45·0.95 − 0.06) · 0.39 ≈ 0.36
```

➡️ Despite extremely weak credit, Romania can still edge France for **risk-tolerant profiles**, but will collapse sharply under INCOME.

---

### Example 3 — Same Italian bond, different profiles

Italian BTP score ≈ 0.72 (base), FX penalty 0, logisticTrust ≈ 0.69.

| Profile | Risk Aversion | Final Score |
|---------|---------------|-------------|
| INCOME | 1.0 | 0.72 × 0.69 ≈ 0.50 |
| BALANCED | 0.7 | 0.72 × 0.78 ≈ 0.56 |
| GROWTH | 0.4 | 0.72 × 0.88 ≈ 0.63 |
| OPPORTUNISTIC | 0.1 | 0.72 × 0.97 ≈ 0.70 |

➡️ Same bond, radically different attractiveness depending on investor profile.

---

## 🎯 How to Interpret Scores

- **Primarily ordinal**: scores are best used for **ranking within the same universe**.
- **Thresholds are possible**, but relative:
   - `>0.65` → Strong buy candidate
   - `0.45–0.65` → Acceptable / neutral
   - `<0.45` → Weak / defensive

Thresholds depend on market regime and should be interpreted **within the distribution**, not absolutely.

---

## 🚨 Issuer Coverage & Alerts

1. **Detection:** If an issuer is not recognized, it is logged automatically.
2. **Reporting:** Unknown issuers generate a `docs/alerts.txt` file.
3. **CI Integration:** GitHub Actions publishes missing issuers in build logs.
4. **Direct Access:**  
   👉 **[Current Unknown Issuers List](https://baol77.github.io/BondReport/alerts.html)**  
   *(404 means database is fully aligned.)*
5. **Resolution:** Add issuer keywords to `IssuerManager` to resolve.

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

This engine is designed to behave like a **real portfolio manager**:

- Yield is attractive, but never blindly.
- FX risk matters more for long maturities and capital-heavy bonds.
- Credit risk is **non-linear** — markets forgive small deterioration, but punish stress brutally.
- Profiles map directly to real investor psychology.

---

## 📌 Next Calibration Steps

If you want to improve further:

- **Calibrate `exp(−spread/600)`** using historical default/spread data.
- **Tune logistic midpoint (0.50–0.60)** to optimize stress sensitivity.
- **Improve λ base calculation** beyond percentile heuristics (e.g., volatility regime detection).

---