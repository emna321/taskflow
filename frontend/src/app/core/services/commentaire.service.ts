import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Commentaire } from '../models';

@Injectable({ providedIn: 'root' })
export class CommentaireService {
  private http = inject(HttpClient);
  private api = environment.apiUrl;

  lister(tacheId: number) {
    return this.http.get<Commentaire[]>(`${this.api}/taches/${tacheId}/commentaires`);
  }
  ajouter(tacheId: number, texte: string) {
    return this.http.post<Commentaire>(`${this.api}/taches/${tacheId}/commentaires`, { texte });
  }
  supprimer(id: number) {
    return this.http.delete<void>(`${this.api}/commentaires/${id}`);
  }
}
