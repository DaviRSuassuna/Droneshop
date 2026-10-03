-- This file allow to write SQL commands that will be emitted in test and dev.
-- The commands are commented as their support depends of the database
-- insert into myentity (id, field) values(1, 'field-1');

insert into marca (nome, slug, pais_origem, site_oficial, descricao, logo_url, status, destacar_home) values
    ('Aerion', 'aerion', 'Estados Unidos', 'https://aerion-drones.com', 'Marca norte-americana focada em drones de alta performance para uso profissional.', 'https://cdn.aeriadrones.com/marcas/aerion.png', 'ATIVA', true);
insert into marca (nome, slug, pais_origem, site_oficial, descricao, logo_url, status, destacar_home) values
    ('Skyline', 'skyline', 'China', 'https://skyline-drones.com', 'Fabricante chinesa com forte presença em drones de consumo e acessórios.', 'https://cdn.aeriadrones.com/marcas/skyline.png', 'ATIVA', true);
insert into marca (nome, slug, pais_origem, site_oficial, descricao, logo_url, status, destacar_home) values
    ('Vortex', 'vortex', 'Brasil', 'https://vortexdrones.com.br', 'Marca brasileira especializada em drones agrícolas e de monitoramento.', 'https://cdn.aeriadrones.com/marcas/vortex.png', 'ATIVA', true);
insert into marca (nome, slug, pais_origem, site_oficial, descricao, logo_url, status, destacar_home) values
    ('Atlas', 'atlas', 'Brasil', null, 'Marca nacional voltada para drones de entrada, foco em custo-benefício.', 'https://cdn.aeriadrones.com/marcas/atlas.png', 'ATIVA', false);
insert into marca (nome, slug, pais_origem, site_oficial, descricao, logo_url, status, destacar_home) values
    ('Heron', 'heron', 'França', 'https://heron-aero.fr', null, 'https://cdn.aeriadrones.com/marcas/heron.png', 'ATIVA', false);
insert into marca (nome, slug, pais_origem, site_oficial, descricao, logo_url, status, destacar_home) values
    ('Sparrow', 'sparrow', 'China', 'https://sparrow-tech.cn', 'Linha descontinuada, mantida apenas para suporte a produtos já vendidos.', 'https://cdn.aeriadrones.com/marcas/sparrow.png', 'INATIVA', false);
insert into marca (nome, slug, pais_origem, site_oficial, descricao, logo_url, status, destacar_home) values
    ('', 'rascunho-1', '', null, null, '', 'RASCUNHO', false);

-- categorias (so o primeiro nivel)
insert into categoria (nome, slug, ordem_exibicao, descricao, status, exibir_menu, icone_url) values
    ('Drones', 'drones', 1, 'Todos os modelos de drone comercializados pela loja.', 'ATIVA', true, 'https://cdn.aeriadrones.com/categorias/drones.svg');
insert into categoria (nome, slug, ordem_exibicao, descricao, status, exibir_menu, icone_url) values
    ('Acessórios', 'acessorios', 2, 'Baterias, hélices, cases e demais itens complementares.', 'ATIVA', true, 'https://cdn.aeriadrones.com/categorias/acessorios.svg');
insert into categoria (nome, slug, ordem_exibicao, descricao, status, exibir_menu, icone_url) values
    ('', 'rascunho-categoria-1', 0, null, 'RASCUNHO', false, null);

-- subcategorias (categoria_id: 1 = Drones, 2 = Acessórios)
insert into subcategoria (nome, slug, categoria_id, ordem_exibicao, descricao, status) values
    ('Drones profissionais', 'drones-profissionais', 1, 1, 'Modelos voltados para inspeção, topografia e mapeamento.', 'ATIVA');
insert into subcategoria (nome, slug, categoria_id, ordem_exibicao, descricao, status) values
    ('Drones compactos', 'drones-compactos', 1, 2, 'Modelos leves e portáteis para uso recreativo e criação de conteúdo.', 'ATIVA');
insert into subcategoria (nome, slug, categoria_id, ordem_exibicao, descricao, status) values
    ('Baterias', 'baterias', 2, 1, 'Baterias originais e de reposição.', 'ATIVA');
insert into subcategoria (nome, slug, categoria_id, ordem_exibicao, descricao, status) values
    ('Hélices', 'helices', 2, 2, null, 'INATIVA');

insert into fornecedor (razao_social, cnpj, nome_contato, email_contato, telefone_contato, status) values
    ('Aerion Brasil Importação Ltda.', '12.345.678/0001-95', 'Paulo Serra', 'comercial@aerion.com.br', '(11) 3456-7890', 'ATIVO');
insert into fornecedor (razao_social, cnpj, nome_contato, email_contato, telefone_contato, status) values
    ('Skyline Trading Co.', '98.765.432/0001-98', 'Ana Yamada', 'ana.yamada@skyline.co', '(11) 2345-6789', 'ATIVO');
insert into fornecedor (razao_social, cnpj, nome_contato, email_contato, telefone_contato, status) values
    ('Vortex Indústria de Drones S.A.', '45.902.113/0001-10', 'Rodrigo Lemos', 'rodrigo@vortexdrones.com.br', '(48) 3222-1100', 'ATIVO');
