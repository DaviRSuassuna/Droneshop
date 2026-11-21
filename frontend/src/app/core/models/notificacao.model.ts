import { StatusRef } from './status.model';

/** Template de e-mail versionado (#32). */
export interface TemplateEmail {
  id: number;
  codigo: string;
  assunto: string;
  versao: number;
  ativo: boolean;
  categoria?: StatusRef;
  corpoTemplate?: string;
  variaveis?: string[];
}

export interface TemplateEmailDTO {
  assunto: string;
  corpoTemplate: string;
}

export interface Notificacao {
  id: number;
  template: { codigo: string; versao?: number };
  categoria: StatusRef;
  statusEnvio: StatusRef;
  destinatario: { email: string };
  assunto: string;
  identificadorProvedor: string | null;
  dataCadastro: string;
}

/** Preferência de marketing do cliente (#42). */
export interface PreferenciaNotificacao {
  tipoNotificacao: string;
  habilitado: boolean;
}
