package org.babi.backend.place.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Address {

    private String streetNumber;
    private String route;
    private String locality;
    private String administrativeAreaLevel2;
    private String administrativeAreaLevel1;
    private String country;
    private String postalCode;
    private double longitude;
    private double latitude;
}
