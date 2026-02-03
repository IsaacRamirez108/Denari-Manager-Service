package com.denari.manager.models.entity.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "property_manager_infos")
@Data
@EqualsAndHashCode(exclude = "rentalData")
@ToString(exclude = "rentalData")
public class PropertyManagerInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "individual_or_company")
    private ManagerType individualOrCompany;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "property_name")
    private String propertyName;

    @OneToOne
    @JoinColumn(name = "rental_data_id", nullable = false)
    private RentalData rentalData;

    public enum ManagerType {
        INDIVIDUAL, COMPANY
    }
}
