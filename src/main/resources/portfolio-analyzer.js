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
           <div id="addBondForm"
                style="display:none;background:#f9f9f9;padding:15px;border-radius:4px;margin-bottom:20px;border-left:4px solid #2196F3;">

               <h4 style="margin-top:0;">Bond Details</h4>

               <div id="bondDetails"
                    style="background:white;padding:10px;border-radius:4px;margin-bottom:15px;font-size:13px;">
               </div>

               <!-- INPUT GRID -->
               <div style="display:grid;
                           grid-template-columns:1fr 1fr;
                           gap:15px;
                           margin-bottom:15px;
                           align-items:start;">

                   <!-- LEFT COLUMN -->
                   <div style="display:flex;flex-direction:column;gap:15px;">
                       <label style="display:flex;flex-direction:column;font-weight:500;gap:5px;">
                           Quantity:
                           <input type="number"
                                  id="quantity"
                                  min="0"
                                  step="1"
                                  style="padding:8px;border:1px solid #ddd;border-radius:4px;">

                           <small id="grossQuantityInfo"
                                  style="color:#666;font-weight:normal;font-size:12px;">
                               Gross quantity: –
                           </small>
                       </label>

                   </div>

                   <!-- RIGHT COLUMN -->
                   <div style="display:flex;flex-direction:column;gap:15px;">

                       <!-- EUR -->
                       <label style="display:flex;flex-direction:column;font-weight:500;">
                           Total Investment (€):
                           <input type="number"
                                  id="amount"
                                  min="0"
                                  step="0.01"
                                  style="padding:8px;border:1px solid #ddd;border-radius:4px;margin-top:5px;">
                       </label>

                       <!-- ORIGINAL -->
                       <label id="originalCurrencyWrapper"
                              style="display:none;flex-direction:column;font-weight:500;gap:5px;">

                           <span>
                               Total Investment (<span id="originalCurrencyLabel"></span>)
                           </span>
                           <input type="number"
                                  id="amountOriginal"
                                  min="0"
                                  step="0.01"
                                  style="padding:8px;border:1px solid #ddd;border-radius:4px;">
                       </label>


                   </div>

               </div>


               <!-- SUMMARY -->
               <div style="background:white;padding:10px;border-radius:4px;margin-bottom:15px;font-size:14px;">
                   Total Investment (EUR): <strong id="totalCost">€0.00</strong>
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
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">Price</th>
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">Currency</th>
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">Rating</th>
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">Qty</th>
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">Investment</th>
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">Maturity</th>
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">Curr. Yield</th>
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">SAY</th>
                                <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">Profit (€)</th>
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
                    <div style="background:white;padding:12px;border-radius:4px;border-left:4px solid #4CAF50;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
                        <div style="font-size:11px;color:#666;font-weight:600;margin-bottom:6px;">Total Profit</div>
                        <p id="statTotalProfit" style="margin:0;font-size:16px;font-weight:bold;color:#4CAF50;">€0.00</p>
                    </div>

                    <div style="background:white;padding:12px;border-radius:4px;border-left:4px solid #673AB7;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
                        <div style="font-size:11px;color:#666;font-weight:600;margin-bottom:6px;">Coupon Income (Current Year)</div>
                        <p id="statTotalCouponIncome" style="margin:0;font-size:16px;font-weight:bold;color:#673AB7;">€0.00</p>
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
            Price: ${bond.currency} ${bond.price.toFixed(2)}${bond.currency !== 'EUR' ? ` (€ ${bond.priceEur.toFixed(2)})` : ''}<br>
            Rating: ${bond.rating} | Coupon: ${bond.coupon.toFixed(2)}% | SAY: ${bond.say.toFixed(2)}%
        `;

        // Reset fields
        document.getElementById('quantity').value = '';
        document.getElementById('amount').value = '';
        document.getElementById('grossQuantityInfo').textContent = 'Gross quantity: –';
        document.getElementById('totalCost').textContent = "-";

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
            const eur = parseFloat(document.getElementById('amount').value) || 0;
            document.getElementById('totalCost').textContent = `€${eur.toFixed(2)}`;
        });

       // Call alignment logic
       this.alignTotalAmounts();

       document.getElementById('addBondForm').style.display = 'block';
    }

    alignTotalAmounts() {
        if (!this.currentBond) return;

        const eurInput = document.getElementById('amount');
        const originalInput = document.getElementById('amountOriginal');
        const totalDisplay = document.getElementById('totalCost');

        if (!eurInput) return;

        const bond = this.currentBond;

        // If EUR bond → only update total display
        if (bond.currency === 'EUR') {
            eurInput.oninput = () => {
                const eur = parseFloat(eurInput.value) || 0;
                totalDisplay.textContent = `€${eur.toFixed(2)}`;

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
            totalDisplay.textContent = `€${eur.toFixed(2)}`;

            this.updateGrossQuantity();
        };

        // Original → EUR
        originalInput.oninput = () => {
            const original = parseFloat(originalInput.value) || 0;
            const eur = original * fxRate;

            eurInput.value = eur.toFixed(2);
            totalDisplay.textContent = `€${eur.toFixed(2)}`;

            this.updateGrossQuantity();
        };
    }

    updateGrossQuantity() {
        if (!this.currentBond) return;

        const eurInput = document.getElementById('amount');
        const grossInfo = document.getElementById('grossQuantityInfo');

        const eur = parseFloat(eurInput.value) || 0;

        if (eur === 0) {
            grossInfo.textContent = 'Gross quantity: –';
            return;
        }

        const grossQty = eur / this.currentBond.priceEur;

        grossInfo.textContent = `Gross quantity: ${grossQty.toFixed(2)}`;
    }

    addBondToPortfolio() {
        if (!this.currentBond) return;

        const qty = parseFloat(document.getElementById('quantity').value) || 1;

        // Get invested EUR
        const totalEur = parseFloat(
            document.getElementById('totalCost')
                .textContent
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
            totalOriginal: totalOriginal
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
        // Find all entries for this specific ISIN
        const matches = this.portfolio.filter(b => b.isin === isin);
        if (matches.length < 2) return;

        // Calculate weighted average price (Cost Basis)
        const totalInvestment = matches.reduce((sum, b) => sum + (b.priceEur * b.quantity), 0);
        const totalQty = matches.reduce((sum, b) => sum + b.quantity, 0);
        const weightedAvgPrice = totalInvestment / totalQty;

        // Keep the market metrics (SAY, Maturity, etc.) from the most recent entry
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

        // Count ISINs to identify which ones need a merge button
        const isinCounts = this.portfolio.reduce((acc, b) => {
            acc[b.isin] = (acc[b.isin] || 0) + 1;
            return acc;
        }, {});

        tbody.innerHTML = this.portfolio.map((bond, idx) => {
            const hasDuplicates = isinCounts[bond.isin] > 1;

            const currentValueEur = bond.quantity * bond.priceEur;
            const gainLoss = currentValueEur - bond.totalEur;

            return `<tr style="border-bottom:1px solid #eee;">
                <td>${bond.isin}</td>
                <td>${bond.issuer}</td>
                <td>€${bond.priceEur.toFixed(2)}</td>
                <td>${bond.currency}</td>
                <td>${bond.rating}</td>
                <td>
                    <input type="number"
                           value="${bond.quantity}"
                           min="1"
                           onchange="window.portfolioAnalyzer.updateQuantityInPortfolio(${idx}, this.value)"
                           style="width:60px;padding:4px;font-size:12px;">
                </td>

                <td>€${(bond.totalEur ?? 0).toFixed(2)}</td>
                <td>${bond.maturity}</td>
                <td>${bond.currentYield.toFixed(2)}%</td>
                <td>${bond.say.toFixed(2)}%</td>
                <td class="${gainLoss >= 0 ? 'good' : 'bad'}">
                    ${gainLoss.toFixed(2)}
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
            document.getElementById('statTotalProfit').textContent = '€0.00';
            document.getElementById('statTotalCouponIncome').textContent = '€0.00';
            return;
        }

        let totalInvestment = 0;
        let weightedSAY = 0;
        let weightedYield = 0;
        let weightedCoupon = 0;
        let weightedRisk = 0;
        let currencyTotals = {}; // Track investment by currency
        let totalProfit = 0;
        let totalCouponIncome = 0;

        this.portfolio.forEach(bond => {

            const currentValue = bond.priceEur * bond.quantity;   // market value
            const investedAmount = bond.totalEur || 0;            // what you paid

            totalInvestment += currentValue;

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
            const marketValue = bond.priceEur * bond.quantity;
            const ratingIndex = ratingOrder.indexOf(bond.rating);
            const ratingScore = ratingIndex >= 0 ? ratingIndex : 20; // Default to lowest if not found
            weightedRatingScore += (ratingScore * marketValue);
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

        // Total Profit
        const profitElement = document.getElementById('statTotalProfit');
        if (profitElement) {
            profitElement.textContent = `€${totalProfit.toFixed(2)}`;
            profitElement.style.color = totalProfit >= 0 ? '#4CAF50' : '#f44336';
        }

        // Total Coupon Income (Current Year)
        const couponElement = document.getElementById('statTotalCouponIncome');
        if (couponElement) {
            couponElement.textContent = `€${totalCouponIncome.toFixed(2)}`;
        }

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