package cz.muni.fi.GUI;

import cz.muni.fi.lease.Lease;
import cz.muni.fi.lease.LeaseManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Table to display leases from database.
 *
 * @author Robert Tamas
 */
public class LeasesTableModel extends DefaultTableModel {

    private final LeaseManager leaseManager;
    private List<Lease> leases;

    private static ResourceBundle texts = ResourceBundle.getBundle("Texts");

    private final static Logger log = Logger.getLogger(
            LeasesTableModel.class.getName());

    public LeasesTableModel(LeaseManager leaseManager) {
        this.leaseManager = leaseManager;
        try {
            leases = leaseManager.findAllLeases();
        } catch (Exception e) {
            log.log(Level.SEVERE, texts.getString("failedFind"), e);
        }
    }

    @Override
    public int getRowCount() {
        return leases != null ? leases.size() : 0;
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public String getColumnName(int column) {

        switch (column) {
            case 0:
                return texts.getString("ID");
            case 1:
                return texts.getString("carID");
            case 2:
                return texts.getString("customerID");
            case 3:
                return texts.getString("total");
            case 4:
                return texts.getString("startTime");
            case 5:
                return texts.getString("expectedEndTime");
            case 6:
                return texts.getString("realEndTime");
            default:
                throw new IllegalArgumentException(
                        texts.getString("noColumn") + column);
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        Lease lease = leases.get(row);
        switch (column) {
            case 0:
                return lease.getId();
            case 1:
                return lease.getCar().getId() +
                        " (" + lease.getCar().getLicencePlate() + ")";
            case 2:
                return lease.getCustomer().getId() +
                        " (" + lease.getCustomer().getFullName() + ")";
            case 3:
                return lease.getPrice();
            case 4:
                return lease.getStartTime();
            case 5:
                return lease.getExpectedEndTime();
            case 6:
                return lease.getRealEndTime();
            default:
                throw new IllegalArgumentException(
                        texts.getString("noColumn") + column);
        }
    }

    /**
     * Deletes lease from DB in background.
     *
     * @param row position of customer in table displayed
     */
    public void deleteLeaseForRow(int row) {
        Lease lease = leases.get(row);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    leaseManager.deleteLease(lease);
                    log.log(Level.INFO, texts.getString("succDeleted") +
                            lease.toString());
                } catch (Exception e) {
                    log.log(Level.SEVERE, texts.getString("cantDelete") +
                            lease.toString(), e);
                } finally {
                    leases = leaseManager.findAllLeases();
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
     * Adds lease to DB in background.
     *
     * @param lease lease to be added
     */
    public void addLease(Lease lease) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    leaseManager.createLease(lease);
                    log.log(Level.INFO, texts.getString("succAdded") +
                            lease.toString());
                } catch (Exception e) {
                    log.log(Level.SEVERE, texts.getString("cantAdd") +
                            lease.toString(), e);
                    System.out.println(e);
                } finally {
                    leases = leaseManager.findAllLeases();
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
     * Updates lease in DB in background.
     *
     * @param lease lease to be updated
     */
    public void updateLease(Lease lease) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    leaseManager.updateLease(lease);
                    log.log(Level.INFO, texts.getString("succUpdated") +
                            lease.toString());
                } catch (Exception e) {
                    log.log(Level.SEVERE, texts.getString("cantUpdate") +
                            lease.toString(), e);
                } finally {
                    leases = leaseManager.findAllLeases();
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
     * Returns lease at specific position in table
     *
     * @param row no. of row where lease is located
     * @return specific lease
     */
    public Lease getLease(int row) {
        return leases.get(row);
    }

    public List<Lease> getLeases() {
        return leases;
    }
}

