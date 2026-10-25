import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, provideRouter, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
  });

  afterEach(() => localStorage.clear());

  it('redireciona para /login com returnUrl quando não autenticado', () => {
    const router = TestBed.inject(Router);
    const estadoFalso = { url: '/animais/42' } as RouterStateSnapshot;

    const resultado = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, estadoFalso),
    );

    expect(resultado).toBeInstanceOf(UrlTree);
    const urlGerada = router.serializeUrl(resultado as UrlTree);
    expect(urlGerada).toContain('/login');
    expect(urlGerada).toContain('returnUrl');
    expect(urlGerada).toContain(encodeURIComponent('/animais/42'));
  });

  it('permite o acesso quando autenticado', () => {
    const authService = TestBed.inject(AuthService);
    Object.defineProperty(authService, 'tokenValido', {
      value: () => true,
    });

    const estadoFalso = { url: '/animais' } as RouterStateSnapshot;
    const resultado = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, estadoFalso),
    );

    expect(resultado).toBe(true);
  });
});
