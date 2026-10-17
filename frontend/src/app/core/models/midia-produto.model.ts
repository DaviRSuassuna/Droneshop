export type NomeTipoMidia = 'IMAGEM' | 'VIDEO' | 'MANUAL_PDF';

export interface TipoMidia {
  id: number;
  nome: NomeTipoMidia;
}

export interface MidiaProduto {
  id: number;
  tipo: TipoMidia;
  url: string;
  principal: boolean;
  ordem: number;
  altText: string | null;
}

export interface MidiaProdutoUpload {
  idTipo: number;
  arquivo?: File;
  url?: string;
  principal: boolean;
  ordem?: number;
  altText?: string;
}

export interface MidiaProdutoAlteracao {
  principal?: boolean;
  ordem?: number;
  altText?: string;
}
