import { Component, effect, inject, input, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { RouterLink } from '@angular/router';
import { AnimalRequest, AnimalResponse } from '../../../core/models/animal.models';
import { CampoErro } from '../../../core/models/common.models';
import {
  Especie,
  ESPECIE_LABELS,
  ESPECIES,
  Porte,
  PORTE_LABELS,
  PORTES,
  Sexo,
  SEXO_LABELS,
  SEXOS,
  StatusAdocao,
  STATUS_ADOCAO,
  STATUS_LABELS,
} from '../../../core/models/enums';

function faixaOpcionalValidator(min: number, max: number) {
  return (controle: { value: unknown }): ValidationErrors | null => {
    const valor = controle.value;
    if (valor === null || valor === undefined || valor === '') {
      return null;
    }
    const numero = Number(valor);
    if (Number.isNaN(numero)) {
      return { numero: true };
    }
    if (numero < min || numero > max) {
      return { faixa: { min, max } };
    }
    return null;
  };
}

// Mesma regra aplicada no backend: vazio é permitido (Validators.pattern já ignora valores
// vazios), senão exige http(s):// seguido de qualquer sequência sem espaços.
const URL_PATTERN = /^https?:\/\/\S+$/i;

@Component({
  selector: 'app-animal-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSlideToggleModule,
  ],
  templateUrl: './animal-form.component.html',
  styleUrl: './animal-form.component.scss',
})
export class AnimalFormComponent {
  private readonly fb = inject(FormBuilder);

  readonly animal = input<AnimalResponse | null>(null);
  readonly enviando = input(false);
  readonly erroServidor = input<CampoErro[] | null>(null);
  readonly textoEnviar = input('Salvar');
  readonly rotaCancelar = input('/animais');

  readonly enviar = output<AnimalRequest>();

  protected readonly especieLabels = ESPECIE_LABELS;
  protected readonly sexoLabels = SEXO_LABELS;
  protected readonly porteLabels = PORTE_LABELS;
  protected readonly statusLabels = STATUS_LABELS;
  protected readonly especies = ESPECIES;
  protected readonly sexos = SEXOS;
  protected readonly portes = PORTES;
  protected readonly statusOpcoes = STATUS_ADOCAO;

  protected readonly formulario = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(60)]],
    especie: [null as Especie | null, [Validators.required]],
    raca: ['', [Validators.maxLength(60)]],
    sexo: [null as Sexo | null, [Validators.required]],
    porte: [null as Porte | null, [Validators.required]],
    idadeAnos: [null as number | null, [Validators.required, Validators.min(0), Validators.max(30)]],
    pesoKg: [null as number | null, [faixaOpcionalValidator(0.1, 120)]],
    castrado: [false, [Validators.required]],
    vacinado: [false, [Validators.required]],
    status: [null as StatusAdocao | null, [Validators.required]],
    descricao: ['', [Validators.maxLength(1000)]],
    fotoUrl: ['', [Validators.maxLength(500), Validators.pattern(URL_PATTERN)]],
  });

  constructor() {
    effect(() => {
      const animal = this.animal();
      if (animal) {
        this.formulario.patchValue({
          nome: animal.nome,
          especie: animal.especie,
          raca: animal.raca ?? '',
          sexo: animal.sexo,
          porte: animal.porte,
          idadeAnos: animal.idadeAnos,
          pesoKg: animal.pesoKg ?? null,
          castrado: animal.castrado,
          vacinado: animal.vacinado,
          status: animal.status,
          descricao: animal.descricao ?? '',
          fotoUrl: animal.fotoUrl ?? '',
        });
      }
    });

    effect(() => {
      const campos = this.erroServidor();
      if (!campos?.length) {
        return;
      }
      for (const campoErro of campos) {
        const controle = this.formulario.get(campoErro.campo);
        if (controle) {
          controle.setErrors({ ...controle.errors, servidor: campoErro.mensagem });
          controle.markAsTouched();
        }
      }
    });
  }

  get nome() {
    return this.formulario.controls.nome;
  }
  get especie() {
    return this.formulario.controls.especie;
  }
  get raca() {
    return this.formulario.controls.raca;
  }
  get sexo() {
    return this.formulario.controls.sexo;
  }
  get porte() {
    return this.formulario.controls.porte;
  }
  get idadeAnos() {
    return this.formulario.controls.idadeAnos;
  }
  get pesoKg() {
    return this.formulario.controls.pesoKg;
  }
  get status() {
    return this.formulario.controls.status;
  }
  get descricao() {
    return this.formulario.controls.descricao;
  }
  get fotoUrl() {
    return this.formulario.controls.fotoUrl;
  }

  protected aoEnviar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const valores = this.formulario.getRawValue();
    const dados: AnimalRequest = {
      nome: valores.nome.trim(),
      especie: valores.especie as Especie,
      raca: valores.raca?.trim() || null,
      sexo: valores.sexo as Sexo,
      porte: valores.porte as Porte,
      idadeAnos: valores.idadeAnos as number,
      pesoKg: valores.pesoKg === null || valores.pesoKg === undefined || (valores.pesoKg as unknown) === '' ? null : Number(valores.pesoKg),
      castrado: valores.castrado,
      vacinado: valores.vacinado,
      status: valores.status as StatusAdocao,
      descricao: valores.descricao?.trim() || null,
      fotoUrl: valores.fotoUrl?.trim() || null,
    };

    this.enviar.emit(dados);
  }
}
