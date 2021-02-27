package nl.harm27.obs.websocket.generator.datamodel.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.harm27.obs.websocket.generator.datamodel.shared.ContentDefinition;
import nl.harm27.obs.websocket.generator.datamodel.shared.ConvertedProperty;
import nl.harm27.obs.websocket.generator.datamodel.shared.Property;

import java.util.List;

public class Request extends ContentDefinition {
    @JsonProperty("params")
    private List<Property> params;

    public List<ConvertedProperty> getParams() {
        return convertProperties(params);
    }
}
