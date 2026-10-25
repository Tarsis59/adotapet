import { Perfil } from './enums';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface UsuarioResponse {
  id: number;
  nome: string;
  email: string;
  perfil: Perfil;
}

export interface LoginResponse {
  token: string;
  tipo: 'Bearer';
  expiraEm: string;
  usuario: UsuarioResponse;
}

export interface RegistroRequest {
  nome: string;
  email: string;
  senha: string;
}
