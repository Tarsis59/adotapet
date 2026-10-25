import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AnimalRequest } from '../../../core/models/animal.models';
import { CampoErro, ErroResponse } from '../../../core/models/common.models';
import { AnimalService } from '../../../core/services/animal.service';
import { NotificacaoService } from '../../../core/services/notificacao.service';
import { AnimalFormComponent } from '../animal-form/animal-form.component';

@Component({
  selector: 'app-animal-create',
  imports: [AnimalFormComponent, RouterLink],
  templateUrl: './animal-create.component.html',
  styleUrl: './animal-create.component.scss',
})
export class AnimalCreateComponent {
  private readonly animalService = inject(AnimalService);
  private readonly notificacaoService = inject(NotificacaoService);
  private readonly router = inject(Router);

  protected readonly enviando = signal(false);
  protected readonly errosServidor = signal<CampoErro[] | null>(null);

  protected salvar(dados: AnimalRequest): void {
    this.enviando.set(true);
    this.errosServidor.set(null);

    this.animalService.criar(dados).subscribe({
      next: (animal) => {
        this.enviando.set(false);
        this.notificacaoService.sucesso('Animal cadastrado com sucesso!');
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
    } else if (erro.status !== 0) {
      this.notificacaoService.erro('Não foi possível cadastrar o animal. Tente novamente.');
    }
  }
}
