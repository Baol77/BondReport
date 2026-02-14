# 📊 Sovereign Bond Analytics Platform

**Discover, analyze, and build custom bond portfolios in seconds. Find the best yields across 30+ countries with intelligent filtering, preset strategies, and real-time portfolio analytics.**

A powerful yet intuitive platform for sovereign bond investors—whether you're a seasoned trader, financial advisor, or individual investor seeking better returns than a savings account.

---

## 🎯 Quick Start (2 Minutes)

### 1. Open the Report
Open `index.html` in your browser. You'll see a table of 1,000+ bonds with filtering options.

### 2. Pick Your Strategy
Click one of 6 preset buttons to instantly filter bonds:

| Button | Best For | What It Shows |
|--------|----------|--------------|
| 📈🔥 **SAY Aggressive** | Risk-takers seeking capital gains | High-return bonds (5%+ SAY), 1-10 year maturity |
| ⚖️🛡️ **Balanced Core** | Balanced growth investors | Investment-grade bonds with solid returns (4.5%+ SAY) |
| 💵🔥 **Max Income** | Income seekers, retirees | High-coupon bonds (6%+ yield), 15+ year maturity |
| 📉🚀 **Deep Discount** | Long-term wealth builders | Bonds below par value (buy low, sell at maturity) |
| 🅿️🛡️ **Cash Parking** | Short-term liquidity needs | Ultra-safe short-term bonds (<1.5 years) |
| 🏰🛡️ **AAA/AA Fortress** | Risk-averse, wealth preservation | Only top-rated sovereigns (AA- or better) |

**That's it!** The table instantly shows 20-50 bonds matching your strategy, ranked by the best opportunities first.

### 3. Review & Act
- ✅ Scroll through the filtered list
- ✅ Check the color-coded heatmap (green = good, red = avoid)
- ✅ Click **"🎯 Portfolio Analysis"** to build a custom portfolio
- ✅ Export your picks to send to your broker

---

## 🔍 Understanding the Bond Table

### What Each Column Means

| Column | What It Is | Why You Care |
|--------|-----------|--------------|
| **ISIN** | Bond identification code | Give this to your broker to buy |
| **Issuer** | Which country issued it | Know who you're lending to (Germany, Italy, Poland, etc.) |
| **Price** | Cost in original currency | What you pay in USD, GBP, CHF, etc. |
| **Currency** | What currency it's issued in | EUR, USD, GBP, CHF, SEK, etc. |
| **Rating** | Credit quality (AAA, BBB+, etc.) | AAA = safest, BB = risky. Higher = safer but lower yield |
| **Price (EUR)** | Cost in euros | Easiest way to compare across currencies |
| **Coupon %** | Annual interest payment | A 5% coupon pays €50/year per €1,000 bond |
| **Maturity** | When you get your money back | 2028 = 2 years, 2035 = 10 years |
| **Curr. Yield %** | Annual income as % of price | €5 income on €100 bond = 5% yield |
| **Total Return (1k€)** | What €1,000 becomes at maturity | €1,000 → €1,300 = €300 profit |
| **SAY (%)** | **Total return per year** | **Most important metric** — includes income + capital gains |

---

## 💡 The Three Metrics You Need to Know

### 1. **SAY (Simple Annual Yield)** — Your Total Return
**This is the single most important number.**

SAY tells you how much money you'll make per year, as a percentage of what you pay today. It includes both coupon income AND capital gains.

**Example**:
- You buy a bond for €100
- It pays €5/year in coupons (5% coupon)
- It's worth €102 when it matures (€2 capital gain over the holding period)
- Total annual return = (€5 + €2/10 years) / €100 = **7% SAY**

**In the table**: Bonds with SAY ≥ 4.5% are highlighted in **green** (good opportunities). Anything under 3% is in **red** (avoid).

### 2. **Current Yield %** — Your Annual Income
How much cash the bond pays you each year, relative to what you're paying today.

**Example**: A bond with a 5% coupon costing €100 = **5% Current Yield**

**Use this if**: You need steady income (retirees). You'll check this column and buy high-yield bonds.

**Don't focus only on this if**: You're investing for growth—SAY is more important because it includes capital appreciation.

### 3. **Maturity** — When You Get Your Money Back
The date the issuer repays your principal.

**Example**:
- Bond matures 2026 = In 2 years, the issuer pays back €100 (or €100+ if you bought at discount)
- Bond matures 2035 = In 10 years, the issuer pays back €100

