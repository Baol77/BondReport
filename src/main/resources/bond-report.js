/* =======================
   COLUMN MAPPING
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
    CAPITAL_AT_MAT: 8,
    CAGR: 9
};

/* =======================
   GLOBAL STATE
======================= */
let currentSortCol = COL.CAGR;
let currentSortDir = "desc";
let currentMode = "cagr"; // "cagr" or "income"

/* =======================
   UTILITY FUNCTIONS
======================= */
function parseValue(v) {
    v = v.replace(/[€CHF%]/g, "").replace(",", ".").trim();
    const n = parseFloat(v);
    return isNaN(n) ? v : n;
}

function parseNum(s) {
    return parseFloat(s.replace(",", "."));
}

/* =======================
   SORTING
======================= */
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
   FILTERING
======================= */
function filterTable() {
    const isin = document.getElementById("filterIsin").value.toLowerCase();
    const issuer = document.getElementById("filterIssuer").value.toLowerCase();
    const priceMax = parseFloat(document.getElementById("filterPrice").value || "0");
    const currency = document.getElementById("filterCurrency").value;
    const minMat = document.getElementById("filterMinMat").value;
    const maxMat = document.getElementById("filterMaxMat").value;
    const minYield = parseFloat(document.getElementById("filterminYield").value || "0");
    const minCapitalAtMat = parseFloat(document.getElementById("filterMinCapitalAtMat").value || "0");
    const minCagr = parseFloat(document.getElementById("filterMinCagr").value || "0");

    const rows = document.querySelectorAll("#bondTable tbody tr");

    rows.forEach(r => {
        const isinCell = r.cells[COL.ISIN].innerText.toLowerCase();
        const issuerCell = r.cells[COL.ISSUER].innerText.toLowerCase();
        const priceCell = parseNum(r.cells[COL.PRICE].innerText);
        const currencyCell = r.cells[COL.CURRENCY].innerText;
        const mat = r.cells[COL.MATURITY].innerText;
        const currCoupon = parseNum(r.cells[COL.CURR_YIELD].innerText);
        const capitalAtMat = parseNum(r.cells[COL.CAPITAL_AT_MAT].innerText);
        const cagr = parseNum(r.cells[COL.CAGR].innerText);

        let ok = true;
        if (isin && isinCell.indexOf(isin) === -1) ok = false;
        if (issuer && issuerCell.indexOf(issuer) === -1) ok = false;
        if (priceMax && priceMax < priceCell) ok = false;
        if (currency && currencyCell !== currency) ok = false;
        if (minMat && mat < minMat) ok = false;
        if (maxMat && mat > maxMat) ok = false;
        if (currCoupon < minYield) ok = false;
        if (capitalAtMat < minCapitalAtMat) ok = false;
        if (cagr < minCagr) ok = false;

        r.style.display = ok ? "" : "none";
    });

    applyHeatmap();
}

function clearColumnFilters() {
    document.getElementById("filterIsin").value = "";
    document.getElementById("filterIssuer").value = "";
    document.getElementById("filterPrice").value = "";
    document.getElementById("filterCurrency").value = "";
    document.getElementById("filterminYield").value = "";
    document.getElementById("filterMinCapitalAtMat").value = "";
    document.getElementById("filterMinCagr").value = "";
    setDefaultMaturityFilters();
    filterTable();
    updatePresetButtons(null);
    document.getElementById("presetDesc").textContent = "";
}

