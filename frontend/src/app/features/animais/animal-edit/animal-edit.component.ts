import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router, RouterLink } from '@angular/router';
import { AnimalRequest, AnimalResponse } from '../../../core/models/animal.models';
import { CampoErro, ErroResponse } from '../../../core/models/common.models';
import { AnimalService } from '../../../core/services/animal.service';
import { NotificacaoService } from '../../../core/services/notificacao.service';
import { AnimalFormComponent } from '../animal-form/animal-form.component';

@Component({
  selector: 'app-animal-edit',
  imports: [AnimalFormComponent, MatProgressSpinnerModule, RouterLink],
  templateUrl: './animal-edit.component.html',
  styleUrl: './animal-edit.component.scss',
})
export class AnimalEditComponent implements OnInit {
  private readonly animalService = inject(AnimalService);
  private readonly notificacaoService = inject(NotificacaoService);
  private readonly router = inject(Router);

  /** Vinculado automaticamente ao parâmetro de rota :id (withComponentInputBinding). */
  readonly id = input<string>('');

  protected readonly animal = signal<AnimalResponse | null>(null);
  protected readonly carregando = signal(true);
  protected readonly naoEncontrado = signal(false);
  protected readonly enviando = signal(false);
  protected readonly errosServidor = signal<CampoErro[] | null>(null);

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
    this.animalService.obterPorId(idNumero).subscribe({
      next: (animal) => {
        this.animal.set(animal);
        this.carregando.set(false);
      },
      error: (erro: HttpErrorResponse) => {
        this.carregando.set(false);
        if (erro.status === 404) {
          this.naoEncontrado.set(true);
          this.notificacaoService.erro('Animal não encontrado.');
          this.router.navigate(['/animais']);
        }
      },
    });
  }

  protected salvar(dados: AnimalRequest): void {
    const idNumero = Number(this.id());
    this.enviando.set(true);
    this.errosServidor.set(null);

    this.animalService.atualizar(idNumero, dados).subscribe({
      next: (animal) => {
        this.enviando.set(false);
        this.notificacaoService.sucesso('Animal atualizado com sucesso!');
        this.router.navigate(['/animais', animal.id]);
      },
      error: (erro: HttpErrorResponse) => {
        this.enviando.set(false);
        this.tratarErro(erro);
      },
    });
  }

  private tratarErro(erro: HttpErrorResponse): void {
    const corpo = erro.error as ErroResponse | undefined;
    if (erro.status === 400) {
      this.errosServidor.set(corpo?.campos ?? []);
      this.notificacaoService.erro(corpo?.mensagem ?? 'Verifique os campos destacados e tente novamente.');
    } else if (erro.status === 404) {
      this.notificacaoService.erro('Animal não encontrado.');
      this.router.navigate(['/animais']);
    } else if (erro.status !== 0) {
      this.notificacaoService.erro('Não foi possível atualizar o animal. Tente novamente.');
    }
  }
}