**Why it matters**:
- **Short (1-3 years)**: Lower risk, lower returns, good for safety
- **Medium (5-10 years)**: Balanced risk/return
- **Long (15+ years)**: Higher returns, more interest rate risk

---

## 🎬 Real Example: Finding Your First Bond

**Your goal**: €5,000 investment, want 4%+ annual return, willing to wait 5-10 years

**Step 1**: Click **⚖️ Balanced Core**
- Filters instantly show ~80 bonds matching this strategy
- All are investment-grade (safe)
- All have 4.5%+ SAY (good returns)
- Maturities between 1-20 years (flexible)

**Step 2**: Sort by SAY (click the "SAY" column header)
- Best opportunities appear at top
- You see:
  ```
  ROMANIA (XS2571924070)     — Price €96, SAY 5.2%, Rating BBB-, 2031
  POLAND (PL0000123456)       — Price €99, SAY 4.8%, Rating A,    2033
  HUNGARY (HU1122334455)      — Price €98, SAY 4.9%, Rating BBB+, 2032
  ```

**Step 3**: Review color coding
- Green background = SAY 3.5%+ (good)
- Light green = SAY 2.5-3.5% (ok)
- Red = SAY < 2.5% (skip)

**Step 4**: Check the rating
- BBB- = Investment Grade, but risky side. OK for growth.
- A = Safer. Less return but more stable.

**Step 5**: Export or add to portfolio
- Send to your broker with €5,000
- Or add to portfolio analyzer for "what if" scenarios

---

## 🎯 Portfolio Analyzer — Build Your Own Portfolio

Click **"🎯 Portfolio Analysis"** button to open the portfolio builder. This is where you model your actual investment.

### How to Use It

#### Step 1: Search for a Bond
1. Enter ISIN code (e.g., `XS2571924070`)
2. Click **🔍 Search**
3. Bond details appear automatically

#### Step 2: Add to Portfolio
Choose ONE of these:
- **Enter Quantity**: "I want 10 units"
- **Enter € Amount**: "I want to invest €5,000"

The tool auto-calculates the other. Click **➕ Add to Portfolio**.

#### Step 3: Add More Bonds
Repeat steps 1-2 to add 3-10 different bonds (diversification is key).

#### Step 4: Review Your Portfolio Dashboard

The portal shows 8 key statistics:

| Stat | What It Means | Example |
|------|---------------|---------|
| **Total Investment** | How much money you're putting in | €10,500 |
| **Avg Price** | Average € price you're paying per bond | €98.50 |
| **Weighted SAY** | Your portfolio's total return | **4.32%** |
| **Weighted Yield** | Your portfolio's annual income | 2.85% |
| **Avg Coupon** | Average interest rate you're getting | 3.75% |
| **Bond Count** | How many different bonds you own | 5 |
| **Avg Risk (Maturity)** | How long till you get your money | 7.2 years |
| **Weighted Rating** | Average credit quality | BBB+ |

**Real example**:
```
Portfolio: 5 bonds worth €10,500
├─ Total Investment: €10,500 (your money in)
├─ Weighted SAY: 4.32% (you make 4.32%/year)
├─ Weighted Rating: BBB+ (mostly safe, some risk)
└─ Avg Maturity: 7.2 years (medium-term holding)
```

#### Step 5: Currency Breakdown
See how much you own in each currency:
```
EUR    65%  €6,820
USD    25%  €2,625
GBP    10%  €1,050
```

**Why this matters**: If 90% is USD and the euro strengthens, your returns drop. Use this to balance currency exposure.

#### Step 6: Manage Your Portfolio
- **Edit quantities**: Click the Qty field, change the number, watch stats update instantly
- **Delete bonds**: Click ❌ to remove a bond
- **Test scenarios**: Add/remove bonds to see "what if" results

#### Step 7: Save Your Portfolio
**Export (📥)**: Save as CSV file
- Backup for later
- Share with your financial advisor
- Open in Excel for further analysis

**Import (📤)**: Load a previously saved portfolio
- Automatically updates prices to today's market values
- Shows which bonds moved up/down
- Recalculates all statistics

**Example alert when importing**:
```
✅ Imported 3 bonds!

📊 Price Changes Since You Saved:
XS2571924070: €96.50 → €98.75 (↑ +€2.25)
US0378331005: €105.00 → €103.50 (↓ -€1.50)
```

