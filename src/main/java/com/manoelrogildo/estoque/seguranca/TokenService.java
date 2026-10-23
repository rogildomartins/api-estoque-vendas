package com.manoelrogildo.estoque.seguranca;

import java.time.Instant;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.manoelrogildo.estoque.seguranca.dto.TokenResponse;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final JwtProperties properties;

    public TokenService(JwtEncoder encoder, JwtProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    public TokenResponse gerar(Authentication autenticacao) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(properties.expiracao());
        String papel = autenticacao.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replaceFirst("^ROLE_", ""))
                .findFirst()
                .orElseThrow();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(SecurityConfig.EMISSOR)
                .subject(autenticacao.getName())
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .claim(SecurityConfig.CLAIM_PAPEL, papel)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", expiraEm, papel);
    }
}
