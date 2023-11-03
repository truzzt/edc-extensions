package de.fraunhofer.iais.eis.util;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.net.URI;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TypedLiteral extends RdfResource implements Serializable {
    @JsonProperty("@language")
    private String language = null;

    public TypedLiteral() {
    }

    public TypedLiteral(String valueAndTypeOrLanguage) {
        String[] splitString;
        if (valueAndTypeOrLanguage.contains("@")) {
            splitString = valueAndTypeOrLanguage.split("@");
            this.value = splitString[0];
            this.language = splitString[1];
        } else if (valueAndTypeOrLanguage.contains("^^")) {
            splitString = valueAndTypeOrLanguage.split("\\^\\^");
            this.value = splitString[0].replace("\"", "");
            this.type = splitString[1];
        } else {
            this.value = valueAndTypeOrLanguage;
            this.type = "http://www.w3.org/2001/XMLSchema#string";
        }

    }

    public TypedLiteral(String value, URI type) {
        super(value, type);
    }

    public TypedLiteral(String value, String language) {
        this.value = value;
        this.language = language;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String toString() {
        String result = this.value;
        if (this.language != null && !this.language.isEmpty()) {
            return "\"" + result + "\"@" + this.language;
        } else {
            return this.type != null && !this.type.isEmpty() ? "\"" + result + "\"^^" + this.type : result;
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            TypedLiteral other = (TypedLiteral)obj;
            return Objects.equals(this.value, other.value) && Objects.equals(this.type, other.type) && Objects.equals(this.language, other.language) && Objects.equals(this.properties, other.properties);
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.value, this.type, this.language, this.properties});
    }
}
