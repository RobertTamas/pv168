package cz.muni.fi.car;

import cz.muni.fi.exceptions.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNotSame;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNull;

/**
 * @author Robert Tamas
 */
public class CarManagerImplTest {

    private CarManagerImpl manager;
    private EmbeddedDatabase ds;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws SQLException {
        ds = new EmbeddedDatabaseBuilder().setType(
                EmbeddedDatabaseType.DERBY).addScript(
                "createTables.sql").build();
        manager = new CarManagerImpl();
        manager.setDataSource(ds);
    }

    @After
    public void tearDown() throws SQLException {
        ds.shutdown();
    }

    @Test
    public void testCreateCar() {
        Car car = new Car("BA123AA", "BMW", "M3", 100);
        manager.createCar(car);

        Long carId = car.getId();
        assertNotNull(carId);
        Car result = manager.getCarById(carId);
        assertEquals(car, result);
        assertNotSame(car, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testcreateNullCar() {
        manager.createCar(null);
    }

    @Test(expected = ValidationException.class)
    public void testcreateCarWithNullLicensePlate() {
        manager.createCar(new Car(null, "BMW", "X3", 100));
    }

    @Test(expected = ValidationException.class)
    public void testcreateCarWithNullBrand() {
        manager.createCar(new Car("BA123AA", null, "X3", 100));
    }

    @Test(expected = ValidationException.class)
    public void testcreateCarWithNullModel() {
        manager.createCar(new Car("BA123AA", "BMW", null, 100));
    }

    @Test(expected = ValidationException.class)
    public void testcreateCarWithNegativePricePerDay() {
        manager.createCar(new Car("BA123AA", "BMW", "X3", -100));
    }

    @Test
    public void testFindAllCars() {
        List<Car> emptyDatabase = manager.findAllCars();
        for (int i = 0; i < emptyDatabase.size(); i++) {
            manager.deleteCar(manager.getCarById(Long.valueOf(i)));


        }
        assertTrue(manager.findAllCars().isEmpty());

        Car car1 = new Car("BA123AA", "BMW", "M3", 100);
        Car car2 = new Car("BA111AA", "BMW", "X1", 80);

        manager.createCar(car1);
        manager.createCar(car2);

        List<Car> expected = Arrays.asList(car1, car2);
        List<Car> actual = manager.findAllCars();

        Collections.sort(actual, CAR_ID_COMPARATOR);
        Collections.sort(expected, CAR_ID_COMPARATOR);

        assertEquals(expected, actual);
    }

    @Test
    public void testUpdateCar() {
        Car car = new Car("BA123AA", "BMW", "M3", 100);
        manager.createCar(car);
        Long carId = car.getId();

        car = manager.getCarById(carId);
        car.setLicencePlate("BA111AA");
        manager.updateCar(car);
        assertEquals("BA111AA", car.getLicencePlate());
        assertEquals("BMW", car.getBrand());
        assertEquals("M3", car.getModel());
        assertEquals(100, car.getPricePerDay());

        car = manager.getCarById(carId);
        car.setBrand("AUDI");
        manager.updateCar(car);
        assertEquals("BA111AA", car.getLicencePlate());
        assertEquals("AUDI", car.getBrand());
        assertEquals("M3", car.getModel());
        assertEquals(100, car.getPricePerDay());

        car = manager.getCarById(carId);
        car.setModel("QUATTRO");
        manager.updateCar(car);
        assertEquals("BA111AA", car.getLicencePlate());
        assertEquals("AUDI", car.getBrand());
        assertEquals("QUATTRO", car.getModel());
        assertEquals(100, car.getPricePerDay());

        car = manager.getCarById(carId);
        car.setPricePerDay(120);
        manager.updateCar(car);
        assertEquals("BA111AA", car.getLicencePlate());
        assertEquals("AUDI", car.getBrand());
        assertEquals("QUATTRO", car.getModel());
        assertEquals(120, car.getPricePerDay());
    }

    @Test
    public void testDeleteCar() {
        Car car1 = new Car("BA123AA", "BMW", "M3", 100);
        Car car2 = new Car("BA111AA", "BMW", "X1", 80);

        manager.createCar(car1);
        manager.createCar(car2);

        assertNotNull(manager.getCarById(car1.getId()));
        assertNotNull(manager.getCarById(car2.getId()));

        manager.deleteCar(car1);

        assertNull(manager.getCarById(car1.getId()));
        assertNotNull(manager.getCarById(car2.getId()));

    }

    @Test
    public void testFindCarById() {
        Car car1 = new Car("BA123AA", "BMW", "M3", 100);
        Car car2 = new Car("BA111AA", "BMW", "X1", 80);
        Car car3 = new Car("BA123BA", "BMW", "M5", 120);
        manager.createCar(car1);
        manager.createCar(car2);
        manager.createCar(car3);

        Car expectedCar = manager.getCarById(Long.valueOf(2));

        assertDeepEquals(car2, expectedCar);
    }

    @Test
    public void testFindCarByLicensePlate() {
        Car car1 = new Car("BA123AA", "BMW", "M3", 100);
        Car car2 = new Car("BA111AA", "BMW", "X1", 80);
        Car car3 = new Car("BA123BA", "BMW", "M5", 120);
        manager.createCar(car1);
        manager.createCar(car2);
        manager.createCar(car3);

        Car expectedCar = manager.findCarByLicensePlate("BA111AA");

        assertEquals(car2, expectedCar);
    }

    private void assertDeepEquals(Car car1, Car car2) {
        assertEquals(car1.getBrand(), car2.getBrand());
        assertEquals(car1.getId(), car2.getId());
        assertEquals(car1.getLicencePlate(), car2.getLicencePlate());
        assertEquals(car1.getModel(), car2.getModel());
        assertEquals(car1.getPricePerDay(), car2.getPricePerDay());
    }

    private static final Comparator<Car> CAR_ID_COMPARATOR =
            Comparator.comparing(Car::getId);
}