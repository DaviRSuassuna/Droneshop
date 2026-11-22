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

-- Vitrine igual ao protótipo 02 (drones.html, acessorios.html, ofertas.html): os drones da carga
-- ficam ativos, entram a marca Aeria e os acessórios do protótipo, as duas ofertas e uma foto por produto.
update marca set status = 2 where slug = 'sparrow';
insert into marca (nome, slug, pais_origem, site_oficial, descricao, logo_url, status, destacar_home) values
    ('Aeria', 'aeria', 'Brasil', null, 'Linha própria de acessórios da Aeria Drones.', 'https://cdn.aeriadrones.com/marcas/aeria.png', 2, false);
update subcategoria set status = 2 where slug = 'helices';
insert into subcategoria (nome, slug, id_categoria, ordem_exibicao, descricao, status)
select 'Transporte e controle', 'transporte-e-controle', c.id, 3, 'Maletas, mochilas, filtros, controles e peças de câmera.', 2
from categoria c where c.slug = 'pecas-e-energia';

update produto set status = 2 where sku in ('DRN-VFR-0027', 'DRN-SKD-0009', 'DRN-NM3C-0039');
update produto set nome = 'Sparrow Kids', slug = 'sparrow-kids', descricao_curta = 'Drone leve para iniciantes, com proteção nas hélices.',
    preco = 890.00, custo_aquisicao = round(890.00 * 0.65, 2) where sku = 'DRN-SKD-0009';
update produto set preco = 4790.00, custo_aquisicao = round(4790.00 * 0.65, 2) where sku = 'DRN-NM3-0038';
update drone set resolucao_video = '4K 60' where id in (select id from produto where sku in ('DRN-FX2P-0042', 'DRN-HSP-0015'));
update drone set resolucao_video = '4K 30' where id in (select id from produto where sku in ('DRN-NM3-0038', 'DRN-NM3C-0039'));
update drone set resolucao_video = '2.7K' where id = (select id from produto where sku = 'DRN-SKD-0009');
-- o Combo entrou ativo: recebe saldo inicial por movimento (RF14)
update produto set estoque_fisico = 8 where sku = 'DRN-NM3C-0039';
insert into movimento_estoque (id_produto, tipo, origem, quantidade, saldo_resultante, observacao, id_autor, dataCadastro)
select p.id, 1, 1, 8, 8, 'Saldo inicial', (select id from usuario where email = 'admin@aeria.com.br'), now()
from produto p where p.sku = 'DRN-NM3C-0039';

-- descrição curta = linha de especificação do card de acessório
update produto p set descricao_curta = v.texto
from (values
    ('ACC-BFX2-0101', '5.000 mAh · 43 min'),
    ('ACC-BNMP-0102', '3.850 mAh · 34 min'),
    ('ACC-BAA-0103', '20.000 mAh · alta descarga'),
    ('ACC-HFX2-0201', 'Par de baixo ruído'),
    ('ACC-HNM-0202', 'Jogo com 4 hélices'),
    ('ACC-HFPV-0203', 'Tripá para corrida')
) as v(sku, texto) where p.sku = v.sku;

insert into produto (tipo, nome, sku, descricao_curta, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses,
    estoque_minimo, ordem_no_grupo, preco, custo_aquisicao, estoque_fisico, peso_embalagem_kg, altura_embalagem_cm, largura_embalagem_cm, comprimento_embalagem_cm)
select 2, v.nome, v.sku, v.curta, (select id from marca where slug = v.marca),
       (select id from subcategoria where slug = 'transporte-e-controle'),
       (select id from fornecedor where cnpj = '12345678000195'), 2, false, v.slug, 12,
       3, 0, v.preco, round(v.preco * 0.65, 2), v.estoque, v.peso, v.altura, v.largura, v.comprimento
