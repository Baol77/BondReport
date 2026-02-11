package bond.fx;

import lombok.SneakyThrows;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

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
}