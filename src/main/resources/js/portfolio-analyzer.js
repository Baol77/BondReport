// Portfolio Analyzer - Client-Side Portfolio Management
// Fixed version: draggable modal, CSV import/export, corrected weighted calculations
// Embedded in FreeMarker template via <#include "portfolio-analyzer.js" parse=false>
// No backend required - uses browser localStorage

class PortfolioAnalyzer {
    constructor() {
        this.portfolio = [];
        this.allBonds = [];
        this.modal = null;
        this.currentBond = null;
        this.init();
    }

    init() {
        // Load saved portfolio from localStorage
        const saved = localStorage.getItem('bondPortfolio');
        if (saved) {
            try {
                this.portfolio = JSON.parse(saved);
            } catch (e) {
                console.warn('Failed to load portfolio:', e);
                this.portfolio = [];
            }
        }

        // Extract bonds from the existing table
        this.loadBondsFromTable();

        // Create modal interface
        this.createModal();

        console.log(`📊 Portfolio Analyzer initialized with ${this.allBonds.length} bonds`);
    }

    loadBondsFromTable() {
        const table = document.querySelector('table#bondTable');
        if (!table) {
            console.error('Bond table not found');
            return;
        }

        const rows = table.querySelectorAll('tbody tr');
        rows.forEach(row => {
            const cells = row.querySelectorAll('td');
            if (cells.length >= 10) {
                this.allBonds.push({
                    isin: cells[0].textContent.trim(),
                    issuer: cells[1].textContent.trim(),
                    price: parseFloat(cells[2].textContent.trim().replace(",",".")),
                    currency: cells[3].textContent.trim(),
                    rating: cells[4].textContent.trim(),
                    priceEur: parseFloat(cells[5].textContent.trim().replace(",",".")),
                    coupon: parseFloat(cells[6].textContent.trim().replace(",",".")),
                    maturity: cells[7].textContent.trim(),
                    currentYield: parseFloat(cells[8].textContent.trim().replace(",",".")),
                    capitalAtMat: parseFloat(cells[9].textContent.trim()),
                    say: cells.length > 10 ? parseFloat(cells[10].textContent.trim().replace(",",".")) : 0,
                    includeInStatistics: true
                });
            }
        });
    }

    createModal() {
        this.modal = document.getElementById('portfolioModal');

        if (!this.modal) {
           console.error('Portfolio modal not found in DOM. Did you include portfolio-modal.ftl?');
           return;
        }

        this.setupDragging();
    }

    setupDragging() {
        const modalContent = document.getElementById('modalContent');
        const modalHeader = document.getElementById('modalHeader');

        let isDragging = false;
        let offsetX = 0;
        let offsetY = 0;
        let minY = 20; // Minimum distance from top

        modalHeader.addEventListener('mousedown', (e) => {
            isDragging = true;
            offsetX = e.clientX - modalContent.offsetLeft;
            offsetY = e.clientY - modalContent.offsetTop;
            modalHeader.style.cursor = 'grabbing';
        });

        document.addEventListener('mousemove', (e) => {
            if (!isDragging) return;

            modalContent.style.position = 'fixed';
            modalContent.style.left = (e.clientX - offsetX) + 'px';

            // Constrain vertical movement - don't allow window to go too far up
            let newTop = e.clientY - offsetY;
            newTop = Math.max(minY, newTop); // Keep at least 20px from top
            newTop = Math.max(0, newTop); // Never negative

            modalContent.style.top = newTop + 'px';
            modalContent.style.margin = '0';
        });

        document.addEventListener('mouseup', () => {
            isDragging = false;
            modalHeader.style.cursor = 'move';
        });
    }

    openModal() {
        if (this.modal) {
            this.modal.style.display = 'flex';
            this.updatePortfolioTable();
            this.updateStatistics();

            document.getElementById('searchResults').style.display = "none";
        }
    }

