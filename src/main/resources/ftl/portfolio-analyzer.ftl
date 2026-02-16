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
                        <!-- EUR -->
                        <label style="display:flex;flex-direction:column;font-weight:500;">
                            Total Investment (€):
                            <input type="number"
                                   id="amount"
                                   min="0"
                                   step="1"
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
                                   min="0.01"
                                   step="0.01"
                                   style="padding:8px;border:1px solid #ddd;border-radius:4px;">
                        </label>
                    </div>

                    <!-- RIGHT COLUMN -->
                    <div style="display:flex;flex-direction:column;gap:15px;">
                        <label style="display:flex;flex-direction:column;font-weight:500;gap:5px;">
                            Quantity:
                            <label id="quantity" style="padding:8px;border:1px solid #ddd;border-radius:4px;">
                            </label>
                    </div>
                </div>

                <!-- SUMMARY -->
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
                            <th style="padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #ddd;font-size:12px;">
                                <div style="display:flex;justify-content:space-between;align-items:center;" title="Toggle to include/exclude all the bonds from statistics calculations">
                                    <span>Σ</span>
                                    <input type="checkbox"
                                           id="toggleAllStatistics"
                                           checked
                                           onchange="window.portfolioAnalyzer.toggleAllStatistics(this.checked)">
                                </div>
                            </th>
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
                        <div style="font-size:11px;color:#666;font-weight:600;margin-bottom:6px;">Total Init. Investment</div>
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