import { Component, Input, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { CommentaireService } from '../../core/services/commentaire.service';
import { Commentaire, errorMessage } from '../../core/models';

/** Commentaires d'une tache, du plus ancien au plus recent. */
@Component({
  selector: 'app-commentaires',
  imports: [DatePipe],
  templateUrl: './commentaires.component.html'
})
export class CommentairesComponent implements OnInit {
  @Input({ required: true }) tacheId!: number;

  private auth = inject(AuthService);
  private commentaireService = inject(CommentaireService);

  commentaires: Commentaire[] = [];
  chargement = true;
  erreur = '';
  envoiEnCours = false;

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.commentaireService.lister(this.tacheId).subscribe({
      next: c => { this.commentaires = c; this.chargement = false; },
      error: err => { this.erreur = errorMessage(err); this.chargement = false; }
    });
  }

  peutSupprimer(c: Commentaire): boolean {
    return this.auth.isAdmin() || c.auteurId === this.auth.currentUser()?.id;
  }

  ajouter(texte: string, champ: HTMLTextAreaElement): void {
    const valeur = texte.trim();
    if (!valeur) return;
    this.envoiEnCours = true;
    this.commentaireService.ajouter(this.tacheId, valeur).subscribe({
      next: () => { this.envoiEnCours = false; champ.value = ''; this.charger(); },
      error: err => { this.envoiEnCours = false; this.erreur = errorMessage(err); }
    });
  }

  supprimer(c: Commentaire): void {
    if (!confirm('Supprimer ce commentaire ?')) return;
    this.commentaireService.supprimer(c.id).subscribe({
      next: () => this.charger(),
      error: err => this.erreur = errorMessage(err)
    });
  }
}
