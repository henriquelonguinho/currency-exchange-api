package com.currency.exchange.exception;

import com.currency.exchange.exception.custom.BusinessException;
import com.currency.exchange.exception.custom.CurrencyConversionException;
import com.currency.exchange.exception.custom.TransactionNotFoundException;
import com.currency.exchange.exception.custom.TreasuryApiUnavailableException;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.databind.exc.InvalidFormatException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final HttpStatusCode UNPROCESSABLE_ENTITY = HttpStatusCode.valueOf(422);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<String> messages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                status.value(),
                "Bad Request",
                getPath(request),
                messages
        );

        return new ResponseEntity<>(apiError, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String errorMessage = "Invalid input format. Check your fields.";

        if (ex.getCause() instanceof InvalidFormatException ife) {
            if (ife.getTargetType().equals(LocalDate.class)) {
                errorMessage = "Invalid date format. Expected format: yyyy-MM-dd";
            }
        }

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                status.value(),
                "Bad Request",
                getPath(request),
                List.of(errorMessage)
        );

        return new ResponseEntity<>(apiError, status);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String message = "Parameter '%s' is required".formatted(ex.getParameterName());

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                status.value(),
                "Bad Request",
                getPath(request),
                List.of(message)
        );

        return new ResponseEntity<>(apiError, status);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException ex, WebRequest request) {
        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                getPath(request),
                List.of(ex.getMessage())
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CurrencyConversionException.class)
    public ResponseEntity<ApiError> handleCurrencyConversionException(CurrencyConversionException ex, WebRequest request) {
        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                UNPROCESSABLE_ENTITY.value(),
                "Unprocessable Entity",
                getPath(request),
                List.of(ex.getMessage())
        );
        return new ResponseEntity<>(apiError, UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ApiError> handleTransactionNotFoundException(BusinessException ex, WebRequest request) {
        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                getPath(request),
                List.of(ex.getMessage())
        );
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TreasuryApiUnavailableException.class)
    public ResponseEntity<ApiError> handleTreasuryApiUnavailable(TreasuryApiUnavailableException ex, WebRequest request) {
        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                getPath(request),
                List.of(ex.getMessage())
        );
        return new ResponseEntity<>(apiError, HttpStatus.SERVICE_UNAVAILABLE);
    }

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
