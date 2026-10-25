import { A11yModule } from '@angular/cdk/a11y';
import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmDialogDados {
  titulo: string;
  mensagem: string;
  textoConfirmar?: string;
  textoCancelar?: string;
  perigoso?: boolean;
}

@Component({
  selector: 'app-confirm-dialog',
  imports: [A11yModule, MatButtonModule, MatDialogModule, MatIconModule],
  template: `
    <h2 mat-dialog-title class="dialogo__titulo">
      @if (dados.perigoso) {
        <mat-icon aria-hidden="true" class="dialogo__icone-alerta">warning</mat-icon>
      }
      {{ dados.titulo }}
    </h2>
    <mat-dialog-content>{{ dados.mensagem }}</mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="ref.close(false)">
        {{ dados.textoCancelar ?? 'Cancelar' }}
      </button>
      <button
        mat-flat-button
        type="button"
        [color]="dados.perigoso ? 'warn' : 'primary'"
        cdkFocusInitial
        (click)="ref.close(true)"
      >
        {{ dados.textoConfirmar ?? 'Excluir' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: `
    .dialogo__titulo {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    .dialogo__icone-alerta {
      color: var(--mat-sys-error);
    }
  `,
})
export class ConfirmDialogComponent {
  protected readonly ref = inject(MatDialogRef<ConfirmDialogComponent, boolean>);
  protected readonly dados: ConfirmDialogDados = inject(MAT_DIALOG_DATA);
}
