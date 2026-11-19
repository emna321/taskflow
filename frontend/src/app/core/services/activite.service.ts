import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Activite } from '../models';

@Injectable({ providedIn: 'root' })
export class ActiviteService {
  private http = inject(HttpClient);
  private api = `${environment.apiUrl}/activites`;

  lister(limit = 20) {
    return this.http.get<Activite[]>(this.api, { params: new HttpParams().set('limit', limit) });
  }
}
