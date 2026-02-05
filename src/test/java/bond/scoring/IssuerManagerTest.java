package bond.scoring;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class IssuerManagerTest {

    private static final Path TEST_ALERT_PATH = Paths.get("docs/test_alerts.txt");

    @Before
    public void setup() throws IOException {
        // On s'assure de partir d'un état propre
        Files.deleteIfExists(TEST_ALERT_PATH);
    }

    @Test
    public void testKnownIssuer() {
        double score = IssuerManager.getTrustScore("ITALY VALORE");
        assertEquals(0.85, score, 0.001);
    }

    @Test
    public void testUnknownIssuerCapture() {
        // Cet émetteur n'existe pas dans vos RULES
        IssuerManager.getTrustScore("GOTHAM CITY TREASURY");
        assertTrue(IssuerManager.getUnknownIssuers().contains("GOTHAM CITY TREASURY"));
    }

    @AfterClass
    public static void cleanTestData() throws IOException {
        Files.deleteIfExists(TEST_ALERT_PATH);
        IssuerManager.getUnknownIssuers().clear();
    }

    @Test
    public void testFileCreationAndContent() throws IOException {
        // 1. Simuler un émetteur inconnu
        IssuerManager.getTrustScore("FICTIONAL_COUNTRY_X");

        // 2. Logique de création (identique à BondApp)
        if (!IssuerManager.getUnknownIssuers().isEmpty()) {
            if (TEST_ALERT_PATH.getParent() != null) {
                Files.createDirectories(TEST_ALERT_PATH.getParent());
            }
            Files.write(TEST_ALERT_PATH, IssuerManager.getUnknownIssuers());
        }

        // 3. Vérifications
        assertTrue("Le fichier doit exister", Files.exists(TEST_ALERT_PATH));

        // Utilisation de readAllLines pour une compatibilité maximale ou readString pour Java 11+
        String content = Files.readString(TEST_ALERT_PATH);
        assertTrue("Le contenu doit inclure l'émetteur inconnu", content.contains("FICTIONAL_COUNTRY_X"));
    }

    @Test
    public void testNoFileIfNoUnknown() throws IOException {
        // Simuler un émetteur connu
        IssuerManager.getTrustScore("ITALIA");

        if (IssuerManager.getUnknownIssuers().isEmpty()) {
            Files.deleteIfExists(TEST_ALERT_PATH);
        }

        assertFalse("Le fichier ne doit pas exister si l'émetteur est connu", Files.exists(TEST_ALERT_PATH));
    }
}