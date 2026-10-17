-- Carga de dados do dev/test (depois de localidades.sql).
-- Status (enums no padrao Regiao, gravados pelo id): marca, categoria, subcategoria e produto
-- 1 = Rascunho, 2 = Ativa(o), 3 = Inativa(o); fornecedor 1 = Ativo, 2 = Inativo.
-- TipoMidia (#12) é enum persistido pelo conversor; 1 = IMAGEM, 2 = VIDEO, 3 = MANUAL_PDF.

insert into configuracao_loja (id, dados_fiscais, cep_origem_frete, remetente_padrao, prazo_arrependimento_dias, prazo_garantia_dias, prazo_analise_devolucao_dias_uteis, frete_reverso_por_conta_loja) values
    (1, 'Aeria Drones Comércio Ltda.', '77001000', 'Aeria Drones', 7, 365, 5, true);

insert into cupom (codigo, tipo_desconto, valor, data_inicio, data_fim, valor_minimo, limite_total_uso, limite_por_cliente, ativo) values
    ('AERIA10', 1, 10.00, '2026-01-01 00:00:00', '2030-12-31 23:59:59', 0.00, 500, 1, true);

-- Administrador semente: login admin@aeria.com.br / aeria123
insert into usuario (nome, email, senha_hash, email_verificado, ativo)
values ('Administrador', 'admin@aeria.com.br',
        '$2a$10$DvWOf27oHRLntAxdCeuH0.l7dTe/yzd3gcRdmdOAV4u6XqEmZ6Nka', true, true);
insert into perfil_administrativo (papel, usuario_id, two_fa_habilitado)
select 1, id, false from usuario where email = 'admin@aeria.com.br';
insert into permissao_modulo (perfil_administrativo_id, modulo, pode_ver, pode_editar, pode_excluir)
select p.id, m.id, true, true, true
from perfil_administrativo p
cross join (values (1), (2), (3), (4), (5), (6), (7), (8)) as m(id)
where p.usuario_id = (select id from usuario where email = 'admin@aeria.com.br');


insert into marca (nome, slug, pais_origem, site_oficial, descricao, logo_url, status, destacar_home) values
    ('Aerion', 'aerion', 'Estados Unidos', 'https://aerion-drones.com', 'Marca norte-americana focada em drones de alta performance para uso profissional.', 'https://cdn.aeriadrones.com/marcas/aerion.png', 2, true);
insert into marca (nome, slug, pais_origem, site_oficial, descricao, logo_url, status, destacar_home) values
    ('Skyline', 'skyline', 'China', 'https://skyline-drones.com', 'Fabricante chinesa com forte presença em drones de consumo e acessórios.', 'https://cdn.aeriadrones.com/marcas/skyline.png', 2, true);
insert into marca (nome, slug, pais_origem, site_oficial, descricao, logo_url, status, destacar_home) values
    ('Vortex', 'vortex', 'Brasil', 'https://vortexdrones.com.br', 'Marca brasileira especializada em drones agrícolas e de monitoramento.', 'https://cdn.aeriadrones.com/marcas/vortex.png', 2, true);
insert into marca (nome, slug, pais_origem, site_oficial, descricao, logo_url, status, destacar_home) values
    ('Atlas', 'atlas', 'Brasil', null, 'Marca nacional voltada para drones de entrada, foco em custo-benefício.', 'https://cdn.aeriadrones.com/marcas/atlas.png', 2, false);
insert into marca (nome, slug, pais_origem, site_oficial, descricao, logo_url, status, destacar_home) values
    ('Heron', 'heron', 'França', 'https://heron-aero.fr', null, 'https://cdn.aeriadrones.com/marcas/heron.png', 2, false);
insert into marca (nome, slug, pais_origem, site_oficial, descricao, logo_url, status, destacar_home) values
    ('Sparrow', 'sparrow', 'China', 'https://sparrow-tech.cn', 'Linha descontinuada, mantida apenas para suporte a produtos já vendidos.', 'https://cdn.aeriadrones.com/marcas/sparrow.png', 3, false);
insert into marca (nome, slug, pais_origem, site_oficial, descricao, logo_url, status, destacar_home) values
    ('', 'rascunho-1', '', null, null, '', 1, false);

-- categorias (so o primeiro nivel). Drone ou acessorio e o tipo do produto, nao a categoria:
-- a categoria organiza a vitrine dentro de um tipo (tipo: 1 = Drone, 2 = Acessorio; rascunho sem tipo)
insert into categoria (nome, slug, ordem_exibicao, descricao, status, exibir_menu, icone_url, tipo) values
    ('Profissionais', 'profissionais', 1, 'Drones para mapeamento, inspeção e agricultura.', 2, true, 'https://cdn.aeriadrones.com/categorias/profissionais.svg', 1);
insert into categoria (nome, slug, ordem_exibicao, descricao, status, exibir_menu, icone_url, tipo) values
    ('Peças e energia', 'pecas-e-energia', 3, 'Baterias, hélices e demais peças de reposição.', 2, true, 'https://cdn.aeriadrones.com/categorias/pecas-e-energia.svg', 2);
insert into categoria (nome, slug, ordem_exibicao, descricao, status, exibir_menu, icone_url, tipo) values
    ('', 'rascunho-categoria-1', 0, null, 1, false, null, null);
insert into categoria (nome, slug, ordem_exibicao, descricao, status, exibir_menu, icone_url, tipo) values
    ('Lazer e criação', 'lazer-e-criacao', 2, 'Drones leves para lazer, viagem, FPV e criação de conteúdo.', 2, true, 'https://cdn.aeriadrones.com/categorias/lazer-e-criacao.svg', 1);

-- subcategorias (id_categoria: 1 = Profissionais, 2 = Peças e energia, 4 = Lazer e criação)
insert into subcategoria (nome, slug, id_categoria, ordem_exibicao, descricao, status) values
    ('Mapeamento e inspeção', 'mapeamento-e-inspecao', 1, 1, 'Modelos voltados para inspeção, topografia, mapeamento e pulverização.', 2);
insert into subcategoria (nome, slug, id_categoria, ordem_exibicao, descricao, status) values
    ('Compactos e FPV', 'compactos-e-fpv', 4, 1, 'Modelos leves e portáteis para uso recreativo e criação de conteúdo.', 2);
insert into subcategoria (nome, slug, id_categoria, ordem_exibicao, descricao, status) values
    ('Baterias', 'baterias', 2, 1, 'Baterias originais e de reposição.', 2);
insert into subcategoria (nome, slug, id_categoria, ordem_exibicao, descricao, status) values
    ('Hélices', 'helices', 2, 2, null, 3);

-- fornecedores: CNPJ, telefone e CEP so com digitos; endereco ficticio em municipio real
-- (o municipio e buscado pelo codigo IBGE, carregado antes por localidades.sql)
insert into fornecedor (razao_social, cnpj, nome_contato, email_contato, telefone_contato, cep, logradouro, numero, complemento, bairro, id_municipio, status) values
    ('Aerion Brasil Importação Ltda.', '12345678000195', 'Paulo Serra', 'comercial@aerion.com.br', '1134567890', '01310100', 'Avenida Paulista', '1578', 'Conjunto 42', 'Bela Vista', (select id from municipio where codigo_ibge = 3550308), 1);
insert into fornecedor (razao_social, cnpj, nome_contato, email_contato, telefone_contato, cep, logradouro, numero, complemento, bairro, id_municipio, status) values
    ('Skyline Trading Co.', '98765432000198', 'Ana Yamada', 'ana.yamada@skyline.co', '1123456789', '13015904', 'Rua Barão de Jaguara', '1481', null, 'Centro', (select id from municipio where codigo_ibge = 3509502), 1);
insert into fornecedor (razao_social, cnpj, nome_contato, email_contato, telefone_contato, cep, logradouro, numero, complemento, bairro, id_municipio, status) values
    ('Vortex Indústria de Drones S.A.', '45902113000110', 'Rodrigo Lemos', 'rodrigo@vortexdrones.com.br', '4832221100', '88010400', 'Rua Felipe Schmidt', '515', 'Sala 801', 'Centro', (select id from municipio where codigo_ibge = 4205407), 1);
insert into fornecedor (razao_social, cnpj, nome_contato, email_contato, telefone_contato, cep, logradouro, numero, complemento, bairro, id_municipio, status) values
    ('Atlas Agro Equipamentos', '31556804000103', 'Carla Nunes', 'carla@atlasagro.com.br', null, '77001002', 'Quadra 104 Norte Avenida LO 2', '12', null, 'Plano Diretor Norte', (select id from municipio where codigo_ibge = 1721000), 1);
insert into fornecedor (razao_social, cnpj, nome_contato, email_contato, telefone_contato, cep, logradouro, numero, complemento, bairro, id_municipio, status) values
    ('Heron Aeronáutica Europa', '07118290000111', 'Marc Girard', 'm.girard@heron.eu', null, '20040020', 'Avenida Rio Branco', '156', 'Sala 1201', 'Centro', (select id from municipio where codigo_ibge = 3304557), 2);

-- produtos (tipo: 1 = Drone, 2 = Acessorio; id_marca / id_subcategoria / id_fornecedor seguem a ordem dos inserts acima;
-- fornecedor = mesma posicao da marca, exceto Sparrow, que usa a Skyline Trading)
insert into produto (tipo, nome, modelo, descricao_curta, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    (1, 'Falcon X2 Pro', 'DRN-FX2P-0042', 'Drone profissional com câmera 1" e 45 min de voo.', 1, 1, 1, 2, true, 'falcon-x2-pro', 13);
insert into produto (tipo, nome, modelo, descricao_curta, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    (1, 'Falcon X2 Standard', 'DRN-FX2S-0041', 'Versão de entrada da linha Falcon.', 1, 1, 1, 2, false, 'falcon-x2-standard', 13);
insert into produto (tipo, nome, modelo, descricao_curta, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    (1, 'Nimbus Mini 3', 'DRN-NM3-0038', 'Compacto abaixo de 250 g, dispensa registro na ANAC.', 2, 2, 2, 2, true, 'nimbus-mini-3', 13);
insert into produto (tipo, nome, modelo, descricao_curta, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    (1, 'Nimbus Mini 3 Combo', 'DRN-NM3C-0039', 'Mini 3 com 3 baterias e case.', 2, 2, 2, 1, false, 'nimbus-mini-3-combo', 13);
insert into produto (tipo, nome, modelo, descricao_curta, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    (1, 'Vortex FPV Racer', 'DRN-VFR-0027', 'Drone FPV para corrida.', 3, 2, 3, 3, false, 'vortex-fpv-racer', 13);
insert into produto (tipo, nome, modelo, descricao_curta, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    (1, 'Atlas Agro 10L', 'DRN-AT10-0019', 'Pulverizador agrícola com tanque de 10 L.', 4, 1, 4, 2, false, 'atlas-agro-10l', 13);
insert into produto (tipo, nome, modelo, descricao_curta, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    (1, 'Heron Survey Pro', 'DRN-HSP-0015', 'Mapeamento com RTK integrado.', 5, 1, 5, 2, true, 'heron-survey-pro', 13);
insert into produto (tipo, nome, modelo, descricao_curta, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    (1, 'Sparrow Kids Drone', 'DRN-SKD-0009', 'Drone de brinquedo para iniciantes.', 6, 2, 2, 1, false, 'sparrow-kids-drone', 13);
insert into produto (tipo, nome, modelo, descricao_curta, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    (2, 'Bateria Inteligente Falcon X2', 'ACC-BFX2-0101', '5000 mAh, compatível com a linha Falcon X2.', 1, 3, 1, 2, false, 'bateria-inteligente-falcon-x2', 13);
insert into produto (tipo, nome, modelo, descricao_curta, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    (2, 'Bateria Nimbus Mini Plus', 'ACC-BNMP-0102', 'Bateria estendida para a linha Mini.', 2, 3, 2, 2, false, 'bateria-nimbus-mini-plus', 13);
insert into produto (tipo, nome, modelo, descricao_curta, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    (2, 'Bateria Atlas Agro 20000', 'ACC-BAA-0103', 'Bateria de alta capacidade para pulverizadores.', 4, 3, 4, 2, false, 'bateria-atlas-agro-20000', 13);
insert into produto (tipo, nome, modelo, descricao_curta, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    (2, 'Hélices Falcon X2', 'ACC-HFX2-0201', 'Par de hélices de baixo ruído.', 1, 4, 1, 2, false, 'helices-falcon-x2', 13);
insert into produto (tipo, nome, modelo, descricao_curta, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    (2, 'Hélices Nimbus Mini', 'ACC-HNM-0202', 'Jogo com 4 hélices de reposição.', 2, 4, 2, 2, false, 'helices-nimbus-mini', 13);
insert into produto (tipo, nome, modelo, descricao_curta, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    (2, 'Hélices FPV Tri-blade', 'ACC-HFPV-0203', 'Hélices tripá para drones de corrida.', 3, 4, 3, 3, false, 'helices-fpv-tri-blade', 13);
update produto set descricao = 'Drone profissional com sensor de 1 polegada, 45 minutos de voo e transmissão de vídeo em até 15 km.', meta_title = 'Falcon X2 Pro | Aeria Drones', meta_description = 'Drone profissional Falcon X2 Pro com câmera de 1 polegada e 45 min de autonomia.', tags = 'profissional,4k,mapeamento', garantia_meses = 12, data_lancamento = '2026-03-01' where modelo = 'DRN-FX2P-0042';

-- drones e acessorios (heranca JOINED, D27): a linha do subtipo reaproveita o id do produto
insert into drone (id, peso_gramas, dimensoes_dobrado, dimensoes_aberto, autonomia_minutos, alcance_metros, velocidade_maxima_kmh, resolucao_foto_mp, tipo_sensor, resolucao_video, estabilizacao, tipo_bateria, capacidade_bateria_mah, tempo_carregamento_minutos, gps_integrado, sensores_anticolisao, exige_registro_anac, classe_anac)
select p.id, v.peso, v.dobrado, v.aberto, v.autonomia, v.alcance, v.velocidade, v.foto, v.sensor, v.video, v.estabilizacao, v.bateria, v.mah, v.carga, v.gps, v.anticolisao, v.anac, v.classe
from produto p join (values
    ('DRN-FX2P-0042', 895, '22x10x9', '35x29x11', 45, 15000, 75, 48, 'CMOS 1"', '5.1K', 'Gimbal 3 eixos', 'LiPo 4S', 5000, 90, true, true, true, 'Classe 3'),
    ('DRN-FX2S-0041', 870, '22x10x9', '35x29x11', 40, 12000, 70, 20, 'CMOS 1/1.3"', '4K', 'Gimbal 3 eixos', 'LiPo 4S', 4500, 80, true, true, true, 'Classe 3'),
    ('DRN-NM3-0038', 249, '15x9x6', '25x36x7', 38, 10000, 57, 48, 'CMOS 1/1.3"', '4K', 'Gimbal 3 eixos', 'Li-ion', 2453, 64, true, false, false, null),
    ('DRN-NM3C-0039', 249, '15x9x6', '25x36x7', 38, 10000, 57, 48, 'CMOS 1/1.3"', '4K', 'Gimbal 3 eixos', 'Li-ion', 2453, 64, true, false, false, null),
    ('DRN-VFR-0027', 410, null, '18x18x6', 8, 2000, 140, null, null, '4K', 'Eletrônica', 'LiPo 6S', 1300, 45, false, false, true, 'Classe 3'),
    ('DRN-AT10-0019', 24800, '98x58x63', '150x150x63', 18, 3000, 36, null, null, null, null, 'LiPo 12S', 20000, 120, true, true, true, 'Classe 2'),
    ('DRN-HSP-0015', 1390, '25x14x10', '40x35x14', 55, 15000, 72, 45, 'CMOS 4/3', '5.1K', 'Gimbal 3 eixos', 'LiPo 4S', 6000, 100, true, true, true, 'Classe 3'),
    ('DRN-SKD-0009', 120, null, '15x15x4', 7, 80, 20, 2, null, '720p', 'Eletrônica', 'LiPo 1S', 600, 40, false, false, false, null)
) as v(modelo, peso, dobrado, aberto, autonomia, alcance, velocidade, foto, sensor, video, estabilizacao, bateria, mah, carga, gps, anticolisao, anac, classe) on v.modelo = p.modelo;
insert into acessorio (id, tipo_acessorio)
select p.id, case when p.modelo like 'ACC-B%' then 'Bateria' else 'Hélice' end from produto p where p.tipo = 2;

-- variantes (D26): uma "Padrão" por produto, com o preço que antes ficava no produto;
-- custo de exemplo em 65% do preço; peso (kg) e altura x largura x comprimento (cm) da embalagem
insert into variante_produto (id_produto, nome, sku, preco, custo_aquisicao, estoque_fisico, estoque_minimo, peso_embalagem_kg, altura_embalagem_cm, largura_embalagem_cm, comprimento_embalagem_cm, eh_kit, ativa)
select p.id, 'Padrão', p.modelo || '-STD', v.preco, round(v.preco * 0.65, 2), v.estoque, v.minimo, v.peso, v.altura, v.largura, v.comprimento, false, true
from produto p join (values
    ('DRN-FX2P-0042', 12499.00, 12, 2, 2.100, 15, 28, 38),
    ('DRN-FX2S-0041', 8990.00, 8, 2, 2.000, 15, 28, 38),
    ('DRN-NM3-0038', 4290.00, 25, 5, 0.900, 10, 20, 26),
    ('DRN-NM3C-0039', 5780.00, 0, 2, 1.800, 14, 25, 32),
    ('DRN-VFR-0027', 3150.00, 5, 1, 1.200, 12, 25, 25),
    ('DRN-AT10-0019', 46900.00, 3, 1, 32.000, 70, 65, 105),
    ('DRN-HSP-0015', 28400.00, 4, 1, 3.200, 20, 35, 45),
    ('DRN-SKD-0009', 690.00, 30, 5, 0.400, 8, 20, 20),
    ('ACC-BFX2-0101', 899.00, 40, 10, 0.600, 6, 10, 18),
    ('ACC-BNMP-0102', 549.00, 35, 10, 0.200, 4, 8, 12),
    ('ACC-BAA-0103', 3890.00, 10, 3, 9.500, 20, 25, 35),
    ('ACC-HFX2-0201', 129.00, 60, 15, 0.100, 3, 12, 25),
    ('ACC-HNM-0202', 79.00, 80, 20, 0.080, 3, 10, 15),
    ('ACC-HFPV-0203', 59.00, 50, 10, 0.050, 2, 8, 12)
) as v(modelo, preco, estoque, minimo, peso, altura, largura, comprimento) on v.modelo = p.modelo;

-- compatibilidade acessorio -> produto (D17): baterias e helices com a linha de cada marca,
-- e a bateria do Falcon tambem com as helices dele (acessorio-acessorio)
insert into acessorio_compativel (id_acessorio, id_produto)
select a.id, p.id
from produto a join produto p on (a.modelo, p.modelo) in (
    ('ACC-BFX2-0101', 'DRN-FX2P-0042'), ('ACC-BFX2-0101', 'DRN-FX2S-0041'),
    ('ACC-HFX2-0201', 'DRN-FX2P-0042'), ('ACC-HFX2-0201', 'DRN-FX2S-0041'),
    ('ACC-BNMP-0102', 'DRN-NM3-0038'), ('ACC-BNMP-0102', 'DRN-NM3C-0039'),
    ('ACC-HNM-0202', 'DRN-NM3-0038'), ('ACC-HNM-0202', 'DRN-NM3C-0039'),
    ('ACC-BAA-0103', 'DRN-AT10-0019'),
    ('ACC-HFPV-0203', 'DRN-VFR-0027'),
    ('ACC-BFX2-0101', 'ACC-HFX2-0201'));

-- kit (D7): Fly More do Falcon X2 Pro = 1 drone + 2 baterias, sem estoque proprio;
-- peso e dimensoes ficam nulos e saem dos componentes (D16)
insert into variante_produto (id_produto, nome, sku, preco, custo_aquisicao, estoque_fisico, estoque_minimo, peso_embalagem_kg, altura_embalagem_cm, largura_embalagem_cm, comprimento_embalagem_cm, eh_kit, ativa)
select p.id, 'Fly More Combo', 'DRN-FX2P-0042-FLY', 13999.00, round(13999.00 * 0.65, 2), null, 0, null, null, null, null, true, true
from produto p where p.modelo = 'DRN-FX2P-0042';
insert into componente_kit (id_kit, id_componente, quantidade)
select k.id, c.id, v.quantidade
from variante_produto k
join (values ('DRN-FX2P-0042-STD', 1), ('ACC-BFX2-0101-STD', 2)) as v(sku, quantidade) on true
join variante_produto c on c.sku = v.sku
where k.sku = 'DRN-FX2P-0042-FLY';

-- #13 estoque fisico so muda por movimento (RF14): o saldo inicial de cada variante comum
-- vira uma ENTRADA (tipo 1) MANUAL (origem 1) do admin semente (D11). Kit nao tem estoque (D7).
insert into movimento_estoque (id_variante, tipo, origem, quantidade, saldo_resultante, observacao, id_autor, dataCadastro)
select v.id, 1, 1, v.estoque_fisico, v.estoque_fisico, 'Saldo inicial',
       (select id from usuario where email = 'admin@aeria.com.br'), now()
from variante_produto v
where v.eh_kit = false and v.estoque_fisico > 0
order by v.id;

-- #16 Pedidos da carga (D19): cliente semente cliente@aeria.com.br / aeria123 e tres pedidos por status.
-- Itens so de acessorios (a regra de numero de serie da #34 nao trava as transicoes) com o preco atual
-- da variante congelado (RN06). Status: 1 PENDENTE, 2 PAGO, 3 SEPARACAO, 4 ENVIADO, 5 ENTREGUE,
-- 6 EM_DEVOLUCAO, 7 DEVOLVIDO, 8 CANCELADO. Origem do historico: 1 MANUAL (autor = admin), 2 SISTEMA, 3 INTEGRACAO.
-- Os ENTREGUE foram entregues ha 60 dias (#37). O endereco de entrega e a fotografia congelada (D12).
insert into usuario (nome, email, senha_hash, email_verificado, ativo)
values ('Cliente Semente', 'cliente@aeria.com.br',
        '$2a$10$DvWOf27oHRLntAxdCeuH0.l7dTe/yzd3gcRdmdOAV4u6XqEmZ6Nka', true, true);
insert into perfil_cliente (tipo_pessoa, nome, documento, telefone, usuario_id)
select 1, 'Cliente Semente', '52998224725', '63999990000', id from usuario where email = 'cliente@aeria.com.br';

insert into pedido (status, id_perfil_cliente, valor_total, valor_frete, valor_desconto, entrega_destinatario, entrega_logradouro, entrega_numero, entrega_complemento, entrega_bairro, entrega_cidade, entrega_uf, entrega_cep, dataCadastro)
select v.status, c.id, 0, v.frete, 0, 'Cliente Semente', 'Quadra 104 Norte', '10', null, 'Plano Diretor Norte', 'Palmas', 'TO', '77001002',
       date_trunc('minute', now()) - make_interval(days => v.dias)
from (select pc.id from perfil_cliente pc join usuario u on u.id = pc.usuario_id where u.email = 'cliente@aeria.com.br') c
cross join (values
    (1, 1, 1, 39.90), (2, 1, 2, 39.90), (3, 1, 3, 0.00),
    (4, 2, 3, 39.90), (5, 2, 4, 39.90), (6, 2, 5, 0.00),
    (7, 3, 5, 39.90), (8, 3, 6, 39.90), (9, 3, 7, 0.00),
    (10, 4, 8, 39.90), (11, 4, 9, 39.90), (12, 4, 10, 0.00),
    (13, 5, 67, 39.90), (14, 5, 68, 39.90), (15, 5, 69, 0.00),
    (16, 6, 30, 39.90), (17, 6, 31, 39.90), (18, 6, 32, 0.00),
    (19, 7, 45, 39.90), (20, 7, 46, 39.90), (21, 7, 47, 0.00),
    (22, 8, 12, 39.90), (23, 8, 13, 39.90), (24, 8, 14, 0.00)
) as v(seq, status, dias, frete)
order by v.seq;

-- itens: seq = ordem de criacao do pedido do cliente semente
insert into item_pedido (id_pedido, id_variante, quantidade, preco_unitario_congelado)
select p.id, vp.id, i.qtd, vp.preco
from (select pd.id, row_number() over (order by pd.id) as seq
      from pedido pd join perfil_cliente pc on pc.id = pd.id_perfil_cliente
      where pc.documento = '52998224725') p
join (values
    (1, 1, 'ACC-BFX2-0101-STD', 1), (1, 2, 'ACC-HFX2-0201-STD', 2),
    (2, 1, 'ACC-BNMP-0102-STD', 2),
    (3, 1, 'ACC-HNM-0202-STD', 1), (3, 2, 'ACC-BNMP-0102-STD', 1),
    (4, 1, 'ACC-BAA-0103-STD', 1),
    (5, 1, 'ACC-BFX2-0101-STD', 2),
    (6, 1, 'ACC-HFX2-0201-STD', 1), (6, 2, 'ACC-HNM-0202-STD', 1),
    (7, 1, 'ACC-BNMP-0102-STD', 1),
    (8, 1, 'ACC-BFX2-0101-STD', 1), (8, 2, 'ACC-HFX2-0201-STD', 1),
    (9, 1, 'ACC-BAA-0103-STD', 2),
    (10, 1, 'ACC-HNM-0202-STD', 4),
    (11, 1, 'ACC-BFX2-0101-STD', 1),
    (12, 1, 'ACC-BNMP-0102-STD', 2), (12, 2, 'ACC-HNM-0202-STD', 2),
    (13, 1, 'ACC-BFX2-0101-STD', 1), (13, 2, 'ACC-HFX2-0201-STD', 2),
    (14, 1, 'ACC-BNMP-0102-STD', 1),
    (15, 1, 'ACC-HFX2-0201-STD', 2),
    (16, 1, 'ACC-BFX2-0101-STD', 1),
    (17, 1, 'ACC-BNMP-0102-STD', 2),
    (18, 1, 'ACC-BAA-0103-STD', 1),
    (19, 1, 'ACC-HFX2-0201-STD', 1),
    (20, 1, 'ACC-BFX2-0101-STD', 2),
    (21, 1, 'ACC-HNM-0202-STD', 2),
    (22, 1, 'ACC-BNMP-0102-STD', 1),
    (23, 1, 'ACC-BAA-0103-STD', 1),
    (24, 1, 'ACC-HFX2-0201-STD', 2), (24, 2, 'ACC-HNM-0202-STD', 1)
) as i(seq, ord, sku, qtd) on i.seq = p.seq
join variante_produto vp on vp.sku = i.sku
order by p.id, i.ord;

update pedido p set valor_total = p.valor_frete - p.valor_desconto
    + (select sum(i.quantidade * i.preco_unitario_congelado) from item_pedido i where i.id_pedido = p.id)
where p.id_perfil_cliente = (select id from perfil_cliente where documento = '52998224725');

-- historico: transicoes da #17 a partir da criacao (horas depois do dataCadastro do pedido)
insert into historico_status_pedido (id_pedido, status_anterior, status_novo, origem, id_autor, observacao, dataCadastro)
select p.id, h.anterior, h.novo, h.origem,
       case when h.origem = 1 then (select id from usuario where email = 'admin@aeria.com.br') end,
       h.obs,
       case when p.status = 5 and h.novo = 5 then date_trunc('minute', now()) - interval '60 days'
            else p.dataCadastro + make_interval(hours => h.horas) end
from pedido p
join (values
    (1, 1, null, 1, 2, 0, 'Pedido criado'),
    (2, 1, null, 1, 2, 0, 'Pedido criado'),
    (2, 2, 1, 2, 3, 1, 'Pagamento confirmado pelo gateway'),
    (3, 1, null, 1, 2, 0, 'Pedido criado'),
    (3, 2, 1, 2, 3, 1, 'Pagamento confirmado pelo gateway'),
    (3, 3, 2, 3, 1, 24, 'Separação iniciada no CD'),
    (4, 1, null, 1, 2, 0, 'Pedido criado'),
    (4, 2, 1, 2, 3, 1, 'Pagamento confirmado pelo gateway'),
    (4, 3, 2, 3, 1, 24, 'Separação iniciada no CD'),
    (4, 4, 3, 4, 1, 48, 'Postado na transportadora'),
    (5, 1, null, 1, 2, 0, 'Pedido criado'),
    (5, 2, 1, 2, 3, 1, 'Pagamento confirmado pelo gateway'),
    (5, 3, 2, 3, 1, 24, 'Separação iniciada no CD'),
    (5, 4, 3, 4, 1, 48, 'Postado na transportadora'),
    (5, 5, 4, 5, 3, 120, 'Entrega confirmada pela transportadora'),
    (6, 1, null, 1, 2, 0, 'Pedido criado'),
    (6, 2, 1, 2, 3, 1, 'Pagamento confirmado pelo gateway'),
    (6, 3, 2, 3, 1, 24, 'Separação iniciada no CD'),
    (6, 4, 3, 4, 1, 48, 'Postado na transportadora'),
    (6, 5, 4, 5, 3, 120, 'Entrega confirmada pela transportadora'),
    (6, 6, 5, 6, 1, 240, 'Devolução aprovada'),
    (7, 1, null, 1, 2, 0, 'Pedido criado'),
    (7, 2, 1, 2, 3, 1, 'Pagamento confirmado pelo gateway'),
    (7, 3, 2, 3, 1, 24, 'Separação iniciada no CD'),
    (7, 4, 3, 4, 1, 48, 'Postado na transportadora'),
    (7, 5, 4, 5, 3, 120, 'Entrega confirmada pela transportadora'),
    (7, 6, 5, 6, 1, 240, 'Devolução aprovada'),
    (7, 7, 6, 7, 1, 480, 'Reembolso concluído'),
    (8, 1, null, 1, 2, 0, 'Pedido criado'),
    (8, 2, 1, 8, 2, 72, 'Pagamento não confirmado no prazo')
) as h(final, ordem, anterior, novo, origem, horas, obs) on h.final = p.status
where p.id_perfil_cliente = (select id from perfil_cliente where documento = '52998224725')
order by p.id, h.ordem;
