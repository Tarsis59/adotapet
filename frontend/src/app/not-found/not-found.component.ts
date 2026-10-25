import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  imports: [MatButtonModule, MatIconModule, RouterLink],
  template: `
    <div class="nao-encontrado">
      <span class="nao-encontrado__icone" aria-hidden="true">🐾</span>
      <h1>404 — Página não encontrada</h1>
      <p>O endereço acessado não existe ou foi movido.</p>
      <a mat-flat-button color="primary" routerLink="/animais">
        <mat-icon aria-hidden="true">home</mat-icon>
        Voltar para Animais
      </a>
    </div>
  `,
  styles: `
    .nao-encontrado {
      min-height: 100dvh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      text-align: center;
      gap: 0.75rem;
      padding: 2rem;
    }
    .nao-encontrado__icone {
      font-size: 3.5rem;
      opacity: 0.8;
    }
    p {
      color: var(--mat-sys-on-surface-variant);
      margin: 0 0 0.5rem;
    }
    a {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
    }
  `,
})
export class NotFoundComponent {}
