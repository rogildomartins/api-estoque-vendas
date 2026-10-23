package com.manoelrogildo.estoque.compartilhado.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converte exceções em respostas no padrão RFC 9457 (Problem Details).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ProblemDetail handleRegraDeNegocio(RegraDeNegocioException ex) {
        return problema(HttpStatus.UNPROCESSABLE_CONTENT, "Regra de negócio violada", ex.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleConcorrencia(ObjectOptimisticLockingFailureException ex) {
        return problema(HttpStatus.CONFLICT, "Conflito de concorrência",
                "O registro foi alterado por outra operação. Tente novamente.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleNaoAutenticado(AuthenticationException ex) {
        // Mesma mensagem para usuário inexistente, senha errada ou conta inativa: não revela quais logins existem.
        boolean falhaNoLogin = ex instanceof BadCredentialsException || ex instanceof AccountStatusException;
        return problema(HttpStatus.UNAUTHORIZED, "Não autenticado", falhaNoLogin
                ? "Login ou senha inválidos"
                : "Informe um token válido no cabeçalho Authorization: Bearer <token>");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAcessoNegado(AccessDeniedException ex) {
        return problema(HttpStatus.FORBIDDEN, "Acesso negado", "Seu perfil não tem permissão para esta operação");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            erros.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Um ou mais campos estão inválidos");
        problema.setProperty("erros", erros);
        return problema;
    }

    private ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
}
