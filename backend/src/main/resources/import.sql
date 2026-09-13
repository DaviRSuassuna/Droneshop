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

insert into categoria (nome, slug, categoria_pai_id, ordem_exibicao, descricao, status, exibir_menu, icone_url) values
    ('Drones', 'drones', null, 1, 'Todos os modelos de drone comercializados pela loja.', 'ATIVA', true, 'https://cdn.aeriadrones.com/categorias/drones.svg');
insert into categoria (nome, slug, categoria_pai_id, ordem_exibicao, descricao, status, exibir_menu, icone_url) values
    ('Acessórios', 'acessorios', null, 2, 'Baterias, hélices, cases e demais itens complementares.', 'ATIVA', true, 'https://cdn.aeriadrones.com/categorias/acessorios.svg');
insert into categoria (nome, slug, categoria_pai_id, ordem_exibicao, descricao, status, exibir_menu, icone_url) values
    ('Drones profissionais', 'drones-profissionais', 1, 1, 'Modelos voltados para inspeção, topografia e mapeamento.', 'ATIVA', true, null);
insert into categoria (nome, slug, categoria_pai_id, ordem_exibicao, descricao, status, exibir_menu, icone_url) values
    ('Drones compactos', 'drones-compactos', 1, 2, 'Modelos leves e portáteis para uso recreativo e criação de conteúdo.', 'ATIVA', true, null);
insert into categoria (nome, slug, categoria_pai_id, ordem_exibicao, descricao, status, exibir_menu, icone_url) values
    ('Baterias', 'baterias', 2, 1, 'Baterias originais e de reposição.', 'ATIVA', false, null);
insert into categoria (nome, slug, categoria_pai_id, ordem_exibicao, descricao, status, exibir_menu, icone_url) values
    ('Hélices', 'helices', 2, 2, null, 'INATIVA', false, null);
insert into categoria (nome, slug, categoria_pai_id, ordem_exibicao, descricao, status, exibir_menu, icone_url) values
    ('', 'rascunho-categoria-1', null, 0, null, 'RASCUNHO', false, null);
