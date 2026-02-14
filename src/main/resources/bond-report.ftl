<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Bond Yield Ranking</title>
    <!-- External CSS -->
    <style>
        <#include "bond-report.css">
    </style>
</head>

<body>

<!-- Loading Overlay -->
<div id="loadingOverlay" class="loading-overlay">
    <div class="loading-spinner">
        <div class="spinner"></div>
        <div class="loading-text">Loading data...</div>
    </div>
</div>

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
    <#list presets as p>
    <button class="preset-button"
            id="${p.id}"
            onclick="applyPreset('${p.id}')"
            title="${p.description}">
        ${p.emoji} ${p.label}
    </button>
</#list>
<button class="preset-button" id="preset-reset"
        onclick="applyPreset('reset')">🧹 Reset
</button>
<button class="preset-button" id="import-yaml-btn"
        onclick="document.getElementById('yamlFileInput').click()"
        title="Import custom profiles from YAML file">
    📁 Import YAML
</button>
<input type="file" id="yamlFileInput" accept=".yaml,.yml" style="display: none;" onchange="handleYamlImport(event)">
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

    <button onclick="clearColumnFilters()" title="Remove all filters except the maturity range">🧹 Clear column filters
    </button>

    <div class="spacer"></div>
    <button onclick="exportCSV()">📥 Export CSV</button>
    <button onclick="window.portfolioAnalyzer?.openModal()"
            style="margin-left: 20px;"
            title="Create and analyze custom bond portfolios">
        🎯 Portfolio Analysis
    </button>
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
            <input id="filterPriceMin" type="number" step="10" placeholder="min"
                   onclick="event.stopPropagation()" oninput="filterTable()" style="width:60px;">
            <input id="filterPriceMax" type="number" step="10" placeholder="max"
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
        <th onclick="sortTable(COL.RATING)">Rating<span class="arrow"></span><br>
            <select id="filterMinRating" onchange="filterTable()" onclick="event.stopPropagation()">
                <option value="">All</option>
                <option value="AAA">≥ AAA</option>
                <option value="AA+">≥ AA+</option>
                <option value="AA">≥ AA</option>
                <option value="AA-">≥ AA-</option>
                <option value="A+">≥ A+</option>
                <option value="A">≥ A</option>
                <option value="A-">≥ A-</option>
                <option value="BBB+">≥ BBB+</option>
                <option value="BBB">≥ BBB</option>
                <option value="BBB-">≥ BBB-</option>
                <option value="BB+">≥ BB+</option>
                <option value="BB">≥ BB</option>
                <option value="B+">≥ B+</option>
                <option value="B">≥ B</option>
            </select>
        </th>
        <th onclick="sortTable(COL.PRICE_R)">Price (${reportCurrency})<span class="arrow"></span></th>
        <th onclick="sortTable(COL.COUPON)">Coupon %<span class="arrow"></span></th>
        <th onclick="sortTable(COL.MATURITY)">Maturity<span class="arrow"></span></th>
        <th title="Supposing an investment of EUR 100, what would the gain be?"
            onclick="sortTable(COL.CURR_YIELD)">
            Curr. Yield %<span class="arrow"></span><br>
            <input id="filterminYield" type="number" step="0.5" placeholder="min %"
                   onclick="event.stopPropagation()" oninput="filterTable()" style="width:70px;">
        </th>
        <th title="Supposing an investment of EUR 1,000, what amount will you have at maturity?"
            onclick="sortTable(COL.CAPITAL_AT_MAT)">
            Total Return (1k€)<span class="arrow"></span><br>
            <input id="filterMinCapitalAtMat" type="number" step="500" placeholder="min"
                   onclick="event.stopPropagation()" oninput="filterTable()" style="width:80px;">
        </th>
        <th title="Simple Annual Yield % (Annual coupon income as a percentage of the bond’s current price)"
            onclick="sortTable(COL.SAY)">
            SAY (%)<span class="arrow"></span><br>
            <input id="filterMinSAY" type="number" step="0.5" placeholder="min %"
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
        <td class="<#if (b.getRating()?starts_with('A'))>good<#elseif (b.getRating()?starts_with('BBB'))>neutral<#else>bad</#if>">
            <strong>${b.getRating()}</strong>
        </td>
        <td>
            ${b.getPriceEur()?string["0.00"]}
        </td>
        <td>${b.getCouponPct()?string["0.00"]}</td>
        <td style="white-space: nowrap;">${b.getMaturity()}</td>
        <td>
            ${b.getCurrentYield()?string["0.00"]}
        </td>
        <td>
            ${b.getFinalCapitalToMat()?string["0"]}
        </td>
        <td>
            ${b.getSimpleAnnualYield()?string["0.00"]}
        </td>
    </tr>
    </#list>
    </tbody>
</table>

<!-- Legend for heatmap -->
<div class="legend">
    <div class="legend-title" id="legendTitle">SAY Heatmap (Capital Gain Mode)</div>
    <table class="legend-table" id="legendTable">
        <tr>
            <td style="background: rgb(255, 215, 215);">< 1%</td>
            <td>Terrible (FX currency bonds)</td>
        </tr>
        <tr>
            <td style="background: rgb(255, 245, 190);">1–2.5%</td>
            <td>Poor (needs improvement)</td>
        </tr>
        <tr>
            <td style="background: rgb(215, 245, 215);">2.5–3.5%</td>
            <td>Good (standard sovereign)</td>
        </tr>
        <tr>
            <td style="background: rgb(100, 200, 100);">3.5–4.5%</td>
            <td>Excellent (best value)</td>
        </tr>
        <tr>
            <td style="background: rgb(50, 180, 50);">> 4.5%</td>
            <td>⭐ Top performers</td>
        </tr>
    </table>
</div>

<!-- External JavaScript -->
<script>
    <#include "bond-report.js">
</script>
<script>
    <#include "portfolio-analyzer.js" parse=false>
</script>
</body>
</html>