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

        /* Price coloring */
        .good { color: #006400; font-weight: bold; }
        .bad  { color: #b00020; font-weight: bold; }

        .arrow { font-size: 10px; margin-left: 4px; }
        button { cursor: pointer; }
    </style>
    <script>
        let currentSortCol = 7;
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
                const isinCell = r.cells[0].innerText.toLowerCase();
                const issuerCell = r.cells[1].innerText.toLowerCase();
                const priceCell = parseNum(r.cells[2].innerText);
                const currencyCell = r.cells[3].innerText;
                const mat = r.cells[6].innerText;
                const currYield = parseNum(r.cells[7].innerText);
                const totalYield = parseNum(r.cells[8].innerText);
                const score = parseNum(r.cells[9].innerText);

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

        /* ===== Gradient heatmap on Current Yield % ===== */

        function lerp(a, b, t) {
            return Math.round(a + (b - a) * t);
        }

        function lerpColor(c1, c2, t) {
            return "rgb(" +
                lerp(c1[0], c2[0], t) + "," +
                lerp(c1[1], c2[1], t) + "," +
                lerp(c1[2], c2[2], t) + ")";
        }

        function parseNum(s) {
            return parseFloat(s.replace(",", "."));
        }

        function applyHeatmap() {
            const rows = document.querySelectorAll("#bondTable tbody tr");

            const red    = [255, 215, 215];   // ~0–1%
            const yellow = [255, 245, 190];   // ~3%
            const green  = [215, 245, 215];   // ~5%+

            rows.forEach(r => {
                const v = parseNum(r.cells[7].innerText); // Curr. Yield %
                const w = parseNum(r.cells[8].innerText); // Total Yield to maturity
                const y = parseNum(r.cells[9].innerText); // Score

                let bg;
                if (v <= 1) {
                    bg = "rgb(" + red.join(",") + ")";
                } else if (v < 3) {
                    bg = lerpColor(red, yellow, (v - 1) / 2);
                } else if (v < 5) {
                    bg = lerpColor(yellow, green, (v - 3) / 2);
                } else {
                    bg = "rgb(" + green.join(",") + ")";
                }

                r.cells[7].style.backgroundColor = bg;

                let bg2;
                if (w <= 1100) {
                    bg2 = "rgb(" + red.join(",") + ")";
                } else if (w < 1400) {
                    bg2 = lerpColor(red, yellow, (w - 1100) / 300);
                } else if (w < 1700) {
                    bg2 = lerpColor(yellow, green, (w - 1500) / 200);
                } else {
                    bg2 = "rgb(" + green.join(",") + ")";
                }

                r.cells[8].style.backgroundColor = bg2;

                let bg3;
                if (y <= 0.16) {
                    bg3 = lerpColor(red, yellow, y / 0.16);
                } else if (y <= 0.30) {
                    bg3 = lerpColor(yellow, green, (y - 0.16) / (0.30 - 0.16));
                } else {
                    bg3 = "rgb(" + green.join(",") + ")";
                }

                r.cells[9].style.backgroundColor = bg3;
            });
        }

        function setDefaultMaturityFilters() {
            var today = new Date();

            // Création des dates Min (A+5) et Max (A+30)
            var min = new Date(today.getFullYear() + 5, today.getMonth(), today.getDate());
            var max = new Date(today.getFullYear() + 30, today.getMonth(), today.getDate());

            // Fonction de formatage compatible sans Template Literals
            var formatDateLocal = function(date) {
                var year = date.getFullYear();
                // Utilisation de slice(-2) pour le padding, plus compatible que padStart sur vieux navigateurs
                var month = ("0" + (date.getMonth() + 1)).slice(-2);
                var day = ("0" + date.getDate()).slice(-2);

                return year + "-" + month + "-" + day;
            };

            document.getElementById("filterMinMat").value = formatDateLocal(min);
            document.getElementById("filterMaxMat").value = formatDateLocal(max);
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

        function normalize(v, min, max) {
            if (max === min) return 0;
            return (v - min) / (max - min);
        }

        function computeScore() {
            const rows = document.querySelectorAll("#bondTable tbody tr");

            const curr = Array.from(rows).map(r => parseNum(r.cells[7].innerText));
            const tot  = Array.from(rows).map(r => parseNum(r.cells[8].innerText));

            const minC = Math.min(...curr), maxC = Math.max(...curr);
            const minT = Math.min(...tot),  maxT = Math.max(...tot);

            const longTerm = document.getElementById("profileLongTerm")?.checked;
            const alpha = longTerm ? 0.35 : 0.6;   // ✔ règle demandée

            rows.forEach((r, i) => {
                const c = curr[i];
                const t = tot[i];

                const score =
                    alpha * normalize(c, minC, maxC) +
                    (1 - alpha) * normalize(t, minT, maxT);

                r.cells[9].innerText = score.toFixed(3);
            });

            sortTable(9, true);
        }

        document.addEventListener("DOMContentLoaded", () => {
            setDefaultMaturityFilters();
            filterTable();
            computeScore();
            sortTable(9, true);
            applyHeatmap();
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

    <label title="Long-term profile: prioritizes Tot. Yield (alpha = 0.35). Unchecked: prioritizes Curr. Yield (alpha = 0.6).">
        <input type="checkbox" id="profileLongTerm" checked onchange="computeScore(); applyHeatmap();">
        Long-term profile
    </label>

    <div class="spacer"></div>
    <button onclick="exportCSV()">📥 Export CSV</button>
</div>

<table id="bondTable">
    <thead>
    <tr>
        <th onclick="sortTable(0)">ISIN<span class="arrow"></span><br>
            <input id="filterIsin" type="text" placeholder="e.g. US900123AT75" onclick="event.stopPropagation()" oninput="filterTable()">
        </th>
        <th onclick="sortTable(1)">Issuer<span class="arrow"></span><br>
            <input id="filterIssuer" type="text" placeholder="e.g. Romania" onclick="event.stopPropagation()" oninput="filterTable()">
        </th>
        <th onclick="sortTable(2)">Price <span class="arrow"></span><br>
            <input id="filterPrice" type="number" step="10" placeholder="max" onclick="event.stopPropagation()"
                   oninput="filterTable()" style="width:60px;">
        </th>
        <th onclick="sortTable(3)">Currency<span class="arrow"></span><br>
            <select id="filterCurrency" onchange="filterTable()" onclick="event.stopPropagation()">
                <option value="">All</option>
                <#list currencies as c>
                <option value="${c}">${c}</option>
            </#list>
            </select>
        </th>
        <th onclick="sortTable(4)">Price (${reportCurrency}) <span class="arrow"></span></th>
        <th onclick="sortTable(5)">Coupon % <span class="arrow"></span></th>
        <th onclick="sortTable(6)">Maturity <span class="arrow"></span></th>
        <th title="Supposing an investment of ${reportCurrency}100, what would the gain be?" onclick="sortTable(7)">Curr. Yield %<span class="arrow"></span><br>
            <input id="filterMinYield" type="number" step="0.5" placeholder="min" onclick="event.stopPropagation()"
                   oninput="filterTable()" style="width:70px;">
        </th>
        <th title="Supposing an investment of ${reportCurrency}1,000, what amount will you have at maturity?" onclick="sortTable(8)">Tot. Yield to Maturity (per ${reportCurrency} 1,000)<span class="arrow"></span><br>
            <input id="filterMinTotal" type="number" step="500" placeholder="min" onclick="event.stopPropagation()"
                   oninput="filterTable()" style="width:80px;">
        </th>
        <th onclick="sortTable(9)" title="Combined score of Current Yield and Total Yield (= 0.45·norm(CurrentYield) + 0.55·norm(TotalYield))">Score<span class="arrow"></span><br>
            <input id="filterScore" type="number" step="0.1" placeholder="min" onclick="event.stopPropagation()"
                   oninput="filterTable()" style="width:70px;">
        </th>
    </tr>
    </thead>

    <tbody>
    <#list bonds as b>
    <tr>
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
        <td class="score"></td>
    </tr>
    </#list>
    </tbody>
</table>

</body>
</html>