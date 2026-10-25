import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, RegistroRequest, UsuarioResponse } from '../models/auth.models';

const CHAVE_TOKEN = 'adotapet_token';
const CHAVE_USUARIO = 'adotapet_usuario';

export const MENSAGEM_SESSAO_EXPIRADA = 'Sua sessão expirou. Faça login novamente.';

interface JwtPayload {
  exp?: number;
  [chave: string]: unknown;
}

/** Decodifica o payload de um JWT sem validar assinatura (uso apenas para ler `exp` no cliente). */
function decodificarPayload(token: string): JwtPayload | null {
  try {
    const partes = token.split('.');
    if (partes.length !== 3) {
      return null;
    }
    const base64 = partes[1].replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
    const json = decodeURIComponent(
      atob(padded)
        .split('')
        .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join(''),
    );
    return JSON.parse(json) as JwtPayload;
  } catch {
    return null;
  }
}

function tokenExpirado(token: string | null): boolean {
  if (!token) {
    return true;
  }
  const payload = decodificarPayload(token);
  if (!payload?.exp) {
    return true;
  }
  return Date.now() >= payload.exp * 1000;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly tokenSignal = signal<string | null>(this.lerTokenArmazenado());
  readonly usuario = signal<UsuarioResponse | null>(this.lerUsuarioArmazenado());

  readonly estaAutenticado = computed(() => !tokenExpirado(this.tokenSignal()));

  /**
   * Verifica a validade do token no momento da chamada (diferente de `estaAutenticado`,
   * que é um `computed` memoizado e só reavalia quando o signal do token muda — não quando
   * o tempo passa). Usado pelos guards, que podem ser acionados bem depois do último login.
   */
  tokenValido(): boolean {
    return !tokenExpirado(this.tokenSignal());
  }

  private timeoutLogoutAutomatico: ReturnType<typeof setTimeout> | undefined;

  constructor() {
    this.agendarLogoutAutomatico();
  }

  get token(): string | null {
    return this.tokenSignal();
  }

  login(email: string, senha: string): Observable<LoginResponse> {
    const corpo: LoginRequest = { email, senha };
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, corpo).pipe(
      tap((resposta) => this.armazenarSessao(resposta)),
    );
  }

  registrar(nome: string, email: string, senha: string): Observable<UsuarioResponse> {
    const corpo: RegistroRequest = { nome, email, senha };
    return this.http.post<UsuarioResponse>(`${environment.apiUrl}/auth/registrar`, corpo);
  }

  logout(motivo?: string): void {
    localStorage.removeItem(CHAVE_TOKEN);
    localStorage.removeItem(CHAVE_USUARIO);
    this.tokenSignal.set(null);
    this.usuario.set(null);
    if (this.timeoutLogoutAutomatico) {
      clearTimeout(this.timeoutLogoutAutomatico);
      this.timeoutLogoutAutomatico = undefined;
    }
    const returnUrl = this.router.routerState.snapshot.url;
    const queryParams: Record<string, string> = {};
    if (motivo) {
      queryParams['motivo'] = motivo;
    }
    if (returnUrl && returnUrl !== '/login' && !returnUrl.startsWith('/login?')) {
      queryParams['returnUrl'] = returnUrl;
    }
    this.router.navigate(['/login'], {
      queryParams: Object.keys(queryParams).length ? queryParams : undefined,
    });
  }

  private armazenarSessao(resposta: LoginResponse): void {
    localStorage.setItem(CHAVE_TOKEN, resposta.token);
    localStorage.setItem(CHAVE_USUARIO, JSON.stringify(resposta.usuario));
    this.tokenSignal.set(resposta.token);
    this.usuario.set(resposta.usuario);
    this.agendarLogoutAutomatico();
  }

  private agendarLogoutAutomatico(): void {
    if (this.timeoutLogoutAutomatico) {
      clearTimeout(this.timeoutLogoutAutomatico);
      this.timeoutLogoutAutomatico = undefined;
    }
    const token = this.tokenSignal();
    if (!token) {
      return;
    }
    const payload = decodificarPayload(token);
    if (!payload?.exp) {
      return;
    }
    const msRestantes = payload.exp * 1000 - Date.now();
    if (msRestantes <= 0) {
      return;
    }
    // setTimeout tem limite de ~24.8 dias; expirações de JWT são bem menores, então é seguro aqui.
    this.timeoutLogoutAutomatico = setTimeout(() => {
      this.logout(MENSAGEM_SESSAO_EXPIRADA);
    }, msRestantes);
  }

  private lerTokenArmazenado(): string | null {
    try {
      return localStorage.getItem(CHAVE_TOKEN);
    } catch {
      return null;
    }
  }

  private lerUsuarioArmazenado(): UsuarioResponse | null {
    try {
      const bruto = localStorage.getItem(CHAVE_USUARIO);
      return bruto ? (JSON.parse(bruto) as UsuarioResponse) : null;
    } catch {
      return null;
    }
  }
}
