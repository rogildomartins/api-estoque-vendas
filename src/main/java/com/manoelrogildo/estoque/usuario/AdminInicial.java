package com.manoelrogildo.estoque.usuario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria o primeiro administrador quando o banco ainda não tem nenhum usuário.
 * Sem ele não haveria como fazer login para cadastrar os demais.
 */
@Component
public class AdminInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String login;
    private final String senha;

    public AdminInicial(UsuarioRepository repository, PasswordEncoder passwordEncoder,
            @Value("${app.seguranca.admin-inicial.login:admin}") String login,
            @Value("${app.seguranca.admin-inicial.senha:}") String senha) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.login = login;
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }
        if (senha.isBlank()) {
            log.warn("Nenhum usuário cadastrado e ADMIN_PASSWORD não informado: ninguém conseguirá fazer login.");
            return;
        }
        repository.save(new Usuario(login, "Administrador", passwordEncoder.encode(senha), Papel.ADMIN));
        log.info("Usuário administrador inicial '{}' criado.", login);
    }
}
