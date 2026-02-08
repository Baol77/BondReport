package bond.calc;

import bond.model.Bond;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class BondCalculatorTest {

    private BondCalculator calc;

    @Before
    public void setup() {
        calc = new BondCalculator();
    }

    // ---------------------------------------------------
    // 1. Cas standard
    // ---------------------------------------------------
    @Test
    public void testBuildBondStandard() {
        Bond b = calc.buildBond(
            "IT0001234567",
            "Italy",
            98.0,
            98.0,
            95.0,
            4.0,
            LocalDate.now().plusYears(10),
            "EUR"
        );

        assertNotNull(b);
        assertEquals("IT0001234567", b.isin());
        assertEquals("Italy", b.issuer());
        assertEquals(4.08, b.currentCoupon(), 0.01); // 100*4/98
        assertTrue(b.finalCapitalToMat() > 0);
    }

    // ---------------------------------------------------
    // 2. Prix ↑ → Yield ↓
    // ---------------------------------------------------
    @Test
    public void testHigherPriceLowerYield() {
        Bond cheap = calc.buildBond(
            "X1", "Issuer", 90, 900, 900, 4,
            LocalDate.now().plusYears(10), "EUR");

        Bond expensive = calc.buildBond(
            "X2", "Issuer", 110, 1100, 1100, 4,
            LocalDate.now().plusYears(10), "EUR");

        assertTrue(cheap.currentCoupon() > expensive.currentCoupon());
    }

    // ---------------------------------------------------
    // 3. CHF vs EUR
    // ---------------------------------------------------
    @Test
    public void testChfYieldCalculation() {
        Bond b = calc.buildBond(
            "CH0000000001",
            "Swiss",
            100,
            1000,
            900,
            3,
            LocalDate.now().plusYears(8),
            "CHF"
        );

        assertEquals(0.3, b.currentCoupon(), 0.001);    // 100*3/1000
        assertEquals(0.33, b.currentCouponChf(), 0.01); // 100*3/900
    }

    // ---------------------------------------------------
    // 4. Maturité trop courte → null
    // ---------------------------------------------------
    @Test
    public void testShortMaturityReturnsNull() {
        Bond b = calc.buildBond(
            "X3", "Issuer", 100, 1000, 1000, 4,
            LocalDate.now().plusMonths(10),
            "EUR"
        );

        assertNull(b);
    }

    // ---------------------------------------------------
    // 5. Arrondi
    // ---------------------------------------------------
    @Test
    public void testRounding() {
        Bond b = calc.buildBond(
            "X4", "Issuer", 100, 333.3333, 333.3333, 4,
            LocalDate.now().plusYears(10),
            "EUR"
        );

        assertEquals(1.2, b.currentCoupon(), 0.001);
    }

    // ---------------------------------------------------
    // 6. Yield total positif
    // ---------------------------------------------------
    @Test
    public void testYieldToMaturityPositive() {
        Bond b = calc.buildBond(
            "X5", "Issuer", 95, 950, 930, 4,
            LocalDate.now().plusYears(12), "EUR");

        assertTrue(b.finalCapitalToMat() > 1000);
    }

    // ---------------------------------------------------
    // 7. Zéro coupon
    // ---------------------------------------------------
    @Test
    public void testZeroCouponBond() {
        Bond b = calc.buildBond(
            "X6", "Issuer", 70, 700, 700, 0,
            LocalDate.now().plusYears(10),
            "EUR"
        );

        assertEquals(0.0, b.currentCoupon(), 0.0001);
        assertTrue(b.finalCapitalToMat() > 1000);
    }
}