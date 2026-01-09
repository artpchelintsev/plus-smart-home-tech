package ru.yandex.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Random;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String country;
    private String city;
    private String street;
    private String house;
    private String flat;

    private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};

    public String getAddress() {
        int index = new Random().nextInt(ADDRESSES.length);
        return ADDRESSES[index];
    }
}
