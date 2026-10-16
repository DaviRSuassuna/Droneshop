/** Espelha o ConfiguracaoLojaResponseDTO devolvido pela API. */
export interface ConfiguracaoLoja {
  id: number;
  dadosFiscais: string;
  cepOrigemFrete: string;
  remetentePadrao: string;
  prazoArrependimentoDias: number;
  prazoGarantiaDias: number;
  prazoAnaliseDevolucaoDiasUteis: number;
  freteReversoPorContaLoja: boolean;
}

/** Espelha o ConfiguracaoLojaRequestDTO aceito pelo PUT /admin/configuracao. */
export type ConfiguracaoLojaDTO = Omit<ConfiguracaoLoja, 'id'>;
