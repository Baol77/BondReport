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

    createModal() {
        const modalHTML = `
<div id="portfolioModal" class="portfolio-modal" style="pointer-events:none; display:none;position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.3);z-index:10000;overflow-y:auto;padding:20px;">
    <div id="modalContent" style="pointer-events:auto; background:white;border-radius:8px;box-shadow:0 4px 20px rgba(0,0,0,0.3);max-width:1000px;width:90%;margin:20px auto;cursor:move;">

        <div id="modalHeader" style="background:linear-gradient(135deg,#2196F3,#1976D2);color:white;padding:20px;border-radius:8px 8px 0 0;display:flex;justify-content:space-between;align-items:center;cursor:move;user-select:none;">
            <h2 style="margin:0;">🎯 Portfolio Analyzer</h2>
            <button onclick="window.portfolioAnalyzer.closeModal()" style="background:none;border:none;color:white;font-size:28px;cursor:pointer;padding:0;">&times;</button>
        </div>

        <div style="padding:20px;max-height:80vh;overflow-y:auto;">

            <!-- Search Section -->
            <div style="margin-bottom:30px;">
                <h3>1️⃣ Search & Add Bond</h3>
                <div style="display:flex;gap:10px;margin-bottom:15px;">
                    <input type="text" id="isinSearch" placeholder="Enter ISIN (e.g., US0378331005)"
                           onkeyup="window.portfolioAnalyzer.searchBond()"
                           style="flex:1;padding:10px;border:1px solid #ddd;border-radius:4px;font-size:14px;">
                    <button onclick="window.portfolioAnalyzer.handleSearch()"
                            style="padding:10px 20px;background:#2196F3;color:white;border:none;border-radius:4px;cursor:pointer;">
                        🔍 Search
                    </button>
                </div>
                <div id="searchResults" style="background:#f5f5f5;padding:10px;border-radius:4px;font-size:14px;min-height:20px;"></div>
            </div>

            <!-- Add Form -->
            <div id="addBondForm" style="display:none;background:#f9f9f9;padding:15px;border-radius:4px;margin-bottom:20px;border-left:4px solid #2196F3;">
                <h4 style="margin-top:0;">Bond Details</h4>
                <div id="bondDetails" style="background:white;padding:10px;border-radius:4px;margin-bottom:15px;font-size:13px;"></div>

                <div style="display:grid;grid-template-columns:1fr 1fr;gap:15px;margin-bottom:15px;">
                    <label style="display:flex;flex-direction:column;font-weight:500;">
                        Quantity:
                        <input type="number" id="quantity" min="1" value="1" onchange="window.portfolioAnalyzer.updateCost()"
                               style="padding:8px;border:1px solid #ddd;border-radius:4px;margin-top:5px;">
                    </label>
                    <label style="display:flex;flex-direction:column;font-weight:500;">
                        OR Amount (€):
                        <input type="number" id="amount" min="0" step="100" onchange="window.portfolioAnalyzer.updateQuantity()"
                               style="padding:8px;border:1px solid #ddd;border-radius:4px;margin-top:5px;">
                    </label>
                </div>

                <div style="background:white;padding:10px;border-radius:4px;margin-bottom:15px;font-size:14px;">
                    Total Investment: <strong id="totalCost">€0.00</strong>
                </div>

                <button onclick="window.portfolioAnalyzer.addBondToPortfolio()"
                        style="width:100%;padding:12px;background:#2196F3;color:white;border:none;border-radius:4px;cursor:pointer;font-weight:500;">
                    ➕ Add to Portfolio
                </button>
            </div>

            <!-- Portfolio Table -->
            <div style="margin-bottom:30px;">
                <h3>2️⃣ Your Portfolio</h3>
                <div style="overflow-x:auto;">
                    <table id="portfolioTable" style="width:100%;border-collapse:collapse;background:white;margin-top:10px;font-size:12px;">
                        <thead>
                            <tr style="background:#f0f0f0;">
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">ISIN</th>
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">Issuer</th>
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">Currency</th>
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">Price</th>
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">Qty</th>
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">Investment</th>
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">SAY</th>
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">Curr. Yield</th>
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">Rating</th>
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">Maturity</th>
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">Action</th>
                            </tr>
                        </thead>
                        <tbody id="portfolioTableBody">
                        </tbody>
                    </table>
                </div>
                <p id="emptyPortfolioMsg" style="text-align:center;color:#999;padding:20px;">Portfolio is empty. Add bonds above.</p>
            </div>

            <!-- Statistics -->
            <div style="margin-bottom:30px;">
                <h3>3️⃣ Portfolio Statistics</h3>
                <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(140px,1fr));gap:12px;">
                    <div style="background:white;padding:12px;border-radius:4px;border-left:4px solid #2196F3;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
                        <div style="font-size:11px;color:#666;font-weight:600;margin-bottom:6px;">Total Investment</div>
                        <p id="statTotalInvestment" style="margin:0;font-size:16px;font-weight:bold;color:#2196F3;">€0.00</p>
                    </div>
                    <div style="background:white;padding:12px;border-radius:4px;border-left:4px solid #2196F3;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
                        <div style="font-size:11px;color:#666;font-weight:600;margin-bottom:6px;">Avg Price</div>
                        <p id="statAvgPrice" style="margin:0;font-size:16px;font-weight:bold;color:#2196F3;">€0.00</p>
                    </div>
                    <div style="background:white;padding:12px;border-radius:4px;border-left:4px solid #2196F3;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
                        <div style="font-size:11px;color:#666;font-weight:600;margin-bottom:6px;">Weighted SAY</div>
                        <p id="statWeightedSAY" style="margin:0;font-size:16px;font-weight:bold;color:#2196F3;">0.00%</p>
                    </div>
                    <div style="background:white;padding:12px;border-radius:4px;border-left:4px solid #2196F3;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
                        <div style="font-size:11px;color:#666;font-weight:600;margin-bottom:6px;">Weighted Yield</div>
                        <p id="statWeightedYield" style="margin:0;font-size:16px;font-weight:bold;color:#2196F3;">0.00%</p>
                    </div>
                    <div style="background:white;padding:12px;border-radius:4px;border-left:4px solid #2196F3;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
                        <div style="font-size:11px;color:#666;font-weight:600;margin-bottom:6px;">Avg Coupon</div>
                        <p id="statAvgCoupon" style="margin:0;font-size:16px;font-weight:bold;color:#2196F3;">0.00%</p>
                    </div>
                    <div style="background:white;padding:12px;border-radius:4px;border-left:4px solid #2196F3;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
                        <div style="font-size:11px;color:#666;font-weight:600;margin-bottom:6px;">Bond Count</div>
                        <p id="statBondCount" style="margin:0;font-size:16px;font-weight:bold;color:#2196F3;">0</p>
                    </div>
                    <div style="background:white;padding:12px;border-radius:4px;border-left:4px solid #FF9800;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
                        <div style="font-size:11px;color:#666;font-weight:600;margin-bottom:6px;">Avg Risk (Maturity)</div>
                        <p id="statWeightedRisk" style="margin:0;font-size:16px;font-weight:bold;color:#FF9800;">0.00 yrs</p>
                    </div>
                    <div style="background:white;padding:12px;border-radius:4px;border-left:4px solid #9C27B0;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
                        <div style="font-size:11px;color:#666;font-weight:600;margin-bottom:6px;">Weighted Rating</div>
                        <p id="statWeightedRating" style="margin:0;font-size:16px;font-weight:bold;color:#9C27B0;">-</p>
                    </div>
                </div>

                <!-- Currency Breakdown -->
                <div style="margin-top:20px;">
                    <h4 style="margin-top:0;margin-bottom:10px;">Currency Breakdown (by Investment %)</h4>
                    <div id="currencyBreakdown" style="display:grid;grid-template-columns:repeat(auto-fit,minmax(120px,1fr));gap:10px;">
                    </div>
                </div>
            </div>

            <!-- Actions -->
            <div style="margin-top:20px;padding-top:20px;border-top:1px solid #eee;display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:10px;">
                <button onclick="window.portfolioAnalyzer.exportPortfolio()"
                        style="padding:10px;background:#4CAF50;color:white;border:none;border-radius:4px;cursor:pointer;font-weight:500;">
                    📥 Export CSV
                </button>
                <button onclick="document.getElementById('csvFileInput').click()"
                        style="padding:10px;background:#FF9800;color:white;border:none;border-radius:4px;cursor:pointer;font-weight:500;">
                    📤 Import CSV
                </button>
                <input type="file" id="csvFileInput" accept=".csv" style="display:none;" onchange="window.portfolioAnalyzer.importPortfolio(event)">
                <button onclick="window.portfolioAnalyzer.clearPortfolio()"
                        style="padding:10px;background:#f44336;color:white;border:none;border-radius:4px;cursor:pointer;font-weight:500;">
                    🗑️ Clear Portfolio
                </button>
            </div>
        </div>
    </div>
</div>
        `;

        const container = document.body;
        const modalDiv = document.createElement('div');
        modalDiv.innerHTML = modalHTML;
        container.appendChild(modalDiv);

        this.modal = document.getElementById('portfolioModal');
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
        }
    }

    closeModal() {
        if (this.modal) {
            this.modal.style.display = 'none';
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

    updateQuantity() {
        if (!this.currentBond) return;

        const amount = parseFloat(document.getElementById('amount').value) || 0;
        const qty = Math.floor(amount / this.currentBond.priceEur);

        document.getElementById('quantity').value = Math.max(1, qty);
        this.updateCost();
    }

    addBondToPortfolio() {
        if (!this.currentBond) return;

        const qty = parseFloat(document.getElementById('quantity').value) || 1;

        // Always add as new entry, don't combine with existing
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

    removeBond(isin) {
        this.portfolio = this.portfolio.filter(b => b.isin !== isin);
        this.savePortfolio();
        this.updatePortfolioTable();
        this.updateStatistics();
    }

    updateQuantityInPortfolio(isin, newQuantity) {
        const qty = parseFloat(newQuantity);
        if (isNaN(qty) || qty < 1) {
            alert('Quantity must be at least 1');
            this.updatePortfolioTable();
            return;
        }

        const bond = this.portfolio.find(b => b.isin === isin);
        if (bond) {
            bond.quantity = qty;
            this.savePortfolio();
            this.updatePortfolioTable();
            this.updateStatistics();
        }
    }

    updatePortfolioTable() {
        const tbody = document.getElementById('portfolioTableBody');
        const emptyMsg = document.getElementById('emptyPortfolioMsg');

        if (this.portfolio.length === 0) {
            tbody.innerHTML = '';
            emptyMsg.style.display = 'block';
            return;
        }

        emptyMsg.style.display = 'none';
        tbody.innerHTML = this.portfolio.map(bond => {
            const investment = bond.priceEur * bond.quantity;
            return `
                <tr style="border-bottom:1px solid #eee;">
                    <td style="padding:8px;font-size:12px;">${bond.isin}</td>
                    <td style="padding:8px;font-size:12px;">${bond.issuer}</td>
                    <td style="padding:8px;font-size:12px;"><strong>${bond.currency}</strong></td>
                    <td style="padding:8px;font-size:12px;">€${bond.priceEur.toFixed(2)}</td>
                    <td style="padding:8px;font-size:12px;">
                        <input type="number" value="${bond.quantity}" min="1"
                               onchange="window.portfolioAnalyzer.updateQuantityInPortfolio('${bond.isin}', this.value)"
                               style="width:50px;padding:4px;border:1px solid #ddd;border-radius:3px;">
                    </td>
                    <td style="padding:8px;font-size:12px;">€${investment.toFixed(2)}</td>
                    <td style="padding:8px;font-size:12px;">${bond.say.toFixed(2)}%</td>
                    <td style="padding:8px;font-size:12px;">${bond.currentYield.toFixed(2)}%</td>
                    <td style="padding:8px;font-size:12px;"><strong>${bond.rating}</strong></td>
                    <td style="padding:8px;font-size:12px;white-space:nowrap;">${bond.maturity}</td>
                    <td style="padding:8px;font-size:12px;">
                        <button onclick="window.portfolioAnalyzer.removeBond('${bond.isin}')"
                                style="background:#f44336;color:white;border:none;padding:4px 8px;border-radius:3px;cursor:pointer;font-size:11px;">
                            ❌ Delete
                        </button>
                    </td>
                </tr>
            `;
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
        let currencyTotals = {}; // Track investment by currency

        this.portfolio.forEach(bond => {
            const investment = bond.priceEur * bond.quantity;
            totalInvestment += investment;

            weightedSAY += (bond.say * investment);
            weightedYield += (bond.currentYield * investment);
            weightedCoupon += (bond.coupon * investment);

            // Calculate years to maturity from maturity date
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

        // Calculate weighted average rating (using rating order)
        const ratingOrder = ['AAA', 'AA+', 'AA', 'AA-', 'A+', 'A', 'A-', 'BBB+', 'BBB', 'BBB-', 'BB+', 'BB', 'BB-', 'B+', 'B', 'B-', 'CCC', 'CC', 'C', 'D'];
        let weightedRatingScore = 0;
        this.portfolio.forEach(bond => {
            const investment = bond.priceEur * bond.quantity;
            const ratingIndex = ratingOrder.indexOf(bond.rating);
            const ratingScore = ratingIndex >= 0 ? ratingIndex : 20; // Default to lowest if not found
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

        // Display currency breakdown
        this.updateCurrencyBreakdown(currencyTotals, totalInvestment);
    }

    updateCurrencyBreakdown(currencyTotals, totalInvestment) {
        const breakdown = document.getElementById('currencyBreakdown');
        const currencies = Object.keys(currencyTotals).sort();

        breakdown.innerHTML = currencies.map(currency => {
            const amount = currencyTotals[currency];
            const percentage = (amount / totalInvestment * 100);
            return `
                <div style="background:white;padding:10px;border-radius:4px;border-left:4px solid #4CAF50;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
                    <div style="font-size:11px;color:#666;font-weight:600;margin-bottom:6px;">${currency}</div>
                    <p style="margin:0;font-size:14px;font-weight:bold;color:#4CAF50;">${percentage.toFixed(1)}%</p>
                    <p style="margin:5px 0 0 0;font-size:11px;color:#999;">€${amount.toFixed(2)}</p>
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

        let csv = 'ISIN,Issuer,Price EUR,Quantity,Investment EUR,SAY %,Current Yield %,Coupon %,Rating,Currency,Maturity\n';
        this.portfolio.forEach(bond => {
            const investment = bond.priceEur * bond.quantity;
            csv += `${bond.isin},"${bond.issuer}",${bond.priceEur},${bond.quantity},${investment},${bond.say},${bond.currentYield},${bond.coupon},"${bond.rating}",${bond.currency},${bond.maturity}\n`;
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

                    if (parts.length < 5) continue;

                    const isin = parts[0].trim();
                    const issuer = parts[1].trim().replace(/^"|"$/g, '');
                    const oldPriceEur = parseFloat(parts[2]);
                    const quantity = parseFloat(parts[3]);

                    // Find bond in allBonds to get CURRENT market data
                    const currentBondData = this.allBonds.find(b => b.isin === isin);

                    if (currentBondData) {
                        const priceChanged = currentBondData.priceEur !== oldPriceEur;

                        newPortfolio.push({
                            ...currentBondData,
                            quantity: quantity
                        });

                        if (priceChanged) {
                            updatedBonds.push({
                                isin: isin,
                                issuer: issuer,
                                oldPrice: oldPriceEur,
                                newPrice: currentBondData.priceEur,
                                change: currentBondData.priceEur - oldPriceEur
                            });
                        }
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