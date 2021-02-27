package nl.harm27.obs.websocket.generator.datamodel.shared;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public abstract class ContentDefinition extends Definition {
    @JsonProperty("description")
    private String description;
    @JsonProperty("api")
    private String api;
    @JsonProperty("name")
    private String name;
    @JsonProperty("category")
    private String category;
    @JsonProperty("since")
    private String since;
    @JsonProperty("deprecated")
    private String deprecated;
    @JsonProperty("returns")
    private List<Property> returns;

    public String getDescription() {
        return description;
    }

    public String getApi() {
        return api;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getSince() {
        return since;
    }

    public String getDeprecated() {
        return deprecated;
    }

    public List<ConvertedProperty> getReturns() {
        return convertProperties(returns);
    }
}
