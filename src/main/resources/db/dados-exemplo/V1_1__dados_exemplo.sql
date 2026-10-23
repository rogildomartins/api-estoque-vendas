-- Dados de exemplo para testar a API localmente (não são carregados no perfil mysql).
INSERT INTO produto (sku, nome, ncm, preco_venda, quantidade_estoque, estoque_minimo, ativo, criado_em, atualizado_em, versao)
VALUES ('ARZ-5KG', 'Arroz Branco Tipo 1 5kg', '10063021', 27.90, 40, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('FEJ-1KG', 'Feijão Carioca 1kg', '07133399', 8.49, 60, 15, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('CAF-500G', 'Café Torrado e Moído 500g', '09012100', 18.90, 8, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('ACU-1KG', 'Açúcar Cristal 1kg', '17019900', 4.99, 100, 20, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('OLE-900ML', 'Óleo de Soja 900ml', '15079011', 7.79, 5, 12, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- O saldo inicial também fica registrado no histórico, mantendo o kardex consistente.
INSERT INTO movimentacao_estoque (produto_id, tipo, quantidade, saldo_anterior, saldo_posterior, motivo, data_hora)
SELECT id, 'ENTRADA', quantidade_estoque, 0, quantidade_estoque, 'Estoque inicial (dados de exemplo)', CURRENT_TIMESTAMP
FROM produto;
