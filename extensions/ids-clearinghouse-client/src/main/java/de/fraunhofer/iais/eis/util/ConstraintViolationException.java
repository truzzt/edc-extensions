package de.fraunhofer.iais.eis.util;


import java.util.Collection;

public class ConstraintViolationException extends RuntimeException {
    private Collection<String> messages;

    public ConstraintViolationException(Collection<String> messages) {
        this.messages = messages;
    }

    public Collection<String> getMessages() {
        return this.messages;
    }

    public String toString() {
        return "ConstraintViolationException{messages=" + this.messages + '}';
    }
}
