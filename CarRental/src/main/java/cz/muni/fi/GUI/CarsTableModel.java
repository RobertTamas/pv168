package cz.muni.fi.GUI;

import cz.muni.fi.car.Car;
import cz.muni.fi.car.CarManager;
import cz.muni.fi.exceptions.ServiceFailureException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Table to display cars from database.
 *
 * @author Robert Tamas
 */
public class CarsTableModel extends DefaultTableModel {
    private final CarManager carManager;
    private List<Car> cars;

    private static ResourceBundle texts = ResourceBundle.getBundle("Texts");

    private final static Logger log = Logger.getLogger(
            CarsTableModel.class.getName());

    public CarsTableModel(CarManager carManager) {
        this.carManager = carManager;
        try {
            cars = carManager.findAllCars();
        } catch (Exception e) {
            log.log(Level.SEVERE, texts.getString("failedFind"), e);
        }
    }

    @Override
    public int getRowCount() {
        return cars != null ? cars.size() : 0;
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public String getColumnName(int column) {

        switch (column) {
            case 0:
                return texts.getString("ID");
            case 1:
                return texts.getString("licence");
            case 2:
                return texts.getString("brand");
            case 3:
                return texts.getString("model");
            case 4:
                return texts.getString("price");
            default:
                throw new IllegalArgumentException(
                        texts.getString("noColumn") + column);
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        Car car = cars.get(row);
        switch (column) {
            case 0:
                return car.getId();
            case 1:
                return car.getLicencePlate();
            case 2:
                return car.getBrand();
            case 3:
                return car.getModel();
            case 4:
                return car.getPricePerDay();
            default:
                throw new IllegalArgumentException(
                        texts.getString("noColumn") + column);
        }
    }

    /**
     * Deletes car from DB in background.
     *
     * @param row position of car in table displayed
     */
    public void deleteCarForRow(int row) {
        Car car = cars.get(row);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    carManager.deleteCar(car);
                    log.log(Level.INFO, texts.getString("succDeleted") +
                            car.toString());
                } catch (ServiceFailureException e) {
                    log.log(Level.SEVERE, texts.getString("cantDelete") +
                            car.toString(), e);
                } finally {
                    cars = carManager.findAllCars();
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
     * Adds car to DB in background.
     *
     * @param car car to be added
     */
    public void addCar(Car car) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    carManager.createCar(car);
                    log.log(Level.INFO, texts.getString("succAdded") +
                            car.toString());
                } catch (ServiceFailureException e) {
                    log.log(Level.SEVERE, texts.getString("cantAdd") +
                            car.toString(), e);
                } finally {
                    cars = carManager.findAllCars();
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
     * Updates car in DB in background.
     *
     * @param car car to be updated
     */
    public void updateCar(Car car) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    carManager.updateCar(car);
                    log.log(Level.INFO, texts.getString("succUpdated") +
                            car.toString());
                } catch (Exception e) {
                    log.log(Level.SEVERE, texts.getString("cantUpdate") +
                            car.toString(), e);
                } finally {
                    cars = carManager.findAllCars();
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
     * Returns car at specific position in table
     *
     * @param row no. of row where car is located
     * @return specific car
     */
    public Car getCar(int row) {
        return cars.get(row);
    }

    public List<Car> getCars() {
        return cars;
    }
}
