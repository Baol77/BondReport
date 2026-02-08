<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Bond Yield Ranking</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #fafafa;
            margin: 20px;
        }
        h2 {
            margin-bottom: 10px;
        }
        .controls {
            display: flex;
            gap: 12px;
            margin-bottom: 10px;
            flex-wrap: wrap;
            align-items: center;
        }
        .controls .spacer {
            margin-left: auto;
        }
        .controls label {
            font-size: 13px;
        }
        .controls input, .controls select, .controls button {
            font-size: 13px;
            padding: 4px 6px;
        }
        .controls input[type="text"] {
            min-width: 200px;
        }

        /* =======================
           PROFILE PRESETS
        ======================= */
        .profile-presets {
            display: flex;
            gap: 10px;
            margin-bottom: 12px;
            padding: 8px 10px;
            background: #f0f8ff;
            border-left: 4px solid #2196F3;
            align-items: center;
            flex-wrap: wrap;
        }

        .profile-presets label {
            font-size: 13px;
            font-weight: bold;
            margin-right: 4px;
        }

        .preset-button {
            padding: 6px 12px;
            border: 1px solid #ccc;
            background: white;
            border-radius: 4px;
            cursor: pointer;
            font-size: 12px;
            transition: all 0.2s ease;
        }

        .preset-button:hover {
            background: #f0f0f0;
            border-color: #666;
        }

        .preset-button.active {
            background: #2196F3;
            color: white;
            border-color: #2196F3;
            font-weight: bold;
        }

        .preset-description {
            font-size: 11px;
            color: #555;
            margin-left: 10px;
            font-style: italic;
        }

        table {
            border-collapse: collapse;
            width: 100%;
            font-size: 14px;
        }
        thead th {
            position: sticky;
            top: 0;
            background: #f2f2f2;
            z-index: 2;
            cursor: pointer;
            user-select: none;
            border-bottom: 2px solid #999;
        }
        th, td {
            border: 1px solid #ccc;
            padding: 6px 8px;
            text-align: right;
        }
        td:first-child, th:first-child,
        td:nth-child(2), th:nth-child(2) {
            text-align: left;
        }
        td:nth-child(7), th:nth-child(7) {
            white-space: nowrap;
        }
        tr:hover {
            background: #eef6ff;
        }

        .good { color: #006400; font-weight: bold; }
        .bad  { color: #b00020; font-weight: bold; }

        .arrow { font-size: 10px; margin-left: 4px; }
        button { cursor: pointer; }

        .score-cell {
            font-weight: bold;
        }
    </style>

    <script>
        /* =======================
           Column mapping
        ======================= */
        const COL = {
            ISIN: 0,
            ISSUER: 1,
            PRICE: 2,
            CURRENCY: 3,
            PRICE_R: 4,
            COUPON: 5,
            MATURITY: 6,
            CURR_COUPON: 7,
            CAPITAL_AT_MAT: 8,
            CAGR: 9
        };

        /* =======================
           Sorting
        ======================= */
        let currentSortCol = COL.CAGR;
        let currentSortDir = "desc";

        function parseValue(v) {
            v = v.replace(/[€CHF%]/g, "").replace(",", ".").trim();
            const n = parseFloat(v);
            return isNaN(n) ? v : n;
        }

        function sortTable(col, initial) {
            const table = document.getElementById("bondTable");
            const tbody = table.tBodies[0];
            const rows = Array.from(tbody.rows);

            const ths = table.tHead.rows[0].cells;
            let dir = "asc";

            if (!initial && col === currentSortCol) {
                dir = currentSortDir === "asc" ? "desc" : "asc";
            } else if (initial) {
                dir = currentSortDir;
            } else {
                dir = "desc";
            }

            currentSortCol = col;
            currentSortDir = dir;

            Array.from(ths).forEach(h => {
                const s = h.querySelector(".arrow");
                if (s) s.textContent = "";
            });
            ths[col].querySelector(".arrow").textContent = dir === "asc" ? "▲" : "▼";

            rows.sort((a, b) => {
                const x = parseValue(a.cells[col].innerText);
                const y = parseValue(b.cells[col].innerText);
                if (typeof x === "number" && typeof y === "number") {
                    return dir === "asc" ? x - y : y - x;
                }
                return dir === "asc"
                        ? x.toString().localeCompare(y.toString())
                        : y.toString().localeCompare(x.toString());
            });

            rows.forEach(r => tbody.appendChild(r));
        }

        /* =======================
           Filtering
        ======================= */
        function parseNum(s) {
            return parseFloat(s.replace(",", "."));
        }

        function filterTable() {
            const isin = document.getElementById("filterIsin").value.toLowerCase();
            const issuer = document.getElementById("filterIssuer").value.toLowerCase();
            const priceMax = parseFloat(document.getElementById("filterPrice").value || "0");
            const currency = document.getElementById("filterCurrency").value;
            const minMat = document.getElementById("filterMinMat").value;
            const maxMat = document.getElementById("filterMaxMat").value;
            const minCoupon = parseFloat(document.getElementById("filterMinCoupon").value || "0");
            const minCapitalAtMat = parseFloat(document.getElementById("filterMinCapitalAtMat").value || "0");
            const minCagr = parseFloat(document.getElementById("filterMinCagr").value || "0");

            const rows = document.querySelectorAll("#bondTable tbody tr");

            rows.forEach(r => {
                const isinCell = r.cells[COL.ISIN].innerText.toLowerCase();
                const issuerCell = r.cells[COL.ISSUER].innerText.toLowerCase();
                const priceCell = parseNum(r.cells[COL.PRICE].innerText);
                const currencyCell = r.cells[COL.CURRENCY].innerText;
                const mat = r.cells[COL.MATURITY].innerText;
                const currCoupon = parseNum(r.cells[COL.CURR_COUPON].innerText);
                const capitalAtMat = parseNum(r.cells[COL.CAPITAL_AT_MAT].innerText);
                const cagr = parseNum(r.cells[COL.CAGR].innerText);

                let ok = true;
                if (isin && isinCell.indexOf(isin) === -1) ok = false;
                if (issuer && issuerCell.indexOf(issuer) === -1) ok = false;
                if (priceMax && priceMax < priceCell) ok = false;
                if (currency && currencyCell !== currency) ok = false;
                if (minMat && mat < minMat) ok = false;
                if (maxMat && mat > maxMat) ok = false;
                if (currCoupon < minCoupon) ok = false;
                if (capitalAtMat < minCapitalAtMat) ok = false;
                if (cagr < minCagr) ok = false;

                r.style.display = ok ? "" : "none";
            });
        }

        function clearColumnFilters() {
            document.getElementById("filterIsin").value = "";
            document.getElementById("filterIssuer").value = "";
            document.getElementById("filterPrice").value = "";
            document.getElementById("filterCurrency").value = "";
            document.getElementById("filterMinCoupon").value = "";
            document.getElementById("filterMinCapitalAtMat").value = "";
            document.getElementById("filterMinCagr").value = "";
            setDefaultMaturityFilters();
            filterTable();
            updatePresetButtons(null);
            document.getElementById("presetDesc").textContent = "";
        }

        /* =======================
           Export CSV
        ======================= */
        function exportCSV() {
            const rows = document.querySelectorAll("#bondTable tr:not([style*='display: none'])");
            let csv = [];

            rows.forEach(r => {
                const cols = Array.from(r.cells).map(td => {
                    const text = td.textContent.replace(/\s+/g, " ").trim();
                    return '"' + text.replace(/"/g, '""') + '"';
                });
                csv.push(cols.join(","));
            });

            const blob = new Blob([csv.join("\n")], { type: "text/csv;charset=utf-8;" });
            const url = URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = "bond-report.csv";
            a.click();
            URL.revokeObjectURL(url);
        }

        /* =======================
           Heatmap
        ======================= */
        function lerp(a, b, t) {
            return Math.round(a + (b - a) * t);
        }

        function lerpColor(c1, c2, t) {
            return "rgb(" +
                lerp(c1[0], c2[0], t) + "," +
                lerp(c1[1], c2[1], t) + "," +
                lerp(c1[2], c2[2], t) + ")";
        }

        function applyHeatmap() {
            const rows = document.querySelectorAll("#bondTable tbody tr");

            const red    = [255, 215, 215];
            const yellow = [255, 245, 190];
            const green  = [215, 245, 215];

            rows.forEach(r => {
                // === Current Coupon ===
                const v = parseNum(r.cells[COL.CURR_COUPON].innerText);
                let bg;
                if (v <= 1.5) bg = "rgb(" + red.join(",") + ")";
                else if (v < 3.0) bg = lerpColor(red, yellow, (v - 1.5) / 1.5);
                else if (v < 5.0) bg = lerpColor(yellow, green, (v - 3.0) / 2.0);
                else bg = "rgb(" + green.join(",") + ")";
                r.cells[COL.CURR_COUPON].style.backgroundColor = bg;

                // === Total Capital at Maturity ===
                const w = parseNum(r.cells[COL.CAPITAL_AT_MAT].innerText);
                let bg2;
                if (w <= 1150) bg2 = "rgb(" + red.join(",") + ")";
                else if (w < 1400) bg2 = lerpColor(red, yellow, (w - 1150) / 250);
                else if (w < 1650) bg2 = lerpColor(yellow, green, (w - 1400) / 250);
                else bg2 = "rgb(" + green.join(",") + ")";
                r.cells[COL.CAPITAL_AT_MAT].style.backgroundColor = bg2;

                // === CAGR ===
                const cagr = parseNum(r.cells[COL.CAGR].innerText);
                let bg3;
                if (cagr <= 1.0) {
                    bg3 = "rgb(" + red.join(",") + ")";
                } else if (cagr <= 2.5) {
                    bg3 = lerpColor(red, yellow, (cagr - 1.0) / 1.5);
                } else if (cagr <= 3.5) {
                    bg3 = lerpColor(yellow, green, (cagr - 2.5) / 1.0);
                } else if (cagr <= 4.5) {
                    const darkGreen = [100, 200, 100];
                    bg3 = lerpColor(green, darkGreen, (cagr - 3.5) / 1.0);
                } else {
                    bg3 = "rgb(50, 180, 50)";
                }
                r.cells[COL.CAGR].style.backgroundColor = bg3;
            });
        }

        /* =======================
           Maturity defaults
        ======================= */
        function setDefaultMaturityFilters() {
            const today = new Date();
            const min = new Date(today.getFullYear() + 5, today.getMonth(), today.getDate());
            const max = new Date(today.getFullYear() + 30, today.getMonth(), today.getDate());

            function formatDate(d) {
                const y = d.getFullYear();
                const m = ("0" + (d.getMonth() + 1)).slice(-2);
                const day = ("0" + d.getDate()).slice(-2);
                return y + "-" + m + "-" + day;
            }

            document.getElementById("filterMinMat").value = formatDate(min);
            document.getElementById("filterMaxMat").value = formatDate(max);
        }

        /* =======================
           PRESET PROFILES
        ======================= */

        const PRESETS = {
            cagrAggressive: {
                name: "CAGR - Aggressive Growth",
                filters: {
                    maxPrice: 85,
                    minMatYears: 10,
                    maxMatYears: 20,
                    minCoupon: 0,
                    minCapitalAtMat: 1800,
                    minCagr: 3.5
                },
                description: "Cheap bonds, 10–20y maturity, high final capital & CAGR."
            },

            cagrConservative: {
                name: "CAGR - Conservative",
                filters: {
                    maxPrice: 120,
                    minMatYears: 5,
                    maxMatYears: 35,
                    minCoupon: 0,
                    minCapitalAtMat: 1300,
                    minCagr: 2.5
                },
                description: "All solid bonds with stable capital growth."
            },

            incomeHigh: {
                name: "Income - High Yield",
                filters: {
                    maxPrice: 120,
                    minMatYears: 20,
                    maxMatYears: 35,
                    minCoupon: 5.5,
                    minCapitalAtMat: 0,
                    minCagr: 0
                },
                description: "High coupon, long duration, strong cash flow."
            },

            incomeModerate: {
                name: "Income - Moderate Yield",
                filters: {
                    maxPrice: 120,
                    minMatYears: 20,
                    maxMatYears: 35,
                    minCoupon: 4.5,
                    minCapitalAtMat: 0,
                    minCagr: 0
                },
                description: "Moderate coupon bonds, long duration, safer income."
            }
        };

        function applyPreset(presetName) {
            if (presetName === "reset") {
                clearColumnFilters();
                updatePresetButtons(null);
                document.getElementById("presetDesc").textContent = "";
                return;
            }

            const preset = PRESETS[presetName];
            if (!preset) return;

            clearColumnFilters();

            // Price
            document.getElementById("filterPrice").value = preset.filters.maxPrice;

            // Maturity
            const today = new Date();
            const minMat = new Date(today);
            minMat.setFullYear(today.getFullYear() + preset.filters.minMatYears);
            const maxMat = new Date(today);
            maxMat.setFullYear(today.getFullYear() + preset.filters.maxMatYears);

            function formatDate(d) {
                const y = d.getFullYear();
                const m = ("0" + (d.getMonth() + 1)).slice(-2);
                const day = ("0" + d.getDate()).slice(-2);
                return y + "-" + m + "-" + day;
            }

            document.getElementById("filterMinMat").value = formatDate(minMat);
            document.getElementById("filterMaxMat").value = formatDate(maxMat);

            // Mode-specific filters
            document.getElementById("filterMinCoupon").value = preset.filters.minCoupon || "";
            document.getElementById("filterMinCapitalAtMat").value = preset.filters.minCapitalAtMat || "";
            document.getElementById("filterMinCagr").value = preset.filters.minCagr || "";

            filterTable();
            updatePresetButtons(presetName);
            document.getElementById("presetDesc").textContent = "✓ " + preset.description;
        }

        function updatePresetButtons(activePreset) {
            const ids = ["cagrAggressive", "cagrConservative", "incomeHigh", "incomeModerate"];
            ids.forEach(id => {
                const btn = document.getElementById("preset-" + id);
                if (btn) btn.classList.toggle("active", id === activePreset);
            });
            document.getElementById("preset-reset").classList.remove("active");
        }

        /* =======================
           Init
        ======================= */
        document.addEventListener("DOMContentLoaded", () => {
            setDefaultMaturityFilters();
            applyPreset("cagrAggressive");   // Default preset
            applyHeatmap();
            sortTable(COL.CAGR,true);
        });
    </script>
</head>

<body>

<h2>
    Bond Yield Ranking (EUR)
    <span style="font-size:12px;color:#666;">
        — 📅 ${generatedAt}
    </span>
</h2>

<!-- =======================
     PROFILE PRESETS UI
======================= -->
<div class="profile-presets">
    <label>Investor profiles:</label>

    <button class="preset-button active" id="preset-cagrAggressive"
            onclick="applyPreset('cagrAggressive')"
            title="Cheap bonds, 10–20y, high CAGR">
        🚀 CAGR Aggressive
    </button>

    <button class="preset-button" id="preset-cagrConservative"
            onclick="applyPreset('cagrConservative')"
            title="Stable bonds, broad maturity">
        🛡️ CAGR Conservative
    </button>

    <button class="preset-button" id="preset-incomeHigh"
            onclick="applyPreset('incomeHigh')"
            title="High coupon bonds, long duration">
        💎 Income High
    </button>

    <button class="preset-button" id="preset-incomeModerate"
            onclick="applyPreset('incomeModerate')"
            title="Moderate coupon bonds, long duration">
        💰 Income Moderate
    </button>

    <button class="preset-button" id="preset-reset"
            onclick="applyPreset('reset')"
            title="Clear all filters">
        🧹 Reset
    </button>

    <span class="preset-description" id="presetDesc"></span>
</div>

<div class="controls">
    <label>
        Maturity from:
        <input id="filterMinMat" type="date" onchange="filterTable()">
    </label>

    <label>
        to:
        <input id="filterMaxMat" type="date" onchange="filterTable()">
    </label>

    <button onclick="clearColumnFilters()" title="Remove all filters except the maturity range">🧹 Clear column filters</button>

    <div class="spacer"></div>
    <button onclick="exportCSV()">📥 Export CSV</button>
</div>

<table id="bondTable">
    <thead>
    <tr>
        <th onclick="sortTable(COL.ISIN)">ISIN<span class="arrow"></span><br>
            <input id="filterIsin" type="text" placeholder="e.g. US900123AT75"
                   onclick="event.stopPropagation()" oninput="filterTable()">
        </th>
        <th onclick="sortTable(COL.ISSUER)">Issuer<span class="arrow"></span><br>
            <input id="filterIssuer" type="text" placeholder="e.g. Romania"
                   onclick="event.stopPropagation()" oninput="filterTable()">
        </th>
        <th onclick="sortTable(COL.PRICE)">Price<span class="arrow"></span><br>
            <input id="filterPrice" type="number" step="10" placeholder="max"
                   onclick="event.stopPropagation()" oninput="filterTable()" style="width:60px;">
        </th>
        <th onclick="sortTable(COL.CURRENCY)">Currency<span class="arrow"></span><br>
            <select id="filterCurrency" onchange="filterTable()" onclick="event.stopPropagation()">
                <option value="">All</option>
                <#list currencies as c>
                <option value="${c}">${c}</option>
            </#list>
            </select>
        </th>
        <th onclick="sortTable(COL.PRICE_R)">Price (${reportCurrency})<span class="arrow"></span></th>
        <th onclick="sortTable(COL.COUPON)">Coupon %<span class="arrow"></span></th>
        <th onclick="sortTable(COL.MATURITY)">Maturity<span class="arrow"></span></th>
        <th title="Supposing an investment of EUR 100, what would the gain be?"
            onclick="sortTable(COL.CURR_COUPON)">
            Curr. Yield %<span class="arrow"></span><br>
            <input id="filterMinCoupon" type="number" step="0.5" placeholder="min %"
                   onclick="event.stopPropagation()" oninput="filterTable()" style="width:70px;">
        </th>
        <th title="Supposing an investment of EUR 1,000, what amount will you have at maturity?"
            onclick="sortTable(COL.CAPITAL_AT_MAT)">
            Tot. Capital to Maturity (per EUR 1,000)<span class="arrow"></span><br>
            <input id="filterMinCapitalAtMat" type="number" step="500" placeholder="min"
                   onclick="event.stopPropagation()" oninput="filterTable()" style="width:80px;">
        </th>
        <th title="Annual Growth % (Compound Annual Growth Rate)"
            onclick="sortTable(COL.CAGR)">
            CAGR (%)<span class="arrow"></span><br>
            <input id="filterMinCagr" type="number" step="0.5" placeholder="min %"
                   onclick="event.stopPropagation()" oninput="filterTable()" style="width:80px;">
        </th>
    </tr>
    </thead>

    <tbody>
    <#list bonds as b>
    <tr>
        <td>${b.getIsin()}</td>
        <td>${b.getIssuer()}</td>
        <td class="<#if (b.getPrice() <= 100)>good<#else>bad</#if>">
            ${b.getPrice()?string["0.00"]}
        </td>
        <td>${b.getCurrency()}</td>
        <td>
            ${b.getPriceEur()?string["0.00"]}
        </td>
        <td>${b.getCouponPct()?string["0.00"]}</td>
        <td>${b.getMaturity()}</td>
        <td>
            ${b.getCurrentCoupon()?string["0.00"]}
        </td>
        <td>
            ${b.getFinalCapitalToMat()?string["0"]}
        </td>
        <td>
            ${b.getCagr()?string["0.00"]}
        </td>
    </tr>
    </#list>
    </tbody>
</table>

</body>
</html>
