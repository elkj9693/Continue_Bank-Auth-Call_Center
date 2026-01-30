package com.trustee.backend.auth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "carrier_user")
public class CarrierUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String carrier;

    @Column(length = 6)
    private String residentFront;

    public CarrierUser() {
    }

    public CarrierUser(String name, String phoneNumber, String carrier, String residentFront) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.carrier = carrier;
        this.residentFront = residentFront;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getResidentFront() {
        return residentFront;
    }

    public void setResidentFront(String residentFront) {
        this.residentFront = residentFront;
    }
}
