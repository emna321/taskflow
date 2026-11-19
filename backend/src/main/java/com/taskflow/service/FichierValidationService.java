package com.taskflow.service;

import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.FileTooLargeException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * Validation des fichiers envoyes : taille, extension + type MIME, et signature binaire
 * (premiers octets) pour les images et les PDF. Nettoie aussi le nom de fichier.
 */
@Service
public class FichierValidationService {

    public static final long TAILLE_MAX_PIECE_JOINTE = 5L * 1024 * 1024; // 5 Mo
    public static final long TAILLE_MAX_IMAGE = 1L * 1024 * 1024; // 1 Mo

    private static final Set<String> EXTENSIONS_IMAGE = Set.of("png", "jpg", "jpeg", "webp");

    private static final Map<String, Set<String>> MIME_PAR_EXTENSION = Map.ofEntries(
            Map.entry("pdf", Set.of("application/pdf")),
            Map.entry("doc", Set.of("application/msword")),
            Map.entry("docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/zip", "application/octet-stream")),
            Map.entry("xls", Set.of("application/vnd.ms-excel")),
            Map.entry("xlsx", Set.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/zip", "application/octet-stream")),
            Map.entry("ppt", Set.of("application/vnd.ms-powerpoint")),
            Map.entry("pptx", Set.of("application/vnd.openxmlformats-officedocument.presentationml.presentation", "application/zip", "application/octet-stream")),
            Map.entry("txt", Set.of("text/plain")),
            Map.entry("csv", Set.of("text/csv", "application/vnd.ms-excel", "text/plain")),
            Map.entry("png", Set.of("image/png")),
            Map.entry("jpg", Set.of("image/jpeg")),
            Map.entry("jpeg", Set.of("image/jpeg")),
            Map.entry("webp", Set.of("image/webp")),
            Map.entry("zip", Set.of("application/zip", "application/x-zip-compressed", "application/octet-stream")));

    /** Valide une piece jointe generique (taches/projets) : types autorises, 5 Mo max. */
    public String validerPieceJointe(MultipartFile file) {
        return valider(file, TAILLE_MAX_PIECE_JOINTE, MIME_PAR_EXTENSION.keySet());
    }

    /** Valide une image (logo client / photo de profil) : png/jpg/jpeg/webp uniquement, 1 Mo max. */
    public String validerImage(MultipartFile file) {
        return valider(file, TAILLE_MAX_IMAGE, EXTENSIONS_IMAGE);
    }

    private String valider(MultipartFile file, long tailleMaxOctets, Set<String> extensionsAutorisees) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Le fichier est vide");
        }
        if (file.getSize() > tailleMaxOctets) {
            throw new FileTooLargeException(
                    "Le fichier depasse la taille maximale autorisee (" + (tailleMaxOctets / (1024 * 1024)) + " Mo)");
        }
        String nomNettoye = nettoyerNomFichier(file.getOriginalFilename());
        String extension = extension(nomNettoye);
        if (extension.isBlank() || !extensionsAutorisees.contains(extension)) {
            throw new BadRequestException("Type de fichier non autorise : ." + extension);
        }
        Set<String> mimesAttendus = MIME_PAR_EXTENSION.get(extension);
        String mimeEnvoye = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (mimesAttendus == null || !mimesAttendus.contains(mimeEnvoye)) {
            throw new BadRequestException("Le type du fichier ne correspond pas a son extension (." + extension + ")");
        }
        if (EXTENSIONS_IMAGE.contains(extension) || extension.equals("pdf")) {
            verifierSignature(file, extension);
        }
        return nomNettoye;
    }

    /** Supprime tout chemin, les ".." et les caracteres speciaux ; limite la longueur. */
    public String nettoyerNomFichier(String nomOriginal) {
        if (nomOriginal == null || nomOriginal.isBlank()) {
            return "fichier";
        }
        String nom = nomOriginal.replace('\\', '/');
        nom = nom.substring(nom.lastIndexOf('/') + 1);
        nom = nom.replace("..", "");
        nom = nom.replaceAll("[^A-Za-z0-9._-]", "_");
        if (nom.isBlank() || nom.equals(".") ) {
            nom = "fichier";
        }
        if (nom.length() > 150) {
            int dot = nom.lastIndexOf('.');
            String ext = dot > 0 ? nom.substring(dot) : "";
            String base = dot > 0 ? nom.substring(0, dot) : nom;
            int maxBase = Math.max(1, 150 - ext.length());
            nom = base.substring(0, Math.min(base.length(), maxBase)) + ext;
        }
        return nom;
    }

    private String extension(String nom) {
        int dot = nom.lastIndexOf('.');
        return (dot < 0 || dot == nom.length() - 1) ? "" : nom.substring(dot + 1).toLowerCase();
    }

    private void verifierSignature(MultipartFile file, String extension) {
        byte[] entete;
        try (InputStream in = file.getInputStream()) {
            entete = in.readNBytes(12);
        } catch (IOException e) {
            throw new BadRequestException("Impossible de lire le fichier envoye");
        }
        boolean valide = switch (extension) {
            case "png" -> correspond(entete, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "jpg", "jpeg" -> correspond(entete, 0xFF, 0xD8, 0xFF);
            case "webp" -> correspond(entete, 0x52, 0x49, 0x46, 0x46)
                    && entete.length >= 12 && correspond(Arrays.copyOfRange(entete, 8, 12), 0x57, 0x45, 0x42, 0x50);
            case "pdf" -> correspond(entete, 0x25, 0x50, 0x44, 0x46);
            default -> true;
        };
        if (!valide) {
            throw new BadRequestException("Le contenu du fichier ne correspond pas au type annonce (." + extension + ")");
        }
    }

    private boolean correspond(byte[] entete, int... signature) {
        if (entete.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((entete[i] & 0xFF) != signature[i]) {
                return false;
            }
        }
        return true;
    }
}
