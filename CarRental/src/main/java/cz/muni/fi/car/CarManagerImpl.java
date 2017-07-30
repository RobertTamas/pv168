package cz.muni.fi.car;

import cz.muni.fi.DBUtils;
import cz.muni.fi.exceptions.IllegalEntityException;
import cz.muni.fi.exceptions.ServiceFailureException;
import cz.muni.fi.exceptions.ValidationException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Car manager implementation.
 *
 * @author Robert Tamas
 */
public class CarManagerImpl implements CarManager {

    private DataSource dataSource;
    private static ResourceBundle texts = ResourceBundle.getBundle("Texts");
    private static final Logger logger = Logger.getLogger(
            CarManagerImpl.class.getName());

    public CarManagerImpl() {
    }

    public CarManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException(texts.getString(
                    "DSNotSetException"));
        }
    }

    @Override
    public void createCar(Car car) throws ServiceFailureException {
        checkDataSource();
        validate(car);
        if (car.getId() != null) {
            throw new IllegalEntityException(texts.getString("carID") +
                    texts.getString("isNull"));
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "INSERT INTO " +
                            "Car (license_plate, brand, model, price_per_day)" +
                            " VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, car.getLicencePlate());
            st.setString(2, car.getBrand());
            st.setString(3, car.getModel());
            st.setInt(4, car.getPricePerDay());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, car, true);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            car.setId(id);
            conn.commit();
        } catch (SQLException ex) {
            String msg = texts.getString("errorInsertingDB");
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public Car getCarById(Long id) throws ServiceFailureException {

        checkDataSource();
        if (id == null) {
            throw new IllegalArgumentException(texts.getString("ID") +
                    texts.getString("isNull"));
        }

        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT * FROM Car WHERE id = ?");
            st.setLong(1, id);
            return executeQueryForSingleCar(st);
        } catch (SQLException ex) {
            String msg = texts.getString("errorGettingDB");
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public Car findCarByLicensePlate(String licencePlate)
            throws ServiceFailureException {

        checkDataSource();
        if (licencePlate == null) {
            throw new IllegalArgumentException(texts.getString("licence") +
                    texts.getString("isNull"));
        }

        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT * FROM CAR WHERE license_plate = ?");
            st.setString(1, licencePlate);
            return executeQueryForSingleCar(st);
        } catch (SQLException ex) {
            String msg = texts.getString("errorGettingDB");
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public List<Car> findAllCars() {
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement("SELECT * FROM Car");
            return executeQueryForMultipleCars(st);
        } catch (SQLException ex) {
            String msg = texts.getString("errorGettingDB");
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void updateCar(Car car) throws ServiceFailureException {
        checkDataSource();
        validate(car);

        if (car == null) {
            throw new IllegalEntityException(texts.getString("car") +
                    texts.getString("isNull"));
        }
        if (car.getId() == null) {
            throw new IllegalEntityException(texts.getString("carsID") +
                    texts.getString("isNull"));
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "UPDATE Car SET license_plate = ?, brand = ?," +
                            " model = ?, price_per_day = ? WHERE id = ?");
            st.setString(1, car.getLicencePlate());
            st.setString(2, car.getBrand());
            st.setString(3, car.getModel());
            st.setInt(4, car.getPricePerDay());
            st.setLong(5, car.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, car, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = texts.getString("errorUpdatingDB");
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }

    }

    @Override
    public void deleteCar(Car car) throws ServiceFailureException {
        checkDataSource();
        if (car == null) {
            throw new IllegalEntityException(texts.getString("car") +
                    texts.getString("isNull"));
        }
        if (car.getId() == null) {
            throw new IllegalEntityException(texts.getString("carsID") +
                    texts.getString("isNull"));
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM Car WHERE id = ?");
            st.setLong(1, car.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, car, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = texts.getString("errorDeletingDB");
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void validate(Car car) {
        if (car == null) {
            throw new IllegalArgumentException(texts.getString("car") +
                    texts.getString("isNull"));
        }
        if (car.getLicencePlate() == null || car.getLicencePlate().equals("")) {
            throw new ValidationException(texts.getString("field") +
                    texts.getString("licence") +
                    texts.getString("cantBeEmpty"));
        }
        if (car.getBrand() == null || car.getBrand().equals("")) {
            throw new ValidationException(texts.getString("field") +
                    texts.getString("brand") +
                    texts.getString("cantBeEmpty"));
        }
        if (car.getModel() == null || car.getModel().equals("")) {
            throw new ValidationException(texts.getString("field") +
                    texts.getString("model") +
                    texts.getString("cantBeEmpty"));
        }
        if (car.getPricePerDay() <= 0) {
            throw new ValidationException(texts.getString("priceException"));
        }
    }

    /**
     * Retrieving car from database.
     *
     * @param st results set
     * @return car entity
     * @throws SQLException            is case of invalid statement
     * @throws ServiceFailureException in case of multiple
     *                                 cars have been returned in query
     */
    private Car executeQueryForSingleCar(PreparedStatement st)
            throws SQLException, ServiceFailureException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Car result = tableRowToBody(rs);
            if (rs.next()) {
                throw new ServiceFailureException(texts.getString(
                        "internalIntegrityError"));
            }
            return result;
        } else {
            return null;
        }
    }

    /**
     * Retrieving cars from database.
     *
     * @param st results set
     * @return list of cars
     * @throws SQLException is case of invalid statement
     */
    static List<Car> executeQueryForMultipleCars(PreparedStatement st)
            throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Car> result = new ArrayList<Car>();
        while (rs.next()) {
            result.add(tableRowToBody(rs));
        }
        return result;
    }

    /**
     * Maps car from row of the database to entity.
     *
     * @param rs results set
     * @return car entity
     * @throws SQLException in case of invalid statement
     */
    private static Car tableRowToBody(ResultSet rs) throws SQLException {
        Car result = new Car(rs.getString("license_plate"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getInt("price_per_day"));
        result.setId(rs.getLong("id"));
        return result;
    }
}
