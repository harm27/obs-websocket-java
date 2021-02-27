package nl.harm27.obs.websocket.generator.datamodel.shared;

import java.util.ArrayList;
import java.util.List;

import static nl.harm27.obs.websocket.generator.generators.generic.StringUtil.*;

public class ConvertedProperty {
    private final String name;
    private final List<ConvertedProperty> properties;
    private String type;
    private String description;
    private boolean array;
    private boolean optional;

    public ConvertedProperty(String name) {
        this(name, false, false);
    }

    public ConvertedProperty(String name, boolean array, boolean optional) {
        this.name = name;
        this.array = array;
        this.optional = optional;
        properties = new ArrayList<>();
    }

    public ConvertedProperty(ConvertedProperty property, String type) {
        this(property.getName(), property.isArray(), property.isOptional());
        setDescription(property.getDescription());
        setType(type);
    }

    public ConvertedProperty(String name, String type, String description) {
        this(name);
        setDescription(description);
        setType(type);
    }

    public String getName() {
        return name;
    }

    public String getFieldName() {
        return generateValidFieldName(getName());
    }

    public String getMethodName(String methodPrefix) {
        return generateFieldMethodName(getName(), methodPrefix);
    }

    public String getClassName() {
        return generateValidClassName(getName());
    }

    public boolean isArray() {
        return array;
    }

    public void setArray(boolean array) {
        this.array = array;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSingleLevel() {
        return properties.isEmpty();
    }

    public List<ConvertedProperty> getProperties() {
        return properties;
    }
}
