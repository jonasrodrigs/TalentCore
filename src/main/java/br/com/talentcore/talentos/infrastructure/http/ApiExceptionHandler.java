package br.com.talentcore.talentos.infrastructure.http;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Handler global de exceções para a API.
 *
 * Mantém o comportamento existente e adiciona respostas 400 mais claras
 * para:
 *  - Datas inválidas (DateTimeParseException)
 *  - JSON malformado / Enum inválido (HttpMessageNotReadableException)
 *
 * Observações:
 *  - Não altera os contratos já usados pelos endpoints de candidatos.
 *  - Respostas seguem padrão com timestamp, status, error, message (e, quando aplicável, campos/paths).
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /* =========================================================
       400 - VALIDAÇÃO DE CAMPOS (Bean Validation)
       ========================================================= */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
                                                                HttpServletRequest req) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> fields.put(fe.getField(), fe.getDefaultMessage()));

        Map<String, Object> map = baseBody(HttpStatus.BAD_REQUEST, "Validação falhou", req);
        map.put("fields", fields);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
    }

    /* =========================================================
       400 - ARGUMENTO INVÁLIDO (regras de domínio / parser manual)
       ========================================================= */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex,
                                                                     HttpServletRequest req) {
        return body(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    /* =========================================================
       400 - JSON MALFORMADO / ENUM INVÁLIDO / TIPOS
       ========================================================= */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex,
                                                                 HttpServletRequest req) {
        // Tenta extrair detalhes quando houve InvalidFormatException (Jackson)
        Throwable cause = rootCause(ex);
        if (cause instanceof InvalidFormatException ife) {
            String fieldPath = jacksonPath(ife);
            String target = ife.getTargetType() != null ? ife.getTargetType().getSimpleName() : "tipo";
            String invalidValue = String.valueOf(ife.getValue());

            String msg;
            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                String accepted = enumAcceptedValues(ife.getTargetType());
                msg = "Valor inválido para campo '%s': '%s'. Valores aceitos: %s"
                        .formatted(fieldPath, invalidValue, accepted);
            } else {
                msg = "Formato inválido para campo '%s'. Valor recebido: '%s' (esperado: %s)"
                        .formatted(fieldPath, invalidValue, target);
            }
            return body(HttpStatus.BAD_REQUEST, msg, req);
        }

        // Pode haver DateTimeParseException como causa
        if (cause instanceof DateTimeParseException dtpe) {
            String msg = "Data inválida. Use o formato ISO-8601: yyyy-MM-dd";
            return body(HttpStatus.BAD_REQUEST, msg, req);
        }

        // Fallback genérico para JSON ilegível
        return body(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou malformado.", req);
    }

    /* =========================================================
       400 - DATAS INVÁLIDAS (quando propagadas explicitamente)
       ========================================================= */
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<Map<String, Object>> handleDateTimeParse(DateTimeParseException ex,
                                                                   HttpServletRequest req) {
        String msg = "Data inválida. Use o formato ISO-8601: yyyy-MM-dd";
        return body(HttpStatus.BAD_REQUEST, msg, req);
    }

    /* =========================================================
       401 - JWT inválido/expirado (fallback)
       (Na prática seu JwtAuthFilter já trata 401 em JSON; deixamos este como segurança)
       ========================================================= */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwt(JwtException ex, HttpServletRequest req) {
        return body(HttpStatus.UNAUTHORIZED, "Token inválido ou não pôde ser processado.", req);
    }

    /* =========================================================
       500 - ERRO GENÉRICO
       ========================================================= */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, HttpServletRequest req) {
        ex.printStackTrace(); // útil em DEV
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Consulte os logs.", req);
    }

    /* =========================================================
       Helpers
       ========================================================= */
    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message, HttpServletRequest req) {
        Map<String, Object> map = baseBody(status, message, req);
        return ResponseEntity.status(status).body(map);
    }

    private Map<String, Object> baseBody(HttpStatus status, String message, HttpServletRequest req) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("timestamp", OffsetDateTime.now().toString());
        map.put("status", status.value());
        map.put("error", status.getReasonPhrase());
        map.put("message", Objects.toString(message, ""));
        if (req != null) {
            map.put("path", req.getRequestURI());
        }
        return map;
    }

    private static Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur;
    }

    /** Constrói o "caminho" do campo a partir do InvalidFormatException (ex.: "experiencias[0].tipo"). */
    private static String jacksonPath(InvalidFormatException ife) {
        try {
            List<com.fasterxml.jackson.databind.JsonMappingException.Reference> path = ife.getPath();
            if (path == null || path.isEmpty()) return "(desconhecido)";
            return path.stream()
                    .map(ref -> ref.getFieldName() != null
                            ? ref.getFieldName()
                            : ("[" + ref.getIndex() + "]"))
                    .collect(Collectors.joining("."));
        } catch (Exception ignored) {
            return "(desconhecido)";
        }
    }

    /** Lista valores aceitos para enums em formato 'VALOR1 | VALOR2 | ...'. */
    private static String enumAcceptedValues(Class<?> enumType) {
        if (!enumType.isEnum()) return "";
        Object[] constants = enumType.getEnumConstants();
        return constants == null ? "" :
                java.util.Arrays.stream(constants)
                        .map(c -> ((Enum<?>) c).name())
                        .collect(Collectors.joining(" | "));
    }
}