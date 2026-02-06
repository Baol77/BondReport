package bond.scrape;

import java.util.Map;

public interface ISovereignSpreadProvider {
    String name();
    Map<String, Double> fetchSpreads() throws Exception;
}