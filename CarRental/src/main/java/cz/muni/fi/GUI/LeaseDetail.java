package cz.muni.fi.GUI;


import cz.muni.fi.car.Car;
import cz.muni.fi.car.CarManager;
import cz.muni.fi.customer.Customer;
import cz.muni.fi.customer.CustomerManager;
import cz.muni.fi.exceptions.ServiceFailureException;
import cz.muni.fi.exceptions.ValidationException;
import cz.muni.fi.lease.Lease;
import cz.muni.fi.lease.LeaseManager;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Form for adding/updating lease.
 *
 * @author Robert Tamas
 */
public class LeaseDetail {
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel carID;
    private JLabel customerID;
    private JLabel expectedEnd;
    private JLabel realDate;
    private JLabel startTime;
    private JPanel detail;
    private JComboBox carsField;
    private JComboBox customersField;
    private JDatePickerImpl jDateStart;
    private JDatePickerImpl jDateExpected;
    private JDatePickerImpl jDateReal;
    private JRadioButton endIsNull;

    private CarManager carManager;
    private CustomerManager customerManager;

    private static ResourceBundle texts = ResourceBundle.getBundle("Texts");

    /**
     * Constructor for Adding new lease.
     * Calling constructor will list all cars and customers for easier
     * picking from JComboBox. Constructor after hitting save button will
     * create new instance of lease and validate it. If there's no problem
     * it'll be sent to LeasesTableModel to save it into database in background
     * using new thread. Otherwise error message will be displayed.
     *
     * @param carManager       manager for cars instances for listing cars
     *                         and displaying more info about them
     * @param customerManager  manager for customers instances for listing cars
     *                         and displaying more info about them
     * @param leaseManager     manager for validation and editing instances
     * @param frame            frame itself so program can close itself
     * @param leasesTableModel table which is used for adding
     *                         a new lease to database
     */
    public LeaseDetail(CarManager carManager, CustomerManager customerManager,
                       LeaseManager leaseManager, JFrame frame,
                       LeasesTableModel leasesTableModel) {
        super();
        this.carManager = carManager;
        this.customerManager = customerManager;

        fillAllFields();

        saveButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Lease lease = new Lease(
                        parseCar(String.valueOf(carsField.getSelectedItem())),
                        parseCustomer(String.valueOf(
                                customersField.getSelectedItem())), 0,
                        parseDate(jDateStart), parseDate(jDateExpected), null);

                if (endIsNull.isSelected()) {
                    lease.setRealEndTime(null);
                } else {
                    lease.setRealEndTime(parseDate(jDateReal));
                }
                calculatePrice(lease);

                try {
                    leaseManager.validate(lease);
                    leasesTableModel.addLease(lease);
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
     * Constructor for Editing existing lease.
     * Calling constructor will list all cars and customers for easier picking
     * from JComboBox. Constructor after hitting save button will create copy
     * of lease you want to update, writes changes to instance and validate it.
     * If there's no problem it'll be sent to LeasesTableModel to save changes
     * into database in background using new thread. Otherwise error message
     * will be displayed.
     *
     * @param carManager       manager for cars instances for listing cars
     *                         and displaying more info about them
     * @param customerManager  manager for customers instances for listing cars
     *                         and displaying more info about them
     * @param leaseManager     manager for validation and editing lease instances
     * @param frame            frame itself so program can close itself
     * @param leasesTableModel table which is used for editing existing
     *                         lease to database
     * @param row              no. of row where lease is displayed in GUI
     */
    public LeaseDetail(CarManager carManager, CustomerManager customerManager,
                       LeaseManager leaseManager, JFrame frame,
                       LeasesTableModel leasesTableModel, Integer row) {
        super();
        this.carManager = carManager;
        this.customerManager = customerManager;
        Lease lease = leasesTableModel.getLeases().get(row);
        Lease leaseCopy = new Lease(lease.getCar(), lease.getCustomer(),
                lease.getPrice(), lease.getStartTime(),
                lease.getExpectedEndTime(), lease.getRealEndTime());
        leaseCopy.setId(lease.getId());

        fillAllFields();

        carsField.setSelectedItem(lease.getCar().getId().toString() +
                " (" + lease.getCar().getLicencePlate() + ")");
        customersField.setSelectedItem(lease.getCustomer().getId().toString() +
                " (" + lease.getCustomer().getFullName() + ")");


        jDateStart.getModel().setDate(lease.getStartTime().getYear(),
                lease.getStartTime().getMonthValue() - 1,
                lease.getStartTime().getDayOfMonth());
        jDateStart.getModel().setSelected(true);

        jDateExpected.getModel().setDate(lease.getExpectedEndTime().getYear(),
                lease.getExpectedEndTime().getMonthValue() - 1,
                lease.getExpectedEndTime().getDayOfMonth());
        jDateExpected.getModel().setSelected(true);

        if (lease.getRealEndTime() != null) {
            jDateReal.getModel().setDate(lease.getRealEndTime().getYear(),
                    lease.getRealEndTime().getMonthValue() - 1,
                    lease.getRealEndTime().getDayOfMonth());
            jDateReal.getModel().setSelected(true);
        }

        saveButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    leaseCopy.setCar(parseCar(
                            String.valueOf(carsField.getSelectedItem())));
                    leaseCopy.setCustomer(parseCustomer(
                            String.valueOf(customersField.getSelectedItem())));
                    leaseCopy.setStartTime(parseDate(jDateStart));
                    leaseCopy.setExpectedEndTime(parseDate(jDateExpected));
                    if (endIsNull.isSelected()) {
                        leaseCopy.setRealEndTime(null);
                    } else {
                        leaseCopy.setRealEndTime(parseDate(jDateReal));
                    }
                    calculatePrice(leaseCopy);

                    leaseManager.validate(leaseCopy);
                    leasesTableModel.updateLease(leaseCopy);
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
     * Finds all cars and customers in database and list them into JComboBoxes.
     */
    public void fillAllFields() {

        for (Car car : carManager.findAllCars()) {
            carsField.addItem(car.getId().toString() + " (" +
                    car.getLicencePlate() + ")");
        }

        for (Customer customer : customerManager.findAllCustomers()) {
            customersField.addItem(customer.getId().toString() + " (" +
                    customer.getFullName() + ")");
        }
    }

    /**
     * Creates fields with JDatePicker.
     */
    private void createUIComponents() {
        UtilDateModel startModel = new UtilDateModel();
        UtilDateModel expectedModel = new UtilDateModel();
        UtilDateModel realModel = new UtilDateModel();

        Properties p = new Properties();
        p.put("text.today", texts.getString("today"));
        p.put("text.month", texts.getString("month"));
        p.put("text.year", texts.getString("year"));

        JDatePanelImpl startPanel = new JDatePanelImpl(startModel, p);
        JDatePanelImpl expectedPanel = new JDatePanelImpl(expectedModel, p);
        JDatePanelImpl realPanel = new JDatePanelImpl(realModel, p);

        jDateStart = new JDatePickerImpl(startPanel, new DateLabelFormatter());
        jDateExpected = new JDatePickerImpl(expectedPanel,
                new DateLabelFormatter());
        jDateReal = new JDatePickerImpl(realPanel, new DateLabelFormatter());

    }

    /**
     * Parses string input from JComboBox and finds car by ID.
     *
     * @param input string following rule where the ID of car is the first thing
     *              followed with space "2 (...)"
     * @return car with specific ID
     */
    private Car parseCar(String input) {
        return carManager.getCarById(Long.valueOf(input.split("")[0]));
    }

    /**
     * Parses string input from JComboBox and finds costumer by ID.
     *
     * @param input string following rule where the ID of customer is the first
     *              thing followed with space "2 (...)"
     * @return car with specific ID
     */
    private Customer parseCustomer(String input) {
        return customerManager.getCustomerById(
                Long.valueOf(input.split("")[0]));
    }

    /**
     * Parses date from JDatePicker.
     *
     * @param picker datePicker to be parsed
     * @return date in LocalDate format
     */
    private LocalDate parseDate(JDatePickerImpl picker) {
        return LocalDate.of(picker.getModel().getYear(),
                Month.of(picker.getModel().getMonth() + 1),
                picker.getModel().getDay());
    }

    /**
     * Calculates price for lease between start date and expected
     * date of return.
     *
     * @param lease lease where price should be calculated
     */
    private void calculatePrice(Lease lease) {
        long daysBetween = ChronoUnit.DAYS.between(lease.getStartTime(),
                lease.getExpectedEndTime());
        lease.setPrice((int) daysBetween * lease.getCar().getPricePerDay());

    }

    public JPanel getDetail() {
        return detail;
    }
}
