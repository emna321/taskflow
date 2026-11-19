export type RoleName = 'ADMIN' | 'COLLABORATEUR';
export type Statut = 'A_FAIRE' | 'EN_COURS' | 'TERMINEE';
export type Priorite = 'BASSE' | 'MOYENNE' | 'HAUTE';

export interface AuthResponse {
  token: string;
  id: number;
  email: string;
  nom: string;
  prenom: string;
  roles: RoleName[];
  hasPhoto?: boolean;
}

export interface LoginRequest { email: string; password: string; }

export interface RegisterRequest {
  email: string; password: string; tel?: string; nom: string; prenom: string;
}

export interface User {
  id: number; email: string; tel?: string; nom: string; prenom: string; roles: RoleName[]; hasPhoto?: boolean;
}

export interface UserRequest {
  email: string; password?: string; tel?: string; nom: string; prenom: string; roles: RoleName[];
}

export interface Client {
  id?: number; nom: string; tel?: string; email?: string; adresse?: string; nbProjets?: number; hasLogo?: boolean;
}

export interface FichierMeta {
  id: number; nomOriginal: string; typeMime: string; taille: number; dateUpload: string;
  uploadeParId: number; uploadeParNom: string;
}

export interface Commentaire {
  id: number; texte: string; dateCreation: string; auteurId: number; auteurNom: string;
}

export interface ProjetStat { projetId: number; projetLibelle: string; nbTaches: number; }
export interface CollaborateurStat { userId: number; userNom: string; nbTaches: number; }

export interface Stats {
  tachesParStatut: Record<Statut, number>;
  tachesParPriorite: Record<Priorite, number>;
  tachesParProjet: ProjetStat[];
  tachesEnRetard: number;
  chargeParCollaborateur: CollaborateurStat[];
  nbClients: number;
  nbProjets: number;
}

export interface Page<T> { content: T[]; totalElements: number; totalPages: number; page: number; taille: number; }

export interface Projet {
  id: number; libelle: string; budget?: number; duree?: number; description?: string;
  clientId: number; clientNom: string; nbTaches: number;
}

export interface ProjetRequest {
  libelle: string; budget?: number; duree?: number; description?: string; clientId: number;
}

export interface Tache {
  id: number; titre: string; description?: string; statut: Statut; priorite: Priorite;
  dateEcheance?: string; projetId: number; projetLibelle: string;
  userId?: number; userNomComplet?: string;
}

export interface TacheRequest {
  titre: string; description?: string; statut?: Statut; priorite?: Priorite;
  dateEcheance?: string | null; projetId: number; userId?: number | null;
}

export const STATUT_LABELS: Record<Statut, string> = {
  A_FAIRE: 'À faire', EN_COURS: 'En cours', TERMINEE: 'Terminée'
};

export const PRIORITE_LABELS: Record<Priorite, string> = {
  BASSE: 'Basse', MOYENNE: 'Moyenne', HAUTE: 'Haute'
};

/** Extrait un message lisible d'une erreur HTTP renvoyee par l'API. */
export function errorMessage(err: any): string {
  if (err?.status === 0) return 'Serveur injoignable. Vérifiez que le backend est démarré.';
  const body = err?.error;
  if (body?.details) return Object.values(body.details).join(' · ');
  return body?.message || 'Une erreur est survenue.';
}
