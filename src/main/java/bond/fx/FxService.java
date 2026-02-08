package bond.fx;

import lombok.SneakyThrows;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class FxService {

    private static final String ECB_FX =
        "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

    // Cache interna per memorizzare i tassi dopo il primo caricamento
    private Map<String, Double> cachedRates;

    private FxService() {}

    private static class Holder {
        private static final FxService INSTANCE = new FxService();
    }

    public static FxService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Restituisce i tassi di cambio.
     * Esegue la chiamata HTTP solo se la cache è vuota (Lazy Loading).
     */
    public synchronized Map<String, Double> loadFxRates() throws Exception {
        if (cachedRates == null) {
            System.out.println("🌐 Fetching FX rates from ECB...");
            cachedRates = fetchFromEcb();
        }
        return cachedRates;
    }

    /**
     * Metodo privato che effettua il lavoro sporco di parsing XML.
     */
    private Map<String, Double> fetchFromEcb() throws Exception {
        Map<String, Double> rates = new HashMap<>();
        rates.put("EUR", 1.0);

        var xml = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(new URL(ECB_FX).openStream());

        var cubes = xml.getElementsByTagName("Cube");
        for (int i = 0; i < cubes.getLength(); i++) {
            var n = cubes.item(i);
            var attrs = n.getAttributes();
            if (attrs != null && attrs.getNamedItem("currency") != null) {
                String ccy = attrs.getNamedItem("currency").getNodeValue();
                double rate = Double.parseDouble(attrs.getNamedItem("rate").getNodeValue());
                rates.put(ccy, rate);
            }
        }
        // Rendiamo la mappa non modificabile prima di salvarla in cache
        return Collections.unmodifiableMap(rates);
    }

    /**
     * Opzionale: permette di forzare un aggiornamento se necessario.
     */
    public synchronized void refresh() {
        this.cachedRates = null;
    }

    @SneakyThrows
    public static double getExchangeRate(String from, String to) {
        if (from.equals(to)) return 1.0;  // No conversion needed

        Map<String, Double> rates = FxService.getInstance().loadFxRates();

        // All rates should be vs EUR in your system
        double rateFrom = rates.getOrDefault(from, 1.0);  // EUR/FROM
        double rateTo = rates.getOrDefault(to, 1.0);      // EUR/TO

        return rateFrom / rateTo;  // Proper cross-rate calculation
    }
}