**Clear (🗑️)**: Delete entire portfolio and start fresh

---

## 🎨 Two Analysis Modes

### Capital Gain Mode (Default)
**Focus**: Total return (SAY)  
**Color coding**:
- 🔴 Red = SAY < 1% (avoid)
- 🟠 Yellow = SAY 1-2.5% (poor)
- 🟢 Light Green = SAY 2.5%+ (good)
- 🟢 Dark Green = SAY 4%+ (excellent)

**Best for**: Growth investors, reinvesting coupons, long-term wealth building

### Income Mode
**Focus**: Annual cash flow (Current Yield)  
**Color coding**:
- 🔴 Red = Yield < 3% (too low)
- 🟠 Yellow = Yield 3-4.5% (acceptable)
- 🟢 Light Green = Yield 4.5%+ (good)
- 🟢 Dark Green = Yield 6%+ (excellent)

**Best for**: Income investors, retirees needing cash, living off bond yields

**Toggle between modes** using the legend at the bottom of the page.

---

## 🔧 Advanced: Custom Filters

Don't like the presets? Build your own filter in 3 clicks.

### Filter Options (Click column headers)

#### **ISIN Search**
Type a bond code to find a specific bond:
```
XS2571924070  →  Shows only that bond
```

#### **Issuer Search**
Filter by country:
```
ROMANIA  →  Shows only Romanian bonds
GERMANY  →  Shows only German bonds
```

#### **Price Range**
Set min/max price to find bonds at specific price points:
```
Min: 80    Max: 110   →  Shows bonds trading €80-€110
Min: 90    Max: 95    →  Shows deep discounts
```

#### **Currency**
Choose specific currencies:
```
EUR  →  Only euro bonds (no FX risk)
USD  →  Only dollar bonds (FX-adjusted returns)
```

#### **Minimum Rating**
Filter by credit safety:
```
≥ BBB   →  Investment grade only (safer)
≥ A     →  High quality (very safe, lower yield)
```

#### **Maturity Range**
Choose bond duration:
```
2025-2027   →  Short-term (1-3 years)
2030-2035   →  Medium-term (5-10 years)
2045-2050   →  Long-term (20+ years)
```

#### **Minimum Yield**
Set income floor:
```
Min 5%  →  Only high-income bonds
Min 4%  →  Moderate income bonds
```

#### **Minimum SAY**
Filter by total return:
```
Min 4.5%  →  Only bonds with 4.5%+ annual return
Min 3.5%  →  Broader selection
```

### Build a Custom Filter Example

**Goal**: "I want Italian bonds, investment-grade, 6-10 year maturity, at least 4% SAY"

1. Issuer → Type `ITALY`
2. Rating → Select `≥ BBB+`
3. Maturity → Set dates for 2030-2034
4. SAY → Set minimum `4.0%`

**Result**: 5-8 bonds matching your exact criteria, ranked by SAY

---

## 💾 Portfolio Features Explained

### Editable Quantities
Click on any Qty in your portfolio to change it instantly. The dashboard recalculates everything automatically.

**Use this for**: "What if I bought 20 units instead of 10?" testing

### Duplicate Detection
If you accidentally add the same bond twice, the system shows a 🔄 merge button. Click it to combine entries with a weighted-average cost basis.

### Smart Import
When you reimport a saved portfolio:
1. Quantities stay the same
2. Prices update to current market
3. All statistics recalculate
4. You see which bonds moved up/down

**Example**:
```
Saved 3 months ago at: €96.50
Today's price:         €98.75
Your bonds made:       +€2.25 per unit!
```

### LocalStorage Persistence
Your portfolio auto-saves to your browser. Even if you close and reopen tomorrow, your portfolio is still there.

**Note**: Portfolio data never leaves your computer. It's stored only locally in your browser.

---

## 🌍 FX Risk Explained

When you buy a **USD bond**, you're taking **currency risk**. The platform automatically adjusts returns to account for this.

### How It Works

A 7-year USD bond might show:
- Nominal SAY: 5.5% (before FX adjustment)
- Adjusted SAY: 4.8% (after FX risk haircut)

The adjustment is **maturity-based**:

| Maturity | Coupon Haircut | Capital Haircut |
|----------|---|---|
| 0-5 years | 5% | 10% |
| 5-10 years | 7.5% | 15% |
| 10-15 years | 10% | 20% |
| 15-20 years | 12.5% | 25% |
| 20+ years | 15% | 30% |

