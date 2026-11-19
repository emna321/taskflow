import { Component, inject } from '@angular/core';
import { HttpEventType } from '@angular/common/http';
import { AuthService } from '../../core/services/auth.service';
import { FichierService } from '../../core/services/fichier.service';
import { errorMessage } from '../../core/models';

const TAILLE_MAX_PHOTO = 1024 * 1024;

@Component({
  selector: 'app-profil',
  templateUrl: './profil.component.html'
})
export class ProfilComponent {
  auth = inject(AuthService);
  private fichierService = inject(FichierService);

  photoUrl: string | null = null;
  erreur = '';
  message = '';
  progression: number | null = null;

  constructor() {
    const u = this.auth.currentUser();
    if (u?.hasPhoto) {
      this.fichierService.getPhotoBlob(u.id).subscribe(blob => this.photoUrl = URL.createObjectURL(blob));
    }
  }

  get initiales(): string {
    const u = this.auth.currentUser();
    return u ? (u.prenom[0] + u.nom[0]).toUpperCase() : '';
  }

  onPhotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const fichier = input.files?.[0];
    input.value = '';
    if (!fichier) return;
    if (!/\.(png|jpe?g|webp)$/i.test(fichier.name)) {
      this.erreur = 'Formats acceptés : png, jpg, webp.';
      return;
    }
    if (fichier.size > TAILLE_MAX_PHOTO) {
      this.erreur = 'Image trop volumineuse (1 Mo max).';
      return;
    }
    this.erreur = '';
    this.progression = 0;
    this.fichierService.uploaderMaPhoto(fichier).subscribe({
      next: evt => {
        if (evt.type === HttpEventType.UploadProgress && evt.total) {
          this.progression = Math.round((100 * evt.loaded) / evt.total);
        } else if (evt.type === HttpEventType.Response) {
          this.progression = null;
          this.message = 'Photo de profil mise à jour.';
          this.photoUrl = URL.createObjectURL(fichier);
          this.auth.marquerPhoto(true);
        }
      },
      error: err => { this.progression = null; this.erreur = errorMessage(err); }
    });
  }
}
