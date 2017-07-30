package cz.muni.fi.car;

import java.util.Objects;
import java.util.ResourceBundle;


/**
 * This entity class represents Car. Car has its licence plate, brand
 * manufacturer plus brand. Car also has its price which is paid for one day
 * of rent. All parameters are mandatory.
 *
 * @author Robert Tamas
 */
public class Car {

    private Long Id;
    private String licencePlate;
    private String brand;
    private String model;
    private int pricePerDay;
    private static ResourceBundle texts = ResourceBundle.getBundle("Texts");


    /**
     * Constructor for Car entity
     *
     * @param licencePlate car licence plate which is unique for each car
     * @param brand        car manufacturer
     * @param model        car model
     * @param pricePerDay  price which is paid as rent for a day
     */
    public Car(String licencePlate, String brand, String model,
               int pricePerDay) {
        this.licencePlate = licencePlate;
        this.brand = brand;
        this.model = model;
        this.pricePerDay = pricePerDay;
    }

    public void setId(Long id) {
        Id = id;
    }

    public void setLicencePlate(String licencePlate) {
        this.licencePlate = licencePlate;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setPricePerDay(int pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public Long getId() {
        return Id;
    }

    public String getLicencePlate() {
        return licencePlate;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getPricePerDay() {
        return pricePerDay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Car)) return false;
        Car car = (Car) o;
        return getId() == car.getId() &&
                getPricePerDay() == car.getPricePerDay() &&
                Objects.equals(getLicencePlate(), car.getLicencePlate()) &&
                Objects.equals(getBrand(), car.getBrand()) &&
                Objects.equals(getModel(), car.getModel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(),
                getLicencePlate(),
                getBrand(),
                getModel(),
                getPricePerDay());
    }

    @Override
    public String toString() {
        return texts.getString("carToString1")
                + getId()
                + texts.getString("carToString2")
                + getLicencePlate();
    }
}
