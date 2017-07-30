package cz.muni.fi.lease;

import cz.muni.fi.car.Car;
import cz.muni.fi.customer.Customer;

import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * This entity class represents Lease. Lease has its car which is rent to
 * customer for some price. Lease also has its start time, expected and
 * real end time. All parameters are mandatory except real end time which
 * can be set to null.
 *
 * @author Robert Tamas
 */
public class Lease {

    private Long id;
    private Car car;
    private Customer customer;
    private int price;
    private LocalDate startTime;
    private LocalDate expectedEndTime;
    private LocalDate realEndTime;
    private static ResourceBundle texts = ResourceBundle.getBundle("Texts");

    public Lease(Car car, Customer customer, int price, LocalDate startTime,
                 LocalDate expectedEndTime, LocalDate realEndTime) {
        this.car = car;
        this.customer = customer;
        this.price = price;
        this.startTime = startTime;
        this.expectedEndTime = expectedEndTime;
        this.realEndTime = realEndTime;
    }

    public Long getId() {
        return id;
    }

    public Car getCar() {
        return car;
    }

    public Customer getCustomer() {
        return customer;
    }

    public int getPrice() {
        return price;
    }

    public LocalDate getStartTime() {
        return startTime;
    }

    public LocalDate getExpectedEndTime() {
        return expectedEndTime;
    }

    public LocalDate getRealEndTime() {
        return realEndTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setStartTime(LocalDate startTime) {
        this.startTime = startTime;
    }

    public void setExpectedEndTime(LocalDate expectedEndTime) {
        this.expectedEndTime = expectedEndTime;
    }

    public void setRealEndTime(LocalDate realEndTime) {
        this.realEndTime = realEndTime;
    }

    @Override
    public String toString() {
        return texts.getString("leaseToString1") + getId() +
                texts.getString("leaseToString2") +
                getCar().getLicencePlate() +
                texts.getString("leaseToString3") +
                getCustomer().getFullName();
    }
}
