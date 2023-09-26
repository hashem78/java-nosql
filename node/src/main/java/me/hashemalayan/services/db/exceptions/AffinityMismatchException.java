package me.hashemalayan.services.db.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AffinityMismatchException extends RuntimeException {
    private final int expectedAffinity;
}
