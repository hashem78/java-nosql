package me.hashemalayan.services.db.exceptions;

public class WrongDocumentAffinityException extends Exception {
    private final int correctAffinity;
    public WrongDocumentAffinityException(int correctAffinity) {
        this.correctAffinity = correctAffinity;
    }

    public int getCorrectAffinity() {
        return correctAffinity;
    }
}
