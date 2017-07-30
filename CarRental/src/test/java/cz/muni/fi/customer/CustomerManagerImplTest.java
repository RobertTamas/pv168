package cz.muni.fi.customer;

import cz.muni.fi.exceptions.IllegalEntityException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * @author Robert Tamas, Maros Struk
 */
public class CustomerManagerImplTest {

    private CustomerManagerImpl manager;
    private EmbeddedDatabase ds;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        ds = new EmbeddedDatabaseBuilder().setType(
                EmbeddedDatabaseType.DERBY).addScript(
                "createTables.sql").build();
        manager = new CustomerManagerImpl(ds);
    }

    @After
    public void tearDown() throws Exception {
        ds.shutdown();
    }

    @Test
    public void createCustomer() {
        Customer customer = new Customer("Jozef Mrkva",
                "Bratislavska 1", "1111-90-21-31");
        manager.createCustomer(customer);

        Long customerId = customer.getId();
        assertNotNull(customerId);
        Customer result = manager.getCustomerById(customerId);
        assertNotSame(customer, result);
        assertDeepEquals(customer, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNullCustomer() {
        manager.createCustomer(null);
    }

    @Test
    public void createCustomerWithExistingId() {
        Customer customer = new Customer("Jozef Mrkva",
                "Bratislavska 1", "1111-90-21-31");
        customer.setId(1l);
        expectedException.expect(IllegalEntityException.class);
        manager.createCustomer(customer);
    }

    @Test
    public void findAllCustomers() {
        assertTrue(manager.findAllCustomers().isEmpty());

        Customer c1 = new Customer("Jozef Mrkva",
                "Bratislavska 1", "1111-90-21-31");
        Customer c2 = new Customer("Peter Vesely",
                "Kosicka 4", "1111-32-78-17");

        manager.createCustomer(c1);
        manager.createCustomer(c2);

        List<Customer> expected = Arrays.asList(c1, c2);
        List<Customer> actual = manager.findAllCustomers();

        Collections.sort(expected, CUSTOMER_ID_COMPARATOR);
        Collections.sort(actual, CUSTOMER_ID_COMPARATOR);

        assertNotSame(expected, actual);
        assertDeepEquals(expected, actual);
    }

    @Test
    public void updateCustomer() {
        Customer c1 = new Customer("Jozef Mrkva",
                "Bratislavska 1", "1111-90-21-31");
        Customer c2 = new Customer("Peter Vesely", "Kosicka 4", "1111-32-78-17");
        manager.createCustomer(c1);
        manager.createCustomer(c2);
        Long customerId = c1.getId();

        c1 = manager.getCustomerById(customerId);
        c1.setFullName("Tomas Navratil");
        manager.updateCustomer(c1);
        assertEquals("Tomas Navratil", c1.getFullName());
        assertEquals("Bratislavska 1", c1.getAddress());
        assertEquals("1111-90-21-31", c1.getPhoneNumber());

        c1 = manager.getCustomerById(customerId);
        c1.setAddress("Moravskeho namesti 7");
        manager.updateCustomer(c1);
        assertEquals("Tomas Navratil", c1.getFullName());
        assertEquals("Moravskeho namesti 7", c1.getAddress());
        assertEquals("1111-90-21-31", c1.getPhoneNumber());

        c1 = manager.getCustomerById(customerId);
        c1.setPhoneNumber("42424242");
        manager.updateCustomer(c1);
        assertEquals("Tomas Navratil", c1.getFullName());
        assertEquals("Moravskeho namesti 7", c1.getAddress());
        assertEquals("42424242", c1.getPhoneNumber());

        // Check if updates didn't affected other records
        assertDeepEquals(c2, manager.getCustomerById(c2.getId()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateNullCustomer() {
        manager.updateCustomer(null);
    }

    @Test
    public void updateCustomerWithNullId() {
        Customer c = new Customer("Jozef Mrkva",
                "Bratislavska 1", "1111-90-21-31");
        manager.createCustomer(c);
        c.setId(null);
        expectedException.expect(IllegalArgumentException.class);
        manager.updateCustomer(c);
    }

    @Test
    public void updateCustomerWithNonExistingId() {
        Customer c = new Customer("Peter Vesely",
                "Kosicka 4", "1111-32-78-17");
        manager.createCustomer(c);
        c.setId(c.getId() + 1);
        expectedException.expect(IllegalEntityException.class);
        manager.updateCustomer(c);
    }

    @Test
    public void deleteCustomer() throws Exception {
        Customer c1 = new Customer("Jozef Mrkva",
                "Bratislavska 1", "1111-90-21-31");
        Customer c2 = new Customer("Peter Vesely",
                "Kosicka 4", "1111-32-78-17");

        manager.createCustomer(c1);
        manager.createCustomer(c2);

        assertNotNull(manager.getCustomerById(c1.getId()));
        assertNotNull(manager.getCustomerById(c2.getId()));

        manager.deleteCustomer(c1);

        assertNull(manager.getCustomerById(c1.getId()));
        assertNotNull(manager.getCustomerById(c2.getId()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullCustomer() {
        manager.deleteCustomer(null);
    }

    @Test
    public void deleteCustomerWithNonExistingId() {
        Customer c = new Customer("Peter Vesely",
                "Kosicka 4", "1111-32-78-17");
        c.setId(1L);
        expectedException.expect(IllegalEntityException.class);
        manager.deleteCustomer(c);
    }

    private void assertDeepEquals(List<Customer> expectedList,
                                  List<Customer> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Customer expected = expectedList.get(i);
            Customer actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }

    private void assertDeepEquals(Customer expected, Customer actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFullName(), actual.getFullName());
        assertEquals(expected.getAddress(), actual.getAddress());
        assertEquals(expected.getPhoneNumber(), actual.getPhoneNumber());
    }

    private static final Comparator<Customer> CUSTOMER_ID_COMPARATOR =
            (c1, c2) -> c1.getId().compareTo(c2.getId());

}