-- Carga de dados do dev/test (depois de localidades.sql).
-- Status (enums no padrao Regiao, gravados pelo id): marca, categoria, subcategoria e produto
-- 1 = Rascunho, 2 = Ativa(o), 3 = Inativa(o); fornecedor 1 = Ativo, 2 = Inativo.

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

-- categorias (so o primeiro nivel)
insert into categoria (nome, slug, ordem_exibicao, descricao, status, exibir_menu, icone_url) values
    ('Drones', 'drones', 1, 'Todos os modelos de drone comercializados pela loja.', 2, true, 'https://cdn.aeriadrones.com/categorias/drones.svg');
insert into categoria (nome, slug, ordem_exibicao, descricao, status, exibir_menu, icone_url) values
    ('Acessórios', 'acessorios', 2, 'Baterias, hélices, cases e demais itens complementares.', 2, true, 'https://cdn.aeriadrones.com/categorias/acessorios.svg');
insert into categoria (nome, slug, ordem_exibicao, descricao, status, exibir_menu, icone_url) values
    ('', 'rascunho-categoria-1', 0, null, 1, false, null);

-- subcategorias (id_categoria: 1 = Drones, 2 = Acessórios)
insert into subcategoria (nome, slug, id_categoria, ordem_exibicao, descricao, status) values
    ('Drones profissionais', 'drones-profissionais', 1, 1, 'Modelos voltados para inspeção, topografia e mapeamento.', 2);
insert into subcategoria (nome, slug, id_categoria, ordem_exibicao, descricao, status) values
    ('Drones compactos', 'drones-compactos', 1, 2, 'Modelos leves e portáteis para uso recreativo e criação de conteúdo.', 2);
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

-- produtos (id_marca / id_subcategoria / id_fornecedor seguem a ordem dos inserts acima;
-- fornecedor = mesma posicao da marca, exceto Sparrow, que usa a Skyline Trading)
insert into produto (nome, modelo, descricao_curta, preco, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    ('Falcon X2 Pro', 'DRN-FX2P-0042', 'Drone profissional com câmera 1" e 45 min de voo.', 12499.00, 1, 1, 1, 2, true, 'falcon-x2-pro', 13);
insert into produto (nome, modelo, descricao_curta, preco, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    ('Falcon X2 Standard', 'DRN-FX2S-0041', 'Versão de entrada da linha Falcon.', 8990.00, 1, 1, 1, 2, false, 'falcon-x2-standard', 13);
insert into produto (nome, modelo, descricao_curta, preco, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    ('Nimbus Mini 3', 'DRN-NM3-0038', 'Compacto abaixo de 250 g, dispensa registro na ANAC.', 4290.00, 2, 2, 2, 2, true, 'nimbus-mini-3', 13);
insert into produto (nome, modelo, descricao_curta, preco, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    ('Nimbus Mini 3 Combo', 'DRN-NM3C-0039', 'Mini 3 com 3 baterias e case.', 5780.00, 2, 2, 2, 1, false, 'nimbus-mini-3-combo', 13);
insert into produto (nome, modelo, descricao_curta, preco, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    ('Vortex FPV Racer', 'DRN-VFR-0027', 'Drone FPV para corrida.', 3150.00, 3, 2, 3, 3, false, 'vortex-fpv-racer', 13);
insert into produto (nome, modelo, descricao_curta, preco, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    ('Atlas Agro 10L', 'DRN-AT10-0019', 'Pulverizador agrícola com tanque de 10 L.', 46900.00, 4, 1, 4, 2, false, 'atlas-agro-10l', 13);
insert into produto (nome, modelo, descricao_curta, preco, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    ('Heron Survey Pro', 'DRN-HSP-0015', 'Mapeamento com RTK integrado.', 28400.00, 5, 1, 5, 2, true, 'heron-survey-pro', 13);
insert into produto (nome, modelo, descricao_curta, preco, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    ('Sparrow Kids Drone', 'DRN-SKD-0009', 'Drone de brinquedo para iniciantes.', 690.00, 6, 2, 2, 1, false, 'sparrow-kids-drone', 13);
insert into produto (nome, modelo, descricao_curta, preco, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    ('Bateria Inteligente Falcon X2', 'ACC-BFX2-0101', '5000 mAh, compatível com a linha Falcon X2.', 899.00, 1, 3, 1, 2, false, 'bateria-inteligente-falcon-x2', 13);
insert into produto (nome, modelo, descricao_curta, preco, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    ('Bateria Nimbus Mini Plus', 'ACC-BNMP-0102', 'Bateria estendida para a linha Mini.', 549.00, 2, 3, 2, 2, false, 'bateria-nimbus-mini-plus', 13);
insert into produto (nome, modelo, descricao_curta, preco, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    ('Bateria Atlas Agro 20000', 'ACC-BAA-0103', 'Bateria de alta capacidade para pulverizadores.', 3890.00, 4, 3, 4, 2, false, 'bateria-atlas-agro-20000', 13);
insert into produto (nome, modelo, descricao_curta, preco, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    ('Hélices Falcon X2 (par)', 'ACC-HFX2-0201', 'Par de hélices de baixo ruído.', 129.00, 1, 4, 1, 2, false, 'helices-falcon-x2-par', 13);
insert into produto (nome, modelo, descricao_curta, preco, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    ('Hélices Nimbus Mini (4 un.)', 'ACC-HNM-0202', 'Jogo com 4 hélices de reposição.', 79.00, 2, 4, 2, 2, false, 'helices-nimbus-mini-4-un', 13);
insert into produto (nome, modelo, descricao_curta, preco, id_marca, id_subcategoria, id_fornecedor, status, destaque, slug, garantia_meses) values
    ('Hélices FPV Tri-blade', 'ACC-HFPV-0203', 'Hélices tripá para drones de corrida.', 59.00, 3, 4, 3, 3, false, 'helices-fpv-tri-blade', 13);
update produto set descricao = 'Drone profissional com sensor de 1 polegada, 45 minutos de voo e transmissão de vídeo em até 15 km.', meta_title = 'Falcon X2 Pro | Aeria Drones', meta_description = 'Drone profissional Falcon X2 Pro com câmera de 1 polegada e 45 min de autonomia.', tags = 'profissional,4k,mapeamento', garantia_meses = 12, data_lancamento = '2026-03-01' where modelo = 'DRN-FX2P-0042';