/* =======================
   EXPORT
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
   HEATMAP - DUAL MODE
======================= */
function applyHeatmap() {
    const rows = document.querySelectorAll("#bondTable tbody tr");

    const red = [255, 215, 215];
    const yellow = [255, 245, 190];
    const green = [215, 245, 215];

    rows.forEach(r => {
        // === Current Yield ===
        const v = parseNum(r.cells[COL.CURR_YIELD].innerText);
        let bg;

        if (currentMode === "income") {
            // INCOME MODE: Strong coloring
            if (v <= 3.0) {
                bg = "rgb(" + red.join(",") + ")";
            } else if (v <= 4.5) {
                bg = lerpColor(red, yellow, (v - 3.0) / 1.5);
            } else if (v <= 5.5) {
                bg = lerpColor(yellow, green, (v - 4.5) / 1.0);
            } else if (v <= 6.5) {
                const darkGreen = [100, 200, 100];
                bg = lerpColor(green, darkGreen, (v - 5.5) / 1.0);
            } else {
                bg = "rgb(50, 180, 50)";
            }
        } else {
            // CAGR MODE: Light coloring
            if (v <= 1.5) {
                bg = "rgba(255, 215, 215, 0.3)";
            } else if (v < 3.0) {
                bg = lerpColor(red, yellow, (v - 1.5) / 1.5);
            } else if (v < 5.0) {
                bg = lerpColor(yellow, green, (v - 3.0) / 2.0);
            } else {
                bg = "rgba(215, 245, 215, 0.5)";
            }
        }
        r.cells[COL.CURR_YIELD].style.backgroundColor = bg;

        // === Total Capital at Maturity ===
        const w = parseNum(r.cells[COL.CAPITAL_AT_MAT].innerText);
        let bg2;
        if (w <= 1150) {
            bg2 = "rgba(255, 215, 215, 0.3)";
        } else if (w < 1400) {
            bg2 = lerpColor(red, yellow, (w - 1150) / 250);
        } else if (w < 1650) {
            bg2 = lerpColor(yellow, green, (w - 1400) / 250);
        } else {
            bg2 = "rgba(215, 245, 215, 0.5)";
        }
        r.cells[COL.CAPITAL_AT_MAT].style.backgroundColor = bg2;

        // === CAGR ===
        const cagr = parseNum(r.cells[COL.CAGR].innerText);
        let bg3;

        if (currentMode === "cagr") {
            // CAGR MODE: Strong coloring
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
        } else {
            // INCOME MODE: Light coloring
            if (cagr <= 1.0) {
                bg3 = "rgba(255, 215, 215, 0.2)";
            } else if (cagr <= 2.5) {
                bg3 = "rgba(255, 245, 190, 0.2)";
            } else if (cagr <= 3.5) {
                bg3 = "rgba(215, 245, 215, 0.2)";
            } else {
                bg3 = "rgba(215, 245, 215, 0.3)";
            }
        }
        r.cells[COL.CAGR].style.backgroundColor = bg3;
    });
}

function lerpColor(c1, c2, t) {
    return "rgb(" +
        Math.round(c1[0] + (c2[0] - c1[0]) * t) + "," +
        Math.round(c1[1] + (c2[1] - c1[1]) * t) + "," +
        Math.round(c1[2] + (c2[2] - c1[2]) * t) + ")";
}

