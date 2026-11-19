import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Statut, Tache, TacheRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class TacheService {
  private http = inject(HttpClient);
  private api = `${environment.apiUrl}/taches`;

  getAll(filtre: { projetId?: number; userId?: number } = {}) {
    let params = new HttpParams();
    if (filtre.projetId) params = params.set('projetId', filtre.projetId);
    if (filtre.userId) params = params.set('userId', filtre.userId);
    return this.http.get<Tache[]>(this.api, { params });
  }
  getById(id: number) { return this.http.get<Tache>(`${this.api}/${id}`); }
  create(t: TacheRequest) { return this.http.post<Tache>(this.api, t); }
  update(id: number, t: TacheRequest) { return this.http.put<Tache>(`${this.api}/${id}`, t); }
  delete(id: number) { return this.http.delete<void>(`${this.api}/${id}`); }
  changerStatut(id: number, statut: Statut) {
    return this.http.patch<Tache>(`${this.api}/${id}/statut`, { statut });
  }
}
