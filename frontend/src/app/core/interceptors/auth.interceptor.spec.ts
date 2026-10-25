import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { LoginResponse, UsuarioResponse } from '../models/auth.models';
import { AuthService } from '../services/auth.service';
import { authInterceptor } from './auth.interceptor';

@Component({ selector: 'app-login-stub', template: '' })
class LoginStubComponent {}

function criarTokenFake(expSegundosAPartirDeAgora: number): string {
  const exp = Math.floor(Date.now() / 1000) + expSegundosAPartirDeAgora;
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const payload = btoa(JSON.stringify({ sub: 'teste@adotapet.com', exp }));
  return `${header}.${payload}.assinatura-fake`;
}

const USUARIO_TESTE: UsuarioResponse = {
  id: 1,
  nome: 'Voluntário Teste',
  email: 'teste@adotapet.com',
  perfil: 'VOLUNTARIO',
};

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authService: AuthService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([{ path: 'login', component: LoginStubComponent }]),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  function autenticar(): void {
    const resposta: LoginResponse = {
      token: criarTokenFake(3600),
      tipo: 'Bearer',
      expiraEm: new Date(Date.now() + 3600_000).toISOString(),
      usuario: USUARIO_TESTE,
    };
    authService.login('teste@adotapet.com', 'Senha@123').subscribe();
    httpMock.expectOne(`${environment.apiUrl}/auth/login`).flush(resposta);
  }

  it('não adiciona o header Authorization na chamada de login', () => {
    http.post(`${environment.apiUrl}/auth/login`, { email: 'a', senha: 'b' }).subscribe();

    const requisicao = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(requisicao.request.headers.has('Authorization')).toBe(false);
    requisicao.flush({});
  });

  it('adiciona o header Authorization: Bearer nas chamadas autenticadas da API', () => {
    autenticar();

    http.get(`${environment.apiUrl}/animais`).subscribe();

    const requisicao = httpMock.expectOne(`${environment.apiUrl}/animais`);
    expect(requisicao.request.headers.get('Authorization')).toBe(`Bearer ${authService.token}`);
    requisicao.flush({});
  });

  it('em 401 numa chamada protegida, executa logout e limpa a sessão', () => {
    autenticar();
    expect(authService.estaAutenticado()).toBe(true);

    http.get(`${environment.apiUrl}/animais/999`).subscribe({ error: () => undefined });

    const requisicao = httpMock.expectOne(`${environment.apiUrl}/animais/999`);
    requisicao.flush(
      { mensagem: 'Token expirado. Faça login novamente.' },
      { status: 401, statusText: 'Unauthorized' },
    );

    expect(authService.token).toBeNull();
    expect(authService.estaAutenticado()).toBe(false);
  });
});
