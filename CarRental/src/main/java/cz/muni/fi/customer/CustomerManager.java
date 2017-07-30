package cz.muni.fi.customer;

import java.util.List;

/**
 * This service allows to manipulate with Customers.
 *
 * @author Robert Tamas
 */
public interface CustomerManager {

    /**
     * Creates an entry in the database.
     *
     * @param customer customer entity which will be put into database
     */
    void createCustomer(Customer customer);

    /**
     * Returns customer entity for specific ID from database.
     *
     * @param id ID of customer we're looking for
     * @return customer with specific ID
     */
    Customer getCustomerById(Long id);

    /**
     * Returns all customers from database.
     *
     * @return list of all customers
     */
    List<Customer> findAllCustomers();

    /**
     * Updates specific customer in database.
     *
     * @param customer customer to be updated
     */
    void updateCustomer(Customer customer);

    /**
     * Deletes specific customer from database.
     *
     * @param customer customer to be deleted
     */
    void deleteCustomer(Customer customer);

    /**
     * Return customer entity for specific name from database.
     *
     * @param name name of customer we're looking for
     * @return customer with specific name
     */

    /**
     * Checks whether customer at entry is valid and has all attributes set.
     *
     * @param customer customer to be checked
     */
    void validate(Customer customer);

}
