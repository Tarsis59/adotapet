import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ErroResponse } from '../../../core/models/common.models';
import { AuthService } from '../../../core/services/auth.service';
import { NotificacaoService } from '../../../core/services/notificacao.service';

function senhasIguaisValidator(grupo: AbstractControl): ValidationErrors | null {
  const senha = grupo.get('senha')?.value;
  const confirmarSenha = grupo.get('confirmarSenha')?.value;
  if (senha && confirmarSenha && senha !== confirmarSenha) {
    return { senhasDiferentes: true };
  }
  return null;
}

@Component({
  selector: 'app-registrar',
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
  templateUrl: './registrar.component.html',
  styleUrl: './registrar.component.scss',
})
export class RegistrarComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly notificacaoService = inject(NotificacaoService);
  private readonly router = inject(Router);

  protected readonly carregando = signal(false);
  protected readonly erroBackend = signal<string | null>(null);
  protected readonly ocultarSenha = signal(true);
  protected readonly ocultarConfirmarSenha = signal(true);

  protected readonly formulario = this.fb.nonNullable.group(
    {
      nome: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      senha: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
      confirmarSenha: ['', [Validators.required]],
    },
    { validators: senhasIguaisValidator },
  );

  get nome() {
    return this.formulario.controls.nome;
  }
  get email() {
    return this.formulario.controls.email;
  }
  get senha() {
    return this.formulario.controls.senha;
  }
  get confirmarSenha() {
    return this.formulario.controls.confirmarSenha;
  }

  enviar(): void {
    this.erroBackend.set(null);
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.carregando.set(true);
    const { nome, email, senha } = this.formulario.getRawValue();

    this.authService
      .registrar(nome, email, senha)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: () => {
          this.notificacaoService.sucesso('Conta criada com sucesso! Faça login para continuar.');
          this.router.navigate(['/login']);
        },
        error: (erro: HttpErrorResponse) => {
          const corpo = erro.error as ErroResponse | undefined;
          if (erro.status === 409) {
            this.erroBackend.set(corpo?.mensagem ?? 'Este e-mail já está cadastrado.');
          } else if (erro.status === 400) {
            this.erroBackend.set(corpo?.mensagem ?? 'Verifique os dados informados.');
          } else if (erro.status !== 0) {
            this.erroBackend.set('Não foi possível concluir o cadastro. Tente novamente.');
          }
        },
      });
  }
}
