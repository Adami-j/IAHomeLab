package fr.lab.iahomelab.common.exception;

import java.time.OffsetDateTime;

public record ApiError(
        String code,
        String message,
        OffsetDateTime timestamp
){

}