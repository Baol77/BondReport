package bond.fx;

import lombok.SneakyThrows;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.TreeMap;

/**
 * Service to manage Foreign Exchange (FX) rates using the European Central Bank (ECB) as a source.
 * It uses a Singleton pattern and caches results to minimize HTTP requests.
 */
public class FxService {

    /** URL for the ECB daily exchange rates in XML format */
    private static final String ECB_FX =
        "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

    /** Internal cache to store rates after the first successful fetch */
    private Map<String, Double> cachedRates;

    /** Risk model mapping years to maturity to capital and coupon haircuts */
    private record RiskThreshold(double capitalHaircut, double couponHaircut) {}

    private static final TreeMap<Integer, RiskThreshold> RISK_MODEL = new TreeMap<>();
    static {
        // Key represents the maximum year of the threshold level
        RISK_MODEL.put(5,  new RiskThreshold(0.10, 0.050));
        RISK_MODEL.put(10, new RiskThreshold(0.15, 0.075));
        RISK_MODEL.put(15, new RiskThreshold(0.20, 0.100));
        RISK_MODEL.put(20, new RiskThreshold(0.25, 0.125));
        RISK_MODEL.put(Integer.MAX_VALUE, new RiskThreshold(0.30, 0.150));
    }

    /** Investment phases differently impacted by currency volatility */
    public enum FxPhase {
        BUY,      // Time of purchase (Current SPOT rate)
        COUPON,   // Interest reception (Medium term)
        MATURITY  // Capital repayment (Long term)
    }

    /** Private constructor to enforce Singleton pattern */
    private FxService() {}

    /** Bill Pugh Singleton Implementation for thread-safety and performance */
    private static class Holder {
        private static final FxService INSTANCE = new FxService();
    }

    public static FxService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Retrieves the FX rates map.
     * Uses Lazy Loading: Performs the HTTP call only once; subsequent calls return cached data.
     * * @return An unmodifiable Map of currency codes to their exchange rates relative to EUR.
     */
    public synchronized Map<String, Double> loadFxRates() throws Exception {
        if (cachedRates == null) {
            System.out.println("🌐 Fetching FX rates from ECB...");
            cachedRates = fetchFromEcb();
        }
        return cachedRates;
    }

    /**
     * Connects to the ECB and parses the XML response.
     * The ECB XML structure uses nested <Cube> elements for currency and rate data.
     */
    private Map<String, Double> fetchFromEcb() throws Exception {
        Map<String, Double> rates = new HashMap<>();
        // EUR is the base currency for all ECB rates
        rates.put("EUR", 1.0);

        var xml = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(new URL(ECB_FX).openStream());

        var cubes = xml.getElementsByTagName("Cube");
        for (int i = 0; i < cubes.getLength(); i++) {
            var n = cubes.item(i);
            var attrs = n.getAttributes();

            // Look for elements that have both 'currency' and 'rate' attributes
            if (attrs != null && attrs.getNamedItem("currency") != null) {
                String ccy = attrs.getNamedItem("currency").getNodeValue();
                double rate = Double.parseDouble(attrs.getNamedItem("rate").getNodeValue());
                rates.put(ccy, rate);
            }
        }
        // Return as unmodifiable to prevent accidental changes during runtime
        return Collections.unmodifiableMap(rates);
    }

    /**
     * Clears the internal cache to force a fresh download on the next request.
     */
    public synchronized void refresh() {
        this.cachedRates = null;
    }

    /**
     * Static utility to calculate the exchange rate between any two currencies.
     * * Logic:
     * Since ECB provides rates as 1 EUR = X CURRENCY (e.g., 1 EUR = 1.08 USD),
     * To convert FROM -> TO, we use the cross-rate formula:
     * Rate = (EUR/FROM) / (EUR/TO)
     * * @param from The source currency code (e.g., "USD")
     * @param to The target currency code (e.g., "EUR")
     * @return The conversion factor
     */
    @SneakyThrows
    public static double getExchangeRate(String from, String to) {
        if (from.equals(to)) return 1.0;

        Map<String, Double> rates = FxService.getInstance().loadFxRates();

        // Get rates relative to EUR (default to 1.0 if not found)
        double rateFrom = rates.getOrDefault(from, 1.0);
        double rateTo = rates.getOrDefault(to, 1.0);

        return rateFrom / rateTo;
    }

    /**
     * Calculates the expected FX multiplier with risk adjustment based on the investment phase and maturity.
     * Applies a "Stress Test" logic to exchange rates where longer horizons receive stronger penalties
     * to simulate a depreciation of the bond's currency.
     *
     * @param bondCurrency   The issuer's currency.
     * @param reportCurrency The investor's reference currency.
     * @param fxPhase        The investment phase (BUY, COUPON, or MATURITY).
     * @param yearsToMaturity Years until bond maturity.
     * @return The exchange rate adjusted by a risk coefficient based on maturity.
     */
    public static double fxExpectedMultiplier(String bondCurrency, String reportCurrency, FxPhase fxPhase, int yearsToMaturity) {
        // No FX risk if currencies are identical
        if (bondCurrency.equalsIgnoreCase(reportCurrency)) return 1.0;

        double currentFx = getExchangeRate(reportCurrency, bondCurrency);

        if (fxPhase == FxPhase.BUY) return currentFx;

        // Retrieve the appropriate risk threshold for the given maturity
        RiskThreshold threshold = RISK_MODEL.ceilingEntry(yearsToMaturity).getValue();

        double haircut = (fxPhase == FxPhase.COUPON)
            ? threshold.couponHaircut()
            : threshold.capitalHaircut();

        return currentFx * (1.0 - haircut);
    }
}