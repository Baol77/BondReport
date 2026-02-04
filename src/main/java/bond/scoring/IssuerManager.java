package bond.scoring;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class IssuerManager {

    private record TrustRule(List<String> keywords, double score) {}

    private static final double DEFAULT_TRUST = 0.80;
    private static final double NO_TRUST = 0;

    // Set synchronisé pour stocker les inconnus (utile si le scoring est multithreadé)
    private static final Set<String> UNKNOWN_ISSUERS = Collections.synchronizedSet(new TreeSet<>());

    private static final List<TrustRule> RULES = List.of(
        new TrustRule(List.of("GERMANIA", "DEUTSCHLAND", "BUND", "GERMANY"), 1.00),
        new TrustRule(List.of("FINLANDIA", "FINLAND"), 1.00),
        new TrustRule(List.of("OLANDA", "NETHERLANDS"), 1.00),
        new TrustRule(List.of("AUSTRIA"), 1.00),
        new TrustRule(List.of("SVEZIA", "SWEDEN"), 1.00),

        new TrustRule(List.of("FRANCIA", "FRANCE"), 0.97),
        new TrustRule(List.of("BELGIO", "BELGIUM"), 0.97),
        new TrustRule(List.of("IRLANDA", "IRELAND"), 0.97),
        new TrustRule(List.of("REGNO UNITO", "UK", "UNITED KINGDOM"), 0.97),

        new TrustRule(List.of("SPAGNA", "SPAIN"), 0.92),
        new TrustRule(List.of("PORTOGALLO", "PORTUGAL"), 0.92),
        new TrustRule(List.of("SLOVENIA"), 0.92),
        new TrustRule(List.of("ESTONIA"), 0.92),
        new TrustRule(List.of("LETTONIA", "LATVIA"), 0.92),

        new TrustRule(List.of("CROAZIA", "CROATIA"), 0.85),
        new TrustRule(List.of("ITALY", "ITALIA", "REPUBBLICA ITALIANA", "BTP"), 0.85),
        new TrustRule(List.of("POLONIA", "POLAND"), 0.85),

        new TrustRule(List.of("UNGHERIA", "HUNGARY"), 0.82),
        new TrustRule(List.of("LITUANIA", "LITHUANIA"), 0.82),

        new TrustRule(List.of("ROMANIA"), 0.78),
        new TrustRule(List.of("BULGARIA"), 0.78),

        new TrustRule(List.of("GRECIA", "GREECE", "REPUBBLICA GRECA"), 0.72),
        new TrustRule(List.of("CIPRO", "CYPRUS"), 0.70),
        new TrustRule(List.of("TURCHIA", "TURKEY"), 0.65)
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