function updateLegend() {
    const legendTitle = document.getElementById("legendTitle");
    const legendTable = document.getElementById("legendTable");

    if (!legendTitle || !legendTable) return;

    if (currentMode === "income") {
        legendTitle.textContent = "Current Yield Heatmap (Income Mode)";
        legendTable.innerHTML = `
            <tr>
                <td style="background: rgb(255, 215, 215); padding: 6px 8px;">< 3%</td>
                <td style="padding: 6px 8px;">Too low (worse than risk-free rate)</td>
            </tr>
            <tr>
                <td style="background: rgb(255, 245, 190); padding: 6px 8px;">3–4.5%</td>
                <td style="padding: 6px 8px;">Acceptable (moderate income)</td>
            </tr>
            <tr>
                <td style="background: rgb(215, 245, 215); padding: 6px 8px;">4.5–5.5%</td>
                <td style="padding: 6px 8px;">Good (solid income)</td>
            </tr>
            <tr>
                <td style="background: rgb(100, 200, 100); padding: 6px 8px;">5.5–6.5%</td>
                <td style="padding: 6px 8px;">Excellent (high income)</td>
            </tr>
            <tr>
                <td style="background: rgb(50, 180, 50); padding: 6px 8px;">> 6.5%</td>
                <td style="padding: 6px 8px;">⭐ Outstanding (premium income)</td>
            </tr>
        `;
    } else {
        legendTitle.textContent = "CAGR Heatmap (Capital Gain Mode)";
        legendTable.innerHTML = `
            <tr>
                <td style="background: rgb(255, 215, 215); padding: 6px 8px;">< 1%</td>
                <td style="padding: 6px 8px;">Terrible (FX currency bonds)</td>
            </tr>
            <tr>
                <td style="background: rgb(255, 245, 190); padding: 6px 8px;">1–2.5%</td>
                <td style="padding: 6px 8px;">Poor (needs improvement)</td>
            </tr>
            <tr>
                <td style="background: rgb(215, 245, 215); padding: 6px 8px;">2.5–3.5%</td>
                <td style="padding: 6px 8px;">Good (standard sovereign)</td>
            </tr>
            <tr>
                <td style="background: rgb(100, 200, 100); padding: 6px 8px;">3.5–4.5%</td>
                <td style="padding: 6px 8px;">Excellent (best value)</td>
            </tr>
            <tr>
                <td style="background: rgb(50, 180, 50); padding: 6px 8px;">> 4.5%</td>
                <td style="padding: 6px 8px;">⭐ Top performers</td>
            </tr>
        `;
    }
}

/* =======================
   MATURITY DEFAULTS
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
        <#list presets as p>
          ${p.id}: {
            name: "${p.label}",
            description: "${p.description}",
            filters: {
            <#list p.filters?keys as k>
              ${k}: ${p.filters[k]?is_number?then(p.filters[k]?c, '"' + p.filters[k] + '"')}<#if k_has_next>,</#if>
            </#list>
            }
          }<#if p_has_next>,</#if>
        </#list>
        };


function showLoading() {
    const overlay = document.getElementById("loadingOverlay");
    if (overlay) {
        overlay.classList.add("active");
    }
}

function hideLoading() {
    const overlay = document.getElementById("loadingOverlay");
    if (overlay) {
        overlay.classList.remove("active");
    }
}

function applyPreset(presetName) {
    showLoading();

    setTimeout(() => {
        if (presetName === "reset") {
            clearColumnFilters();
            updatePresetButtons(presetName);
            updateLegend();
            applyHeatmap();
            document.getElementById("presetDesc").textContent = "";
            hideLoading();
            return;
        }

        const preset = PRESETS[presetName];
        if (!preset) {
            hideLoading();
            return;
        }

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
        document.getElementById("filterminYield").value = preset.filters.minYield || "";
        document.getElementById("filterMinCapitalAtMat").value = preset.filters.minCapitalAtMat || "";
        document.getElementById("filterMinCagr").value = preset.filters.minCagr || "";

        currentMode = presetName.startsWith("cagr") ? "cagr" : "income";
        filterTable();
        updatePresetButtons(presetName);
        updateLegend();
        applyHeatmap();
        document.getElementById("presetDesc").textContent = "✓ " + preset.description;

        if (presetName.startsWith("income"))
            sortTable(COL.CURR_YIELD, true);
        else
            sortTable(COL.CAGR, true);

        hideLoading();
    }, 100);
}

function updatePresetButtons(activePreset) {
    const ids = ["cagrAggressive", "cagrConservative", "incomeHigh", "incomeModerate"];
    ids.forEach(id => {
        const btn = document.getElementById(id);
        if (btn) btn.classList.toggle("active", id === activePreset);
    });
    document.getElementById("preset-reset").classList.remove("active");
}

/* =======================
   INITIALIZATION
======================= */
document.addEventListener("DOMContentLoaded", () => {
    setDefaultMaturityFilters();
    applyPreset("cagrAggressive");
});