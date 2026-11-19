package com.taskflow.exception;

/** Fichier trop volumineux (depassement de la limite de taille propre a l'endpoint). Renvoie 413. */
public class FileTooLargeException extends RuntimeException {
    public FileTooLargeException(String message) {
        super(message);
    }
}
