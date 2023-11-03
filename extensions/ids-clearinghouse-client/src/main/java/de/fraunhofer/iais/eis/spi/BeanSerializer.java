package de.fraunhofer.iais.eis.spi;


public interface BeanSerializer {
    String toRdf(Object var1);

    <T> T fromRdf(String var1, Class<T> var2);
}
