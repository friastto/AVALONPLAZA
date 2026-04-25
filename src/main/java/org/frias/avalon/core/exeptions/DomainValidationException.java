package org.frias.avalon.core.exeptions;

public class DomainValidationException extends RuntimeException{

    public DomainValidationException(String message) {
        super(message);
    }

}
