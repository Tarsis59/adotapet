import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { ErroResponse } from '../../../core/models/common.models';

const MENSAGEM_SESSAO_INVALIDA = 'Sua sessão expirou ou é inválida. Entre novamente.';

/** Troca motivos técnicos de logout forçado (token ausente/expirado/inválido) por uma mensagem amigável. */
function mensagemAmigavelDeSessao(motivo: string | null): string | null {
  if (!motivo) {
    return null;
  }
  const normalizado = motivo.trim().toLowerCase();
  if (normalizado.includes('token') || normalizado.includes('expir') || normalizado.includes('inválid') || normalizado.includes('invalid')) {
    return MENSAGEM_SESSAO_INVALIDA;
  }
  return motivo;
}

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly carregando = signal(false);
  protected readonly erroBackend = signal<string | null>(null);
  protected readonly ocultarSenha = signal(true);
  protected readonly mensagemSessao = signal<string | null>(
    mensagemAmigavelDeSessao(this.route.snapshot.queryParamMap.get('motivo')),
  );

  protected readonly formulario = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required]],
  });

  get email() {
    return this.formulario.controls.email;
  }

  get senha() {
    return this.formulario.controls.senha;
  }

  alternarVisibilidadeSenha(): void {
    this.ocultarSenha.set(!this.ocultarSenha());
  }

  enviar(): void {
    this.erroBackend.set(null);
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.carregando.set(true);
    const { email, senha } = this.formulario.getRawValue();

    this.authService
      .login(email, senha)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: () => {
          const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/animais';
          this.router.navigateByUrl(returnUrl);
        },
        error: (erro: HttpErrorResponse) => {
          const corpo = erro.error as ErroResponse | undefined;
          if (erro.status === 401) {
            this.erroBackend.set(corpo?.mensagem ?? 'E-mail ou senha inválidos.');
          } else if (erro.status === 400) {
            this.erroBackend.set(corpo?.mensagem ?? 'Verifique os dados informados.');
          } else if (erro.status !== 0) {
            this.erroBackend.set('Não foi possível entrar. Tente novamente em instantes.');
          }
        },
      });
  }
}