    closeModal() {
        if (this.modal) {
            this.modal.style.display = 'none';
            this.clearSearch();
        }
    }

    searchBond() {

        const input = document.getElementById('isinSearch');
        const query = input.value.toLowerCase().trim();

        const resultsContainer = document.getElementById('searchResults');

        if (!query) {
            resultsContainer.innerHTML = '';
            resultsContainer.style.display = "none";
            return;
        }

        // --- normalize helper ---
        const normalize = s =>
            String(s)
                .toLowerCase()
                .replace(',', '.')
                .replace('%', '')
                .trim();

        // --- split words ---
        // --- split tokens ---
        const tokens = query
            .split(/\s+/)
            .map(t => t.trim())
            .filter(t => t.length > 0);

        // --- classify tokens ---
        const percentTokens = [];
        const textTokens = [];

        tokens.forEach(t => {
            const normalized = t.replace(',', '.');
            // nombre avec ou sans %
            if (/^\d+(\.\d*)?%?$/.test(normalized)) {
                percentTokens.push(parseFloat(normalized));
            } else {
                textTokens.push(normalize(t));
            }
        });


        const matches = this.allBonds.filter(bond => {
            const coupon = Number(bond.coupon);

            // --- coupon match (optional) ---
            let couponMatch = true;
            if (percentTokens.length > 0) {
                couponMatch = percentTokens.every(p => {
                    if (Number.isInteger(p)) {
                        return Math.floor(coupon) === p;
                    }

                    const decimals = (String(p).split('.')[1] || '').length;
                    const factor = 10 ** decimals;

                    return Math.floor(coupon * factor) === Math.floor(p * factor);
                });
            }

            // --- text match (optional) ---
            let textMatch = true;
            if (textTokens.length > 0) {
                const searchable = normalize([
                    bond.isin,
                    bond.issuer
                ].join(' '));

                textMatch = textTokens.every(t => searchable.includes(t));
            }

            return couponMatch && textMatch;
        });

        this.showSearchResults(matches);
    }

    showSearchResults = function (matches) {

        const container = document.getElementById("searchResults");
        container.innerHTML = "";

        if (!matches || matches.length === 0) {
            container.innerHTML = `
                <div class="search-no-results">
                    No bond found
                </div>
            `;
            container.style.display = "block";
            return;
        }

        // limit results (important UX)
        const MAX_RESULTS = 8;
        const resultsToShow = matches.slice(0, MAX_RESULTS);

        resultsToShow.forEach(bond => {

            const row = document.createElement("div");
            row.className = "search-result";

            row.innerHTML = `
                <div class="sr-main">
                    <strong>${bond.issuer}</strong>
                    ${bond.coupon}%
                    ${this.formatDate(bond.maturity)}
                </div>
                <div class="sr-meta">
                    ${bond.isin} - ${bond.price}${bond.currency}
                </div>
            `;

            row.onclick = () => {
                this.showAddBondForm(bond);
                container.innerHTML = "";
                container.style.display = "none";
            };

            container.appendChild(row);
        });

        // Show message in case the results are too many
        if (matches.length > MAX_RESULTS) {
            const remaining = matches.length - MAX_RESULTS;

            const moreMsg = document.createElement("div");
            moreMsg.className = "search-results-more";
            moreMsg.textContent = `${remaining} more result${remaining > 1 ? "s are" : " is"} not shown`;

            container.appendChild(moreMsg);
        }

        container.style.display = "block";
    };

    formatDate = function (dateStr) {
        const d = new Date(dateStr);
        return d.toLocaleDateString();
    };


    handleSearch() {
        this.searchBond();
    }

