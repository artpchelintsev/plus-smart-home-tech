package ru.yandex.practicum.exceptions;

import lombok.*;

@Getter
@Setter
@Builder
public class NotAuthorizedUserException extends RuntimeException {

    public NotAuthorizedUserException() {
        super("Имя пользователя не должно быть пустым");
    }
}
