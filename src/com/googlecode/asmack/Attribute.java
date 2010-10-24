package com.googlecode.asmack;

/**
 * A XML tag attribute, a pair of {name, namespace, value}.
 */
public class Attribute {

    /**
     * The name of the attribute.
     */
    private final String name;

    /**
     * The namespace of the attribute.
     */
    private final String namespace;

    /**
     * The value of the attribute. 
     */
    private final String value;

    /**
     * Create a new attribute instance by name, namespace, value.
     * The null namespace will be transformed into the empty namespace.
     * @param name The attribute name.
     * @param namespace The attribute namespace.
     * @param value The attribute value.
     */
    public Attribute(String name, String namespace, String value) {
        this.name = name;
        this.namespace = namespace == null ? "" : namespace;
        this.value = value;
    }

    /**
     * Retrieve the attribute name.
     * @return The attribute name.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieve the attribute namespace.
     * @return The attribute namespace.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Retrieve the attribute value.
     * @return The attribute value.
     */
    public String getValue() {
        return value;
    }

}
