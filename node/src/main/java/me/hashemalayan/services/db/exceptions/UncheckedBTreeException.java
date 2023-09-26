package me.hashemalayan.services.db.exceptions;

import btree4j.BTreeException;

public class UncheckedBTreeException extends RuntimeException {

    public UncheckedBTreeException(BTreeException e){
        super(e);
    }
}
