package nl.harm27.obswebsocket.generator.datamodel.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.harm27.obswebsocket.generator.datamodel.shared.Definition;
import nl.harm27.obswebsocket.generator.datamodel.shared.Property;

import java.util.List;

public class Request extends Definition {
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
    @JsonProperty("params")
    private List<Property> params;

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

    public List<Property> getReturns() {
        return filteredProperties(returns);
    }

    public List<Property> getParams() {
        return filteredProperties(params);
    }
}