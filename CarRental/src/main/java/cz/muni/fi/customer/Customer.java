package cz.muni.fi.customer;

import java.util.ResourceBundle;

/**
 * This entity class represents Customer. Customer has its full name,
 * address plus phone number. All parameters are mandatory.
 *
 * @author Robert Tamas
 */
public class Customer {

    private Long Id;
    private String fullName;
    private String address;
    private String phoneNumber;
    private static ResourceBundle texts = ResourceBundle.getBundle("Texts");

    /**
     * Constructor for customer.
     *
     * @param fullName    full name of customer separated with space
     * @param address     address of customer
     * @param phoneNumber phone number of customer
     */
    public Customer(String fullName, String address, String phoneNumber) {
        this.fullName = fullName;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Constructor for customer with ID.
     *
     * @param id          id of car
     * @param fullName    full name of customer separated with space
     * @param address     address of customer
     * @param phoneNumber phone number of customer
     */
    public Customer(Long id, String fullName, String address,
                    String phoneNumber) {
        this.Id = id;
        this.fullName = fullName;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public Long getId() {
        return Id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setId(Long id) {
        Id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return texts.getString("customerToString1") + getId() +
                texts.getString("customerToString2") + getFullName();
    }
}
