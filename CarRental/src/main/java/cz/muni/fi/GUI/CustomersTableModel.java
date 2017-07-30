package cz.muni.fi.GUI;

import cz.muni.fi.customer.Customer;
import cz.muni.fi.customer.CustomerManager;
import cz.muni.fi.exceptions.ServiceFailureException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Table to display customers from database.
 *
 * @author Robert Tamas
 */
public class CustomersTableModel extends DefaultTableModel {
    private final CustomerManager customerManager;
    private List<Customer> customers;

    private static ResourceBundle texts = ResourceBundle.getBundle("Texts");

    private final static Logger log = Logger.getLogger(
            CustomersTableModel.class.getName());

    public CustomersTableModel(CustomerManager customerManager) {
        this.customerManager = customerManager;
        try {
            customers = customerManager.findAllCustomers();
        } catch (ServiceFailureException e) {
            log.log(Level.SEVERE, texts.getString("failedFind"), e);
        }
    }

    @Override
    public int getRowCount() {
        return customers != null ? customers.size() : 0;
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int column) {

        switch (column) {
            case 0:
                return texts.getString("ID");
            case 1:
                return texts.getString("fullName");
            case 2:
                return texts.getString("address");
            case 3:
                return texts.getString("phoneNumber");
            default:
                throw new IllegalArgumentException(
                        texts.getString("noColumn") + column);
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        Customer customer = customers.get(row);
        switch (column) {
            case 0:
                return customer.getId();
            case 1:
                return customer.getFullName();
            case 2:
                return customer.getAddress();
            case 3:
                return customer.getPhoneNumber();
            default:
                throw new IllegalArgumentException(
                        texts.getString("noColumn") + column);
        }
    }

    /**
     * Deletes customer from DB in background.
     *
     * @param row position of customer in table displayed
     */
    public void deleteCustomerForRow(int row) {
        Customer customer = customers.get(row);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    customerManager.deleteCustomer(customer);
                    log.log(Level.INFO, texts.getString("succDeleted") +
                            customer.toString());
                } catch (ServiceFailureException e) {
                    log.log(Level.SEVERE, texts.getString("cantDelete") +
                            customer.toString(), e);
                } finally {
                    customers = customerManager.findAllCustomers();
                }
                return null;
            }

            @Override
            protected void done() {
                fireTableDataChanged();
            }
        }.execute();
    }

    /**
     * Adds customer to DB in background.
     *
     * @param customer customer to be added
     */
    public void addCustomer(Customer customer) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    customerManager.createCustomer(customer);
                    log.log(Level.INFO, texts.getString("succAdded") +
                            customer.toString());
                } catch (ServiceFailureException e) {
                    log.log(Level.SEVERE, texts.getString("cantAdd") +
                            customer.toString(), e);
                } finally {
                    customers = customerManager.findAllCustomers();
                }
                return null;
            }

            @Override
            protected void done() {
                fireTableDataChanged();
            }
        }.execute();
    }


    /**
     * Updates customer in DB in background.
     *
     * @param customer car to be updated
     */
    public void updateCustomer(Customer customer) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    customerManager.updateCustomer(customer);
                    log.log(Level.INFO, texts.getString("succUpdated") +
                            customer.toString());
                } catch (ServiceFailureException e) {
                    log.log(Level.SEVERE, texts.getString("cantUpdate") +
                            customer.toString(), e);
                } finally {
                    customers = customerManager.findAllCustomers();
                }
                return null;
            }

            @Override
            protected void done() {
                fireTableDataChanged();
            }
        }.execute();
    }

    /**
     * Returns customer at specific position in table
     *
     * @param row no. of row where customer is located
     * @return specific customer
     */
    public Customer getCustomer(int row) {
        return customers.get(row);
    }

    public List<Customer> getCustomers() {
        return customers;
    }
}
