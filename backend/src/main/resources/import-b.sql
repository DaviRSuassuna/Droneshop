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
insert into midia_produto (id_produto, tipo, url, principal, ordem, alt_text, data_cadastro)
select p.id, 1, '/midias/arquivos/' || f.arquivo, true, 1, p.nome, now()
from produto p join (values
    ('DRN-FX2P-0042', '3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d01.webp'),
    ('DRN-FX2S-0041', '3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d02.webp'),
    ('DRN-FX2P-0042-FLY', '3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d03.webp'),
    ('DRN-NM3-0038', '3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d04.webp'),
    ('DRN-NM3C-0039', '3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d05.webp'),
    ('DRN-HSP-0015', '3f6c2a10-8b4e-4c1d-9a7e-0f1a2b3c4d06.webp')
) as f(sku, arquivo) on f.sku = p.sku;
