package ru.yandex.practicum.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private String country;
    private String city;
    private String street;
    private String house;
    private String flat;
}
