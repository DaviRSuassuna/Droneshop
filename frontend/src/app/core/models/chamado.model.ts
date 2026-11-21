import { StatusRef } from './status.model';

/** Ids de StatusChamado (#38). */
export const STATUS_CHAMADO = { ABERTO: 1, EM_ANDAMENTO: 2, ENCERRADO: 3 } as const;

export interface MensagemChamado {
  id: number;
  tipoAutor: StatusRef;
  texto: string;
  dataCadastro: string;
}

export interface Chamado {
  id: number;
  protocolo: string;
  status: StatusRef;
  assunto: string;
  pedido: { id: number } | null;
  cliente?: { id: number; nome: string } | null;
  atendente?: { id: number; nome: string } | null;
  mensagens: MensagemChamado[];
  dataCadastro: string;
}

export interface ChamadoDTO {
  assunto: string;
  idPedido: number | null;
  mensagem: string;
}
