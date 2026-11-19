import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Projet, ProjetRequest } from '../models';

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
}