    showAddBondForm(bond) {
        this.currentBond = bond;

        const detailsDiv = document.getElementById('bondDetails');
        detailsDiv.innerHTML = `
            <strong>${bond.issuer}</strong><br>
            ISIN: <i>${bond.isin}</i><br>
            Maturity: <i>${bond.maturity}</i><br>
            Price: <i>${bond.currency} ${bond.price.toFixed(2)}${bond.currency !== 'EUR' ? ` (€ ${bond.priceEur.toFixed(2)})` : ''}</i><br>
            Rating: <i>${bond.rating}</i> | Coupon: <i>${bond.coupon.toFixed(2)}%</i> | SAY: <i>${bond.say.toFixed(2)}%</i>
        `;

        // Reset fields
        document.getElementById('quantity').innerText = '0';
        document.getElementById('amount').value = '';

        const originalWrapper = document.getElementById('originalCurrencyWrapper');
        const originalLabel = document.getElementById('originalCurrencyLabel');
        const originalInput = document.getElementById('amountOriginal');

        if (bond.currency === 'EUR') {
            originalWrapper.style.display = 'none';
            originalInput.value = '';
        } else {
            originalWrapper.style.display = 'flex';
            originalLabel.textContent = bond.currency;
            originalInput.value = '';
        }

        // Update displayed total live (only EUR total shown)
        document.getElementById('amount').addEventListener('input', () => {
            const eur = parseFloat(document.getElementById('amount').value.replace(",",".")) || 0;
            this.updateGrossQuantity();
        });

       // Call alignment logic
       this.alignTotalAmounts();

       document.getElementById('addBondForm').style.display = 'block';
    }

    alignTotalAmounts() {
        if (!this.currentBond) return;

        const eurInput = document.getElementById('amount');
        const originalInput = document.getElementById('amountOriginal');

        if (!eurInput) return;

        const bond = this.currentBond;

        // If EUR bond → only update total display
        if (bond.currency === 'EUR') {
            eurInput.oninput = () => {
                const eur = parseFloat(eurInput.value) || 0;
                 this.updateGrossQuantity();
            };
            return;
        }

        const fxRate = bond.priceEur / bond.price;

        // EUR → Original
        eurInput.oninput = () => {
            const eur = parseFloat(eurInput.value) || 0;
            const original = eur / fxRate;

            originalInput.value = original.toFixed(2);

            this.updateGrossQuantity();
        };

        // Original → EUR
        originalInput.oninput = () => {
            const original = parseFloat(originalInput.value) || 0;
            const eur = original * fxRate;

            eurInput.value = eur.toFixed(2);

            this.updateGrossQuantity();
        };
    }

    updateGrossQuantity() {
        if (!this.currentBond) return;

        const eurInput = document.getElementById('amount');
        const grossInfo = document.getElementById('quantity');

        const eur = parseFloat(eurInput.value) || 0;

        if (eur === 0) {
            grossInfo.innerText = '0';
            return;
        }

        const grossQty = eur / this.currentBond.priceEur;

        grossInfo.innerText = `${grossQty.toFixed(2)}`;
    }

    addBondToPortfolio() {
        if (!this.currentBond) return;

        const qty = parseFloat(document.getElementById('quantity').innerText.replace(",",".")) || 1;

        // Get invested EUR
        const totalEur = parseFloat(
            document.getElementById('amount')
                .value
                .replace(/[^\d.-]/g, '')
        ) || 0;

        // Get original currency total (if exists)
        const totalOriginalField = document.getElementById('totalOriginal');
        const totalOriginal = totalOriginalField
            ? parseFloat(totalOriginalField.value) || 0
            : totalEur;

        // Keep it simple
        this.portfolio.push({
            ...this.currentBond,
            quantity: qty,
            totalEur: totalEur,
            totalOriginal: totalOriginal,
            includeInStatistics: true
        });

        this.savePortfolio();
        this.updatePortfolioTable();
        this.updateStatistics();

        document.getElementById('isinSearch').value = '';
        document.getElementById('addBondForm').style.display = 'none';
        document.getElementById('searchResults').innerHTML = '';

        this.currentBond = null;

        alert(`✅ Bond added! Quantity added: ${qty}`);
    }

