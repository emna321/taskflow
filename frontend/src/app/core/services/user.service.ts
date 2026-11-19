import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { RoleName, User, UserRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);
  private api = `${environment.apiUrl}/users`;

  /** Utilisateurs a qui on peut affecter une tache (tout utilisateur connecte) */
  getAssignables() { return this.http.get<User[]>(`${this.api}/assignables`); }

  // --- Admin uniquement ---
  getAll() { return this.http.get<User[]>(this.api); }
  create(u: UserRequest) { return this.http.post<User>(this.api, u); }
  update(id: number, u: UserRequest) { return this.http.put<User>(`${this.api}/${id}`, u); }
  updateRoles(id: number, roles: RoleName[]) { return this.http.put<User>(`${this.api}/${id}/roles`, { roles }); }
  delete(id: number) { return this.http.delete<void>(`${this.api}/${id}`); }

  // --- Mon profil (tout utilisateur connecte) ---
  modifierMonProfil(nom: string, prenom: string, tel: string) {
    return this.http.put<User>(`${this.api}/me`, { nom, prenom, tel });
  }
  changerMonMotDePasse(ancien: string, nouveau: string, confirmation: string) {
    return this.http.put<void>(`${this.api}/me/password`, { ancien, nouveau, confirmation });
  }
}
