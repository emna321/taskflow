import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

type Format = 'xlsx' | 'pdf';

@Injectable({ providedIn: 'root' })
export class ExportService {
  private http = inject(HttpClient);
  private api = environment.apiUrl;

  exporterTaches(format: Format) {
    return this.http.get(`${this.api}/export/taches?format=${format}`, { responseType: 'blob' });
  }
  exporterProjets(format: Format) {
    return this.http.get(`${this.api}/export/projets?format=${format}`, { responseType: 'blob' });
  }
  exporterFicheProjet(projetId: number) {
    return this.http.get(`${this.api}/projets/${projetId}/export`, { responseType: 'blob' });
  }

  /** Declenche le telechargement d'un blob avec le nom de fichier donne. */
  telecharger(blob: Blob, nomFichier: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = nomFichier;
    a.click();
    URL.revokeObjectURL(url);
  }
}