    removeBond(index) {
        this.portfolio.splice(index, 1);
        this.savePortfolio();
        this.updatePortfolioTable();
        this.updateStatistics();
    }

    updateQuantityInPortfolio(index, newQuantity) {
        const qty = parseFloat(newQuantity);

        if (isNaN(qty) || qty < 1) {
            alert('Quantity must be at least 1');
            this.updatePortfolioTable();
            return;
        }

        const bond = this.portfolio[index];

        // Calculate unit cost BEFORE changing quantity
        const unitCost = bond.totalEur / bond.quantity;

        // Update quantity
        bond.quantity = qty;

        // Scale invested proportionally
        bond.totalEur = unitCost * qty;

        this.savePortfolio();
        this.updatePortfolioTable();
        this.updateStatistics();
    }

    mergeBond(isin) {
        const matches = this.portfolio.filter(b => b.isin === isin);
        if (matches.length < 2) return;

        // REAL invested capital (what user paid)
        const totalInvested = matches.reduce(
            (sum, b) => sum + (b.totalEur || 0),
            0
        );

        // Total quantity
        const totalQty = matches.reduce(
            (sum, b) => sum + b.quantity,
            0
        );

        // True weighted average cost basis
        const weightedAvgPrice = totalQty > 0
            ? totalInvested / totalQty
            : 0;

        // Keep latest market data
        const latestData = matches[matches.length - 1];

        // Remove old entries
        this.portfolio = this.portfolio.filter(b => b.isin !== isin);

        // Push consolidated bond
        this.portfolio.push({
            ...latestData,
            quantity: totalQty,
            totalEur: totalInvested,        // VERY IMPORTANT
            priceEur: latestData.priceEur   // keep current market price
        });

        this.savePortfolio();
        this.updatePortfolioTable();
        this.updateStatistics();

        console.log(
            `Consolidated ${matches.length} entries for ${isin}.
             New Avg Cost: €${weightedAvgPrice.toFixed(2)}`
        );
    }

    updatePortfolioTable() {
        const tbody = document.getElementById('portfolioTableBody');
        const empty = document.getElementById('emptyPortfolioMsg');

        if (this.portfolio.length === 0) {
            tbody.innerHTML = '';
            empty.style.display = 'block';
            return;
        }
        empty.style.display = 'none';

        // Count ISINs to identify which ones need a merge button
        const isinCounts = this.portfolio.reduce((acc, b) => {
            acc[b.isin] = (acc[b.isin] || 0) + 1;
            return acc;
        }, {});

        tbody.innerHTML = this.portfolio.map((bond, idx) => {
            const hasDuplicates = isinCounts[bond.isin] > 1;

            const currentValueEur = bond.quantity * bond.priceEur;
            const gainLoss = Math.round(currentValueEur - bond.totalEur);

            return `<tr style="border-bottom:1px solid #eee;">
                <td>${bond.isin}</td>
                <td>${bond.issuer}</td>
                <td>€${bond.priceEur.toFixed(2)}</td>
                <td>${bond.currency}</td>
                <td>${bond.rating}</td>
                <td>
                    <input type="number"
                           value="${bond.quantity}"
                           min="0.01"
                           step="0.01"
                           onchange="window.portfolioAnalyzer.updateQuantityInPortfolio(${idx}, this.value)"
                           style="width:45px;padding:4px;font-size:12px;">
                </td>

                <td>€${(bond.totalEur ?? 0).toFixed(2)}</td>
                <td style="white-space: nowrap;">${bond.maturity}</td>
                <td>${bond.currentYield.toFixed(2)}%</td>
                <td>${bond.say.toFixed(2)}%</td>
                <td class="${gainLoss >= 0 ? 'good' : 'bad'}">
                    ${gainLoss}
                </td>
                <td>
                    <input type="checkbox" title="Toggle to include/exclude this bond from statistics calculations"
                           ${bond.includeInStatistics ? 'checked' : ''}
                           onchange="window.portfolioAnalyzer.toggleStatistics(${idx})">
                </td>
                <td>
                   <div style="display:flex;justify-content:flex-end;align-items:center;gap:10px;width:100%;">
                       ${hasDuplicates ? `<span onclick="window.portfolioAnalyzer.mergeBond('${bond.isin}')" title="Merge duplicates" style="cursor:pointer;font-size:18px;transition:opacity 0.15s ease;" onmouseover="this.style.opacity='0.6'" onmouseout="this.style.opacity='1'">🔄</span>` : ''}
                       <span onclick="window.portfolioAnalyzer.removeBond(${idx})" title="Delete bond" style="cursor:pointer;font-size:18px;transition:opacity 0.15s ease;" onmouseover="this.style.opacity='0.6'" onmouseout="this.style.opacity='1'">❌</span>
                   </div>
                </td>
            </tr>`;
        }).join('');
    }