insert into fornecedor (razao_social, cnpj, nome_contato, email_contato, telefone_contato, status) values
    ('Atlas Agro Equipamentos', '31.556.804/0001-03', 'Carla Nunes', 'carla@atlasagro.com.br', null, 'ATIVO');
insert into fornecedor (razao_social, cnpj, nome_contato, email_contato, telefone_contato, status) values
    ('Heron Aeronáutica Europa', '07.118.290/0001-11', 'Marc Girard', 'm.girard@heron.eu', '+33 1 4000 0000', 'INATIVO');

-- produtos (marca_id / subcategoria_id / fornecedor_id seguem a ordem dos inserts acima;
-- fornecedor = mesma posicao da marca, exceto Sparrow, que usa a Skyline Trading)
insert into produto (nome, modelo, descricao_curta, preco, marca_id, subcategoria_id, fornecedor_id, status, destaque) values
    ('Falcon X2 Pro', 'DRN-FX2P-0042', 'Drone profissional com câmera 1" e 45 min de voo.', 12499.00, 1, 1, 1, 'ATIVO', true);
insert into produto (nome, modelo, descricao_curta, preco, marca_id, subcategoria_id, fornecedor_id, status, destaque) values
    ('Falcon X2 Standard', 'DRN-FX2S-0041', 'Versão de entrada da linha Falcon.', 8990.00, 1, 1, 1, 'ATIVO', false);
insert into produto (nome, modelo, descricao_curta, preco, marca_id, subcategoria_id, fornecedor_id, status, destaque) values
    ('Nimbus Mini 3', 'DRN-NM3-0038', 'Compacto abaixo de 250 g, dispensa registro na ANAC.', 4290.00, 2, 2, 2, 'ATIVO', true);
insert into produto (nome, modelo, descricao_curta, preco, marca_id, subcategoria_id, fornecedor_id, status, destaque) values
    ('Nimbus Mini 3 Combo', 'DRN-NM3C-0039', 'Mini 3 com 3 baterias e case.', 5780.00, 2, 2, 2, 'RASCUNHO', false);
insert into produto (nome, modelo, descricao_curta, preco, marca_id, subcategoria_id, fornecedor_id, status, destaque) values
    ('Vortex FPV Racer', 'DRN-VFR-0027', 'Drone FPV para corrida.', 3150.00, 3, 2, 3, 'INATIVO', false);
insert into produto (nome, modelo, descricao_curta, preco, marca_id, subcategoria_id, fornecedor_id, status, destaque) values
    ('Atlas Agro 10L', 'DRN-AT10-0019', 'Pulverizador agrícola com tanque de 10 L.', 46900.00, 4, 1, 4, 'ATIVO', false);
insert into produto (nome, modelo, descricao_curta, preco, marca_id, subcategoria_id, fornecedor_id, status, destaque) values
    ('Heron Survey Pro', 'DRN-HSP-0015', 'Mapeamento com RTK integrado.', 28400.00, 5, 1, 5, 'ATIVO', true);
insert into produto (nome, modelo, descricao_curta, preco, marca_id, subcategoria_id, fornecedor_id, status, destaque) values
    ('Sparrow Kids Drone', 'DRN-SKD-0009', 'Drone de brinquedo para iniciantes.', 690.00, 6, 2, 2, 'RASCUNHO', false);
insert into produto (nome, modelo, descricao_curta, preco, marca_id, subcategoria_id, fornecedor_id, status, destaque) values
    ('Bateria Inteligente Falcon X2', 'ACC-BFX2-0101', '5000 mAh, compatível com a linha Falcon X2.', 899.00, 1, 3, 1, 'ATIVO', false);
insert into produto (nome, modelo, descricao_curta, preco, marca_id, subcategoria_id, fornecedor_id, status, destaque) values
    ('Bateria Nimbus Mini Plus', 'ACC-BNMP-0102', 'Bateria estendida para a linha Mini.', 549.00, 2, 3, 2, 'ATIVO', false);
insert into produto (nome, modelo, descricao_curta, preco, marca_id, subcategoria_id, fornecedor_id, status, destaque) values
    ('Bateria Atlas Agro 20000', 'ACC-BAA-0103', 'Bateria de alta capacidade para pulverizadores.', 3890.00, 4, 3, 4, 'ATIVO', false);
insert into produto (nome, modelo, descricao_curta, preco, marca_id, subcategoria_id, fornecedor_id, status, destaque) values
    ('Hélices Falcon X2 (par)', 'ACC-HFX2-0201', 'Par de hélices de baixo ruído.', 129.00, 1, 4, 1, 'ATIVO', false);
insert into produto (nome, modelo, descricao_curta, preco, marca_id, subcategoria_id, fornecedor_id, status, destaque) values
    ('Hélices Nimbus Mini (4 un.)', 'ACC-HNM-0202', 'Jogo com 4 hélices de reposição.', 79.00, 2, 4, 2, 'ATIVO', false);
insert into produto (nome, modelo, descricao_curta, preco, marca_id, subcategoria_id, fornecedor_id, status, destaque) values
    ('Hélices FPV Tri-blade', 'ACC-HFPV-0203', 'Hélices tripá para drones de corrida.', 59.00, 3, 4, 3, 'INATIVO', false);
