package cz.muni.fi.lease;

import cz.muni.fi.car.Car;
import cz.muni.fi.car.CarManagerImpl;
import cz.muni.fi.customer.Customer;
import cz.muni.fi.customer.CustomerManagerImpl;
import cz.muni.fi.exceptions.IllegalEntityException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.time.Month.FEBRUARY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Robert Tamas, Maros Struk
 */
public class LeaseManagerImplTest {

    private LeaseManagerImpl manager;
    private CarManagerImpl carManager;
    private CustomerManagerImpl customerManager;
    private EmbeddedDatabase ds;

    private final static ZonedDateTime NOW = LocalDateTime.of(2016,
            FEBRUARY, 29, 14, 00).atZone(ZoneId.of("UTC"));

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        manager = new LeaseManagerImpl(Clock.fixed(NOW.toInstant(), NOW.getZone()));
        carManager = new CarManagerImpl();
        customerManager = new CustomerManagerImpl();
        ds = new EmbeddedDatabaseBuilder().setType(
                EmbeddedDatabaseType.DERBY).addScript(
                "createTables.sql").build();
        manager.setDataSource(ds);
        carManager.setDataSource(ds);
        customerManager.setDataSource(ds);
    }

    @After
    public void tearDown() throws SQLException {
        ds.shutdown();
    }

    @Test
    public void createLease() {
        Customer c = new Customer("Jozef Mrkva",
                "Bratislavska 1", "1111-90-21-31");
        Car car = new Car("ZA1234", "Audi", "A7", 50);
        Lease lease = new Lease(car, c, 50, LocalDate.of(2014, 10, 10),
                LocalDate.of(2014, 12, 10), null);
        carManager.createCar(car);
        customerManager.createCustomer(c);
        manager.createLease(lease);

        Long leaseId = lease.getId();
        assertNotNull(leaseId);
        Lease result = manager.getLeaseById(leaseId);
        assertNotSame(lease, result);
        assertDeepEquals(lease, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNullLease() {
        manager.createLease(null);
    }

    @Test(expected = IllegalEntityException.class)
    public void createLeaseWithExistingId() {
        Customer c = new Customer("Jozef Mrkva",
                "Bratislavska 1", "1111-90-21-31");
        Car car = new Car("ZA1234", "Audi", "A7", 50);
        Lease lease = new Lease(car, c, 50, LocalDate.of(2014, 10, 10),
                LocalDate.of(2014, 12, 10), null);
        lease.setId(1l);
        manager.createLease(lease);
    }

    @Test
    public void findAllLeases() {
        assertTrue(manager.findAllLeases().isEmpty());

        Customer c1 = new Customer("Peter Vesely",
                "Kosicka 4", "1111-32-78-17");
        Car car1 = new Car("BA0193", "Skoda", "Superb", 40);
        Lease lease1 = new Lease(car1, c1, 40, LocalDate.of(2015, 2, 6),
                LocalDate.of(2015, 3, 8), null);
        Customer c2 = new Customer("Jozef Mrkva",
                "Bratislavska 1", "1111-90-21-31");
        Car car2 = new Car("ZA1234", "Audi", "A7", 50);
        Lease lease2 = new Lease(car2, c2, 50, LocalDate.of(2014, 10, 10),
                LocalDate.of(2014, 12, 10), null);

        carManager.createCar(car1);
        customerManager.createCustomer(c1);
        carManager.createCar(car2);
        customerManager.createCustomer(c2);
        manager.createLease(lease1);
        manager.createLease(lease2);

        List<Lease> expected = Arrays.asList(lease1, lease2);
        List<Lease> actual = manager.findAllLeases();

        Collections.sort(expected, LEASE_ID_COMPARATOR);
        Collections.sort(actual, LEASE_ID_COMPARATOR);

        assertDeepEquals(expected, actual);
    }

    @Test
    public void updateLease() throws Exception {
        Customer c1 = new Customer("Peter Vesely",
                "Kosicka 4", "1111-32-78-17");
        Car car1 = new Car("BA0193", "Skoda", "Superb", 40);
        Lease lease1 = new Lease(car1, c1, 40, LocalDate.of(2015, 2, 6),
                LocalDate.of(2015, 3, 8), null);
        Customer c2 = new Customer("Jozef Mrkva",
                "Bratislavska 1", "1111-90-21-31");
        Car car2 = new Car("ZA1234", "Audi", "A7", 50);
        Lease lease2 = new Lease(car2, c2, 50, LocalDate.of(2014, 10, 10),
                LocalDate.of(2014, 12, 10), null);

        carManager.createCar(car1);
        customerManager.createCustomer(c1);
        carManager.createCar(car2);
        customerManager.createCustomer(c2);
        manager.createLease(lease1);
        manager.createLease(lease2);
        Long leaseId = c1.getId();

        lease1 = manager.getLeaseById(leaseId);
        Car car3 = new Car("KA2222", "Subaru", "Impreza", 39);
        carManager.createCar(car3);
        lease1.setCar(car3);
        manager.updateLease(lease1);
        assertDeepEqualsCar(car3, lease1.getCar());
        assertDeepEqualsCustomer(c1, lease1.getCustomer());
        assertEquals(40, lease1.getPrice());
        assertEquals(LocalDate.of(2015, 2, 6), lease1.getStartTime());
        assertEquals(LocalDate.of(2015, 3, 8), lease1.getExpectedEndTime());
        assertEquals(null, lease1.getRealEndTime());

        lease1 = manager.getLeaseById(leaseId);
        Customer c3 = new Customer("Miroslav Novotny",
                "Fesakova 69", "0911 222 333 444");
        customerManager.createCustomer(c3);
        lease1.setCustomer(c3);
        manager.updateLease(lease1);
        assertDeepEqualsCar(car3, lease1.getCar());
        assertDeepEqualsCustomer(c3, lease1.getCustomer());
        assertEquals(40, lease1.getPrice());
        assertEquals(LocalDate.of(2015, 2, 6), lease1.getStartTime());
        assertEquals(LocalDate.of(2015, 3, 8), lease1.getExpectedEndTime());
        assertEquals(null, lease1.getRealEndTime());

        lease1 = manager.getLeaseById(leaseId);
        lease1.setPrice(100);
        manager.updateLease(lease1);
        assertDeepEqualsCar(car3, lease1.getCar());
        assertDeepEqualsCustomer(c3, lease1.getCustomer());
        assertEquals(100, lease1.getPrice());
        assertEquals(LocalDate.of(2015, 2, 6), lease1.getStartTime());
        assertEquals(LocalDate.of(2015, 3, 8), lease1.getExpectedEndTime());
        assertEquals(null, lease1.getRealEndTime());

        lease1 = manager.getLeaseById(leaseId);
        lease1.setStartTime(LocalDate.of(2010, 1, 1));
        manager.updateLease(lease1);
        assertDeepEqualsCar(car3, lease1.getCar());
        assertDeepEqualsCustomer(c3, lease1.getCustomer());
        assertEquals(100, lease1.getPrice());
        assertEquals(LocalDate.of(2010, 1, 1), lease1.getStartTime());
        assertEquals(LocalDate.of(2015, 3, 8), lease1.getExpectedEndTime());
        assertEquals(null, lease1.getRealEndTime());

        lease1 = manager.getLeaseById(leaseId);
        lease1.setExpectedEndTime(LocalDate.of(2011, 12, 12));
        manager.updateLease(lease1);
        assertDeepEqualsCar(car3, lease1.getCar());
        assertDeepEqualsCustomer(c3, lease1.getCustomer());
        assertEquals(100, lease1.getPrice());
        assertEquals(LocalDate.of(2010, 1, 1), lease1.getStartTime());
        assertEquals(LocalDate.of(2011, 12, 12), lease1.getExpectedEndTime());
        assertEquals(null, lease1.getRealEndTime());

        lease1 = manager.getLeaseById(leaseId);
        lease1.setRealEndTime(LocalDate.of(2020, 2, 13));
        manager.updateLease(lease1);
        assertDeepEqualsCar(car3, lease1.getCar());
        assertDeepEqualsCustomer(c3, lease1.getCustomer());
        assertEquals(100, lease1.getPrice());
        assertEquals(LocalDate.of(2010, 1, 1), lease1.getStartTime());
        assertEquals(LocalDate.of(2011, 12, 12), lease1.getExpectedEndTime());
        assertEquals(LocalDate.of(2020, 2, 13), lease1.getRealEndTime());

        // Check if updates didn't affected other records
        assertDeepEquals(lease2, manager.getLeaseById(lease2.getId()));
    }

    @Test(expected = IllegalEntityException.class)
    public void updateNullLease() {
        manager.updateLease(null);
    }

    @Test(expected = IllegalEntityException.class)
    public void updateLeaseWithNullId() {
        Customer c1 = new Customer("Peter Vesely",
                "Kosicka 4", "1111-32-78-17");
        Car car1 = new Car("BA0193",
                "Skoda", "Superb", 40);
        Lease lease = new Lease(car1, c1, 40, LocalDate.of(2015, 2, 6),
                LocalDate.of(2015, 3, 8), null);
        carManager.createCar(car1);
        customerManager.createCustomer(c1);
        manager.createLease(lease);
        lease.setId(null);
        manager.updateLease(lease);
    }

    @Test(expected = IllegalEntityException.class)
    public void updateLeaseWithNonExistingId() {
        Customer c1 = new Customer("Peter Vesely",
                "Kosicka 4", "1111-32-78-17");
        Car car1 = new Car("BA0193", "Skoda", "Superb", 40);
        Lease lease = new Lease(car1, c1, 40, LocalDate.of(2015, 2, 6),
                LocalDate.of(2015, 3, 8), null);
        carManager.createCar(car1);
        customerManager.createCustomer(c1);
        manager.createLease(lease);
        lease.setId(lease.getId() + 1);
        manager.updateLease(lease);
    }

    @Test
    public void deleteLease() throws Exception {
        Customer c1 = new Customer("Peter Vesely",
                "Kosicka 4", "1111-32-78-17");
        Car car1 = new Car("BA0193", "Skoda", "Superb", 40);
        Lease lease1 = new Lease(car1, c1, 40, LocalDate.of(2015, 2, 6),
                LocalDate.of(2015, 3, 8), null);
        Customer c2 = new Customer("Jozef Mrkva", "Bratislavska 1", "1111-90-21-31");
        Car car2 = new Car("ZA1234", "Audi", "A7", 50);
        Lease lease2 = new Lease(car2, c2, 50, LocalDate.of(2014, 10, 10),
                LocalDate.of(2014, 12, 10), null);

        carManager.createCar(car1);
        customerManager.createCustomer(c1);
        carManager.createCar(car2);
        customerManager.createCustomer(c2);
        manager.createLease(lease1);
        manager.createLease(lease2);

        assertNotNull(manager.getLeaseById(lease1.getId()));
        assertNotNull(manager.getLeaseById(lease2.getId()));

        manager.deleteLease(lease1);

        assertNull(manager.getLeaseById(lease1.getId()));
        assertNotNull(manager.getLeaseById(lease2.getId()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullLease() {
        manager.deleteLease(null);
    }

    @Test(expected = IllegalEntityException.class)
    public void deleteLeaseWithNonExistingId() {
        Customer c1 = new Customer("Peter Vesely",
                "Kosicka 4", "1111-32-78-17");
        Car car1 = new Car("BA0193", "Skoda", "Superb", 40);
        Lease lease = new Lease(car1, c1, 40, LocalDate.of(2015, 2, 6),
                LocalDate.of(2015, 3, 8), null);
        lease.setId(1L);
        carManager.createCar(car1);
        customerManager.createCustomer(c1);
        manager.deleteLease(lease);
    }

    @Test
    public void findAllLeasesForCustomer() throws Exception {
        Customer c1 = new Customer("Peter Vesely",
                "Kosicka 4", "1111-32-78-17");
        Car car1 = new Car("BA0193", "Skoda", "Superb", 40);
        Lease lease1 = new Lease(car1, c1, 40, LocalDate.of(2015, 2, 6),
                LocalDate.of(2015, 3, 8), null);
        Customer c2 = new Customer("Jozef Mrkva",
                "Bratislavska 1", "1111-90-21-31");
        Car car2 = new Car("ZA1234", "Audi", "A7", 50);
        Lease lease2 = new Lease(car2, c2, 50, LocalDate.of(2014, 10, 10),
                LocalDate.of(2014, 12, 10), null);

        carManager.createCar(car1);
        customerManager.createCustomer(c1);
        carManager.createCar(car2);
        customerManager.createCustomer(c2);
        manager.createLease(lease1);
        manager.createLease(lease2);

        List<Lease> leases = manager.findAllLeasesForCustomer(c1);
        assertDeepEquals(leases.get(0), lease1);
    }

    @Test
    public void findAllLeasesForCar() throws Exception {
        Customer c1 = new Customer("Peter Vesely",
                "Kosicka 4", "1111-32-78-17");
        Car car1 = new Car("BA0193", "Skoda", "Superb", 40);
        Lease lease1 = new Lease(car1, c1, 40, LocalDate.of(2015, 2, 6),
                LocalDate.of(2015, 3, 8), null);
        Customer c2 = new Customer("Jozef Mrkva",
                "Bratislavska 1", "1111-90-21-31");
        Car car2 = new Car("ZA1234", "Audi", "A7", 50);
        Lease lease2 = new Lease(car2, c2, 50, LocalDate.of(2014, 10, 10),
                LocalDate.of(2014, 12, 10), null);

        carManager.createCar(car1);
        customerManager.createCustomer(c1);
        carManager.createCar(car2);
        customerManager.createCustomer(c2);
        manager.createLease(lease1);
        manager.createLease(lease2);

        List<Lease> leases = manager.findAllLeasesForCar(car1);
        assertDeepEquals(leases.get(0), lease1);
    }

    private void assertDeepEquals(List<Lease> expectedList,
                                  List<Lease> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Lease expected = expectedList.get(i);
            Lease actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }

    private void assertDeepEquals(Lease expected, Lease actual) {
        assertEquals(expected.getId(), actual.getId());
        assertDeepEqualsCar(expected.getCar(), actual.getCar());
        assertDeepEqualsCustomer(expected.getCustomer(), actual.getCustomer());
        assertEquals(expected.getPrice(), actual.getPrice());
        assertEquals(expected.getStartTime(), actual.getStartTime());
        assertEquals(expected.getExpectedEndTime(),
                actual.getExpectedEndTime());
        assertEquals(expected.getRealEndTime(), actual.getRealEndTime());
    }

    private void assertDeepEqualsCustomer(Customer expected, Customer actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFullName(), actual.getFullName());
        assertEquals(expected.getAddress(), actual.getAddress());
        assertEquals(expected.getPhoneNumber(), actual.getPhoneNumber());
    }

    private void assertDeepEqualsCar(Car expected, Car actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getLicencePlate(), actual.getLicencePlate());
        assertEquals(expected.getBrand(), actual.getBrand());
        assertEquals(expected.getModel(), actual.getModel());
        assertEquals(expected.getPricePerDay(), actual.getPricePerDay());
    }

    private static final Comparator<Lease> LEASE_ID_COMPARATOR =
            (l1, l2) -> l1.getId().compareTo(l2.getId());

}