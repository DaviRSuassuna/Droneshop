/** Áreas da "Minha conta", na ordem do protótipo 02 (conta-perfil.html e conta-folha.js). */
export type AreaConta = 'pedidos' | 'perfil' | 'favoritos' | 'enderecos' | 'pagamentos';

export interface ItemAreaConta {
  area: AreaConta;
  rota: string;
  nome: string;
}

export const AREAS_CONTA: readonly ItemAreaConta[] = [
  { area: 'pedidos', rota: '/conta/pedidos', nome: 'Meus pedidos' },
  { area: 'perfil', rota: '/conta/perfil', nome: 'Perfil e dados' },
  { area: 'favoritos', rota: '/conta/favoritos', nome: 'Favoritos' },
  { area: 'enderecos', rota: '/conta/enderecos', nome: 'Endereços' },
  { area: 'pagamentos', rota: '/conta/pagamentos', nome: 'Meios de pagamento' },
];

/** Quem está logado e quantos itens há em cada área; quem preenche é a trilha A (#19). */
export interface ResumoConta {
  nome: string;
  /** Linha de baixo do nome: "Cliente desde 2024" no menu lateral, o e-mail na folha do celular. */
  detalhe?: string;
  contagens?: Partial<Record<AreaConta, number>>;
}

/** Iniciais do avatar: primeira letra do primeiro e do último nome ("Marina Ribeiro" → "MR"). */
export function iniciais(nome: string): string {
  const partes = nome.trim().split(/\s+/).filter(Boolean);
  if (!partes.length) return '';
  const primeira = partes[0][0];
  const ultima = partes.length > 1 ? partes[partes.length - 1][0] : '';
  return (primeira + ultima).toUpperCase();
}
