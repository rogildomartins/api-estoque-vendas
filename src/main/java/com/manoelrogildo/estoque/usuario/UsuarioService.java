package com.manoelrogildo.estoque.usuario;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manoelrogildo.estoque.compartilhado.exception.RecursoNaoEncontradoException;
import com.manoelrogildo.estoque.compartilhado.exception.RegraDeNegocioException;
import com.manoelrogildo.estoque.usuario.dto.UsuarioRequest;
import com.manoelrogildo.estoque.usuario.dto.UsuarioResponse;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse cadastrar(UsuarioRequest request) {
        String login = normalizarLogin(request.login());
        if (repository.existsByLogin(login)) {
            throw new RegraDeNegocioException("Já existe um usuário com o login " + login);
        }
        Usuario usuario = new Usuario(login, request.nome().trim(), passwordEncoder.encode(request.senha()),
                request.papel());
        return UsuarioResponse.de(repository.save(usuario));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return repository.findAll(Sort.by("login")).stream().map(UsuarioResponse::de).toList();
    }

    /**
     * Inativa o usuário, que deixa de conseguir fazer login. Tokens já emitidos
     * continuam válidos até expirar.
     */
    @Transactional
    public void inativar(Long id, String loginDoSolicitante) {
        Usuario usuario = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
        if (usuario.getLogin().equals(loginDoSolicitante)) {
            throw new RegraDeNegocioException("Você não pode inativar o próprio usuário");
        }
        usuario.inativar();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) {
        Usuario usuario = repository.findByLogin(normalizarLogin(login))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + login));
        return User.withUsername(usuario.getLogin())
                .password(usuario.getSenhaHash())
                .roles(usuario.getPapel().name())
                .disabled(!usuario.isAtivo())
                .build();
    }

    private String normalizarLogin(String login) {
        return login.trim().toLowerCase();
    }
}
