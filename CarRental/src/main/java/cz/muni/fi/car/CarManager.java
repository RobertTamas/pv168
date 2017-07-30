package cz.muni.fi.car;

import java.util.List;


/**
 * This service allows to manipulate with Cars.
 *
 * @author Robert Tamas
 */
public interface CarManager {

    /**
     * Creates an entry in the database.
     *
     * @param car car entity which will be put into database
     */
    void createCar(Car car);

    /**
     * Returns car entity for specific ID from database.
     *
     * @param id ID of car we're looking for
     * @return car with specific ID
     */
    Car getCarById(Long id);

    /**
     * Returns all cars from database.
     *
     * @return list of all cars
     */
    List<Car> findAllCars();

    /**
     * Updates specific car in database.
     *
     * @param car car to be updated
     */
    void updateCar(Car car);

    /**
     * Deletes specific car from database.
     *
     * @param car car to be deleted
     */
    void deleteCar(Car car);

    /**
     * Return car entity for specific licence plate from database.
     *
     * @param licencePlate Licence plate of car we're looking for
     * @return car with specific licence plate
     */
    Car findCarByLicensePlate(String licencePlate);

    /**
     * Checks whether car at entry is valid and has all attributes set.
     *
     * @param car car to be checked
     */
    void validate(Car car);

}
