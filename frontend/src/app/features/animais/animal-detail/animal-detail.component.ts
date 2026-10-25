import { HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router, RouterLink } from '@angular/router';
import { AnimalResponse } from '../../../core/models/animal.models';
import {
  ESPECIE_LABELS,
  PORTE_LABELS,
  SEXO_LABELS,
  STATUS_CHIP_CLASS,
  STATUS_LABELS,
} from '../../../core/models/enums';
import { AnimalService } from '../../../core/services/animal.service';
import { NotificacaoService } from '../../../core/services/notificacao.service';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';

const ICONE_ESPECIE: Record<string, string> = {
  CACHORRO: '🐶',
  GATO: '🐱',
  OUTRO: '🐾',
};

@Component({
  selector: 'app-animal-detail',
  imports: [CommonModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule, RouterLink],
  templateUrl: './animal-detail.component.html',
  styleUrl: './animal-detail.component.scss',
})
export class AnimalDetailComponent implements OnInit {
  private readonly animalService = inject(AnimalService);
  private readonly notificacaoService = inject(NotificacaoService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  /** Vinculado automaticamente ao parâmetro de rota :id (withComponentInputBinding). */
  readonly id = input<string>('');

  protected readonly animal = signal<AnimalResponse | null>(null);
  protected readonly carregando = signal(true);
  protected readonly naoEncontrado = signal(false);
  protected readonly excluindo = signal(false);
  protected readonly erroFoto = signal(false);
  protected readonly fotoCarregada = signal(false);

  protected readonly especieLabels = ESPECIE_LABELS;
  protected readonly sexoLabels = SEXO_LABELS;
  protected readonly porteLabels = PORTE_LABELS;
  protected readonly statusLabels = STATUS_LABELS;
  protected readonly statusChipClass = STATUS_CHIP_CLASS;
  protected readonly iconeEspecie = ICONE_ESPECIE;

  ngOnInit(): void {
    // Inputs vinculados à rota só estão disponíveis a partir do ngOnInit.
    this.carregarAnimal();
  }

  private carregarAnimal(): void {
    const idNumero = Number(this.id());
    if (!idNumero) {
      this.naoEncontrado.set(true);
      this.carregando.set(false);
      return;
    }

    this.carregando.set(true);
    this.naoEncontrado.set(false);
    this.animalService.obterPorId(idNumero).subscribe({
      next: (animal) => {
        this.erroFoto.set(false);
        this.fotoCarregada.set(false);
        this.animal.set(animal);
        this.carregando.set(false);
      },
      error: (erro: HttpErrorResponse) => {
        this.carregando.set(false);
        if (erro.status === 404) {
          this.naoEncontrado.set(true);
        }
      },
    });
  }

  protected excluir(): void {
    const animal = this.animal();
    if (!animal) {
      return;
    }

    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        titulo: 'Excluir animal?',
        mensagem: `Tem certeza de que deseja excluir "${animal.nome}"? Esta ação não pode ser desfeita.`,
        perigoso: true,
      },
      autoFocus: 'first-tabbable',
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }

      this.excluindo.set(true);
      this.animalService.excluir(animal.id).subscribe({
        next: () => {
          this.notificacaoService.sucesso('Animal excluído com sucesso!');
          this.router.navigate(['/animais']);
        },
        error: () => {
          this.excluindo.set(false);
          this.notificacaoService.erro('Não foi possível excluir o animal.');
        },
      });
    });
  }
}
