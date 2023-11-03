package de.fraunhofer.iais.eis.spi;


import de.fraunhofer.iais.eis.util.ConstraintViolationException;

public interface BeanValidator {
    <T> void validate(T var1) throws ConstraintViolationException;
}
