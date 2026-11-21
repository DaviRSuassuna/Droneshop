-- Carga de dados da trilha A (identidade, compra, pagamento, pedido, estoque e devolucao). Ver docs/agentes/README.md.
-- Carregada depois de import.sql e import-b.sql (ordem: localidades, import, import-b, import-a).
-- Regras para as duas trilhas não colidirem:
--   * sem id fixo: deixe a sequence gerar;
--   * referência a outro registro por chave natural, com subconsulta
--     (ex.: (select id from produto where sku = 'DRN-FX2P')), nunca pelo número do id;
--   * só referencie dados do import.sql ou deste arquivo.

-- #19 Endereço principal do cliente semente (o mesmo dos pedidos da carga, que guardam a própria cópia: RN15).
insert into endereco_cliente (apelido, principal, cep, logradouro, numero, complemento, bairro, id_municipio, id_perfil_cliente, dataCadastro)
select 'Casa', true, '77001002', 'Quadra 104 Norte', '10', null, 'Plano Diretor Norte',
       (select id from municipio where codigo_ibge = 1721000),
       (select id from perfil_cliente where documento = '52998224725'), now();
