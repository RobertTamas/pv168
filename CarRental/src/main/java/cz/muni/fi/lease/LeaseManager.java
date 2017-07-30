package cz.muni.fi.lease;

import cz.muni.fi.car.Car;
import cz.muni.fi.customer.Customer;

import java.util.List;

/**
 * This service allows to manipulate with Leases.
 *
 * @author Robert Tamas
 */
public interface LeaseManager {

    /**
     * Creates an entry in the database.
     *
     * @param lease lease entity which will be put into database
     */
    void createLease(Lease lease);

    /**
     * Returns lease entity for specific ID from database.
     *
     * @param id ID of lease we're looking for
     * @return lease with specific ID
     */
    Lease getLeaseById(Long id);

    /**
     * Returns all leases from database.
     *
     * @return list of all leases
     */
    List<Lease> findAllLeases();

    /**
     * Updates specific lease in database.
     *
     * @param lease lease to be updated
     */
    void updateLease(Lease lease);

    /**
     * Deletes specific lease from database.
     *
     * @param lease lease to be deleted
     */
    void deleteLease(Lease lease);

    /**
     * Return list of leases for specific customer from database.
     *
     * @param customer customer to be searched for leases
     * @return customers list of leases
     */
    List<Lease> findAllLeasesForCustomer(Customer customer);

    /**
     * Return list of leases for specific car from database.
     *
     * @param car car to be searched for leases
     * @return cars list of leases
     */
    List<Lease> findAllLeasesForCar(Car car);

    /**
     * Checks if all parameters of lease are correct.
     *
     * @param lease to be checked
     */
    void validate(Lease lease);
}
