import { Component, inject } from '@angular/core';
import { HttpEventType } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { FichierService } from '../../core/services/fichier.service';
import { UserService } from '../../core/services/user.service';
import { errorMessage } from '../../core/models';

const TAILLE_MAX_PHOTO = 1024 * 1024;

@Component({
  selector: 'app-profil',
  imports: [ReactiveFormsModule],
  templateUrl: './profil.component.html'
})
export class ProfilComponent {
  auth = inject(AuthService);
  private fichierService = inject(FichierService);
  private userService = inject(UserService);
  private fb = inject(FormBuilder);

  photoUrl: string | null = null;
  erreur = '';
  message = '';
  progression: number | null = null;

  // Informations personnelles
  erreurProfil = '';
  formProfil = this.fb.nonNullable.group({
    nom: ['', Validators.required],
    prenom: ['', Validators.required],
    tel: ['']
  });

  // Changement de mot de passe
  erreurMdp = '';
  messageMdp = '';
  formMdp = this.fb.nonNullable.group({
    ancien: ['', Validators.required],
    nouveau: ['', [Validators.required, Validators.minLength(6)]],
    confirmation: ['', Validators.required]
  });

  constructor() {
    const u = this.auth.currentUser();
    if (u?.hasPhoto) {
      this.fichierService.getPhotoBlob(u.id).subscribe(blob => this.photoUrl = URL.createObjectURL(blob));
    }
    if (u) {
      this.formProfil.reset({ nom: u.nom, prenom: u.prenom, tel: '' });
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

  enregistrerProfil(): void {
    if (this.formProfil.invalid) { this.formProfil.markAllAsTouched(); return; }
    const v = this.formProfil.getRawValue();
    this.erreurProfil = '';
    this.userService.modifierMonProfil(v.nom, v.prenom, v.tel).subscribe({
      next: () => {
        this.message = 'Profil mis à jour.';
        this.auth.actualiserIdentite(v.nom, v.prenom);
      },
      error: err => this.erreurProfil = errorMessage(err)
    });
  }

  changerMotDePasse(): void {
    if (this.formMdp.invalid) { this.formMdp.markAllAsTouched(); return; }
    const v = this.formMdp.getRawValue();
    if (v.nouveau !== v.confirmation) {
      this.erreurMdp = 'La confirmation ne correspond pas au nouveau mot de passe.';
      return;
    }
    this.erreurMdp = '';
    this.userService.changerMonMotDePasse(v.ancien, v.nouveau, v.confirmation).subscribe({
      next: () => {
        this.messageMdp = 'Mot de passe modifié.';
        this.formMdp.reset({ ancien: '', nouveau: '', confirmation: '' });
      },
      error: err => this.erreurMdp = errorMessage(err)
    });
  }
}
