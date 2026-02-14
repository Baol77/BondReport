/**
 * Portfolio Analyzer - Pure JavaScript Implementation
 *
 * Usage:
 * 1. Include portfolio-analyzer.ftl in your template
 * 2. Include portfolio-analyzer.css in your stylesheet
 * 3. Include this script file
 * 4. Access via: window.portfolioAnalyzer.openModal()
 *
 * Requires DOM elements from portfolio-analyzer.ftl
 * Uses localStorage for persistence
 */

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

        // Get modal reference
        this.modal = document.getElementById('portfolioModal');

        // Setup dragging if modal exists
        if (this.modal) {
            this.setupDragging();
        }

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
                    price: parseFloat(cells[2].textContent.trim()),
                    currency: cells[3].textContent.trim(),
                    rating: cells[4].textContent.trim(),
                    priceEur: parseFloat(cells[5].textContent.trim()),
                    coupon: parseFloat(cells[6].textContent.trim()),
                    maturity: cells[7].textContent.trim(),
                    currentYield: parseFloat(cells[8].textContent.trim()),
                    capitalAtMat: parseFloat(cells[9].textContent.trim()),
                    say: cells.length > 10 ? parseFloat(cells[10].textContent.trim()) : 0
                });
            }
        });
    }

    setupDragging() {
        const modalContent = document.getElementById('modalContent');
        const modalHeader = document.getElementById('modalHeader');

        if (!modalContent || !modalHeader) return;

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

            let newTop = e.clientY - offsetY;
            newTop = Math.max(minY, newTop);
            newTop = Math.max(0, newTop);

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
            this.modal.classList.add('open');
            this.updatePortfolioTable();
            this.updateStatistics();
        }
    }

    closeModal() {
        if (this.modal) {
            this.modal.classList.remove('open');
            this.clearSearch();
        }
    }

    searchBond() {
        const isin = document.getElementById('isinSearch').value.trim().toUpperCase();
        const resultsDiv = document.getElementById('searchResults');

        if (isin.length < 3) {
            resultsDiv.innerHTML = '';
            document.getElementById('addBondForm').style.display = 'none';
            return;
        }

        const bond = this.allBonds.find(b => b.isin.toUpperCase() === isin);

        if (bond) {
            resultsDiv.innerHTML = `✅ Bond found: <strong>${bond.issuer}</strong>`;
            this.showAddBondForm(bond);
        } else {
            resultsDiv.innerHTML = `❌ No bond found with ISIN: ${isin}`;
            document.getElementById('addBondForm').style.display = 'none';
        }
    }

    handleSearch() {
        this.searchBond();
    }

    showAddBondForm(bond) {
        this.currentBond = bond;

        const detailsDiv = document.getElementById('bondDetails');
        detailsDiv.innerHTML = `
            <strong>${bond.issuer}</strong><br>
            ISIN: ${bond.isin}<br>
            Price: €${bond.priceEur.toFixed(2)}<br>
            Rating: ${bond.rating} | Coupon: ${bond.coupon.toFixed(2)}% | SAY: ${bond.say.toFixed(2)}%
        `;

        document.getElementById('quantity').value = '1';
        document.getElementById('amount').value = '';

        this.updateCost();
        document.getElementById('addBondForm').style.display = 'block';
    }

    updateCost() {
        if (!this.currentBond) return;

        const qty = parseFloat(document.getElementById('quantity').value) || 1;
        const totalCost = qty * this.currentBond.priceEur;

        document.getElementById('totalCost').textContent = `€${totalCost.toFixed(2)}`;
        document.getElementById('amount').value = totalCost.toFixed(2);
    }

    updateQuantityInPortfolio(index, newQuantity) {
        const qty = parseFloat(newQuantity);

        if (isNaN(qty) || qty < 1) {
            alert('Quantity must be at least 1');
            this.updatePortfolioTable();
            return;
        }

        this.portfolio[index].quantity = qty;
        this.savePortfolio();
        this.updateStatistics();
    }

    addBondToPortfolio() {
        if (!this.currentBond) return;

        const qty = parseFloat(document.getElementById('quantity').value) || 1;

        this.portfolio.push({
            ...this.currentBond,
            quantity: qty
        });

        this.savePortfolio();
        this.updatePortfolioTable();
        this.updateStatistics();

        document.getElementById('isinSearch').value = '';
        document.getElementById('addBondForm').style.display = 'none';
        document.getElementById('searchResults').innerHTML = '';

        alert(`✅ Bond added! Total in portfolio: ${qty}`);
    }

    removeBond(index) {
        this.portfolio.splice(index, 1);
        this.savePortfolio();
        this.updatePortfolioTable();
        this.updateStatistics();
    }

    mergeBond(isin) {
        // Find all entries for this specific ISIN
        const matches = this.portfolio.filter(b => b.isin === isin);
        if (matches.length < 2) return;

        // Calculate weighted average price (Cost Basis)
        const totalInvestment = matches.reduce((sum, b) => sum + (b.priceEur * b.quantity), 0);
        const totalQty = matches.reduce((sum, b) => sum + b.quantity, 0);
        const weightedAvgPrice = totalInvestment / totalQty;

        // Keep the market metrics from the most recent entry
        const latestData = matches[matches.length - 1];

        // Remove all old entries and push the new consolidated one
        this.portfolio = this.portfolio.filter(b => b.isin !== isin);
        this.portfolio.push({
            ...latestData,
            priceEur: weightedAvgPrice,
            quantity: totalQty
        });

        this.savePortfolio();
        this.updatePortfolioTable();
        this.updateStatistics();

        console.log(`Consolidated ${matches.length} entries for ${isin}. New Avg Price: €${weightedAvgPrice.toFixed(2)}`);
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

        // Count ISINs to identify duplicates
        const isinCounts = this.portfolio.reduce((acc, b) => {
            acc[b.isin] = (acc[b.isin] || 0) + 1;
            return acc;
        }, {});

        tbody.innerHTML = this.portfolio.map((bond, idx) => {
            const hasDuplicates = isinCounts[bond.isin] > 1;

            return `<tr>
                <td>${bond.isin}</td>
                <td>${bond.issuer}</td>
                <td>€${bond.priceEur.toFixed(2)}</td>
                <td>${bond.currency}</td>
                <td>${bond.rating}</td>
                <td>
                    <input type="number"
                           value="${bond.quantity}"
                           min="1"
                           onchange="window.portfolioAnalyzer.updateQuantityInPortfolio(${idx}, this.value)">
                </td>
                <td>€${(bond.priceEur * bond.quantity).toFixed(2)}</td>
                <td>${bond.maturity}</td>
                <td>${bond.currentYield.toFixed(2)}%</td>
                <td>${bond.say.toFixed(2)}%</td>
                <td>
                   <div class="portfolio-actions">
                       ${hasDuplicates ? `<span class="portfolio-action-icon" onclick="window.portfolioAnalyzer.mergeBond('${bond.isin}')" title="Merge duplicates">🔄</span>` : ''}
                       <span class="portfolio-action-icon" onclick="window.portfolioAnalyzer.removeBond(${idx})" title="Delete bond">❌</span>
                   </div>
                </td>
            </tr>`;
        }).join('');
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
            return;
        }

        let totalInvestment = 0;
        let weightedSAY = 0;
        let weightedYield = 0;
        let weightedCoupon = 0;
        let weightedRisk = 0;
        let currencyTotals = {};

        this.portfolio.forEach(bond => {
            const investment = bond.priceEur * bond.quantity;
            totalInvestment += investment;

            weightedSAY += (bond.say * investment);
            weightedYield += (bond.currentYield * investment);
            weightedCoupon += (bond.coupon * investment);

            // Calculate years to maturity
            const maturityDate = new Date(bond.maturity);
            const today = new Date();
            const yearsToMaturity = (maturityDate - today) / (365.25 * 24 * 60 * 60 * 1000);
            weightedRisk += (Math.max(0, yearsToMaturity) * investment);

            // Track currency totals
            if (!currencyTotals[bond.currency]) {
                currencyTotals[bond.currency] = 0;
            }
            currencyTotals[bond.currency] += investment;
        });

        const totalQty = this.portfolio.reduce((sum, b) => sum + b.quantity, 0);
        const avgPrice = totalInvestment / totalQty;

        const weightedSAYPercent = (weightedSAY / totalInvestment);
        const weightedYieldPercent = (weightedYield / totalInvestment);
        const weightedCouponPercent = (weightedCoupon / totalInvestment);
        const weightedRiskYears = (weightedRisk / totalInvestment);

        // Calculate weighted average rating
        const ratingOrder = ['AAA', 'AA+', 'AA', 'AA-', 'A+', 'A', 'A-', 'BBB+', 'BBB', 'BBB-', 'BB+', 'BB', 'BB-', 'B+', 'B', 'B-', 'CCC', 'CC', 'C', 'D'];
        let weightedRatingScore = 0;
        this.portfolio.forEach(bond => {
            const investment = bond.priceEur * bond.quantity;
            const ratingIndex = ratingOrder.indexOf(bond.rating);
            const ratingScore = ratingIndex >= 0 ? ratingIndex : 20;
            weightedRatingScore += (ratingScore * investment);
        });
        const avgRatingScore = weightedRatingScore / totalInvestment;
        const weightedRating = ratingOrder[Math.round(avgRatingScore)] || '-';

        document.getElementById('statTotalInvestment').textContent = `€${totalInvestment.toFixed(2)}`;
        document.getElementById('statAvgPrice').textContent = `€${avgPrice.toFixed(2)}`;
        document.getElementById('statWeightedSAY').textContent = `${weightedSAYPercent.toFixed(2)}%`;
        document.getElementById('statWeightedYield').textContent = `${weightedYieldPercent.toFixed(2)}%`;
        document.getElementById('statAvgCoupon').textContent = `${weightedCouponPercent.toFixed(2)}%`;
        const uniqueISINs = new Set(this.portfolio.map(b => b.isin));
        document.getElementById('statBondCount').textContent = uniqueISINs.size;
        document.getElementById('statWeightedRisk').textContent = `${weightedRiskYears.toFixed(2)} yrs`;
        document.getElementById('statWeightedRating').textContent = weightedRating;

        this.updateCurrencyBreakdown(currencyTotals, totalInvestment);
    }

    updateCurrencyBreakdown(currencyTotals, totalInvestment) {
        const breakdown = document.getElementById('currencyBreakdown');
        const currencies = Object.keys(currencyTotals).sort();

        breakdown.innerHTML = currencies.map(currency => {
            const amount = currencyTotals[currency];
            const percentage = (amount / totalInvestment * 100);
            return `
                <div class="portfolio-currency-card">
                    <div class="portfolio-currency-label">${currency}</div>
                    <p class="portfolio-currency-value">${percentage.toFixed(1)}%</p>
                    <p class="portfolio-currency-amount">€${amount.toFixed(2)}</p>
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

        let csv = 'ISIN,Issuer,Quantity,Investment EUR,Coupon %,Rating,Currency,Maturity\n';

        this.portfolio.forEach(bond => {
            const investment = bond.priceEur * bond.quantity;
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

                const newPortfolio = [];
                const notFoundBonds = [];

                for (let i = 1; i < lines.length; i++) {
                    const line = lines[i];
                    const parts = this.parseCSVLine(line);

                    if (parts.length < 3) continue;

                    const isin = parts[0].trim();
                    const quantity = parseFloat(parts[2]);

                    // Find bond in allBonds to get CURRENT market data
                    const currentBondData = this.allBonds.find(b => b.isin === isin);

                    if (currentBondData) {
                        newPortfolio.push({
                            ...currentBondData,
                            quantity: quantity
                        });
                    } else {
                        notFoundBonds.push(isin);
                    }
                }

                if (newPortfolio.length === 0) {
                    alert('No valid bonds found in CSV');
                    return;
                }

                this.portfolio = newPortfolio;
                this.savePortfolio();
                this.updatePortfolioTable();
                this.updateStatistics();

                let message = `✅ Imported ${newPortfolio.length} bonds!`;
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
