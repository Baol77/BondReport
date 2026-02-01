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
            margin-bottom: 12px;
        }
        table {
            border-collapse: collapse;
            width: 100%;
            font-size: 14px;
        }
        th, td {
            border: 1px solid #ccc;
            padding: 6px 8px;
            text-align: right;
        }
        th {
            background: #f2f2f2;
            text-align: center;
        }
        td:first-child, th:first-child {
            text-align: left;
        }
        td:nth-child(2), th:nth-child(2) {
            text-align: left;
        }
        tr:hover {
            background: #eef6ff;
        }
        .good { color: #006400; font-weight: bold; }
        .bad  { color: #b00020; }
    </style>
</head>
<body>

<h2>Bond Yield Ranking (EUR equivalent, maturity &gt; 5 years)</h2>

<table>
    <thead>
    <tr>
        <th>ISIN</th>
        <th>Issuer</th>
        <th>Price (EUR)</th>
        <th>Coupon %</th>
        <th>Maturity</th>
        <th>Orig. Currency</th>
        <th>Current Yield %</th>
        <th>Total Yield % to Maturity</th>
    </tr>
    </thead>

    <tbody>
    <#list bonds as b>
        <tr>
            <td>${b.isin()}</td>
            <td>${b.issuer()}</td>
            <td class="<#if (b.priceEur() <=100)>good<#else>bad</#if>">
                ${b.priceEur()?string["0.00"]}</td>
            <td>${b.couponPct()?string["0.00"]}</td>
            <td>${b.maturity()}</td>
            <td>${b.currency()}</td>
            <td>${b.currentYieldPct()?string["0.00"]}</td>
            <td>${b.totalYieldPctToMaturity()?string["0"]}</td>
        </tr>
    </#list>
    </tbody>
</table>

</body>
</html>