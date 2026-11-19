import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Client } from '../models';

@Injectable({ providedIn: 'root' })
export class ClientService {
  private http = inject(HttpClient);
  private api = `${environment.apiUrl}/clients`;

  getAll() { return this.http.get<Client[]>(this.api); }
  getById(id: number) { return this.http.get<Client>(`${this.api}/${id}`); }
  create(c: Client) { return this.http.post<Client>(this.api, c); }
  update(id: number, c: Client) { return this.http.put<Client>(`${this.api}/${id}`, c); }
  delete(id: number) { return this.http.delete<void>(`${this.api}/${id}`); }
}
