import { Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { ProjetService } from '../../core/services/projet.service';
import { TacheService } from '../../core/services/tache.service';
import { UserService } from '../../core/services/user.service';
import { FileUploadComponent } from '../../shared/file-upload/file-upload.component';
import { CommentairesComponent } from '../../shared/commentaires/commentaires.component';
import { ExportService } from '../../core/services/export.service';
import {
  PRIORITE_LABELS, Priorite, Projet, STATUT_LABELS, Statut, Tache, TacheRequest, User, errorMessage
} from '../../core/models';

@Component({
  selector: 'app-taches',
  imports: [ReactiveFormsModule, DatePipe, FileUploadComponent, CommentairesComponent],
  templateUrl: './taches.component.html'
})
export class TachesComponent implements OnInit {
  auth = inject(AuthService);
  private fb = inject(FormBuilder);
  private tacheService = inject(TacheService);
  private projetService = inject(ProjetService);
  private userService = inject(UserService);
  private exportService = inject(ExportService);

  statuts: Statut[] = ['A_FAIRE', 'EN_COURS', 'TERMINEE'];
  priorites: Priorite[] = ['BASSE', 'MOYENNE', 'HAUTE'];
  statutLabels = STATUT_LABELS;
  prioriteLabels = PRIORITE_LABELS;

  taches: Tache[] = [];
  projets: Projet[] = [];
  users: User[] = [];
  chargement = true;
  erreur = '';
  message = '';

  // Filtres (recherche serveur, GET /api/taches/recherche)
  filtreQ = '';
  filtreProjet: number | null = null;
  filtreStatut: Statut | '' = '';
  filtrePriorite: Priorite | '' = '';
  filtreDateDebut = '';
  filtreDateFin = '';
  mesTachesSeulement = false;
  private debounceId: ReturnType<typeof setTimeout> | null = null;

  // Pagination
  pageCourante = 0;
  taillePage = 10;
  totalElements = 0;
  totalPages = 0;

  formOuvert = false;
  enEdition: Tache | null = null;
  erreurForm = '';
  form = this.fb.group({
    titre: ['', Validators.required],
    projetId: [null as number | null, Validators.required],
    userId: [null as number | null],
    priorite: ['MOYENNE' as Priorite, Validators.required],
    statut: ['A_FAIRE' as Statut, Validators.required],
    dateEcheance: [''],
    description: ['']
  });

  detail: Tache | null = null;

  ngOnInit(): void {
    this.projetService.getAll().subscribe({ next: p => this.projets = p, error: err => this.erreur = errorMessage(err) });
    this.userService.getAssignables().subscribe({ next: u => this.users = u });
    this.charger();
  }

  /** Reinitialise la pagination puis recharge (appele quand un filtre change). */
  appliquerFiltres(): void {
    this.pageCourante = 0;
    this.charger();
  }

  /** Recherche texte : petite temporisation pour eviter une requete a chaque frappe. */
  onRechercheTexte(valeur: string): void {
    this.filtreQ = valeur;
    if (this.debounceId) clearTimeout(this.debounceId);
    this.debounceId = setTimeout(() => this.appliquerFiltres(), 300);
  }

  charger(): void {
    this.chargement = true;
    const moi = this.auth.currentUser()?.id;
    this.tacheService.rechercher({
      q: this.filtreQ || undefined,
      statut: this.filtreStatut || undefined,
      priorite: this.filtrePriorite || undefined,
      projetId: this.filtreProjet ?? undefined,
      userId: this.mesTachesSeulement ? moi : undefined,
      dateDebut: this.filtreDateDebut || undefined,
      dateFin: this.filtreDateFin || undefined,
      page: this.pageCourante,
      taille: this.taillePage
    }).subscribe({
      next: p => {
        this.taches = p.content;
        this.totalElements = p.totalElements;
        this.totalPages = p.totalPages;
        this.chargement = false;
      },
      error: err => { this.erreur = errorMessage(err); this.chargement = false; }
    });
  }

  pagePrecedente(): void {
    if (this.pageCourante > 0) { this.pageCourante--; this.charger(); }
  }
  pageSuivante(): void {
    if (this.pageCourante + 1 < this.totalPages) { this.pageCourante++; this.charger(); }
  }

  /** Admin : toutes les taches ; collaborateur : uniquement les siennes */
  peutChangerStatut(t: Tache): boolean {
    return this.auth.isAdmin() || t.userId === this.auth.currentUser()?.id;
  }

  changerStatut(t: Tache, statut: string): void {
    this.tacheService.changerStatut(t.id, statut as Statut).subscribe({
      next: maj => { t.statut = maj.statut; this.message = `Statut de « ${t.titre} » : ${this.statutLabels[maj.statut]}.`; },
      error: err => { this.erreur = errorMessage(err); this.charger(); }
    });
  }

  badgeStatut(s: Statut): string {
    return { A_FAIRE: 'badge-soft-muted', EN_COURS: 'badge-soft-primary', TERMINEE: 'badge-soft-success' }[s];
  }

  badgePriorite(p: Priorite): string {
    return { BASSE: 'badge-soft-info', MOYENNE: 'badge-soft-warning', HAUTE: 'badge-soft-danger' }[p];
  }

  enRetard(t: Tache): boolean {
    return !!t.dateEcheance && t.statut !== 'TERMINEE' && new Date(t.dateEcheance) < new Date(new Date().toDateString());
  }

  invalide(champ: string): boolean {
    const c = this.form.get(champ);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  exporter(format: 'xlsx' | 'pdf'): void {
    this.exportService.exporterTaches(format).subscribe({
      next: blob => this.exportService.telecharger(blob, `taches.${format}`),
      error: err => this.erreur = errorMessage(err)
    });
  }

  ouvrirAjout(): void {
    this.enEdition = null;
    this.form.reset({ titre: '', projetId: this.filtreProjet, userId: this.auth.currentUser()?.id ?? null,
                      priorite: 'MOYENNE', statut: 'A_FAIRE', dateEcheance: '', description: '' });
    this.erreurForm = '';
    this.formOuvert = true;
  }

  ouvrirEdition(t: Tache): void {
    this.enEdition = t;
    this.form.reset({ titre: t.titre, projetId: t.projetId, userId: t.userId ?? null, priorite: t.priorite,
                      statut: t.statut, dateEcheance: t.dateEcheance ?? '', description: t.description ?? '' });
    this.erreurForm = '';
    this.formOuvert = true;
  }

  enregistrer(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    const data: TacheRequest = {
      titre: v.titre!, description: v.description ?? '', projetId: Number(v.projetId),
      userId: v.userId ? Number(v.userId) : null, priorite: v.priorite!, statut: v.statut!,
      dateEcheance: v.dateEcheance || null
    };
    const req = this.enEdition ? this.tacheService.update(this.enEdition.id, data) : this.tacheService.create(data);
    req.subscribe({
      next: () => {
        this.message = this.enEdition ? 'Tâche modifiée.' : 'Tâche ajoutée.';
        this.formOuvert = false;
        this.charger();
      },
      error: err => this.erreurForm = errorMessage(err)
    });
  }

  supprimer(t: Tache): void {
    if (!confirm(`Supprimer la tâche « ${t.titre} » ?`)) return;
    this.tacheService.delete(t.id).subscribe({
      next: () => { this.message = 'Tâche supprimée.'; this.charger(); },
      error: err => this.erreur = errorMessage(err)
    });
  }
}
