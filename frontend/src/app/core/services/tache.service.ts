import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Page, Priorite, Statut, Tache, TacheRequest } from '../models';

export interface TacheFiltre {
  q?: string; statut?: Statut | ''; priorite?: Priorite | ''; projetId?: number | null; userId?: number | null;
  dateDebut?: string; dateFin?: string; tri?: string; page?: number; taille?: number;
}

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

  /** Recherche avancee paginee : GET /api/taches/recherche */
  rechercher(f: TacheFiltre) {
    let params = new HttpParams();
    if (f.q) params = params.set('q', f.q);
    if (f.statut) params = params.set('statut', f.statut);
    if (f.priorite) params = params.set('priorite', f.priorite);
    if (f.projetId) params = params.set('projetId', f.projetId);
    if (f.userId) params = params.set('userId', f.userId);
    if (f.dateDebut) params = params.set('dateDebut', f.dateDebut);
    if (f.dateFin) params = params.set('dateFin', f.dateFin);
    params = params.set('tri', f.tri ?? 'dateEcheance');
    params = params.set('page', f.page ?? 0);
    params = params.set('taille', f.taille ?? 10);
    return this.http.get<Page<Tache>>(`${this.api}/recherche`, { params });
  }
}
