package com.taskflow.service;

import com.taskflow.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** Envoie un email a l'admin apres chaque inscription. */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String adminEmail;
    private final String from;
    private final boolean enabled;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.admin.email}") String adminEmail,
                        @Value("${spring.mail.username:}") String from,
                        @Value("${app.mail.enabled:false}") boolean enabled) {
        this.mailSender = mailSender;
        this.adminEmail = adminEmail;
        this.from = from;
        this.enabled = enabled;
    }

    @Async
    public void notifierAdminInscription(User user) {
        String sujet = "[TaskFlow] Nouvelle inscription : " + user.getPrenom() + " " + user.getNom();
        String corps = """
                Bonjour,

                Un nouvel utilisateur vient de s'inscrire sur TaskFlow :

                  Nom      : %s %s
                  Email    : %s
                  Telephone: %s
                  Role     : COLLABORATEUR

                Vous pouvez modifier ses roles depuis la page Utilisateurs.

                -- TaskFlow
                """.formatted(user.getPrenom(), user.getNom(), user.getEmail(),
                user.getTel() == null || user.getTel().isBlank() ? "-" : user.getTel());

        if (!enabled) {
            log.info("Email desactive (app.mail.enabled=false). Email qui aurait ete envoye a {} :\n{}\n{}",
                    adminEmail, sujet, corps);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (from != null && !from.isBlank()) {
                message.setFrom(from);
            }
            message.setTo(adminEmail);
            message.setSubject(sujet);
            message.setText(corps);
            mailSender.send(message);
            log.info("Email d'inscription envoye a l'admin ({})", adminEmail);
        } catch (Exception e) {
            // L'inscription ne doit pas echouer si le serveur SMTP est indisponible
            log.error("Echec de l'envoi de l'email a l'admin : {}", e.getMessage());
        }
    }
}
