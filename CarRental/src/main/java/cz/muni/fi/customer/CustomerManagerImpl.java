package cz.muni.fi.customer;

import cz.muni.fi.exceptions.IllegalEntityException;
import cz.muni.fi.exceptions.ValidationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Customer manager implementation.
 *
 * @author Robert Tamas, Maros Struk
 */
public class CustomerManagerImpl implements CustomerManager {

    private JdbcTemplate jdbc;
    private static ResourceBundle texts = ResourceBundle.getBundle("Texts");

    public CustomerManagerImpl() {
    }

    public CustomerManagerImpl(DataSource dataSource) {
        jdbc = new JdbcTemplate(dataSource);
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public void createCustomer(Customer customer) {
        validate(customer);
        if (customer.getId() != null) {
            throw new IllegalEntityException(texts.getString("customerID") +
                    texts.getString("isntNull"));
        }
        SimpleJdbcInsert insertCustomer = new SimpleJdbcInsert(jdbc)
                .withTableName("customer").usingGeneratedKeyColumns("id");

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("full_name", customer.getFullName())
                .addValue("address", customer.getAddress())
                .addValue("phone_number", customer.getPhoneNumber());

        Number id = insertCustomer.executeAndReturnKey(parameters);
        customer.setId(id.longValue());
    }

    @Override
    public Customer getCustomerById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(texts.getString("customerID") +
                    texts.getString("isNull"));
        }
        try {
            return jdbc.queryForObject("SELECT * FROM customer WHERE id=?",
                    customerMapper, id);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    @Transactional
    @Override
    public List<Customer> findAllCustomers() {
        return jdbc.query("SELECT * FROM customer", customerMapper);
    }

    @Override
    public void updateCustomer(Customer customer) {
        validate(customer);
        if (customer.getId() == null) {
            throw new IllegalArgumentException(texts.getString("customerID") +
                    texts.getString("isNull"));
        }
        int rowsChanged = jdbc.update("UPDATE customer " +
                        "set full_name=?,address=?,phone_number=? where id=?",
                customer.getFullName(), customer.getAddress(),
                customer.getPhoneNumber(), customer.getId());
        if (rowsChanged == 0) {
            throw new IllegalEntityException(texts.getString(
                    "errorInsertingDB"));
        }
    }

    @Override
    public void deleteCustomer(Customer customer) {
        validate(customer);
        int rowsChanged = jdbc.update("DELETE FROM customer WHERE id=?",
                customer.getId());
        if (rowsChanged == 0) {
            throw new IllegalEntityException(texts.getString(
                    "errorDeletingDB"));
        }
    }

    /**
     * Maps each line of results to customer entity
     *
     * @param rs results set
     * @return customer entity
     */
    private RowMapper<Customer> customerMapper = (rs, rowNum) ->
            new Customer(rs.getLong("id"),
                    rs.getString("full_name"),
                    rs.getString("address"),
                    rs.getString("phone_number"));

    @Override
    public void validate(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException(texts.getString("customerID") +
                    texts.getString("isNull"));
        }
        if (customer.getFullName() == null
                || customer.getFullName().equals("")) {
            throw new ValidationException(texts.getString("field") +
                    texts.getString("fullName") +
                    texts.getString("cantBeEmpty"));
        }
        if (customer.getAddress() == null
                || customer.getAddress().equals("")) {
            throw new ValidationException(texts.getString("field") +
                    texts.getString("address") +
                    texts.getString("cantBeEmpty"));
        }
        if (customer.getPhoneNumber() == null
                || customer.getPhoneNumber().equals("")) {
            throw new ValidationException(texts.getString("field") +
                    texts.getString("phoneNumber") +
                    texts.getString("cantBeEmpty"));
        }
    }
}
