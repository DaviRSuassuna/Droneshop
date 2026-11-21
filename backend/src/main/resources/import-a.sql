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

-- #30 RegraFrete: retirada no showroom e fallback do agregador para EXPRESSA e PADRAO (ids do enum ModalidadeEntrega).
insert into regra_frete (modalidade, custo_fixo, prazo_dias, cep_origem, dataCadastro) values
  (1, 89.90, 3, '77001000', now()),
  (2, 39.90, 8, '77001000', now()),
  (3, 0.00, 1, '77001000', now());

-- #34 Pedidos da carga que já passaram da separação: cada drone tem a sua unidade com
-- número de série (RN12), senão não poderiam ser enviados. Status 3 = VENDIDA.
insert into unidade_individual (id_produto, status, numero_serie, validade_garantia, id_item_pedido, dataCadastro)
select i.id_produto, 3, 'SN-CARGA-' || i.id || '-' || n, current_date + interval '12 months', i.id, now()
from item_pedido i
join pedido p on p.id = i.id_pedido
join drone d on d.id = i.id_produto
cross join lateral generate_series(1, i.quantidade) as n
where p.status in (3, 4, 5, 6, 7);
