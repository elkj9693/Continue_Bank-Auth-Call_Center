package com.entrusting.backend.user.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String accountName;

    @Column(nullable = false)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String accountPin; // Hashed PIN
    private String salt; // Unique salt for PIN
    private String status; // ACTIVE, SUSPENDED
    private int pinFailCount = 0;
    private String accountType; // e.g., "SAVINGS", "CHECKING"

    private LocalDateTime createdAt;

    public Account() {
    }

    public Account(String accountNumber, String accountName, BigDecimal balance, User user, String accountPin,
            String salt) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.balance = balance;
        this.user = user;
        this.accountPin = accountPin;
        this.salt = salt;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAccountPin() {
        return accountPin;
    }

    public void setAccountPin(String accountPin) {
        this.accountPin = accountPin;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPinFailCount() {
        return pinFailCount;
    }

    public void setPinFailCount(int pinFailCount) {
        this.pinFailCount = pinFailCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}