    toggleAllStatistics(checked) {
        this.portfolio.forEach(b => {
            b.includeInStatistics = checked;
        });

        this.savePortfolio();
        this.updatePortfolioTable();
        this.updateStatistics();
    }

    toggleStatistics(index) {
        this.portfolio[index].includeInStatistics =
            !this.portfolio[index].includeInStatistics;

        this.savePortfolio();
        this.updateStatistics();
    }

    updateStatistics() {
        if (this.portfolio.length === 0) {
            document.getElementById('statTotalInvestment').textContent = '€0.00';
            document.getElementById('statAvgPrice').textContent = '€0.00';
            document.getElementById('statWeightedSAY').textContent = '0.00%';
            document.getElementById('statWeightedYield').textContent = '0.00%';
            document.getElementById('statAvgCoupon').textContent = '0.00%';
            document.getElementById('statBondCount').textContent = '0';
            document.getElementById('statWeightedRisk').textContent = '0.00 yrs';
            document.getElementById('statWeightedRating').textContent = '-';
            document.getElementById('currencyBreakdown').innerHTML = '';
            document.getElementById('statTotalProfit').textContent = '€0';
            document.getElementById('statTotalCouponIncome').textContent = '€0.00';
            return;
        }

        let totalInvestment = 0;
        let totalInvestment1 = 0;
        let weightedSAY = 0;
        let weightedYield = 0;
        let weightedCoupon = 0;
        let weightedRisk = 0;
        let currencyTotals = {}; // Track investment by currency
        let totalProfit = 0;
        let totalCouponIncome = 0;

        const bonds = this.portfolio.filter(b => b.includeInStatistics);
        bonds.forEach(bond => {

            const currentValue = bond.priceEur * bond.quantity;   // market value
            const investedAmount = bond.totalEur || 0;            // what you paid

            totalInvestment += investedAmount;
            totalInvestment1 += currentValue;

            weightedSAY += (bond.say * currentValue);
            weightedYield += (bond.currentYield * currentValue);
            weightedCoupon += (bond.coupon * currentValue);

            // TOTAL PROFIT (correct now)
            totalProfit += (currentValue - investedAmount);

            // TOTAL COUPON INCOME (ANNUAL, IN EUR)
            const nominal = bond.nominal || 100;
            const annualCouponOriginal = (bond.coupon / 100) * nominal * bond.quantity;

            const annualCouponEur = bond.currency === 'EUR'
                ? annualCouponOriginal
                : annualCouponOriginal * (bond.priceEur / bond.price);

            totalCouponIncome += annualCouponEur;

            // Risk (years to maturity)
            const maturityDate = new Date(bond.maturity);
            const today = new Date();
            const yearsToMaturity =
                (maturityDate - today) / (365.25 * 24 * 60 * 60 * 1000);

            weightedRisk += (Math.max(0, yearsToMaturity) * currentValue);

            // Currency breakdown
            if (!currencyTotals[bond.currency]) {
                currencyTotals[bond.currency] = 0;
            }
            currencyTotals[bond.currency] += currentValue;

        });

        const totalQty = bonds.reduce((sum, b) => sum + b.quantity, 0);
        const avgPrice = totalInvestment / totalQty;

        const weightedSAYPercent = (weightedSAY / totalInvestment1);
        const weightedYieldPercent = (weightedYield / totalInvestment1);
        const weightedCouponPercent = (weightedCoupon / totalInvestment1);
        const weightedRiskYears = (weightedRisk / totalInvestment1);

        // Calculate weighted average rating (using rating order)
        const ratingOrder = ['AAA', 'AA+', 'AA', 'AA-', 'A+', 'A', 'A-', 'BBB+', 'BBB', 'BBB-', 'BB+', 'BB', 'BB-', 'B+', 'B', 'B-', 'CCC', 'CC', 'C', 'D'];
        let weightedRatingScore = 0;
        bonds.forEach(bond => {
            const marketValue = bond.priceEur * bond.quantity;
            const ratingIndex = ratingOrder.indexOf(bond.rating);
            const ratingScore = ratingIndex >= 0 ? ratingIndex : 20; // Default to lowest if not found
            weightedRatingScore += (ratingScore * marketValue);
        });
        const avgRatingScore = weightedRatingScore / totalInvestment1;
        const weightedRating = ratingOrder[Math.round(avgRatingScore)] || '-';

        totalInvestment = Math.round(totalInvestment);
        document.getElementById('statTotalInvestment').textContent = `€${totalInvestment}`;
        document.getElementById('statAvgPrice').textContent = `€${avgPrice.toFixed(2)}`;
        document.getElementById('statWeightedSAY').textContent = `${weightedSAYPercent.toFixed(2)}%`;
        document.getElementById('statWeightedYield').textContent = `${weightedYieldPercent.toFixed(2)}%`;
        document.getElementById('statAvgCoupon').textContent = `${weightedCouponPercent.toFixed(2)}%`;
        const uniqueISINs = new Set(bonds.map(b => b.isin));
        document.getElementById('statBondCount').textContent = uniqueISINs.size;
        document.getElementById('statWeightedRisk').textContent = `${weightedRiskYears.toFixed(2)} yrs`;
        document.getElementById('statWeightedRating').textContent = weightedRating;

        // Total Profit
        totalProfit = Math.round(totalProfit);
        const profitElement = document.getElementById('statTotalProfit');
        if (profitElement) {
            profitElement.textContent = `€${totalProfit}`;
            profitElement.style.color = totalProfit >= 0 ? '#4CAF50' : '#f44336';
        }

        // Total Coupon Income (Current Year)
        totalCouponIncome = Math.round(totalCouponIncome);
        const couponElement = document.getElementById('statTotalCouponIncome');
        if (couponElement) {
            couponElement.textContent = `€${totalCouponIncome}`;
        }

        // Display currency breakdown
        this.updateCurrencyBreakdown(currencyTotals, totalInvestment1);
    }

