package bond.scoring;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class IssuerManager {

    public record TrustRule(List<String> keywords, double score, double spreadBps) {}

    private static final double DEFAULT_TRUST = 0.80;
    private static final double NO_TRUST = 0;

    // Set synchronisé pour stocker les inconnus (utile si le scoring est multithreadé)
    private static final Set<String> UNKNOWN_ISSUERS = Collections.synchronizedSet(new TreeSet<>());

    public static List<TrustRule> getTrustRules() {
        return RULES;
    }

    private static final List<TrustRule> RULES = List.of(
        // Tier 1: Core (Spread 0-30 bps)
        new TrustRule(List.of("GERMANIA", "DEUTSCHLAND", "BUND", "GERMANY"), 1.00, 0.0),
        new TrustRule(List.of("FINLANDIA", "FINLAND"), 1.00, 5.0),
        new TrustRule(List.of("OLANDA", "NETHERLANDS"), 1.00, 10.0),
        new TrustRule(List.of("AUSTRIA"), 1.00, 12.0),
        new TrustRule(List.of("SVEZIA", "SWEDEN"), 1.00, 30.0),
        new TrustRule(List.of("NORVEGIA", "NORWAY"), 1.00, 35.0),

        // Tier 2: Strong (Spread 20-60 bps)
        new TrustRule(List.of("FRANCIA", "FRANCE"), 0.97, 20.0),
        new TrustRule(List.of("BELGIO", "BELGIUM"), 0.97, 35.0),
        new TrustRule(List.of("IRLANDA", "IRELAND"), 0.97, 40.0),
        new TrustRule(List.of("REGNO UNITO", "UK", "UNITED KINGDOM"), 0.97, 60.0),

        // Tier 3: Moderate (Spread 45-90 bps)
        new TrustRule(List.of("ESTONIA"), 0.92, 45.0),
        new TrustRule(List.of("SLOVENIA"), 0.92, 65.0),
        new TrustRule(List.of("PORTOGALLO", "PORTUGAL"), 0.92, 75.0),
        new TrustRule(List.of("LETTONIA", "LATVIA"), 0.92, 80.0),
        new TrustRule(List.of("SPAGNA", "SPAIN"), 0.92, 85.0),

        // Tier 4: Higher Risk (Spread 110-160 bps)
        new TrustRule(List.of("LITUANIA", "LITHUANIA"), 0.85, 70.0), // Trust corretto in base allo spread basso
        new TrustRule(List.of("BULGARIA"), 0.85, 110.0),
        new TrustRule(List.of("POLONIA", "POLAND"), 0.85, 115.0),
        new TrustRule(List.of("CROAZIA", "CROATIA"), 0.85, 140.0),
        new TrustRule(List.of("ITALY", "ITALIA", "REPUBBLICA ITALIANA", "BTP"), 0.85, 160.0),

        // Tier 5: Speculative/Risky (Spread 200-300 bps)
        new TrustRule(List.of("CIPRO", "CYPRUS"), 0.78, 200.0),
        new TrustRule(List.of("ROMANIA"), 0.78, 280.0),
        new TrustRule(List.of("UNGHERIA", "HUNGARY"), 0.78, 290.0),

        // Tier 6: High Risk (Spread 300+ bps)
        new TrustRule(List.of("GRECIA", "GREECE", "REPUBBLICA GRECA"), 0.72, 320.0),
        new TrustRule(List.of("TURCHIA", "TURKEY"), 0.65, 450.0)
    );

    /**
     * Retourne le score de confiance basé sur le nom de l'émetteur.
     * Si l'émetteur n'est pas trouvé, il est ajouté à la liste des inconnus.
     */
    public static double getTrustScore(String issuerName) {
        if (issuerName == null || issuerName.isBlank()) {
            return NO_TRUST;
        }

        String normalized = issuerName.toUpperCase();

        var match = RULES.stream()
            .filter(rule -> rule.keywords().stream().anyMatch(normalized::contains))
            .findFirst();

        if (match.isPresent()) {
            return match.get().score();
        } else {
            // Logique de capture : on ajoute l'émetteur original à la liste des alertes
            UNKNOWN_ISSUERS.add(issuerName);
            return DEFAULT_TRUST;
        }
    }

    /**
     * Permet de récupérer la liste des émetteurs qui n'ont pas matché une règle.
     * À utiliser en fin de build pour générer le fichier d'alerte.
     */
    public static Set<String> getUnknownIssuers() {
        return UNKNOWN_ISSUERS;
    }
}