package com.taskflow.service;

import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.FileTooLargeException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FichierValidationServiceTest {

    private final FichierValidationService service = new FichierValidationService();

    private static final byte[] PDF_SIGNATURE = {0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34};
    private static final byte[] PNG_SIGNATURE =
            {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x00};

    @Test
    void validerPieceJointe_fichierValide_renvoieNomNettoye() {
        MockMultipartFile fichier = new MockMultipartFile("fichier", "rapport.pdf", "application/pdf", PDF_SIGNATURE);

        String nom = service.validerPieceJointe(fichier);

        assertThat(nom).isEqualTo("rapport.pdf");
    }

    @Test
    void validerPieceJointe_fichierVide_refuse() {
        MockMultipartFile vide = new MockMultipartFile("fichier", "rapport.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> service.validerPieceJointe(vide)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void validerPieceJointe_tropVolumineux_refuse413() {
        byte[] contenu = new byte[(int) FichierValidationService.TAILLE_MAX_PIECE_JOINTE + 1];
        MockMultipartFile gros = new MockMultipartFile("fichier", "gros.txt", "text/plain", contenu);

        assertThatThrownBy(() -> service.validerPieceJointe(gros)).isInstanceOf(FileTooLargeException.class);
    }

    @Test
    void validerPieceJointe_extensionNonAutorisee_refuse() {
        MockMultipartFile exe = new MockMultipartFile("fichier", "programme.exe", "application/octet-stream",
                "contenu".getBytes());

        assertThatThrownBy(() -> service.validerPieceJointe(exe)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void validerPieceJointe_mimeIncoherentAvecExtension_refuse() {
        MockMultipartFile incoherent = new MockMultipartFile("fichier", "rapport.pdf", "text/plain", PDF_SIGNATURE);

        assertThatThrownBy(() -> service.validerPieceJointe(incoherent)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void validerPieceJointe_signatureNeCorrespondPasAuPdf_refuse() {
        MockMultipartFile fauxPdf = new MockMultipartFile("fichier", "rapport.pdf", "application/pdf",
                "pas vraiment un pdf".getBytes());

        assertThatThrownBy(() -> service.validerPieceJointe(fauxPdf)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void validerPieceJointe_nomAvecCheminEtPointsDouble_estNettoye() {
        MockMultipartFile fichier = new MockMultipartFile("fichier", "../../etc/mot de passe.txt", "text/plain",
                "contenu".getBytes());

        String nom = service.validerPieceJointe(fichier);

        assertThat(nom).doesNotContain("..", "/", " ");
        assertThat(nom).endsWith(".txt");
    }

    @Test
    void validerImage_webpValide_accepte() {
        byte[] webp = new byte[]{0x52, 0x49, 0x46, 0x46, 0, 0, 0, 0, 0x57, 0x45, 0x42, 0x50};
        MockMultipartFile logo = new MockMultipartFile("logo", "logo.webp", "image/webp", webp);

        String nom = service.validerImage(logo);

        assertThat(nom).isEqualTo("logo.webp");
    }

    @Test
    void validerImage_pdfRefuse() {
        MockMultipartFile pdf = new MockMultipartFile("logo", "logo.pdf", "application/pdf", PDF_SIGNATURE);

        assertThatThrownBy(() -> service.validerImage(pdf)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void validerImage_tropVolumineuse_refuse413() {
        byte[] contenu = avecSignaturePng(new byte[(int) FichierValidationService.TAILLE_MAX_IMAGE + 1]);
        MockMultipartFile grosLogo = new MockMultipartFile("logo", "logo.png", "image/png", contenu);

        assertThatThrownBy(() -> service.validerImage(grosLogo)).isInstanceOf(FileTooLargeException.class);
    }

    @Test
    void validerImage_signaturePngValide_accepte() {
        MockMultipartFile logo = new MockMultipartFile("logo", "logo.png", "image/png", PNG_SIGNATURE);

        String nom = service.validerImage(logo);

        assertThat(nom).isEqualTo("logo.png");
    }

    private static byte[] avecSignaturePng(byte[] contenu) {
        System.arraycopy(PNG_SIGNATURE, 0, contenu, 0, PNG_SIGNATURE.length);
        return contenu;
    }
}
