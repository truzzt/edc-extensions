package de.fraunhofer.iais.eis.util;


import de.fraunhofer.iais.eis.spi.BeanSerializer;
import de.fraunhofer.iais.eis.spi.BeanValidator;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.UUID;

public class VocabUtil {
    private final ServiceLoader<BeanSerializer> serializerLoader = ServiceLoader.load(BeanSerializer.class);
    private final ServiceLoader<BeanValidator> validatorLoader = ServiceLoader.load(BeanValidator.class);
    private static final String PROTOCOL = "https";
    private static final String HOST = "w3id.org";
    public static String randomUrlBase;

    public VocabUtil() {
    }

    /** @deprecated */
    @Deprecated
    public static VocabUtil getInstance() {
        return new VocabUtil();
    }

    public URI createRandomUrl(String path) {
        try {
            if (randomUrlBase != null) {
                if (!randomUrlBase.endsWith("/")) {
                    randomUrlBase = randomUrlBase + "/";
                }

                return (new URL(randomUrlBase + path + "/" + UUID.randomUUID())).toURI();
            } else {
                return (new URL("https", "w3id.org", "/idsa/autogen/" + path + "/" + UUID.randomUUID())).toURI();
            }
        } catch (URISyntaxException | MalformedURLException var3) {
            throw new RuntimeException(var3);
        }
    }

    public <T> void validate(T objToValidate) throws ConstraintViolationException {
        Iterator var2 = this.validatorLoader.iterator();

        while(var2.hasNext()) {
            BeanValidator beanValidator = (BeanValidator)var2.next();
            beanValidator.validate(objToValidate);
        }

    }

    public String toRdf(Object obj) {
        Iterator<BeanSerializer> iterator = this.serializerLoader.iterator();
        return iterator.hasNext() ? ((BeanSerializer)iterator.next()).toRdf(obj) : "";
    }

    public <T> T fromRdf(String rdf, Class<T> valueType) {
        Iterator<BeanSerializer> iterator = this.serializerLoader.iterator();
        return iterator.hasNext() ? ((BeanSerializer)iterator.next()).fromRdf(rdf, valueType) : null;
    }

    public <T> T getByString(T[] values, String label) {
        Object[] var3 = values;
        int var4 = values.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            T value = (T) var3[var5];
            if (value.toString().equals(label)) {
                return value;
            }
        }

        return null;
    }
}
