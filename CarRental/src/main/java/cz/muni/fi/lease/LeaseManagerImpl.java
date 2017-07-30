package cz.muni.fi.lease;

import cz.muni.fi.DBUtils;
import cz.muni.fi.car.Car;
import cz.muni.fi.car.CarManagerImpl;
import cz.muni.fi.customer.Customer;
import cz.muni.fi.customer.CustomerManagerImpl;
import cz.muni.fi.exceptions.IllegalEntityException;
import cz.muni.fi.exceptions.ServiceFailureException;
import cz.muni.fi.exceptions.ValidationException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lease manager implementation
 *
 * @author Robert Tamas
 */
public class LeaseManagerImpl implements LeaseManager {

    private DataSource dataSource;

    private CarManagerImpl carManager = new CarManagerImpl();
    private CustomerManagerImpl customerManager = new CustomerManagerImpl();
    private final Clock clock;
    private static ResourceBundle texts = ResourceBundle.getBundle("Texts");
    private static final Logger logger = Logger.getLogger(
            CarManagerImpl.class.getName());

    public LeaseManagerImpl(Clock clock) {
        this.clock = clock;
    }

    public LeaseManagerImpl(DataSource dataSource, Clock clock) {
        this.dataSource = dataSource;
        this.clock = clock;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException(texts.getString(
                    "DSNotSetException"));
        }
        if (carManager.getDataSource() == null) {
            carManager.setDataSource(dataSource);
            customerManager.setDataSource(dataSource);
        }
    }

    @Override
    public void createLease(Lease lease) throws ValidationException,
            ServiceFailureException, IllegalArgumentException {
        checkDataSource();
        if (lease == null) {
            throw new IllegalArgumentException(texts.getString("lease") +
                    texts.getString("isNull"));
        }
        if (lease.getId() != null) {
            throw new IllegalEntityException(texts.getString("leaseID") +
                    texts.getString("isntNull"));
        }
        try {
            validate(lease);
        } catch (Exception e) {
            throw e;
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "INSERT INTO Lease (car_id, customer_id, price," +
                            " start_time, expected_end_time, real_end_time) " +
                            "VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setLong(1, lease.getCar().getId());
            st.setLong(2, lease.getCustomer().getId());
            st.setInt(3, lease.getPrice());
            st.setDate(4, toSqlDate(lease.getStartTime()));
            st.setDate(5, toSqlDate(lease.getExpectedEndTime()));
            st.setDate(6, toSqlDate(lease.getRealEndTime()));

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, lease, true);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            lease.setId(id);
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
    public Lease getLeaseById(Long id) {
        checkDataSource();
        if (id == null) {
            throw new IllegalArgumentException(texts.getString("leaseID") +
                    texts.getString("isNull"));
        }

        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, car_id, customer_id, price, start_time," +
                            " expected_end_time, real_end_time " +
                            "FROM Lease WHERE id = ?");
            st.setLong(1, id);
            return executeQueryForSingleLease(st);
        } catch (SQLException ex) {
            String msg = texts.getString("errorGettingDB");
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public List<Lease> findAllLeases() {
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, car_id, customer_id, price, start_time," +
                            " expected_end_time, real_end_time FROM Lease");
            return executeQueryForMultipleLeases(st);
        } catch (SQLException ex) {
            String msg = texts.getString("errorGettingDB");
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void updateLease(Lease lease) throws ValidationException,
            ServiceFailureException, IllegalArgumentException {
        checkDataSource();

        if (lease == null) {
            throw new IllegalEntityException(texts.getString("lease") +
                    texts.getString("isNull"));
        }
        if (lease.getId() == null) {
            throw new IllegalEntityException(texts.getString("leaseID") +
                    texts.getString("isNull"));
        }
        validate(lease);

        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement("UPDATE Lease SET car_id = ?," +
                    " customer_id = ?, price = ?, start_time = ?," +
                    " expected_end_time = ?, real_end_time = ? WHERE id = ?");
            st.setLong(1, lease.getCar().getId());
            st.setLong(2, lease.getCustomer().getId());
            st.setInt(3, lease.getPrice());
            st.setDate(4, toSqlDate(lease.getStartTime()));
            st.setDate(5, toSqlDate(lease.getExpectedEndTime()));
            st.setDate(6, toSqlDate(lease.getRealEndTime()));
            st.setLong(7, lease.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, lease, false);
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
    public void deleteLease(Lease lease) throws IllegalArgumentException {
        checkDataSource();

        if (lease == null) {
            throw new IllegalArgumentException(texts.getString("lease") +
                    texts.getString("isNull"));
        }
        if (lease.getId() == null) {
            throw new IllegalEntityException(texts.getString("leaseID") +
                    texts.getString("isNull"));
        }

        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM Lease WHERE id = ?");
            st.setLong(1, lease.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, lease, false);
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
    public List<Lease> findAllLeasesForCustomer(Customer customer) {
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement("SELECT id, car_id, customer_id," +
                    " price, start_time, expected_end_time, real_end_time " +
                    "FROM Lease WHERE customer_id = ?");
            st.setLong(1, customer.getId());
            return executeQueryForMultipleLeases(st);
        } catch (SQLException ex) {
            String msg = texts.getString("errorGettingDB");
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public List<Lease> findAllLeasesForCar(Car car) {
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement("SELECT id, car_id, customer_id," +
                    " price, start_time, expected_end_time, real_end_time " +
                    "FROM Lease WHERE car_id = ?");
            st.setLong(1, car.getId());
            return executeQueryForMultipleLeases(st);
        } catch (SQLException ex) {
            String msg = texts.getString("errorGettingDB");
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    /**
     * Retrieving lease from database.
     *
     * @param st statement
     * @return lease
     * @throws SQLException            is case of invalid statement
     * @throws ServiceFailureException in case of multiple leases have been
     *                                 returned in query
     */
    private Lease executeQueryForSingleLease(PreparedStatement st)
            throws SQLException, ServiceFailureException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Lease result = tableRowToLease(rs);
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
     * Retrieving leases from database.
     *
     * @param st statement
     * @return list of leases
     * @throws SQLException in case of invalid statement
     */
    private List<Lease> executeQueryForMultipleLeases(PreparedStatement st)
            throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Lease> result = new ArrayList<Lease>();
        while (rs.next()) {
            result.add(tableRowToLease(rs));
        }
        return result;
    }

    /**
     * Maps lease from row of the database to entity.
     *
     * @param rs result set
     * @return lease
     * @throws SQLException in case of invalid statement
     */
    private Lease tableRowToLease(ResultSet rs) throws SQLException {
        Lease result = new Lease(
                carManager.getCarById(rs.getLong("car_id")),
                customerManager.getCustomerById(rs.getLong("customer_id")),
                rs.getInt("price"),
                toLocalDate(rs.getDate("start_time")),
                toLocalDate(rs.getDate("expected_end_time")),
                toLocalDate(rs.getDate("real_end_time")));
        result.setId(rs.getLong("id"));
        return result;
    }

    @Override
    public void validate(Lease lease)
            throws ValidationException, ServiceFailureException {
        if (lease.getCar().getId() == null) {
            throw new ServiceFailureException(texts.getString("car") +
                    texts.getString("wasntCreatedException"));
        }
        if (lease.getCar().getId() == null) {
            throw new ServiceFailureException(texts.getString("customer") +
                    texts.getString("wasntCreatedException"));
        }
        if (lease == null) {
            throw new IllegalArgumentException(texts.getString("lease") +
                    texts.getString("isNull"));
        }
        if (lease.getCar() == null) {
            throw new ValidationException(texts.getString("field") +
                    texts.getString("car") +
                    texts.getString("cantBeEmpty"));
        }
        if (lease.getCustomer() == null) {
            throw new ValidationException(texts.getString("field") +
                    texts.getString("customer") +
                    texts.getString("cantBeEmpty"));
        }
        if (lease.getStartTime() == null) {
            throw new ValidationException(texts.getString("field") +
                    texts.getString("startTime") +
                    texts.getString("cantBeEmpty"));
        }
        if (lease.getExpectedEndTime() == null) {
            throw new ValidationException(texts.getString("field") +
                    texts.getString("expectedEndTime") +
                    texts.getString("cantBeEmpty"));
        }
        if (lease.getExpectedEndTime().isBefore(lease.getStartTime())) {
            throw new ValidationException(texts.getString("expectedEndTime") +
                    texts.getString("isBeforeException"));
        }
        if (lease.getRealEndTime() != null
                && lease.getRealEndTime().isBefore(lease.getStartTime())) {
            throw new ValidationException(texts.getString("realEndTime") +
                    texts.getString("isBeforeException"));
        }
        if (lease.getPrice() < 0) {
            throw new ValidationException(texts.getString("priceException"));
        }
    }

    private static Date toSqlDate(LocalDate localDate) {
        return localDate == null ? null : Date.valueOf(localDate);
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
