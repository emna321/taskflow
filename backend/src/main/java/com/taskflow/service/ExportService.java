package com.taskflow.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.taskflow.entity.Projet;
import com.taskflow.entity.Tache;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.ProjetRepository;
import com.taskflow.repository.TacheRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/** Export des taches et des projets en Excel (Apache POI) et PDF (OpenPDF). */
@Service
public class ExportService {

    private static final String[] COLONNES_TACHES = {"Titre", "Projet", "Client", "Affectée à", "Priorité", "Statut", "Échéance"};
    private static final String[] COLONNES_PROJETS = {"Libellé", "Client", "Budget (DT)", "Durée (jours)", "Nb tâches"};

    private final TacheRepository tacheRepository;
    private final ProjetRepository projetRepository;

    public ExportService(TacheRepository tacheRepository, ProjetRepository projetRepository) {
        this.tacheRepository = tacheRepository;
        this.projetRepository = projetRepository;
    }

    @Transactional(readOnly = true)
    public byte[] exporterTachesExcel() {
        return genererExcel("Tâches", COLONNES_TACHES, tacheRepository.findAll(), ExportService::ligneTache);
    }

    @Transactional(readOnly = true)
    public byte[] exporterTachesPdf() {
        return genererPdf("Liste des tâches", COLONNES_TACHES, tacheRepository.findAll(), ExportService::ligneTache);
    }

    @Transactional(readOnly = true)
    public byte[] exporterProjetsExcel() {
        return genererExcel("Projets", COLONNES_PROJETS, projetRepository.findAll(), ExportService::ligneProjet);
    }

    @Transactional(readOnly = true)
    public byte[] exporterProjetsPdf() {
        return genererPdf("Liste des projets", COLONNES_PROJETS, projetRepository.findAll(), ExportService::ligneProjet);
    }

    /** Fiche d'un projet (PDF) : informations generales + liste de ses taches. */
    @Transactional(readOnly = true)
    public byte[] exporterFicheProjetPdf(Long projetId) {
        Projet p = projetRepository.findById(projetId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable (id=" + projetId + ")"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph(p.getLibelle(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            doc.add(new Paragraph(" "));
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);
            doc.add(new Paragraph("Client : " + p.getClient().getNom(), normal));
            doc.add(new Paragraph("Budget : " + (p.getBudget() != null ? p.getBudget() + " DT" : "-"), normal));
            doc.add(new Paragraph("Durée : " + (p.getDuree() != null ? p.getDuree() + " jours" : "-"), normal));
            doc.add(new Paragraph("Description : " + (p.getDescription() != null ? p.getDescription() : "-"), normal));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Tâches (" + p.getTaches().size() + ")", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13)));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            ajouterEntetes(table, new String[]{"Titre", "Statut", "Priorité", "Affectée à"});
            for (Tache t : p.getTaches()) {
                table.addCell(t.getTitre());
                table.addCell(t.getStatut().name());
                table.addCell(t.getPriorite().name());
                table.addCell(t.getUser() != null ? t.getUser().getPrenom() + " " + t.getUser().getNom() : "-");
            }
            doc.add(table);
        } catch (DocumentException e) {
            throw new IllegalStateException("Erreur lors de la generation du PDF", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    // ----- Generation generique -----

    private <T> byte[] genererExcel(String nomFeuille, String[] colonnes, List<T> lignes, LigneFournisseur<T> fournisseur) {
        try (Workbook classeur = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet feuille = classeur.createSheet(nomFeuille);
            Row entete = feuille.createRow(0);
            for (int i = 0; i < colonnes.length; i++) {
                entete.createCell(i).setCellValue(colonnes[i]);
            }
            int numeroLigne = 1;
            for (T ligne : lignes) {
                Row row = feuille.createRow(numeroLigne++);
                String[] valeurs = fournisseur.valeurs(ligne);
                for (int i = 0; i < valeurs.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(valeurs[i]);
                }
            }
            for (int i = 0; i < colonnes.length; i++) {
                feuille.autoSizeColumn(i);
            }
            classeur.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Erreur lors de la generation du fichier Excel", e);
        }
    }

    private <T> byte[] genererPdf(String titre, String[] colonnes, List<T> lignes, LigneFournisseur<T> fournisseur) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph(titre + " - TaskFlow", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            doc.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(colonnes.length);
            table.setWidthPercentage(100);
            ajouterEntetes(table, colonnes);
            for (T ligne : lignes) {
                for (String valeur : fournisseur.valeurs(ligne)) {
                    table.addCell(valeur);
                }
            }
            doc.add(table);
        } catch (DocumentException e) {
            throw new IllegalStateException("Erreur lors de la generation du PDF", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    private void ajouterEntetes(PdfPTable table, String[] colonnes) {
        Font gras = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        for (String colonne : colonnes) {
            table.addCell(new PdfPCell(new Phrase(colonne, gras)));
        }
    }

    private static String[] ligneTache(Tache t) {
        return new String[]{
                t.getTitre(),
                t.getProjet().getLibelle(),
                t.getProjet().getClient().getNom(),
                t.getUser() != null ? t.getUser().getPrenom() + " " + t.getUser().getNom() : "-",
                t.getPriorite().name(),
                t.getStatut().name(),
                t.getDateEcheance() != null ? t.getDateEcheance().toString() : "-"
        };
    }

    private static String[] ligneProjet(Projet p) {
        return new String[]{
                p.getLibelle(),
                p.getClient().getNom(),
                p.getBudget() != null ? p.getBudget().toString() : "-",
                p.getDuree() != null ? p.getDuree().toString() : "-",
                String.valueOf(p.getTaches().size())
        };
    }

    @FunctionalInterface
    private interface LigneFournisseur<T> {
        String[] valeurs(T t);
    }
}
