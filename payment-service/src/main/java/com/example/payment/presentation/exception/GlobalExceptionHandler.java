package com.example.payment.presentation.exception;

import com.example.payment.domain.exception.InvalidPaymentStateException;
import com.example.payment.domain.exception.PaymentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(PaymentNotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({InvalidPaymentStateException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(RuntimeException e) {
        return new ErrorResponse(e.getMessage());
    }
}
