<div id="portfolioModal" class="portfolio-modal">
    <div id="modalContent">

        <!-- Modal Header -->
        <div id="modalHeader">
            <h2>🎯 Portfolio Analyzer</h2>
            <button onclick="window.portfolioAnalyzer.closeModal()">&times;</button>
        </div>

        <!-- Modal Content -->
        <div class="portfolio-modal-content">

            <!-- 1. Search & Add Bond Section -->
            <div class="portfolio-search-section">
                <h3>1️⃣ Search & Add Bond</h3>
                <div class="portfolio-search-container">
                    <input 
                        type="text" 
                        id="isinSearch" 
                        placeholder="Enter ISIN (e.g., US0378331005)"
                        onkeyup="window.portfolioAnalyzer.searchBond()">
                    <button class="portfolio-search-button" onclick="window.portfolioAnalyzer.handleSearch()">
                        🔍 Search
                    </button>
                </div>
                <div id="searchResults"></div>
            </div>

            <!-- Add Bond Form -->
            <div id="addBondForm">
                <h4>Bond Details</h4>
                <div id="bondDetails"></div>

                <div class="portfolio-form-grid">
                    <div class="portfolio-form-group">
                        <label>Quantity:</label>
                        <input 
                            type="number" 
                            id="quantity" 
                            min="1" 
                            value="1" 
                            onchange="window.portfolioAnalyzer.updateCost()">
                    </div>
                    <div class="portfolio-form-group">
                        <label>OR Amount (€):</label>
                        <input 
                            type="number" 
                            id="amount" 
                            min="0" 
                            step="100" 
                            onchange="window.portfolioAnalyzer.updateQuantityInPortfolio()">
                    </div>
                </div>

                <div class="portfolio-total-cost">
                    Total Investment: <strong id="totalCost">€0.00</strong>
                </div>

                <button class="portfolio-add-button" onclick="window.portfolioAnalyzer.addBondToPortfolio()">
                    ➕ Add to Portfolio
                </button>
            </div>

            <!-- 2. Portfolio Table Section -->
            <div class="portfolio-table-section">
                <h3>2️⃣ Your Portfolio</h3>
                <div class="portfolio-table-wrapper">
                    <table id="portfolioTable">
                        <thead>
                            <tr>
                                <th>ISIN</th>
                                <th>Issuer</th>
                                <th>Price</th>
                                <th>Currency</th>
                                <th>Rating</th>
                                <th>Qty</th>
                                <th>Investment</th>
                                <th>Maturity</th>
                                <th>Curr. Yield</th>
                                <th>SAY</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody id="portfolioTableBody">
                        </tbody>
                    </table>
                </div>
                <p id="emptyPortfolioMsg">Portfolio is empty. Add bonds above.</p>
            </div>

            <!-- 3. Portfolio Statistics Section -->
            <div class="portfolio-stats-section">
                <h3>3️⃣ Portfolio Statistics</h3>

                <!-- Stats Grid -->
                <div class="portfolio-stats-grid">
                    <div class="portfolio-stat-card">
                        <div class="portfolio-stat-label">Total Investment</div>
                        <p class="portfolio-stat-value" id="statTotalInvestment">€0.00</p>
                    </div>
                    <div class="portfolio-stat-card">
                        <div class="portfolio-stat-label">Avg Price</div>
                        <p class="portfolio-stat-value" id="statAvgPrice">€0.00</p>
                    </div>
                    <div class="portfolio-stat-card">
                        <div class="portfolio-stat-label">Weighted SAY</div>
                        <p class="portfolio-stat-value" id="statWeightedSAY">0.00%</p>
                    </div>
                    <div class="portfolio-stat-card">
                        <div class="portfolio-stat-label">Weighted Yield</div>
                        <p class="portfolio-stat-value" id="statWeightedYield">0.00%</p>
                    </div>
                    <div class="portfolio-stat-card">
                        <div class="portfolio-stat-label">Avg Coupon</div>
                        <p class="portfolio-stat-value" id="statAvgCoupon">0.00%</p>
                    </div>
                    <div class="portfolio-stat-card">
                        <div class="portfolio-stat-label">Bond Count</div>
                        <p class="portfolio-stat-value" id="statBondCount">0</p>
                    </div>
                    <div class="portfolio-stat-card risk">
                        <div class="portfolio-stat-label">Avg Risk (Maturity)</div>
                        <p class="portfolio-stat-value" id="statWeightedRisk">0.00 yrs</p>
                    </div>
                    <div class="portfolio-stat-card rating">
                        <div class="portfolio-stat-label">Weighted Rating</div>
                        <p class="portfolio-stat-value" id="statWeightedRating">-</p>
                    </div>
                </div>

                <!-- Currency Breakdown -->
                <div class="portfolio-currency-section">
                    <h4>Currency Breakdown (by Investment %)</h4>
                    <div id="currencyBreakdown"></div>
                </div>
            </div>

            <!-- Action Buttons -->
            <div class="portfolio-actions-section">
                <button class="portfolio-action-btn portfolio-btn-export" onclick="window.portfolioAnalyzer.exportPortfolio()">
                    📥 Export CSV
                </button>
                <button class="portfolio-action-btn portfolio-btn-import" onclick="document.getElementById('csvFileInput').click()">
                    📤 Import CSV
                </button>
                <input 
                    type="file" 
                    id="csvFileInput" 
                    accept=".csv" 
                    onchange="window.portfolioAnalyzer.importPortfolio(event)">
                <button class="portfolio-action-btn portfolio-btn-clear" onclick="window.portfolioAnalyzer.clearPortfolio()">
                    🗑️ Clear Portfolio
                </button>
            </div>
        </div>
    </div>
</div>
