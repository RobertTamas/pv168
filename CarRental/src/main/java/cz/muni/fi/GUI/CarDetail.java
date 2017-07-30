package cz.muni.fi.GUI;

import cz.muni.fi.car.Car;
import cz.muni.fi.car.CarManager;
import cz.muni.fi.exceptions.ServiceFailureException;
import cz.muni.fi.exceptions.ValidationException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

/**
 * Form for adding/updating car.
 *
 * @author Robert Tamas
 */
public class CarDetail {
    private JTextField licencePlate;
    private JTextField model;
    private JTextField brand;
    private JTextField price;
    private JButton saveButton;
    private JButton cancelButton;
    private JPanel detail;
    private static ResourceBundle texts = ResourceBundle.getBundle("Texts");

    /**
     * Constructor for Adding new car.
     * Constructor after hitting save button will create new instance of car
     * and validate it. If there's no problem it'll be sent to CarsTableModel
     * to save it into database in background using new thread. Otherwise error
     * message will be displayed.
     *
     * @param carManager     manager for validation and editing cars instances
     * @param frame          frame itself so program can close itself
     * @param carsTableModel table which is used for adding a new car to DB
     */
    public CarDetail(CarManager carManager, JFrame frame,
                     CarsTableModel carsTableModel) {
        super();
        saveButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Car car = new Car(licencePlate.getText(),
                            brand.getText(),
                            model.getText(),
                            Integer.parseInt(price.getText()));
                    carManager.validate(car);
                    carsTableModel.addCar(car);
                    frame.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame,
                            texts.getString("mustBeNumber"));
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
     * Constructor for Editing existing car.
     * Constructor after hitting save button will create copy of car you want
     * to update, writes changes to instance and validates it. If there's no
     * problem it'll be sent to CarsTableModel to save changes into database
     * in background using new thread. Otherwise error message will be
     * displayed.
     *
     * @param carManager     manager for validation and editing cars instances
     * @param frame          frame itself so program can close itself
     * @param carsTableModel table which is used for editing existing
     *                       cars to database
     * @param row            no. of row where car is displayed in GUI
     */
    public CarDetail(CarManager carManager, JFrame frame,
                     CarsTableModel carsTableModel, Integer row) {
        super();
        Car car = carsTableModel.getCars().get(row);
        Car carCopy = new Car(car.getLicencePlate(), car.getBrand(),
                car.getModel(), car.getPricePerDay());
        carCopy.setId(car.getId());
        licencePlate.setText(car.getLicencePlate());
        model.setText(car.getModel());
        brand.setText(car.getBrand());
        price.setText(Integer.toString(car.getPricePerDay()));
        saveButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    carCopy.setLicencePlate(licencePlate.getText());
                    carCopy.setModel(model.getText());
                    carCopy.setBrand(brand.getText());
                    carCopy.setPricePerDay(Integer.parseInt(price.getText()));
                    carManager.validate(carCopy);
                    carsTableModel.updateCar(carCopy);
                    frame.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame,
                            texts.getString("mustBeNumber"));
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
