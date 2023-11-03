package de.fraunhofer.iais.eis;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.fraunhofer.iais.eis.util.Beta;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DynamicAttributeTokenImpl.class)
})
public interface DynamicAttributeToken extends Token {

    // standard methods

    @Beta
    public DynamicAttributeToken deepCopy();

}
