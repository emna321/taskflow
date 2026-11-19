import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Page, Projet, ProjetRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class ProjetService {
  private http = inject(HttpClient);
  private api = `${environment.apiUrl}/projets`;

  getAll(clientId?: number) {
    const params = clientId ? new HttpParams().set('clientId', clientId) : undefined;
    return this.http.get<Projet[]>(this.api, { params });
  }
  getById(id: number) { return this.http.get<Projet>(`${this.api}/${id}`); }
  create(p: ProjetRequest) { return this.http.post<Projet>(this.api, p); }
  update(id: number, p: ProjetRequest) { return this.http.put<Projet>(`${this.api}/${id}`, p); }
  delete(id: number) { return this.http.delete<void>(`${this.api}/${id}`); }

  /** Recherche avancee paginee : GET /api/projets/recherche */
  rechercher(q: string, clientId: number | null, tri: string, page: number, taille: number) {
    let params = new HttpParams().set('tri', tri).set('page', page).set('taille', taille);
    if (q) params = params.set('q', q);
    if (clientId) params = params.set('clientId', clientId);
    return this.http.get<Page<Projet>>(`${this.api}/recherche`, { params });
  }
}
