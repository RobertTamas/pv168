package cz.muni.fi.GUI;

import cz.muni.fi.customer.Customer;
import cz.muni.fi.customer.CustomerManager;
import cz.muni.fi.exceptions.ServiceFailureException;
import cz.muni.fi.exceptions.ValidationException;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Form for adding/updating customer.
 *
 * @author Robert Tamas
 */
public class CustomerDetail {
    private JButton saveButton;
    private JButton cancelButton;
    private JTextField fullName;
    private JTextField address;
    private JTextField number;
    private JPanel detail;

    /**
     * Constructor for Adding new customer.
     * Constructor after hitting save button will create new instance of
     * customer and validate it. If there's no problem it'll be sent to
     * CustomersTableModel to save it into database in background using
     * new thread. Otherwise error message will be displayed.
     *
     * @param customerManager     manager for validation and editing
     *                            customers instances
     * @param frame               frame itself so program can close itself
     * @param customersTableModel table which is used for adding
     *                            a new customer to database
     */
    public CustomerDetail(CustomerManager customerManager, JFrame frame,
                          CustomersTableModel customersTableModel) {
        super();
        saveButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Customer customer = new Customer(fullName.getText(),
                            address.getText(), number.getText());
                    customerManager.validate(customer);
                    customersTableModel.addCustomer(customer);
                    frame.dispose();
                } catch (IllegalArgumentException |
                        ServiceFailureException | ValidationException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
            }
        });
        cancelButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
    }

    /**
     * Constructor for Editing existing customer.
     * Constructor after hitting save button will create copy of customer
     * you want to update, writes changes to instance and validates it.
     * If there's no problem it'll be sent to CustomersTableModel to save
     * changes into database in background using new thread.
     * Otherwise error message will be displayed.
     *
     * @param customerManager     manager for validation and editing
     *                            customers instances
     * @param frame               frame itself so program can close itself
     * @param customersTableModel table which is used for editing existing
     *                            customers to database
     * @param row                 no. of row where customer is displayed in GUI
     */
    public CustomerDetail(CustomerManager customerManager, JFrame frame,
                          CustomersTableModel customersTableModel,
                          Integer row) {
        super();
        Customer customer = customersTableModel.getCustomers().get(row);
        Customer customerCopy = new Customer(customer.getFullName(),
                customer.getAddress(), customer.getPhoneNumber());
        customerCopy.setId(customer.getId());
        fullName.setText(customer.getFullName());
        address.setText(customer.getAddress());
        number.setText(customer.getPhoneNumber());
        saveButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    customerCopy.setFullName(fullName.getText());
                    customerCopy.setAddress(address.getText());
                    customerCopy.setPhoneNumber(number.getText());
                    customerManager.validate(customerCopy);
                    customersTableModel.updateCustomer(customerCopy);
                    frame.dispose();
                } catch (IllegalArgumentException |
                        ServiceFailureException | ValidationException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
            }
        });
        cancelButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
    }

    public JPanel getDetail() {
        return detail;
    }
}
