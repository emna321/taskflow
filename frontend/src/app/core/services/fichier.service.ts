import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpRequest } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Client, FichierMeta, User } from '../models';

/** Pieces jointes (taches/projets), logo client et photo de profil : tout est stocke en base cote backend. */
@Injectable({ providedIn: 'root' })
export class FichierService {
  private http = inject(HttpClient);
  private api = environment.apiUrl;

  listerPourTache(tacheId: number) {
    return this.http.get<FichierMeta[]>(`${this.api}/taches/${tacheId}/fichiers`);
  }
  listerPourProjet(projetId: number) {
    return this.http.get<FichierMeta[]>(`${this.api}/projets/${projetId}/fichiers`);
  }
  uploaderPourTache(tacheId: number, fichier: File) {
    return this.uploader<FichierMeta>(`${this.api}/taches/${tacheId}/fichiers`, 'fichier', fichier);
  }
  uploaderPourProjet(projetId: number, fichier: File) {
    return this.uploader<FichierMeta>(`${this.api}/projets/${projetId}/fichiers`, 'fichier', fichier);
  }
  supprimer(id: number) {
    return this.http.delete<void>(`${this.api}/fichiers/${id}`);
  }
  /** Telechargement authentifie (le token n'apparait jamais dans l'URL) : recupere un blob puis declenche le download. */
  telecharger(id: number) {
    return this.http.get(`${this.api}/fichiers/${id}/telecharger`, { responseType: 'blob' });
  }

  uploaderLogo(clientId: number, fichier: File) {
    return this.uploader<Client>(`${this.api}/clients/${clientId}/logo`, 'logo', fichier);
  }
  /** Image chargee via HttpClient (avec le token) puis convertie en ObjectURL : jamais dans une balise <img src> directe. */
  getLogoBlob(clientId: number) {
    return this.http.get(`${this.api}/clients/${clientId}/logo`, { responseType: 'blob' });
  }

  uploaderMaPhoto(fichier: File) {
    return this.uploader<User>(`${this.api}/users/me/photo`, 'photo', fichier);
  }
  getPhotoBlob(userId: number) {
    return this.http.get(`${this.api}/users/${userId}/photo`, { responseType: 'blob' });
  }

  /** reportProgress permet d'afficher une barre de progression (HttpEventType.UploadProgress). */
  private uploader<T>(url: string, champ: string, fichier: File) {
    const formData = new FormData();
    formData.append(champ, fichier);
    const req = new HttpRequest<FormData>('POST', url, formData, { reportProgress: true });
    return this.http.request<T>(req);
  }
}
