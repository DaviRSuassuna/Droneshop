export type Papel = 'CLIENTE' | 'ADMINISTRATIVO' | 'ADMIN';

export interface UsuarioAutenticado {
  id: number;
  nome: string;
  email: string;
  papeis: Papel[];
}

export interface LoginResponse {
  accessToken: string;
  expiraEm: number;
  papeis: Papel[];
}