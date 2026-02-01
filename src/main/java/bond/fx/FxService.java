package bond.fx;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FxService {

    private static final String ECB_FX =
        "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

    public Map<String, Double> loadFxRates() throws Exception {
        Map<String, Double> rates = new HashMap<>();
        rates.put("EUR", 1.0);

        var xml = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(new URL(ECB_FX).openStream());

        var cubes = xml.getElementsByTagName("Cube");
        for (int i = 0; i < cubes.getLength(); i++) {
            var n = cubes.item(i);
            if (n.getAttributes() != null && n.getAttributes().getNamedItem("currency") != null) {
                String ccy = n.getAttributes().getNamedItem("currency").getNodeValue();
                double rate = Double.parseDouble(n.getAttributes().getNamedItem("rate").getNodeValue());
                rates.put(ccy, rate);
            }
        }
        return rates;
    }
}