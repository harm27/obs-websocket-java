package nl.harm27.obs.websocket.generator.generators.events;

import com.helger.jcodemodel.*;
import nl.harm27.obs.websocket.generator.datamodel.events.Event;
import nl.harm27.obs.websocket.generator.generators.generic.GenericClassGenerator;
import nl.harm27.obs.websocket.generator.generators.generic.TypeManager;
import nl.harm27.obs.websocket.generator.generators.generic.UnknownTypeException;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static nl.harm27.obs.websocket.generator.generators.generic.StringConstants.EVENT_TYPE_METHOD_JAVADOC;
import static nl.harm27.obs.websocket.generator.generators.generic.StringUtil.*;

public class EventCategoryGenerator extends GenericClassGenerator {
    private final JPackage listenerPackageModel;
    private final JPackage eventCategoryPackageModel;
    private final String category;
    private final List<Event> events;
    private final EventsBaseGenerator eventsBaseGenerator;

    public EventCategoryGenerator(JPackage listenerPackageModel, JPackage eventsPackageModel, EventsBaseGenerator eventsBaseGenerator, TypeManager typeManager, String category, List<Event> events) {
        super(typeManager);
        this.listenerPackageModel = listenerPackageModel;
        this.eventsBaseGenerator = eventsBaseGenerator;
        this.category = category;
        this.events = events;
        eventCategoryPackageModel = eventsPackageModel.subPackage(generateValidPackageName(category));
    }

    public void generate() throws JCodeModelException, UnknownTypeException {
        List<GeneratedEvent> generatedEvents = new ArrayList<>();
        for (Event event : events) {
            EventGenerator eventGenerator = new EventGenerator(eventCategoryPackageModel, eventsBaseGenerator, typeManager, event);
            generatedEvents.add(eventGenerator.generate());
        }
        generateEventListener(generatedEvents);
    }

    private void generateEventListener(List<GeneratedEvent> generatedEvents) throws JCodeModelException {
        String className = format("%sEventListener", generateValidClassName(category));
        JDefinedClass listenerClass = listenerPackageModel._class(JMod.ABSTRACT | JMod.PUBLIC, className);
        listenerClass._implements(eventsBaseGenerator.getEventListenerClass());
        listenerClass.javadoc().add(format("The EventListener for the events that are part of the %s category.", capitalizeFirstChar(category)));

        for (GeneratedEvent generatedEvent : generatedEvents)
            generateEventTypeMethod(listenerClass, generatedEvent);

        generateCallEventMethod(generatedEvents, className, listenerClass);
        generateGetSupportedEventsMethod(generatedEvents, listenerClass);
    }

    private void generateCallEventMethod(List<GeneratedEvent> generatedEvents, String className, JDefinedClass listenerClass) {
        JMethod callEventMethod = listenerClass.method(JMod.PUBLIC | JMod.FINAL, typeManager.getVoidType(), "callEvent");
        callEventMethod.annotate(Override.class);
        JVar baseEventVar = callEventMethod.param(eventsBaseGenerator.getBaseEventClass(), "baseEvent");
        JBlock body = callEventMethod.body();
        JSwitch eventTypeSwitch = body._switch(baseEventVar.invoke("getUpdateType"));
        for (GeneratedEvent generatedEvent : generatedEvents) {
            JBlock caseStatementBody = eventTypeSwitch._case(eventsBaseGenerator.getEnumValue(generatedEvent.getName())).body();
            caseStatementBody.add(JExpr.invoke(generatedEvent.getEventListenerMethod()).arg(baseEventVar.castTo(generatedEvent.getEventClass())));
            caseStatementBody._break();
        }
        eventTypeSwitch._default().body()._throw(typeManager.getException(IllegalArgumentException.class, format("Unexpected EventType for %s.", className)));
    }

    private void generateEventTypeMethod(JDefinedClass listenerClass, GeneratedEvent generatedEvent) {
        JMethod eventTypeMethod = listenerClass.method(JMod.PUBLIC, typeManager.getVoidType(), generateValidMethodName(generatedEvent.getName()));
        JVar param = eventTypeMethod.param(generatedEvent.getEventClass(), generateValidFieldName(generatedEvent.getName()));
        generateJavadocForClass(eventTypeMethod.javadoc(), generatedEvent.getDescription(), generatedEvent.getName(), generatedEvent.getSince(), generatedEvent.getDeprecated());
        eventTypeMethod.javadoc().addParam(param).add(EVENT_TYPE_METHOD_JAVADOC);
        generatedEvent.setMethod(eventTypeMethod);
    }

    private void generateGetSupportedEventsMethod(List<GeneratedEvent> generatedEvents, JDefinedClass listenerClass) {
        AbstractJClass eventAndClassMap = typeManager.getEnumClassMap(eventsBaseGenerator.getEventTypeEnum());
        JMethod getSupportedEventsMethod = listenerClass.method(JMod.PUBLIC | JMod.FINAL, eventAndClassMap, "getSupportedEvents");
        getSupportedEventsMethod.annotate(Override.class);
        JBlock body = getSupportedEventsMethod.body();
        JVar supportedEventsVar = body.decl(eventAndClassMap, "supportedEvents", typeManager.getEnumMap(eventsBaseGenerator.getEventTypeEnum()));

        for (GeneratedEvent generatedEvent : generatedEvents)
            body.add(supportedEventsVar.invoke("put").arg(eventsBaseGenerator.getEnumValue(generatedEvent.getName())).arg(generatedEvent.getDotClass()));

        body._return(supportedEventsVar);
    }
}
