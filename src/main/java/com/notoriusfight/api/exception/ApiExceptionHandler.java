package com.notoriusfight.api.exception;

import com.notoriusfight.application.RecursoDuplicadoException;
import com.notoriusfight.application.CredenciaisInvalidasException;
import com.notoriusfight.application.RecursoEmUsoException;
import com.notoriusfight.application.RecursoNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ApiError> tratarCredenciaisInvalidas(
            CredenciaisInvalidasException exception,
            HttpServletRequest request) {
        return resposta(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                request,
                Map.of());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiError> tratarNaoEncontrado(
            RecursoNaoEncontradoException exception,
            HttpServletRequest request) {
        return resposta(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                Map.of());
    }

    @ExceptionHandler({RecursoDuplicadoException.class, RecursoEmUsoException.class,
            DataIntegrityViolationException.class})
    public ResponseEntity<ApiError> tratarConflito(
            RuntimeException exception,
            HttpServletRequest request) {
        String mensagem = exception instanceof RecursoDuplicadoException
                || exception instanceof RecursoEmUsoException
                ? exception.getMessage()
                : "A operação viola uma regra de integridade";
        return resposta(HttpStatus.CONFLICT, mensagem, request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> tratarValidacao(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                campos.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage()));

        return resposta(
                HttpStatus.BAD_REQUEST,
                "Um ou mais campos são inválidos",
                request,
                campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> tratarJsonInvalido(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        return resposta(
                HttpStatus.BAD_REQUEST,
                "JSON ausente ou inválido",
                request,
                Map.of());
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiError> tratarRegraInvalida(
            RuntimeException exception,
            HttpServletRequest request) {
        return resposta(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request,
                Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> tratarParametroInvalido(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        return resposta(
                HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro: " + exception.getName(),
                request,
                Map.of());
    }

    @ExceptionHandler({
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class,
            HttpMediaTypeNotAcceptableException.class,
            MissingServletRequestParameterException.class,
            NoResourceFoundException.class})
    public ResponseEntity<ApiError> tratarErroDeProtocolo(
            Exception exception,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(
                ((ErrorResponse) exception).getStatusCode().value());
        String mensagem = switch (status) {
            case NOT_FOUND -> "Rota não encontrada";
            case METHOD_NOT_ALLOWED -> "Método HTTP não permitido nesta rota";
            case UNSUPPORTED_MEDIA_TYPE -> "Tipo de conteúdo não suportado";
            case NOT_ACCEPTABLE -> "Formato de resposta não disponível";
            default -> "Parâmetro obrigatório ausente";
        };
        return resposta(status, mensagem, request, Map.of());
    }

    /** Falhas inesperadas são registradas no servidor sem expor detalhes ao cliente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> tratarErroInesperado(
            Exception exception,
            HttpServletRequest request) throws Exception {
        if (exception instanceof AccessDeniedException
                || exception instanceof AuthenticationException) {
            throw exception;
        }
        LOGGER.error("Erro inesperado em {}", request.getRequestURI(), exception);
        return resposta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno inesperado",
                request,
                Map.of());
    }

    private ResponseEntity<ApiError> resposta(
            HttpStatus status,
            String mensagem,
            HttpServletRequest request,
            Map<String, String> campos) {
        ApiError erro = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI(),
                campos);
        return ResponseEntity.status(status).body(erro);
    }
}