**Translation**: Longer-duration bonds get bigger FX haircuts because currency risk compounds over time.

**Bottom line**: The numbers you see are **realistic FX-adjusted returns**, not optimistic nominal returns.

---

## 🎓 Pro Tips for Better Investing

### Tip 1: Diversify Across Ratings
Don't buy only AAA bonds (boring yields). Mix:
- 60% A-rated bonds (good returns, low risk)
- 40% BBB-rated bonds (better returns, acceptable risk)
- **Result**: 4-5% portfolio SAY with reasonable safety

### Tip 2: Diversify Across Maturities
Avoid buying only 10-year bonds. Mix:
- 30% Short (1-3 years) - Quick cash back, reinvestment options
- 40% Medium (5-10 years) - Balanced duration
- 30% Long (15+ years) - High returns, long-term wealth
- **Result**: Smooth cash flow + flexibility

### Tip 3: Mix Currencies
If you're all-in EUR and the euro weakens, your returns suffer. Mix:
- 70% EUR (home currency)
- 20% USD (major currency, diversification)
- 10% GBP/CHF (minor diversification)

### Tip 4: Use Presets as Starting Points
The 6 presets aren't rigid rules. Use them to get 20-30 candidate bonds, then:
1. Read about the top 5 issuers
2. Check recent news
3. Customize your own portfolio
4. Export and send to your broker

### Tip 5: Export, Review, Act
1. Build portfolio in the analyzer
2. Export to CSV
3. Review for 24 hours
4. Send to broker to execute
5. Never buy on impulse

### Tip 6: Track Price Changes
Every quarter, reimport your saved portfolio. The system shows you which bonds:
- Went up (winners)
- Went down (potential rebalancing opportunities)
- Stayed flat (boring but stable)

### Tip 7: Check Currency Breakdown Quarterly
If 80% of your portfolio is suddenly USD (because you kept adding USD bonds), rebalance back to your target allocation.

---

## ❓ Common Questions

### "Which preset should I use?"

**Use this flowchart**:
```
Do you need money in 1-2 years?
├─ YES → Cash Parking 🅿️
└─ NO ↓

Are you retired/need income?
├─ YES → Max Income 💵
└─ NO ↓

Do you want maximum safety?
├─ YES → AAA/AA Fortress 🏰
└─ NO ↓

Do you want balanced returns?
├─ YES → Balanced Core ⚖️
└─ NO ↓

Do you want maximum returns?
├─ YES → SAY Aggressive 📈 OR Deep Discount 📉
```

### "I'm new to bonds. Where do I start?"

1. Click **⚖️ Balanced Core** (good for everyone)
2. Pick top 5 bonds by SAY
3. Add to portfolio analyzer
4. Review statistics
5. Export and show to your advisor

### "Should I buy the highest SAY bond?"

**Not always.** High SAY often comes with:
- Lower credit rating (riskier)
- Non-EUR currency (FX risk)
- Longer maturity (interest rate risk)

**Better approach**: Build a portfolio with 5-10 bonds diversified across rating/maturity/currency.

### "How often does data update?"

Check the timestamp at the top of the page. Your administrator controls update frequency (could be daily, weekly, or real-time).

### "Can I use this on my phone?"

Yes! The app is fully mobile-responsive:
- **Portrait**: Single-column layout, easy scrolling
- **Landscape**: Compact view, still usable
- **Desktop**: Full view with all columns

### "My portfolio doesn't save. Why?"

