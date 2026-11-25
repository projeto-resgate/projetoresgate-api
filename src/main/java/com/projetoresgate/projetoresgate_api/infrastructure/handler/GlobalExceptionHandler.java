package com.projetoresgate.projetoresgate_api.infrastructure.handler;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.UnauthorizedException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        var errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.getReasonPhrase(), "Registro não encontrado.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        var errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        var errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        var errorResponse = new ErrorResponse(HttpStatus.CONFLICT.getReasonPhrase(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        var errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.getReasonPhrase(), "Credenciais inválidas.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        var errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.getReasonPhrase(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        var errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.getReasonPhrase(), "Acesso negado.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(InternalException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(InternalException ex) {
        var errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<FieldErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        var errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Erro de validação nos campos.",
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        var errorResponse = new ErrorResponse(HttpStatus.CONFLICT.getReasonPhrase(), "Violação de integridade de dados. Verifique se o registro já existe.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        String field = jsonPath(ex);
        if (isNull(field)) {
            return badRequest("O corpo da requisição está ausente ou mal formatado.");
        }
        var errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Erro de validação nos campos.",
                List.of(new FieldErrorDetail(field, "Valor inválido ou em formato incorreto."))
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "O parâmetro '" + ex.getName() + "' tem um valor inválido: '" + ex.getValue() + "'.";
        Class<?> requiredType = ex.getRequiredType();
        if (nonNull(requiredType) && requiredType.isEnum()) {
            message += " Valores aceitos: " + Arrays.stream(requiredType.getEnumConstants())
                    .map(String::valueOf)
                    .collect(Collectors.joining(", ")) + ".";
        }
        return badRequest(message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        return badRequest("O parâmetro '" + ex.getParameterName() + "' é obrigatório.");
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<ErrorResponse> handleRequestBinding(ServletRequestBindingException ex) {
        return badRequest("A requisição está incompleta ou mal formatada.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        var errorResponse = new ErrorResponse(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(), "Método HTTP não suportado para este endereço.");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        var errorResponse = new ErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(), "Tipo de conteúdo não suportado. Envie application/json.");
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        var errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.getReasonPhrase(), "Endereço não encontrado.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        var errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Ocorreu um erro inesperado. Tente novamente mais tarde.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private static ResponseEntity<ErrorResponse> badRequest(String message) {
        var errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    private static String jsonPath(Throwable ex) {
        Throwable current = ex;
        while (nonNull(current)) {
            if (current instanceof JacksonException jacksonException && !jacksonException.getPath().isEmpty()) {
                StringBuilder path = new StringBuilder();
                for (JacksonException.Reference reference : jacksonException.getPath()) {
                    if (nonNull(reference.getPropertyName())) {
                        if (!path.isEmpty()) {
                            path.append('.');
                        }
                        path.append(reference.getPropertyName());
                    } else if (reference.getIndex() >= 0) {
                        path.append('[').append(reference.getIndex()).append(']');
                    }
                }
                return path.isEmpty() ? null : path.toString();
            }
            current = current.getCause();
        }
        return null;
    }
}
