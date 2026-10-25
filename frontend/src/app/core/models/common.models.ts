export interface PaginaResponse<T> {
  conteudo: T[];
  pagina: number;
  tamanho: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface CampoErro {
  campo: string;
  mensagem: string;
}

export interface ErroResponse {
  timestamp: string;
  status: number;
  erro: string;
  mensagem: string;
  caminho: string;
  campos?: CampoErro[];
}
