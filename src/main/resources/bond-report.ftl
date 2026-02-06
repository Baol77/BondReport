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

        .profile-box {
            border: 1px solid #bbb;
            border-radius: 6px;
            padding: 8px 12px;
            background: #f9f9f9;
            display: flex;
            align-items: center;
            gap: 14px;
        }

        .profile-box legend {
            font-size: 12px;
            font-weight: bold;
            color: #444;
            padding: 0 6px;
        }

        .profile-option {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 13px;
            cursor: pointer;
        }

        .profile-option input {
            cursor: pointer;
        }

        .profile-option span {
            border-bottom: 1px dotted #666;
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

        .risk-scale {
            width: 100%;
            height: 6px;
            /* Gradient du vert (Income) au rouge (Opportunistic) */
            background: linear-gradient(to right, #28a745, #ffc107, #fd7e14, #dc3545);
            border-radius: 3px;
            margin-bottom: 4px;
            position: relative;
        }

        .risk-labels {
            display: flex;
            justify-content: space-between;
            font-size: 10px;
            font-weight: bold;
            color: #666;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 8px;
        }

        /* On ajuste la box des profils pour empiler verticalement la barre et les radios */
        .profile-container {
            display: flex;
            flex-direction: column;
            width: 380px; /* Ajuste selon tes besoins */
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
            CURR_YIELD: 7,
            TOTAL_YIELD: 8,
            SCORE: 9
        };

        /* =======================
           Sorting
        ======================= */
        let currentSortCol = COL.SCORE;
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
            const minYield = parseFloat(document.getElementById("filterMinYield").value || "0");
            const minTotal = parseFloat(document.getElementById("filterMinTotal").value || "0");
            const minScore = parseFloat(document.getElementById("filterScore").value || "0");

            const rows = document.querySelectorAll("#bondTable tbody tr");

            rows.forEach(r => {
                const isinCell = r.cells[COL.ISIN].innerText.toLowerCase();
                const issuerCell = r.cells[COL.ISSUER].innerText.toLowerCase();
                const priceCell = parseNum(r.cells[COL.PRICE].innerText);
                const currencyCell = r.cells[COL.CURRENCY].innerText;
                const mat = r.cells[COL.MATURITY].innerText;
                const currYield = parseNum(r.cells[COL.CURR_YIELD].innerText);
                const totalYield = parseNum(r.cells[COL.TOTAL_YIELD].innerText);
                const score = parseNum(r.cells[COL.SCORE].innerText);

                let ok = true;
                if (isin && isinCell.indexOf(isin) === -1) ok = false;
                if (issuer && issuerCell.indexOf(issuer) === -1) ok = false;
                if (priceMax && priceMax < priceCell) ok = false;
                if (currency && currencyCell !== currency) ok = false;
                if (minMat && mat < minMat) ok = false;
                if (maxMat && mat > maxMat) ok = false;
                if (currYield < minYield) ok = false;
                if (totalYield < minTotal) ok = false;
                if (score < minScore) ok = false;

                r.style.display = ok ? "" : "none";
            });
        }

        function clearColumnFilters() {
            document.getElementById("filterIsin").value = "";
            document.getElementById("filterIssuer").value = "";
            document.getElementById("filterPrice").value = "";
            document.getElementById("filterCurrency").value = "";
            document.getElementById("filterMinYield").value = "";
            document.getElementById("filterMinTotal").value = "";
            document.getElementById("filterScore").value = "";
            filterTable();
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
                const v = parseNum(r.cells[COL.CURR_YIELD].innerText);
                const w = parseNum(r.cells[COL.TOTAL_YIELD].innerText);
                const y = parseNum(r.cells[COL.SCORE].innerText);

                // Curr Yield
                let bg;
                if (v <= 1.5) bg = "rgb(" + red.join(",") + ")";
                else if (v < 3.0) bg = lerpColor(red, yellow, (v - 1.5) / 1.5);
                else if (v < 5.0) bg = lerpColor(yellow, green, (v - 3.0) / 2.0);
                else bg = "rgb(" + green.join(",") + ")";
                r.cells[COL.CURR_YIELD].style.backgroundColor = bg;

                // Total Yield
                let bg2;
                if (w <= 1150) bg2 = "rgb(" + red.join(",") + ")";
                else if (w < 1400) bg2 = lerpColor(red, yellow, (w - 1150) / 250);
                else if (w < 1650) bg2 = lerpColor(yellow, green, (w - 1400) / 250);
                else bg2 = "rgb(" + green.join(",") + ")";
                r.cells[COL.TOTAL_YIELD].style.backgroundColor = bg2;

                // Score background
                let bg3;
                if (y <= 0.45) {
                    bg3 = "rgb(" + red.join(",") + ")";
                } else if (y <= 0.65) {
                    bg3 = lerpColor(red, yellow, (y - 0.45) / 0.20);   // red → yellow
                } else if (y <= 0.85) {
                    bg3 = lerpColor(yellow, green, (y - 0.65) / 0.20); // yellow → green
                } else {
                    bg3 = "rgb(" + green.join(",") + ")";
                }
                 r.cells[COL.SCORE].style.backgroundColor = bg3;
            });
        }

        /* =======================
           Profile selector
        ======================= */
        function updateScores() {
            const profile = document.querySelector("input[name='profile']:checked").value.toLowerCase();
            const rows = document.querySelectorAll("#bondTable tbody tr");

            rows.forEach(r => {
                const cell = r.querySelector(".score-cell");
                const raw = r.dataset["score" + profile];
                const num = parseFloat(raw.replace(",", "."));
                cell.innerText = isNaN(num) ? "" : num.toFixed(3);
            });

            sortTable(COL.SCORE, true);
            applyHeatmap();
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

        document.addEventListener("DOMContentLoaded", () => {
            setDefaultMaturityFilters();
            filterTable();
            updateScores();
        });
    </script>
</head>

<body>

<h2>
    Bond Yield Ranking (${reportCurrency})
    <span style="font-size:12px;color:#666;">
        — 📅 ${generatedAt}
    </span>
</h2>

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

    <fieldset class="profile-box">
        <legend>Scoring profile & Risk appetite</legend>

        <div class="profile-container">
            <div class="risk-scale"></div>

            <div class="risk-labels">
                <span>Lower Risk</span>
                <span>Higher Risk</span>
            </div>

            <div style="display: flex; justify-content: space-between;">
                <label class="profile-option" title="Focuses on high current income. Prioritizes coupon and yield.">
                    <input type="radio" name="profile" value="INCOME" onchange="updateScores()">
                    <span>Income</span>
                </label>

                <label class="profile-option" title="Balanced trade-off between income, risk, and total return.">
                    <input type="radio" name="profile" value="BALANCED" checked onchange="updateScores()">
                    <span>Balanced</span>
                </label>

                <label class="profile-option" title="Targets higher long-term total return. Accepts more volatility.">
                    <input type="radio" name="profile" value="GROWTH" onchange="updateScores()">
                    <span>Growth</span>
                </label>

                <label class="profile-option" title="Seeks maximum yield. Tolerates high credit and FX risk.">
                    <input type="radio" name="profile" value="OPPORTUNISTIC" onchange="updateScores()">
                    <span>Opportunistic</span>
                </label>
            </div>
        </div>
    </fieldset>

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
        <th title="Supposing an investment of ${reportCurrency}100, what would the gain be?"
            onclick="sortTable(COL.CURR_YIELD)">
            Curr. Yield %<span class="arrow"></span><br>
            <input id="filterMinYield" type="number" step="0.5" placeholder="min"
                   onclick="event.stopPropagation()" oninput="filterTable()" style="width:70px;">
        </th>
        <th title="Supposing an investment of ${reportCurrency}1,000, what amount will you have at maturity?"
            onclick="sortTable(COL.TOTAL_YIELD)">
            Tot. Yield to Maturity (per ${reportCurrency} 1,000)<span class="arrow"></span><br>
            <input id="filterMinTotal" type="number" step="500" placeholder="min"
                   onclick="event.stopPropagation()" oninput="filterTable()" style="width:80px;">
        </th>
        <th onclick="sortTable(COL.SCORE)" title="Profile-based composite score">
            Score<span class="arrow"></span><br>
            <input id="filterScore" type="number" step="0.1" placeholder="min"
                   onclick="event.stopPropagation()" oninput="filterTable()" style="width:70px;">
        </th>
    </tr>
    </thead>

    <tbody>
    <#list rows as r>
    <#assign b = r.bond()>
    <#assign s = r.scores()>
    <tr
            data-scoreINCOME="${s['INCOME']?string['0.000']}"
            data-scoreBALANCED="${s['BALANCED']?string['0.000']}"
            data-scoreGROWTH="${s['GROWTH']?string['0.000']}"
            data-scoreOPPORTUNISTIC="${s['OPPORTUNISTIC']?string['0.000']}"
    >
    <td>${b.isin()}</td>
    <td>${b.issuer()}</td>

    <td class="<#if (b.price() <= 100)>good<#else>bad</#if>">
        ${b.price()?string["0.00"]}
    </td>

    <td>${b.currency()}</td>

    <td>
        <#if reportCurrency == "EUR">
        ${b.priceEur()?string["0.00"]}
        <#else>
        ${b.priceChf()?string["0.00"]}
    </#if>
    </td>

    <td>${b.couponPct()?string["0.00"]}</td>
    <td>${b.maturity()}</td>

    <td>
        <#if reportCurrency == "EUR">
        ${b.currentYieldPct()?string["0.00"]}
        <#else>
        ${b.currentYieldPctChf()?string["0.00"]}
    </#if>
    </td>

    <td>
        <#if reportCurrency == "EUR">
        ${b.totalYieldToMat()?string["0"]}
        <#else>
        ${b.totalYieldToMatChf()?string["0"]}
    </#if>
    </td>

    <td class="score-cell"></td>
    </tr>
    </#list>
    </tbody>
</table>

</body>
</html>
