import { Component, Input, OnInit, inject } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpEventType } from '@angular/common/http';
import { AuthService } from '../../core/services/auth.service';
import { FichierService } from '../../core/services/fichier.service';
import { FichierMeta, errorMessage } from '../../core/models';

const EXTENSIONS_AUTORISEES = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'csv', 'png', 'jpg', 'jpeg', 'webp', 'zip'];
const TAILLE_MAX_OCTETS = 5 * 1024 * 1024;

const ICONES: Record<string, string> = {
  pdf: 'bi-file-earmark-pdf text-danger',
  doc: 'bi-file-earmark-word text-primary', docx: 'bi-file-earmark-word text-primary',
  xls: 'bi-file-earmark-excel text-success', xlsx: 'bi-file-earmark-excel text-success',
  ppt: 'bi-file-earmark-ppt text-warning', pptx: 'bi-file-earmark-ppt text-warning',
  txt: 'bi-file-earmark-text', csv: 'bi-file-earmark-spreadsheet',
  png: 'bi-file-earmark-image text-info', jpg: 'bi-file-earmark-image text-info',
  jpeg: 'bi-file-earmark-image text-info', webp: 'bi-file-earmark-image text-info',
  zip: 'bi-file-earmark-zip'
};

/** Pieces jointes d'une tache ou d'un projet : liste + envoi (bouton ou glisser-deposer) + suppression. */
@Component({
  selector: 'app-file-upload',
  imports: [DatePipe, DecimalPipe],
  templateUrl: './file-upload.component.html',
  styleUrl: './file-upload.component.css'
})
export class FileUploadComponent implements OnInit {
  @Input({ required: true }) type!: 'tache' | 'projet';
  @Input({ required: true }) id!: number;

  private auth = inject(AuthService);
  private fichierService = inject(FichierService);

  fichiers: FichierMeta[] = [];
  chargement = true;
  erreur = '';
  progression: number | null = null;
  glisseSurvol = false;

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    const obs = this.type === 'tache' ? this.fichierService.listerPourTache(this.id) : this.fichierService.listerPourProjet(this.id);
    obs.subscribe({
      next: f => { this.fichiers = f; this.chargement = false; },
      error: err => { this.erreur = errorMessage(err); this.chargement = false; }
    });
  }

  peutSupprimer(f: FichierMeta): boolean {
    return this.auth.isAdmin() || f.uploadeParId === this.auth.currentUser()?.id;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const fichier = input.files?.[0];
    input.value = '';
    if (fichier) this.envoyer(fichier);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.glisseSurvol = false;
    const fichier = event.dataTransfer?.files?.[0];
    if (fichier) this.envoyer(fichier);
  }

  supprimer(f: FichierMeta): void {
    if (!confirm(`Supprimer « ${f.nomOriginal} » ?`)) return;
    this.fichierService.supprimer(f.id).subscribe({
      next: () => this.charger(),
      error: err => this.erreur = errorMessage(err)
    });
  }

  telecharger(f: FichierMeta): void {
    this.fichierService.telecharger(f.id).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = f.nomOriginal;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: err => this.erreur = errorMessage(err)
    });
  }

  icone(f: FichierMeta): string {
    const ext = f.nomOriginal.split('.').pop()?.toLowerCase() ?? '';
    return ICONES[ext] ?? 'bi-file-earmark';
  }

  /** Validation cote client (type + taille) avant envoi, en plus de la validation serveur. */
  private envoyer(fichier: File): void {
    this.erreur = '';
    const extension = fichier.name.split('.').pop()?.toLowerCase() ?? '';
    if (!EXTENSIONS_AUTORISEES.includes(extension)) {
      this.erreur = `Type de fichier non autorisé : .${extension}`;
      return;
    }
    if (fichier.size === 0) {
      this.erreur = 'Le fichier est vide.';
      return;
    }
    if (fichier.size > TAILLE_MAX_OCTETS) {
      this.erreur = 'Le fichier dépasse la taille maximale autorisée (5 Mo).';
      return;
    }

    this.progression = 0;
    const obs = this.type === 'tache'
      ? this.fichierService.uploaderPourTache(this.id, fichier)
      : this.fichierService.uploaderPourProjet(this.id, fichier);
    obs.subscribe({
      next: evt => {
        if (evt.type === HttpEventType.UploadProgress && evt.total) {
          this.progression = Math.round((100 * evt.loaded) / evt.total);
        } else if (evt.type === HttpEventType.Response) {
          this.progression = null;
          this.charger();
        }
      },
      error: err => { this.progression = null; this.erreur = errorMessage(err); }
    });
  }
}