Check:
1. Is localStorage enabled? (Browser settings → Privacy)
2. Are you in incognito mode? (Doesn't support storage)
3. Is your browser cache cleared? (Try closing/reopening)
4. Try a different browser?

### "Can I share my portfolio with my advisor?"

Yes! Click **📥 Export CSV**, email the file to your advisor. They can:
- Review in Excel
- Make notes
- Send back for confirmation
- Track changes over time

### "What if prices change after I export?"

When you **reimport**, the system automatically updates prices to today's market. You'll see which bonds moved and recalculate your returns.

---

## 📈 Real-World Walkthrough: Building Your First Portfolio

**Scenario**: You have €15,000 to invest for 8 years. You want 4%+ annual return with moderate risk.

### Step 1: Pick a Preset
Click **⚖️ Balanced Core**
- Shows ~100 bonds fitting your strategy
- All investment-grade (safe)
- 4.5%+ SAY (good returns)

### Step 2: Sort by SAY
Click the SAY column header to sort best-to-worst

Top candidates:
```
1. ROMANIA (XS2571924070)    €96.00    BBB-    5.2%    2031
2. POLAND (PL0000000001)     €99.00    A       4.8%    2033
3. HUNGARY (HU1122334455)    €98.00    A-      4.9%    2032
4. CZECHIA (CZ0000000001)    €102.00   A       4.5%    2030
5. CROATIA (HR1111111111)    €94.00    BBB+    5.1%    2031
```

### Step 3: Build Diversified Portfolio
Add 5 different bonds (not all from same country):
```
Bond 1: ROMANIA      €3,000  (Higher yield, bit riskier)
Bond 2: POLAND       €3,000  (Good balance)
Bond 3: CZECHIA      €3,000  (Safe, good yield)
Bond 4: HUNGARY      €3,000  (Good balance)
Bond 5: GERMANY      €3,000  (Ultra-safe, lower yield)
Total:               €15,000
```

### Step 4: Check Dashboard
```
Total Investment:    €15,000
Weighted SAY:        4.62%  ← Your average return
Weighted Rating:     A-     ← Average credit quality
Avg Maturity:        8.2 yrs ← Good match for your timeline
```

Currency breakdown:
```
EUR    80%  €12,000
CHF    20%  €3,000
```

### Step 5: Export
Click **📥 Export CSV**, get a file like:
```
ISIN,Issuer,Price EUR,Quantity,Investment EUR,SAY %,...
XS2571924070,ROMANIA,96.00,31.25,3000.00,5.2%,...
PL0000000001,POLAND,99.00,30.30,3000.00,4.8%,...
...
```

### Step 6: Send to Broker
Email to your broker with message:
```
Hi,

Please execute the following bond purchases (attached CSV):
- 31 units of XS2571924070 (Romania)
- 30 units of PL0000000001 (Poland)
- etc.

Total investment: €15,000

Thanks!
```

### Step 7: Quarterly Check-In
Set reminder for 3 months later:
1. Open portfolio CSV
2. Click **📤 Import CSV**
3. System updates prices
4. See which bonds moved:
   ```
   ✅ ROMANIA: €96.00 → €98.50 (+€2.50 gain!)
   ❌ GERMANY: €102.00 → €100.50 (-€1.50)
   ```
5. Rebalance if needed

**Done!** You've built a professional bond portfolio in 20 minutes.

---

## 🎯 Key Features Summary

✅ **1,000+ bonds** across 30+ countries  
✅ **6 instant presets** for quick starts  
✅ **Advanced filtering** for custom strategies  
✅ **SAY analysis** focused on total returns  
✅ **FX-adjusted** returns for realistic expectations  
✅ **Portfolio builder** with real-time stats  
✅ **CSV export/import** for tracking  
✅ **Mobile responsive** (phone, tablet, desktop)  
✅ **Currency breakdown** for FX risk analysis  
✅ **Dual analysis modes** (capital gain / income)  
✅ **Price editing** in portfolio for "what if" scenarios  
✅ **Zero cost** — completely free to use  
✅ **No login needed** — just open and use  
✅ **Data stays local** — nothing sent to servers

---

## 📞 Getting Help

### If Portfolio Doesn't Save
→ Check browser localStorage is enabled (Settings → Privacy)

### If Search Doesn't Find Bonds
→ Verify ISIN is spelled correctly (copy/paste from table)

### If Filters Don't Work
→ Click column header and verify filter is entered

### If Numbers Look Wrong
→ Check if currency is EUR or needs FX adjustment

### If You Need More Help
→ Ask your administrator or financial advisor

---

## 🚀 Next Steps

1. **Right now**: Open the platform and click a preset
2. **In 5 minutes**: Add 5 bonds to your portfolio
3. **In 15 minutes**: Export your portfolio and review
4. **Next week**: Send to your broker or advisor
5. **Every 3 months**: Reimport to track performance

---

## 💡 Remember

- **SAY is your friend** — It shows total return, not just coupons
- **Diversify** — Mix ratings, maturities, and currencies
- **Use presets first** — They save you from analysis paralysis
- **Export everything** — Keep records of your research
- **Review quarterly** — Markets change, so do opportunities

---

**Ready to build your bond portfolio?**

**Open the platform. Click a preset. Find your next investment. 🎯**

---

*Last Updated: February 2026*  
*Version: 5.0 — With Portfolio Analyzer*  
*For Investors, By Investors* 📊