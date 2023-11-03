package de.fraunhofer.iais.eis.util;


import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName("RdfResource")
public class RdfResource implements Serializable {
    @JsonProperty("@value")
    @JsonAlias({"@value", "value"})
    String value = null;
    @JsonProperty("@type")
    @JsonAlias({"@type", "type"})
    String type = null;
    @JsonIgnore
    ArrayList<Map<URI, RdfResource>> properties;

    public RdfResource() {
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.value, this.type, this.properties});
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            RdfResource other = (RdfResource)obj;
            return Objects.equals(this.value, other.value) && Objects.equals(this.type, other.type) && Objects.equals(this.properties, other.properties);
        }
    }

    public RdfResource(String valueAndType) {
        if (valueAndType.contains("^^")) {
            String[] splitString = valueAndType.split("\\^\\^");
            this.value = splitString[0].replace("\"", "");
            this.type = splitString[1];
        } else {
            this.value = valueAndType;
            this.type = "http://www.w3.org/2001/XMLSchema#string";
        }

    }

    public RdfResource(String value, URI type) {
        this.value = value;
        this.type = type.toString();
    }

    @JsonProperty("@value")
    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @JsonProperty("@type")
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
