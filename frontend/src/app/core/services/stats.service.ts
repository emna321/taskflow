import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Stats } from '../models';

@Injectable({ providedIn: 'root' })
export class StatsService {
  private http = inject(HttpClient);
  private api = `${environment.apiUrl}/stats`;

  obtenir() {
    return this.http.get<Stats>(this.api);
  }
}
