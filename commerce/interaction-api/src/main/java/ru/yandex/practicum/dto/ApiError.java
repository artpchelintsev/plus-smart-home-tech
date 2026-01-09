package ru.yandex.practicum.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private Throwable cause;
    private List<StackTraceElement> stackTrace;
    private HttpStatus httpStatus;
    private String userMessage;
    private String message;
    private List<Throwable> suppressed;
    private String localizedMessage;

    public static ApiError fromException(Exception e, HttpStatus status) {
        return ApiError.builder()
                .cause(e.getCause())
                .stackTrace(Arrays.asList(e.getStackTrace()))
                .httpStatus(status)
                .userMessage(e.getMessage())
                .message(e.toString())
                .localizedMessage(e.getLocalizedMessage())
                .suppressed(Arrays.asList(e.getSuppressed()))
                .build();
    }
}
