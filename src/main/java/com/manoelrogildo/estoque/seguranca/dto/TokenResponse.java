package com.manoelrogildo.estoque.seguranca.dto;

import java.time.Instant;

public record TokenResponse(String accessToken, String tokenType, Instant expiraEm, String papel) {
}
