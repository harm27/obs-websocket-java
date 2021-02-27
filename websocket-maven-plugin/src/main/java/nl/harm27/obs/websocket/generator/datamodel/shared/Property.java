package nl.harm27.obs.websocket.generator.datamodel.shared;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Property {
    @JsonProperty("type")
    private String type;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;

    public String getType() {
        return type.replace(" (optional)", "").replace(" (Optional)", "").replace("Array<", "").replace(">", "");
    }

    public boolean isOptional() {
        return type.contains("(optional)") || type.contains("(Optional)");
    }

    public boolean isArray() {
        return type.contains("Array<");
    }

    public String getName() {
        return name.replace(".*.", ".");
    }

    public String getDescription() {
        return description;
    }
}
