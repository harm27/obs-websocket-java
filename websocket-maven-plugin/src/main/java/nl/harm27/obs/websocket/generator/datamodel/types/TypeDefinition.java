package nl.harm27.obs.websocket.generator.datamodel.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.harm27.obs.websocket.generator.datamodel.shared.ConvertedProperty;
import nl.harm27.obs.websocket.generator.datamodel.shared.Definition;
import nl.harm27.obs.websocket.generator.datamodel.shared.Property;

import java.util.List;
import java.util.Optional;

public class TypeDefinition extends Definition {
    @JsonProperty("properties")
    private List<Property> properties;
    @JsonProperty("typedefs")
    private List<TypeMetadata> definitions;

    public List<ConvertedProperty> getProperties() {
        return convertProperties(properties);
    }

    private Optional<TypeMetadata> getTypeDefinition() {
        return definitions.stream().findFirst();
    }

    public String getName() {
        return getTypeDefinition().map(TypeMetadata::getName).orElse("");
    }

    public String getDescription() {
        return getTypeDefinition().map(TypeMetadata::getDescription).orElse("");
    }
}