    updateCurrencyBreakdown(currencyTotals, totalInvestment) {
        const breakdown = document.getElementById('currencyBreakdown');
        const currencies = Object.keys(currencyTotals).sort();

        breakdown.innerHTML = currencies.map(currency => {
            const amount = Math.round(currencyTotals[currency]);
            const percentage = (amount / totalInvestment * 100);
            return `
                <div style="background:white;padding:10px;border-radius:4px;border-left:4px solid #4CAF50;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
                    <div style="font-size:11px;color:#666;font-weight:600;margin-bottom:6px;">${currency}</div>
                    <p style="margin:0;font-size:14px;font-weight:bold;color:#4CAF50;">${percentage.toFixed(1)}%</p>
                    <p style="margin:5px 0 0 0;font-size:11px;color:#999;">€${amount}</p>
                </div>
            `;
        }).join('');
    }

    savePortfolio() {
        localStorage.setItem('bondPortfolio', JSON.stringify(this.portfolio));
    }

    exportPortfolio() {
        if (this.portfolio.length === 0) {
            alert('Portfolio is empty');
            return;
        }

        // New reduced header
        let csv = 'ISIN,Issuer,Quantity,Investment EUR,Coupon %,Rating,Currency,Maturity\n';

        this.portfolio.forEach(bond => {
            const investment = bond.totalEur ?? 0;

            csv += `${bond.isin},"${bond.issuer}",${bond.quantity},${investment.toFixed(2)},${bond.coupon},"${bond.rating}",${bond.currency},${bond.maturity}\n`;
        });

        const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', 'portfolio.csv');
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }


