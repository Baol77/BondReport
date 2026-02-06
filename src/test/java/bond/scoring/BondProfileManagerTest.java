package bond.scoring;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class BondProfileManagerTest {

    @Test
    public void loadsProfilesFromYaml() {
        BondProfileManager.load();
        List<BondProfileManager.BondProfile> profiles = BondProfileManager.all();

        assertNotNull(profiles);
        assertEquals(4, profiles.size());

        BondProfileManager.BondProfile income = profiles.get(0);
        assertEquals("INCOME", income.getName());
        assertEquals(0.80, income.getAlpha(), 1e-9);
        assertEquals(1.50, income.getLambdaFactor(), 1e-9);
    }

    @Test
    public void profilesAreImmutable() {
        BondProfileManager.load();
        List<BondProfileManager.BondProfile> profiles = BondProfileManager.all();

        try {
            profiles.add(new BondProfileManager.BondProfile());
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // ok
        }
    }
}