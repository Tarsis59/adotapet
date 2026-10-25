import { Especie, Porte, Sexo, StatusAdocao } from './enums';

export interface AnimalRequest {
  nome: string;
  especie: Especie;
  raca?: string | null;
  sexo: Sexo;
  porte: Porte;
  idadeAnos: number;
  pesoKg?: number | null;
  castrado: boolean;
  vacinado: boolean;
  status: StatusAdocao;
  descricao?: string | null;
  fotoUrl?: string | null;
}

export interface AnimalResponse extends AnimalRequest {
  id: number;
  criadoEm: string;
  atualizadoEm: string;
}

export interface EstatisticasResponse {
  total: number;
  disponiveis: number;
  emProcesso: number;
  adotados: number;
}

export interface AnimalFiltro {
  busca?: string;
  especie?: Especie | '';
  status?: StatusAdocao | '';
  page?: number;
  size?: number;
  sort?: string;
}
