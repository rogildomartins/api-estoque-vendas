CREATE TABLE usuario (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    login      VARCHAR(60)  NOT NULL,
    nome       VARCHAR(120) NOT NULL,
    senha_hash VARCHAR(100) NOT NULL,
    papel      VARCHAR(10)  NOT NULL,
    ativo      BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em  DATETIME(6)  NOT NULL,
    CONSTRAINT uk_usuario_login UNIQUE (login)
);
