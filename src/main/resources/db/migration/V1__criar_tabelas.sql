CREATE TABLE produto (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku                VARCHAR(30)    NOT NULL,
    nome               VARCHAR(120)   NOT NULL,
    ncm                VARCHAR(8)     NOT NULL,
    preco_venda        DECIMAL(12, 2) NOT NULL,
    quantidade_estoque INT            NOT NULL DEFAULT 0,
    estoque_minimo     INT            NOT NULL DEFAULT 0,
    ativo              BOOLEAN        NOT NULL DEFAULT TRUE,
    criado_em          DATETIME(6)    NOT NULL,
    atualizado_em      DATETIME(6)    NOT NULL,
    versao             BIGINT,
    CONSTRAINT uk_produto_sku UNIQUE (sku),
    CONSTRAINT ck_produto_estoque_nao_negativo CHECK (quantidade_estoque >= 0)
);

CREATE TABLE movimentacao_estoque (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    produto_id      BIGINT       NOT NULL,
    tipo            VARCHAR(10)  NOT NULL,
    quantidade      INT          NOT NULL,
    saldo_anterior  INT          NOT NULL,
    saldo_posterior INT          NOT NULL,
    motivo          VARCHAR(200) NOT NULL,
    data_hora       DATETIME(6)  NOT NULL,
    CONSTRAINT fk_movimentacao_produto FOREIGN KEY (produto_id) REFERENCES produto (id)
);

CREATE INDEX idx_movimentacao_produto_data ON movimentacao_estoque (produto_id, data_hora);

CREATE TABLE venda (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    data_hora           DATETIME(6)    NOT NULL,
    status              VARCHAR(10)    NOT NULL,
    valor_total         DECIMAL(12, 2) NOT NULL,
    cancelada_em        DATETIME(6),
    motivo_cancelamento VARCHAR(200)
);

CREATE TABLE item_venda (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    venda_id       BIGINT         NOT NULL,
    produto_id     BIGINT         NOT NULL,
    quantidade     INT            NOT NULL,
    preco_unitario DECIMAL(12, 2) NOT NULL,
    subtotal       DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_item_venda_venda FOREIGN KEY (venda_id) REFERENCES venda (id),
    CONSTRAINT fk_item_venda_produto FOREIGN KEY (produto_id) REFERENCES produto (id)
);
