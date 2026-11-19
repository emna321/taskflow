import { Component, OnInit, inject } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { ClientService } from '../../core/services/client.service';
import { ProjetService } from '../../core/services/projet.service';
import { TacheService } from '../../core/services/tache.service';
import { FileUploadComponent } from '../../shared/file-upload/file-upload.component';
import { ExportService } from '../../core/services/export.service';
import { Client, Projet, ProjetRequest, STATUT_LABELS, Tache, errorMessage } from '../../core/models';

@Component({
  selector: 'app-projets',
  imports: [ReactiveFormsModule, DecimalPipe, FileUploadComponent],
  templateUrl: './projets.component.html'
})
export class ProjetsComponent implements OnInit {
  auth = inject(AuthService);
  private fb = inject(FormBuilder);
  private projetService = inject(ProjetService);
  private clientService = inject(ClientService);
  private tacheService = inject(TacheService);
  private exportService = inject(ExportService);

  statutLabels = STATUT_LABELS;
  projets: Projet[] = [];
  clients: Client[] = [];
  filtreClient: number | null = null;
  filtreQ = '';
  chargement = true;
  erreur = '';
  message = '';
  private debounceId: ReturnType<typeof setTimeout> | null = null;

  // Pagination
  pageCourante = 0;
  taillePage = 10;
  totalElements = 0;
  totalPages = 0;

  formOuvert = false;
  enEdition: Projet | null = null;
  erreurForm = '';
  form = this.fb.group({
    libelle: ['', Validators.required],
    clientId: [null as number | null, Validators.required],
    budget: [null as number | null, Validators.min(0)],
    duree: [null as number | null, Validators.min(1)],
    description: ['']
  });

  detail: Projet | null = null;
  tachesDuProjet: Tache[] = [];

  ngOnInit(): void {
    this.clientService.getAll().subscribe({ next: c => this.clients = c, error: err => this.erreur = errorMessage(err) });
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.projetService.rechercher(this.filtreQ, this.filtreClient, 'libelle', this.pageCourante, this.taillePage).subscribe({
      next: p => {
        this.projets = p.content;
        this.totalElements = p.totalElements;
        this.totalPages = p.totalPages;
        this.chargement = false;
      },
      error: err => { this.erreur = errorMessage(err); this.chargement = false; }
    });
  }

  appliquerFiltres(): void {
    this.pageCourante = 0;
    this.charger();
  }

  changerFiltre(valeur: string): void {
    this.filtreClient = valeur ? Number(valeur) : null;
    this.appliquerFiltres();
  }

  onRechercheTexte(valeur: string): void {
    this.filtreQ = valeur;
    if (this.debounceId) clearTimeout(this.debounceId);
    this.debounceId = setTimeout(() => this.appliquerFiltres(), 300);
  }

  pagePrecedente(): void {
    if (this.pageCourante > 0) { this.pageCourante--; this.charger(); }
  }
  pageSuivante(): void {
    if (this.pageCourante + 1 < this.totalPages) { this.pageCourante++; this.charger(); }
  }

  invalide(champ: string): boolean {
    const c = this.form.get(champ);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  ouvrirAjout(): void {
    this.enEdition = null;
    this.form.reset({ libelle: '', clientId: this.filtreClient, budget: null, duree: null, description: '' });
    this.erreurForm = '';
    this.formOuvert = true;
  }

  ouvrirEdition(p: Projet): void {
    this.enEdition = p;
    this.form.reset({ libelle: p.libelle, clientId: p.clientId, budget: p.budget ?? null,
                      duree: p.duree ?? null, description: p.description ?? '' });
    this.erreurForm = '';
    this.formOuvert = true;
  }

  enregistrer(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    const data: ProjetRequest = {
      libelle: v.libelle!, clientId: Number(v.clientId), budget: v.budget ?? undefined,
      duree: v.duree ?? undefined, description: v.description ?? ''
    };
    const req = this.enEdition ? this.projetService.update(this.enEdition.id, data) : this.projetService.create(data);
    req.subscribe({
      next: () => {
        this.message = this.enEdition ? 'Projet modifié.' : 'Projet ajouté.';
        this.formOuvert = false;
        this.charger();
      },
      error: err => this.erreurForm = errorMessage(err)
    });
  }

  supprimer(p: Projet): void {
    if (!confirm(`Supprimer le projet « ${p.libelle} » et toutes ses tâches ?`)) return;
    this.projetService.delete(p.id).subscribe({
      next: () => { this.message = 'Projet supprimé.'; this.charger(); },
      error: err => this.erreur = errorMessage(err)
    });
  }

  consulter(p: Projet): void {
    this.detail = p;
    this.tachesDuProjet = [];
    this.tacheService.getAll({ projetId: p.id }).subscribe(t => this.tachesDuProjet = t);
  }

  exporter(format: 'xlsx' | 'pdf'): void {
    this.exportService.exporterProjets(format).subscribe({
      next: blob => this.exportService.telecharger(blob, `projets.${format}`),
      error: err => this.erreur = errorMessage(err)
    });
  }

  exporterFiche(p: Projet): void {
    this.exportService.exporterFicheProjet(p.id).subscribe({
      next: blob => this.exportService.telecharger(blob, `projet-${p.id}.pdf`),
      error: err => this.erreur = errorMessage(err)
    });
  }
}