from (values
    ('Maleta rígida Falcon', 'ACC-MAL-0301', 'EVA · comporta 3 baterias', 'aerion', 'maleta-rigida-falcon', 690.00, 12, 2.400, 15, 35, 45),
    ('Mochila de campo 25 L', 'ACC-MOC-0302', 'Compartimento para tablet', 'aeria', 'mochila-de-campo-25l', 649.00, 15, 1.300, 20, 30, 50),
    ('Filtros ND (kit 4)', 'ACC-ND-0303', 'ND8 · ND16 · ND32 · ND64', 'aeria', 'filtros-nd-kit-4', 349.00, 25, 0.200, 4, 10, 14),
    ('Carregador 3 baterias', 'ACC-CAR-0304', '100 W · 80 min por ciclo', 'aerion', 'carregador-3-baterias', 890.00, 18, 0.700, 6, 14, 20),
    ('Antena de longo alcance', 'ACC-ANT-0305', '+40% de estabilidade no link', 'aeria', 'antena-de-longo-alcance', 429.00, 20, 0.300, 5, 10, 25),
    ('Controle Aeria RC Pro', 'ACC-RCP-0306', 'Tela 7" · 1.200 nits', 'aeria', 'controle-aeria-rc-pro', 3890.00, 6, 1.100, 10, 20, 28),
    ('Gimbal de 3 eixos', 'ACC-GMB-0307', 'Câmera 4K · reposição', 'aerion', 'gimbal-de-3-eixos', 2490.00, 5, 0.500, 8, 12, 15),
    ('Controle remoto', 'ACC-CTL-0308', 'Suporte para celular', 'aerion', 'controle-remoto', 1290.00, 10, 0.800, 8, 18, 24)
) as v(nome, sku, curta, marca, slug, preco, estoque, peso, altura, largura, comprimento);
insert into acessorio (id, tipo_acessorio, sugerir_nos_compativeis)
select p.id, case when p.sku in ('ACC-RCP-0306', 'ACC-CTL-0308') then 'Controle' else 'Acessório' end,
       p.sku in ('ACC-MAL-0301', 'ACC-CAR-0304', 'ACC-CTL-0308')
from produto p where p.sku like 'ACC-%-03%';
insert into acessorio_compativel (id_acessorio, id_produto)
select a.id, p.id from produto a join produto p on (a.sku, p.sku) in (
    ('ACC-MAL-0301', 'DRN-FX2P-0042'), ('ACC-MAL-0301', 'DRN-FX2S-0041'),
    ('ACC-CAR-0304', 'DRN-FX2P-0042'), ('ACC-CAR-0304', 'DRN-FX2S-0041'),
    ('ACC-GMB-0307', 'DRN-FX2P-0042'), ('ACC-GMB-0307', 'DRN-FX2S-0041'),
    ('ACC-CTL-0308', 'DRN-FX2P-0042'), ('ACC-CTL-0308', 'DRN-FX2S-0041'));
insert into movimento_estoque (id_produto, tipo, origem, quantidade, saldo_resultante, observacao, id_autor, dataCadastro)
select p.id, 1, 1, p.estoque_fisico, p.estoque_fisico, 'Saldo inicial', (select id from usuario where email = 'admin@aeria.com.br'), now()
from produto p where p.sku like 'ACC-%-03%' order by p.id;

-- ofertas do protótipo: Nimbus Mini 3 de R$ 4.790 por R$ 4.290, Sparrow Kids de R$ 890 por R$ 690, mochila de R$ 649 por R$ 549
insert into promocao (nome, escopo, data_inicio, data_fim, valor_desconto, ativa, dataCadastro) values
    ('Nimbus Mini 3 com R$ 500 off', 1, now() - interval '1 day', now() + interval '60 days', 500.00, true, now()),
    ('Sparrow Kids com R$ 200 off', 1, now() - interval '1 day', now() + interval '60 days', 200.00, true, now()),
    ('Mochila de campo com R$ 100 off', 1, now() - interval '1 day', now() + interval '60 days', 100.00, true, now());
insert into promocao_produto (id_promocao, id_produto)
select pr.id, p.id from promocao pr join produto p on (pr.nome, p.sku) in (
    ('Nimbus Mini 3 com R$ 500 off', 'DRN-NM3-0038'),
    ('Sparrow Kids com R$ 200 off', 'DRN-SKD-0009'),
    ('Mochila de campo com R$ 100 off', 'ACC-MOC-0302'));

-- uma foto por produto (arquivos em midias-carga/, copiados para o S3 na subida por CargaMidias)
insert into midia_produto (id_produto, tipo, url, principal, ordem, alt_text, dataCadastro)
select p.id, 1, '/midias/arquivos/3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d' || f.n || '.webp', true, 1, p.nome, now()
from produto p join (values
    ('DRN-AT10-0019', '07'), ('DRN-VFR-0027', '08'), ('DRN-SKD-0009', '09'),
    ('ACC-BFX2-0101', '10'), ('ACC-BNMP-0102', '11'), ('ACC-BAA-0103', '12'),
    ('ACC-HFX2-0201', '13'), ('ACC-HNM-0202', '14'), ('ACC-HFPV-0203', '15'),
    ('ACC-MAL-0301', '16'), ('ACC-MOC-0302', '17'), ('ACC-ND-0303', '18'), ('ACC-CAR-0304', '19'),
    ('ACC-ANT-0305', '20'), ('ACC-RCP-0306', '21'), ('ACC-GMB-0307', '22'), ('ACC-CTL-0308', '23')
) as f(sku, n) on f.sku = p.sku;
