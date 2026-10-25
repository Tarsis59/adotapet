import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { AnimalResponse, EstatisticasResponse } from '../../../core/models/animal.models';
import { Especie, ESPECIE_LABELS, ESPECIES, PORTE_LABELS, SEXO_LABELS, StatusAdocao, STATUS_CHIP_CLASS, STATUS_LABELS, STATUS_ADOCAO } from '../../../core/models/enums';
import { AnimalService } from '../../../core/services/animal.service';
import { NotificacaoService } from '../../../core/services/notificacao.service';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-animal-list',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './animal-list.component.html',
  styleUrl: './animal-list.component.scss',
})
export class AnimalListComponent {
  protected readonly iconeEspecie: Record<string, string> = { CACHORRO: '🐶', GATO: '🐱', OUTRO: '🐾' };

  /** Usa uma versão reduzida da foto na listagem quando o provedor aceita redimensionamento. */
  protected miniatura(url: string): string {
    return url.includes('images.unsplash.com') ? url.replace(/([?&])w=\d+/, '$1w=160') : url;
  }

  private readonly animalService = inject(AnimalService);
  private readonly notificacaoService = inject(NotificacaoService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly especieLabels = ESPECIE_LABELS;
  protected readonly statusLabels = STATUS_LABELS;
  protected readonly porteLabels = PORTE_LABELS;
  protected readonly sexoLabels = SEXO_LABELS;
  protected readonly statusChipClass = STATUS_CHIP_CLASS;
  protected readonly especies = ESPECIES;
  protected readonly statusOpcoes = STATUS_ADOCAO;
  protected readonly colunas = ['nome', 'especie', 'raca', 'sexo', 'porte', 'idade', 'status', 'acoes'];

  protected readonly buscaControl = new FormControl('', { nonNullable: true });
  protected readonly especieControl = new FormControl<Especie | ''>('', { nonNullable: true });
  protected readonly statusControl = new FormControl<StatusAdocao | ''>('', { nonNullable: true });

  protected readonly estatisticas = signal<EstatisticasResponse | null>(null);
  protected readonly animais = signal<AnimalResponse[]>([]);
  protected readonly totalElementos = signal(0);
  protected readonly pageIndex = signal(0);
  protected readonly pageSize = signal(10);
  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);

  constructor() {
    this.buscaControl.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.pageIndex.set(0);
        this.carregar();
      });

    this.especieControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.pageIndex.set(0);
      this.carregar();
    });

    this.statusControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.pageIndex.set(0);
      this.carregar();
    });

    this.carregarEstatisticas();
    this.carregar();
  }

  protected carregarEstatisticas(): void {
    this.animalService.obterEstatisticas().subscribe({
      next: (resposta) => this.estatisticas.set(resposta),
      error: () => this.estatisticas.set(null),
    });
  }

  protected carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);

    this.animalService
      .listar({
        busca: this.buscaControl.value,
        especie: this.especieControl.value,
        status: this.statusControl.value,
        page: this.pageIndex(),
        size: this.pageSize(),
        sort: 'nome,asc',
      })
      .subscribe({
        next: (resposta) => {
          this.animais.set(resposta.conteudo);
          this.totalElementos.set(resposta.totalElementos);
          this.carregando.set(false);
        },
        error: (erro: HttpErrorResponse) => {
          this.carregando.set(false);
          if (erro.status !== 0) {
            this.erro.set('Não foi possível carregar a lista de animais.');
          }
        },
      });
  }

  protected aoMudarPagina(evento: PageEvent): void {
    this.pageIndex.set(evento.pageIndex);
    this.pageSize.set(evento.pageSize);
    this.carregar();
  }

  protected limparFiltros(): void {
    this.buscaControl.setValue('');
    this.especieControl.setValue('');
    this.statusControl.setValue('');
  }

  protected ver(animal: AnimalResponse): void {
    this.router.navigate(['/animais', animal.id]);
  }

  protected editar(animal: AnimalResponse): void {
    this.router.navigate(['/animais', animal.id, 'editar']);
  }

  protected excluir(animal: AnimalResponse): void {
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

      this.animalService.excluir(animal.id).subscribe({
        next: () => {
          this.notificacaoService.sucesso('Animal excluído com sucesso!');
          if (this.animais().length === 1 && this.pageIndex() > 0) {
            this.pageIndex.set(this.pageIndex() - 1);
          }
          this.carregar();
          this.carregarEstatisticas();
        },
        error: () => {
          this.notificacaoService.erro('Não foi possível excluir o animal.');
        },
      });
    });
  }
}
