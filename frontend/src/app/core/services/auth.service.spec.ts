import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { LoginResponse, UsuarioResponse } from '../models/auth.models';
import { AuthService } from './auth.service';

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

describe('AuthService', () => {
  let authService: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'login', component: LoginStubComponent }]),
      ],
    });
    authService = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('login armazena o token e o usuário e marca como autenticado', () => {
    const resposta: LoginResponse = {
      token: criarTokenFake(3600),
      tipo: 'Bearer',
      expiraEm: new Date(Date.now() + 3600_000).toISOString(),
      usuario: USUARIO_TESTE,
    };

    authService.login('teste@adotapet.com', 'Senha@123').subscribe();

    const requisicao = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(requisicao.request.method).toBe('POST');
    requisicao.flush(resposta);

    expect(authService.token).toBe(resposta.token);
    expect(authService.usuario()).toEqual(USUARIO_TESTE);
    expect(authService.estaAutenticado()).toBe(true);
    expect(localStorage.getItem('adotapet_token')).toBe(resposta.token);
  });

  it('estaAutenticado é falso quando o token já está expirado', () => {
    const resposta: LoginResponse = {
      token: criarTokenFake(-10),
      tipo: 'Bearer',
      expiraEm: new Date(Date.now() - 10_000).toISOString(),
      usuario: USUARIO_TESTE,
    };

    authService.login('teste@adotapet.com', 'Senha@123').subscribe();
    httpMock.expectOne(`${environment.apiUrl}/auth/login`).flush(resposta);

    expect(authService.estaAutenticado()).toBe(false);
  });

  it('logout limpa o token, o usuário e o localStorage', () => {
    const resposta: LoginResponse = {
      token: criarTokenFake(3600),
      tipo: 'Bearer',
      expiraEm: new Date(Date.now() + 3600_000).toISOString(),
      usuario: USUARIO_TESTE,
    };

    authService.login('teste@adotapet.com', 'Senha@123').subscribe();
    httpMock.expectOne(`${environment.apiUrl}/auth/login`).flush(resposta);

    expect(authService.estaAutenticado()).toBe(true);

    authService.logout();

    expect(authService.token).toBeNull();
    expect(authService.usuario()).toBeNull();
    expect(authService.estaAutenticado()).toBe(false);
    expect(localStorage.getItem('adotapet_token')).toBeNull();
    expect(localStorage.getItem('adotapet_usuario')).toBeNull();
  });
});
