/** Enums espelhando exatamente os valores do contrato da API (seção 2 do plano). */

export type Especie = 'CACHORRO' | 'GATO' | 'OUTRO';
export type Sexo = 'MACHO' | 'FEMEA';
export type Porte = 'PEQUENO' | 'MEDIO' | 'GRANDE';
export type StatusAdocao = 'DISPONIVEL' | 'EM_PROCESSO' | 'ADOTADO';
export type Perfil = 'ADMIN' | 'VOLUNTARIO';

export const ESPECIES: Especie[] = ['CACHORRO', 'GATO', 'OUTRO'];
export const SEXOS: Sexo[] = ['MACHO', 'FEMEA'];
export const PORTES: Porte[] = ['PEQUENO', 'MEDIO', 'GRANDE'];
export const STATUS_ADOCAO: StatusAdocao[] = ['DISPONIVEL', 'EM_PROCESSO', 'ADOTADO'];

// Tipadas como Record<string, string> (com `satisfies` garantindo exaustividade na definição)
// para permitir indexação direta nos templates (`rotulos[animal.especie]`) sem erro de
// "implicit any" do verificador de templates do Angular.

export const ESPECIE_LABELS: Record<string, string> = {
  CACHORRO: 'Cachorro',
  GATO: 'Gato',
  OUTRO: 'Outro',
} satisfies Record<Especie, string>;

export const SEXO_LABELS: Record<string, string> = {
  MACHO: 'Macho',
  FEMEA: 'Fêmea',
} satisfies Record<Sexo, string>;

export const PORTE_LABELS: Record<string, string> = {
  PEQUENO: 'Pequeno',
  MEDIO: 'Médio',
  GRANDE: 'Grande',
} satisfies Record<Porte, string>;

export const STATUS_LABELS: Record<string, string> = {
  DISPONIVEL: 'Disponível',
  EM_PROCESSO: 'Em processo',
  ADOTADO: 'Adotado',
} satisfies Record<StatusAdocao, string>;

export const PERFIL_LABELS: Record<string, string> = {
  ADMIN: 'Administrador',
  VOLUNTARIO: 'Voluntário',
} satisfies Record<Perfil, string>;

/** Classe de cor usada nos chips de status (ver styles do status-chip). */
export const STATUS_CHIP_CLASS: Record<string, string> = {
  DISPONIVEL: 'status-chip--disponivel',
  EM_PROCESSO: 'status-chip--em-processo',
  ADOTADO: 'status-chip--adotado',
} satisfies Record<StatusAdocao, string>;