    importPortfolio(event) {
        const file = event.target.files[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onload = (e) => {
            try {
                const csv = e.target.result;
                const lines = csv.trim().split('\n');

                if (lines.length < 2) {
                    alert('Invalid CSV format');
                    return;
                }

                // Skip header line
                const newPortfolio = [];
                const updatedBonds = [];
                const notFoundBonds = [];

                for (let i = 1; i < lines.length; i++) {
                    const line = lines[i];
                    const parts = this.parseCSVLine(line);

                    if (parts.length < 3) continue;

                    const isin = parts[0].trim();
                    const issuer = parts[1].trim().replace(/^"|"$/g, '');
                    const quantity = parseFloat(parts[2]);
                    const totalEur = parseFloat(
                        parts[3]
                            ?.toString()
                            .replace(/[^\d.-]/g, '')
                    ) || 0;

                    // Find bond in allBonds to get CURRENT market data
                    const currentBondData = this.allBonds.find(b => b.isin === isin);

                    if (currentBondData) {
                        newPortfolio.push({
                            ...currentBondData,
                            quantity: quantity,
                            totalEur: totalEur
                        });
                    } else {
                        notFoundBonds.push(isin);
                    }
                }

                if (newPortfolio.length === 0) {
                    alert('No valid bonds found in CSV');
                    return;
                }

                // Append imported bonds (do NOT replace existing ones)
                newPortfolio.forEach(bond => {
                    this.portfolio.push(bond);
                });
                this.savePortfolio();
                this.updatePortfolioTable();
                this.updateStatistics();

                // Show import summary with price changes
                let message = `✅ Imported ${newPortfolio.length} bonds!`;

                if (updatedBonds.length > 0) {
                    message += `\n\n📊 Price Updates (Market has changed):\n`;
                    updatedBonds.forEach(bond => {
                        const change = bond.change > 0 ? '+' : '';
                        message += `${bond.isin}: €${bond.oldPrice.toFixed(2)} → €${bond.newPrice.toFixed(2)} (${change}€${bond.change.toFixed(2)})\n`;
                    });
                }

                if (notFoundBonds.length > 0) {
                    message += `\n⚠️ Not found in current table: ${notFoundBonds.join(', ')}`;
                }

                alert(message);
                document.getElementById('csvFileInput').value = '';
            } catch (error) {
                alert('Error importing CSV: ' + error.message);
                console.error(error);
            }
        };
        reader.readAsText(file);
    }

    parseCSVLine(line) {
        const result = [];
        let current = '';
        let insideQuotes = false;

        for (let i = 0; i < line.length; i++) {
            const char = line[i];

            if (char === '"') {
                insideQuotes = !insideQuotes;
                current += char;
            } else if (char === ',' && !insideQuotes) {
                result.push(current);
                current = '';
            } else {
                current += char;
            }
        }
        result.push(current);
        return result;
    }

    clearPortfolio() {
        if (confirm('Clear entire portfolio?')) {
            this.portfolio = [];
            this.savePortfolio();
            this.updatePortfolioTable();
            this.updateStatistics();
        }
    }

    clearSearch() {
        document.getElementById('isinSearch').value = '';
        document.getElementById('searchResults').innerHTML = '';
        document.getElementById('addBondForm').style.display = 'none';
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.portfolioAnalyzer = new PortfolioAnalyzer();
});