import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService, MENSAGEM_SESSAO_EXPIRADA } from '../services/auth.service';
import { NotificacaoService } from '../services/notificacao.service';

const ROTAS_PUBLICAS = [`${environment.apiUrl}/auth/login`, `${environment.apiUrl}/auth/registrar`];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const notificacaoService = inject(NotificacaoService);

  const ehChamadaApi = req.url.startsWith(environment.apiUrl);
  const ehRotaPublica = ROTAS_PUBLICAS.some((rota) => req.url.startsWith(rota));

  let requisicao = req;
  if (ehChamadaApi && !ehRotaPublica && authService.token) {
    requisicao = req.clone({
      setHeaders: { Authorization: `Bearer ${authService.token}` },
    });
  }

  return next(requisicao).pipe(
    catchError((erro: HttpErrorResponse) => {
      if (erro.status === 0) {
        notificacaoService.erro('Não foi possível conectar ao servidor.');
      } else if (erro.status === 401 && ehChamadaApi && !ehRotaPublica) {
        const mensagem = erro.error?.mensagem ?? MENSAGEM_SESSAO_EXPIRADA;
        authService.logout(mensagem);
      }
      return throwError(() => erro);
    }),
  );
};
