-- Carga de dados da trilha B (catalogo, administracao, atendimento e notificacao). Ver docs/agentes/README.md.
-- Carregada depois de import.sql e import-b.sql (ordem: localidades, import, import-b, import-a).
-- Regras para as duas trilhas não colidirem:
--   * sem id fixo: deixe a sequence gerar;
--   * referência a outro registro por chave natural, com subconsulta
--     (ex.: (select id from produto where sku = 'DRN-FX2P')), nunca pelo número do id;
--   * só referencie dados do import.sql ou deste arquivo.

-- #87: foto principal dos produtos de exemplo (fotos do protótipo em midias-carga/, um arquivo por produto
-- para a exclusão de uma mídia não apagar a foto de outro;
-- copiadas para o armazenamento na subida por CargaMidias). tipo 1 = IMAGEM.
insert into midia_produto (id_produto, tipo, url, principal, ordem, alt_text, dataCadastro)
select p.id, 1, '/midias/arquivos/' || f.arquivo, true, 1, p.nome, now()
from produto p join (values
    ('DRN-FX2P-0042', '3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d01.webp'),
    ('DRN-FX2S-0041', '3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d02.webp'),
    ('DRN-FX2P-0042-FLY', '3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d03.webp'),
    ('DRN-NM3-0038', '3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d04.webp'),
    ('DRN-NM3C-0039', '3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d05.webp'),
    ('DRN-HSP-0015', '3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d06.webp')
) as f(sku, arquivo) on f.sku = p.sku;

-- #84: peso do item em gramas (diferente do peso da embalagem, RN18), sugestão na página dos
-- drones compatíveis e o kit Fly More na página Ofertas.
update acessorio a set peso_item_g = v.peso, sugerir_nos_compativeis = v.sugerir
from produto p join (values
    ('ACC-BFX2-0101', 335, true),
    ('ACC-BNMP-0102', 80, true),
    ('ACC-BAA-0103', 8500, true),
    ('ACC-HFX2-0201', 20, true),
    ('ACC-HNM-0202', 9, true),
    ('ACC-HFPV-0203', 4, false)
) as v(sku, peso, sugerir) on v.sku = p.sku
where a.id = p.id;
update kit set exibir_em_ofertas = true where id = (select id from produto where sku = 'DRN-FX2P-0042-FLY');

-- #32: templates de e-mail (versão 1). Verificação de e-mail e recuperação de senha ficam com o Keycloak (D56).
insert into template_notificacao (codigo, assunto, corpoTemplate, versao, ativo, categoria, variaveis, dataCadastro) values
('PEDIDO_CONFIRMADO', 'Pedido {{numeroPedido}} confirmado', '<p>Olá {{nome}},</p><p>recebemos o seu pedido {{numeroPedido}}, no valor de R$ {{valorTotal}}. Avisaremos assim que o pagamento for aprovado.</p><p>Aeria Drones</p>', 1, true, 1, 'nome,numeroPedido,valorTotal', now()),
('PAGAMENTO_APROVADO', 'Pagamento do pedido {{numeroPedido}} aprovado', '<p>Olá {{nome}},</p><p>o pagamento do pedido {{numeroPedido}} foi aprovado. Já estamos separando os itens.</p><p>Aeria Drones</p>', 1, true, 1, 'nome,numeroPedido,valorTotal', now()),
('PEDIDO_ENVIADO', 'Pedido {{numeroPedido}} enviado', '<p>Olá {{nome}},</p><p>o pedido {{numeroPedido}} saiu para entrega. Acompanhe o rastreio na sua conta.</p><p>Aeria Drones</p>', 1, true, 1, 'nome,numeroPedido,valorTotal', now()),
('RASTREIO_ATUALIZADO', 'Novidade na entrega do pedido {{numeroPedido}}', '<p>Olá {{nome}},</p><p>a entrega do pedido {{numeroPedido}} foi atualizada: {{evento}}.</p><p>Aeria Drones</p>', 1, true, 1, 'nome,numeroPedido,evento', now()),
('DEVOLUCAO_ATUALIZADA', 'Sua solicitação de devolução foi atualizada', '<p>Olá {{nome}},</p><p>a devolução do pedido {{numeroPedido}} está agora como {{status}}.</p><p>Aeria Drones</p>', 1, true, 1, 'nome,numeroPedido,status', now()),
('CHAMADO_RESPONDIDO', 'Resposta no chamado {{protocolo}}', '<p>Olá {{nome}},</p><p>respondemos o seu chamado {{protocolo}} ({{assunto}}). Veja a resposta na sua conta.</p><p>Aeria Drones</p>', 1, true, 1, 'nome,protocolo,assunto', now()),
('CARRINHO_ABANDONADO', 'Seus itens ainda estão no carrinho', '<p>Olá {{nome}},</p><p>você deixou itens no carrinho. Eles continuam esperando por você na Aeria Drones.</p><p><small>Não quer mais receber este e-mail? <a href="{{linkDescadastro}}">Descadastre-se</a>.</small></p>', 1, true, 2, 'nome,linkDescadastro', now());
