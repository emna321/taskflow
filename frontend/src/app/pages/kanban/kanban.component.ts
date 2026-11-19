import { Component, OnInit, inject } from '@angular/core';
import { CdkDragDrop, DragDropModule, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { AuthService } from '../../core/services/auth.service';
import { TacheService } from '../../core/services/tache.service';
import { PRIORITE_LABELS, Priorite, Statut, Tache, errorMessage } from '../../core/models';

interface Colonne { statut: Statut; titre: string; taches: Tache[]; }

/** Vue Kanban : glisser-deposer une tache entre colonnes change son statut (PATCH /taches/{id}/statut). */
@Component({
  selector: 'app-kanban',
  imports: [DragDropModule],
  templateUrl: './kanban.component.html',
  styleUrl: './kanban.component.css'
})
export class KanbanComponent implements OnInit {
  auth = inject(AuthService);
  private tacheService = inject(TacheService);

  prioriteLabels = PRIORITE_LABELS;
  chargement = true;
  erreur = '';
  message = '';

  colonnes: Colonne[] = [
    { statut: 'A_FAIRE', titre: 'À faire', taches: [] },
    { statut: 'EN_COURS', titre: 'En cours', taches: [] },
    { statut: 'TERMINEE', titre: 'Terminée', taches: [] }
  ];

  get idsColonnes(): string[] {
    return this.colonnes.map(c => c.statut);
  }

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.tacheService.getAll().subscribe({
      next: taches => {
        for (const col of this.colonnes) col.taches = taches.filter(t => t.statut === col.statut);
        this.chargement = false;
      },
      error: err => { this.erreur = errorMessage(err); this.chargement = false; }
    });
  }

  /** Admin : toutes les cartes deplacables. Collaborateur : seulement les siennes. */
  peutDeplacer(t: Tache): boolean {
    return this.auth.isAdmin() || t.userId === this.auth.currentUser()?.id;
  }

  badgePriorite(p: Priorite): string {
    return { BASSE: 'badge-soft-info', MOYENNE: 'badge-soft-warning', HAUTE: 'badge-soft-danger' }[p];
  }

  drop(event: CdkDragDrop<Tache[]>, nouveauStatut: Statut): void {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      return;
    }

    const tache = event.previousContainer.data[event.previousIndex];
    const ancienStatut = tache.statut;
    const colonneOrigine = event.previousContainer.data;
    transferArrayItem(event.previousContainer.data, event.container.data, event.previousIndex, event.currentIndex);
    tache.statut = nouveauStatut;

    this.tacheService.changerStatut(tache.id, nouveauStatut).subscribe({
      next: () => this.message = `« ${tache.titre} » déplacée vers « ${this.libelleColonne(nouveauStatut)} ».`,
      error: err => {
        // Annule le deplacement visuel si l'API refuse (ex. 403)
        this.erreur = errorMessage(err);
        const indexActuel = event.container.data.indexOf(tache);
        transferArrayItem(event.container.data, colonneOrigine, indexActuel, event.previousIndex);
        tache.statut = ancienStatut;
      }
    });
  }

  private libelleColonne(s: Statut): string {
    return this.colonnes.find(c => c.statut === s)?.titre ?? s;
  }
}
