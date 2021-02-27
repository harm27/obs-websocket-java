package nl.harm27.obs.websocket.generator.generators.events;

import com.helger.jcodemodel.JCodeModelException;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JPackage;
import nl.harm27.obs.websocket.generator.datamodel.events.Event;
import nl.harm27.obs.websocket.generator.datamodel.shared.ConvertedProperty;
import nl.harm27.obs.websocket.generator.generators.generic.FunctionType;
import nl.harm27.obs.websocket.generator.generators.generic.GenericClassGenerator;
import nl.harm27.obs.websocket.generator.generators.generic.TypeManager;
import nl.harm27.obs.websocket.generator.generators.generic.UnknownTypeException;

public class EventGenerator extends GenericClassGenerator {
    private final JPackage eventCategoryPackageModel;
    private final Event event;
    private final EventsBaseGenerator eventsBaseGenerator;

    public EventGenerator(JPackage eventCategoryPackageModel, EventsBaseGenerator eventsBaseGenerator, TypeManager typeManager, Event event) {
        super(typeManager);
        this.eventCategoryPackageModel = eventCategoryPackageModel;
        this.eventsBaseGenerator = eventsBaseGenerator;
        this.event = event;
    }

    public GeneratedEvent generate() throws JCodeModelException, UnknownTypeException {
        String eventName = event.getName();
        JDefinedClass eventClass = eventCategoryPackageModel._class(eventName);
        eventClass._extends(eventsBaseGenerator.getBaseEventClass());
        generateJavadocForClass(eventClass.javadoc(), event.getDescription(), eventName, event.getSince(), event.getDeprecated());

        for (ConvertedProperty property : event.getReturns()) {
            generateProperty(eventClass, property, FunctionType.GETTER);
        }

        return new GeneratedEvent(event, eventClass);
    }
}
