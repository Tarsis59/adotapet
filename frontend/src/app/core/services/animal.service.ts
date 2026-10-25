import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AnimalFiltro, AnimalRequest, AnimalResponse, EstatisticasResponse } from '../models/animal.models';
import { PaginaResponse } from '../models/common.models';

@Injectable({ providedIn: 'root' })
export class AnimalService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/animais`;

  listar(filtro: AnimalFiltro): Observable<PaginaResponse<AnimalResponse>> {
    let params = new HttpParams()
      .set('page', String(filtro.page ?? 0))
      .set('size', String(filtro.size ?? 10))
      .set('sort', filtro.sort ?? 'nome,asc');

    if (filtro.busca) {
      params = params.set('busca', filtro.busca);
    }
    if (filtro.especie) {
      params = params.set('especie', filtro.especie);
    }
    if (filtro.status) {
      params = params.set('status', filtro.status);
    }

    return this.http.get<PaginaResponse<AnimalResponse>>(this.base, { params });
  }

  obterEstatisticas(): Observable<EstatisticasResponse> {
    return this.http.get<EstatisticasResponse>(`${this.base}/estatisticas`);
  }

  obterPorId(id: number): Observable<AnimalResponse> {
    return this.http.get<AnimalResponse>(`${this.base}/${id}`);
  }

  criar(dados: AnimalRequest): Observable<AnimalResponse> {
    return this.http.post<AnimalResponse>(this.base, dados);
  }

  atualizar(id: number, dados: AnimalRequest): Observable<AnimalResponse> {
    return this.http.put<AnimalResponse>(`${this.base}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
