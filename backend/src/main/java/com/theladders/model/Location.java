package com.theladders.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Table(name = "location")
@Getter
@Setter
@NoArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;

    private String state;

    @NonNull
    private String country = "";

    public Location(String city, String state, @NonNull String country) {
        if (country.isBlank()) {
            throw new IllegalArgumentException("country cannot be blank");
        }
        this.city = city;
        this.state = state;
        this.country = country;
    }
}
