package com.manoelrogildo.estoque.seguranca;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração do token JWT (prefixo {@code app.seguranca.jwt}).
 *
 * @param segredo   chave HMAC-SHA256; precisa ter pelo menos 32 bytes
 * @param expiracao validade do token após o login
 */
@ConfigurationProperties("app.seguranca.jwt")
public record JwtProperties(String segredo, Duration expiracao) {

    public JwtProperties {
        if (segredo == null || segredo.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.seguranca.jwt.segredo (JWT_SECRET) deve ter pelo menos 32 bytes");
        }
        if (expiracao == null) {
            expiracao = Duration.ofHours(2);
        }
    }

    public SecretKey chave() {
        return new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
