package cz.muni.fi.GUI;


import cz.muni.fi.car.CarManager;
import cz.muni.fi.car.CarManagerImpl;
import cz.muni.fi.customer.CustomerManager;
import cz.muni.fi.customer.CustomerManagerImpl;
import cz.muni.fi.lease.LeaseManager;
import cz.muni.fi.lease.LeaseManagerImpl;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.swing.*;
import java.awt.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.time.Month.FEBRUARY;

/**
 * Main GUI window.
 *
 * @author Robert Tamas
 */
public class CarRentalGUI {
    private static EmbeddedDatabase dataSource;

    private JPanel panel1;
    private JTabbedPane tabbedPane1;
    private JButton createCarButton;
    private JButton updateCarButton;
    private JButton deleteCarButton;
    private JTable carTable;
    private JTable customerTable;
    private JButton createCustomerButton;
    private JButton updateCustomerButton;
    private JButton deleteCustomerButton;
    private JTable leaseTable;
    private JButton createLeaseButton;
    private JButton updateLeaseButton;
    private JButton deleteLeaseButton;

    private static CarManager carManager;
    private static CustomerManager customerManager;
    private static CarsTableModel carsTableModel;
    private static CustomersTableModel customersTableModel;
    private static LeaseManager leaseManager;
    private static LeasesTableModel leasesTableModel;
    private static ResourceBundle texts = ResourceBundle.getBundle("Texts");

    private final static Logger log = Logger.getLogger(
            CarRentalGUI.class.getName());
    private final static ZonedDateTime NOW = LocalDateTime.of(2016, FEBRUARY,
            29, 14, 00).atZone(ZoneId.of("UTC"));

