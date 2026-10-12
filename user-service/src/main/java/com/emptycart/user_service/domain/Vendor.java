package com.emptycart.user_service.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "vendors")
public class Vendor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name="user_id",nullable = false,unique = true)
    private User user;

    @Column(nullable = false)
    private String shopName;

    @Column(nullable = false)
    private boolean active = false;   // an admin activates it later

    protected Vendor() {}   // required by JPA

    public Vendor(User user, String shopName) {
        this.user = user;
        this.shopName = shopName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
