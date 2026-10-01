package com.billing.billing_system.shop;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "shop_profile")
public class ShopProfile {

    @Id
    private Long id = 1L;

    @NotBlank(message = "Shop name is required")
    @Size(max = 100, message = "Shop name is too long")
    @Column(nullable = false)
    private String shopName;

    @Size(max = 255, message = "Address is too long")
    private String address;

    @Size(max = 20, message = "Phone is too long")
    private String phone;

    @Size(max = 15, message = "GSTIN must be at most 15 characters")
    private String gstin;

    @Size(max = 255, message = "Footer text is too long")
    private String footerText;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }

    public String getFooterText() { return footerText; }
    public void setFooterText(String footerText) { this.footerText = footerText; }
}