    /**
     * Creates action listeners for create/update/delete buttons for
     * all tree instances. Save and update will create new frame. Deleting
     * will create YES/NO question.
     */
    public CarRentalGUI() {
        super();

        deleteCarButton.setEnabled(false);
        updateCarButton.setEnabled(false);
        deleteCustomerButton.setEnabled(false);
        updateCustomerButton.setEnabled(false);
        deleteLeaseButton.setEnabled(false);
        updateLeaseButton.setEnabled(false);

        createCarButton.addActionListener((e) -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setTitle(texts.getString("createCar"));
            CarDetail carDetail = new CarDetail(carManager, frame,
                    carsTableModel);
            carDetail.getDetail().setPreferredSize(new Dimension(250, 270));
            frame.setContentPane(carDetail.getDetail());
            frame.pack();
            frame.setVisible(true);
        });
        updateCarButton.addActionListener((e) -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setTitle(texts.getString("updateCar"));
            CarDetail carDetail = new CarDetail(carManager, frame,
                    carsTableModel, carTable.convertRowIndexToModel(
                    carTable.getSelectedRow()));
            carDetail.getDetail().setPreferredSize(new Dimension(250, 270));
            frame.setContentPane(carDetail.getDetail());
            frame.pack();
            frame.setVisible(true);
        });
        deleteCarButton.addActionListener((e) -> {
            int row = carTable.convertRowIndexToModel(
                    carTable.getSelectedRow());
            if (leaseManager.findAllLeasesForCar(
                    carsTableModel.getCars().get(row)).isEmpty()) {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(
                        WindowConstants.DISPOSE_ON_CLOSE);
                int n = JOptionPane.showConfirmDialog(frame,
                        texts.getString("deleteQuestion") +
                                carsTableModel.getCar(row).toString() + "?",
                        texts.getString("sillyFrame"),
                        JOptionPane.YES_NO_OPTION);
                if (n == 0) {
                    carsTableModel.deleteCarForRow(row);
                }
            } else {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(
                        WindowConstants.DISPOSE_ON_CLOSE);
                JOptionPane.showMessageDialog(frame,
                        texts.getString("ups") +
                                carsTableModel.getCar(row).toString() +
                                texts.getString("cannotDelete"));
            }
        });

        createCustomerButton.addActionListener((e) -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setTitle(texts.getString("createCustomer"));
            CustomerDetail customerDetail = new CustomerDetail(customerManager,
                    frame, customersTableModel);
            customerDetail.getDetail().setPreferredSize(new Dimension(250, 270));
            frame.setContentPane(customerDetail.getDetail());
            frame.pack();
            frame.setVisible(true);
        });
        updateCustomerButton.addActionListener((e) -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setTitle(texts.getString("updateCustomer"));
            CustomerDetail customerDetail = new CustomerDetail(customerManager,
                    frame, customersTableModel,
                    customerTable.convertRowIndexToModel(
                            customerTable.getSelectedRow()));
            customerDetail.getDetail().setPreferredSize(new Dimension(250, 250));
            frame.setContentPane(customerDetail.getDetail());
            frame.pack();
            frame.setVisible(true);
        });
        deleteCustomerButton.addActionListener((e) -> {
            int row = customerTable.convertRowIndexToModel(
                    customerTable.getSelectedRow());
            if (leaseManager.findAllLeasesForCustomer(
                    customersTableModel.getCustomers().get(row)).isEmpty()) {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(
                        WindowConstants.DISPOSE_ON_CLOSE);
                int n = JOptionPane.showConfirmDialog(frame,
                        texts.getString("deleteQuestion") +
                                customersTableModel.getCustomer(
                                        row).toString() + "?",
                        texts.getString("sillyFrame"),
                        JOptionPane.YES_NO_OPTION);
                if (n == 0) {
                    customersTableModel.deleteCustomerForRow(row);
                }
            } else {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(
                        WindowConstants.DISPOSE_ON_CLOSE);
                JOptionPane.showMessageDialog(frame,
                        texts.getString("ups") +
                                carsTableModel.getCar(row).toString() +
                                texts.getString("cannotDelete"));
            }
        });

        createLeaseButton.addActionListener((e) -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setTitle(texts.getString("createLease"));
            LeaseDetail leaseDetail = new LeaseDetail(carManager,
                    customerManager, leaseManager, frame, leasesTableModel);
            leaseDetail.getDetail().setPreferredSize(new Dimension(300, 350));
            frame.setContentPane(leaseDetail.getDetail());
            frame.pack();
            frame.setVisible(true);
        });
        updateLeaseButton.addActionListener((e) -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setTitle(texts.getString("updateLease"));
            LeaseDetail leaseDetail = new LeaseDetail(carManager,
                    customerManager, leaseManager, frame, leasesTableModel,
                    leaseTable.convertRowIndexToModel(
                            leaseTable.getSelectedRow()));
            leaseDetail.getDetail().setPreferredSize(new Dimension(300, 350));
            frame.setContentPane(leaseDetail.getDetail());
            frame.pack();
            frame.setVisible(true);
        });
        deleteLeaseButton.addActionListener((e) -> {
            int row = leaseTable.convertRowIndexToModel(
                    leaseTable.getSelectedRow());
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            int n = JOptionPane.showConfirmDialog(frame,
                    texts.getString("deleteQuestion") +
                            leasesTableModel.getLease(row).toString() + "?",
                    texts.getString("sillyFrame"),
                    JOptionPane.YES_NO_OPTION);
            if (n == 0) {
                leasesTableModel.deleteLeaseForRow(row);
            }
        });

    }

    /**
     * Creates embedded database from script, instances managers,
     * table models and main GUI frame.
     *
     * @param args
     */
    public static void main(String[] args) {
        log.log(Level.INFO, texts.getString("init"));

        try {
            dataSource = new EmbeddedDatabaseBuilder().setType(
                    EmbeddedDatabaseType.DERBY).addScript(
                    "fillTables.sql").build();
            log.log(Level.INFO, texts.getString("connected"));
        } catch (Exception e) {
            log.log(Level.SEVERE, texts.getString("cantConnect"), e);
        }


        carManager = new CarManagerImpl(dataSource);
        carsTableModel = new CarsTableModel(carManager);
        customerManager = new CustomerManagerImpl(dataSource);
        customersTableModel = new CustomersTableModel(customerManager);
        leaseManager = new LeaseManagerImpl(dataSource,
                Clock.fixed(NOW.toInstant(), NOW.getZone()));
        leasesTableModel = new LeasesTableModel(leaseManager);

        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setTitle(texts.getString("carRental"));
            CarRentalGUI mainForm = new CarRentalGUI();
            mainForm.panel1.setPreferredSize(new Dimension(800, 600));
            frame.setContentPane(mainForm.panel1);
            frame.pack();
            frame.setVisible(true);
        });
    }


    /**
     * Creates JTable sites and fill them. Also adds selection listeners
     * to buttons so update and delete are clickable only when something
     * is selected.
     */
    private void createUIComponents() {
        carTable = new JTable(carsTableModel);
        ListSelectionModel carSelectionModel = carTable.getSelectionModel();
        carSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        carSelectionModel.addListSelectionListener((e) -> {
            addListSelectionListener(carTable, deleteCarButton,
                    updateCarButton);
        });

        customerTable = new JTable(customersTableModel);
        ListSelectionModel customerSelectionModel =
                customerTable.getSelectionModel();
        customerSelectionModel.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        customerSelectionModel.addListSelectionListener((e) -> {
            addListSelectionListener(customerTable,
                    deleteCustomerButton, updateCustomerButton);
        });

        leaseTable = new JTable(leasesTableModel);
        ListSelectionModel leaseSelectionModel = leaseTable.getSelectionModel();
        leaseSelectionModel.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        leaseSelectionModel.addListSelectionListener((e) -> {
            addListSelectionListener(leaseTable,
                    deleteLeaseButton, updateLeaseButton);
        });
    }

    private void addListSelectionListener(JTable table,
                                          JButton delete, JButton update) {
        if (table.getSelectedRowCount() == 1) {
            delete.setEnabled(true);
            update.setEnabled(true);
        } else {
            delete.setEnabled(false);
            update.setEnabled(false);
        }
        ;
    }
}
