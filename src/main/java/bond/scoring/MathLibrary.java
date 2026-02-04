package bond.scoring;

import java.util.ArrayList;
import java.util.List;

public class MathLibrary {
    private static double percentile(List<Double> values, double p) {
        if (values.isEmpty()) return 0;

        List<Double> sorted = new ArrayList<>(values); // copie mutable
        sorted.sort(Double::compareTo);

        double index = p * (sorted.size() - 1);
        int lo = (int) Math.floor(index);
        int hi = (int) Math.ceil(index);
        if (lo == hi) return sorted.get(lo);

        double w = index - lo;
        return sorted.get(lo) * (1 - w) + sorted.get(hi) * w;
    }



    public static double normWinsorized(double v, List<Double> marketValues) {
        double lo = MathLibrary.percentile(marketValues, 0.05);
        double hi = MathLibrary.percentile(marketValues, 0.95);
        if (hi == lo) return 1.0;
        return Math.max(0, Math.min(1, (v - lo) / (hi - lo)));
    }
}