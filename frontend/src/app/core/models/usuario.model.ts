export interface PapelAdministrativo {
  id: number;
  nome: string;
}

export interface ModuloSistema {
  id: number;
  nome: string;
}

export interface PermissaoModulo {
  modulo: ModuloSistema;
  podeVer: boolean;
  podeEditar: boolean;
  podeExcluir: boolean;
}

export interface UsuarioAdministrativo {
  id: number;
  nome: string;
  email: string;
  ativo: boolean;
  papel: PapelAdministrativo;
  permissoes: PermissaoModulo[];
  twoFAHabilitado?: boolean;
}

export interface PermissaoItemDTO {
  idModulo: number;
  podeVer?: boolean;
  podeEditar?: boolean;
  podeExcluir?: boolean;
}

export interface UsuarioAdministrativoDTO {
  nome: string;
  email: string;
  senha?: string;
  idPapel: number;
  ativo: boolean;
  permissoes?: PermissaoItemDTO[];
}
