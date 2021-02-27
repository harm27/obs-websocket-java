package nl.harm27.obs.websocket.generator.generators.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.helger.jcodemodel.*;
import nl.harm27.obs.websocket.generator.datamodel.shared.ConvertedProperty;
import nl.harm27.obs.websocket.generator.generators.generic.FunctionType;
import nl.harm27.obs.websocket.generator.generators.generic.GenericBaseGenerator;
import nl.harm27.obs.websocket.generator.generators.generic.TypeManager;
import nl.harm27.obs.websocket.generator.generators.generic.UnknownTypeException;

import java.time.Duration;
import java.util.List;

import static nl.harm27.obs.websocket.generator.generators.generic.StringConstants.*;
import static nl.harm27.obs.websocket.generator.generators.generic.StringUtil.generateEnumValue;
import static nl.harm27.obs.websocket.generator.generators.generic.StringUtil.generateFieldMethodName;

public class EventsBaseGenerator extends GenericBaseGenerator {
    private final JPackage basePackageModel;
    private final JPackage listenerPackageModel;
    private final List<String> eventNames;
    private JDefinedClass eventTypeEnum;
    private JDefinedClass eventListenerClass;
    private JDefinedClass baseEventClass;

    public EventsBaseGenerator(JPackage basePackageModel, JPackage listenerPackageModel, TypeManager typeManager, List<String> eventNames) {
        super(typeManager);
        this.basePackageModel = basePackageModel;
        this.listenerPackageModel = listenerPackageModel;
        this.eventNames = eventNames;
    }

    public void generate() throws JCodeModelException, UnknownTypeException {
        eventTypeEnum = generateEnum(basePackageModel, "EventType", eventNames, BASE_EVENT_TYPE_JAVADOC);
        typeManager.addApiType(eventTypeEnum.name(), eventTypeEnum);
        generateBaseEvent();
        generateEventListener();
    }

    private void generateEventListener() throws JCodeModelException {
        eventListenerClass = listenerPackageModel._interface("EventListener");

        JMethod callEventMethod = eventListenerClass.method(JMod.NONE, typeManager.getVoidType(), "callEvent");
        callEventMethod.param(baseEventClass, "baseEvent");

        eventListenerClass.method(JMod.NONE, typeManager.getEnumClassMap(eventTypeEnum), "getSupportedEvents");
    }

    private void generateBaseEvent() throws JCodeModelException, UnknownTypeException {
        baseEventClass = basePackageModel._class(JMod.ABSTRACT | JMod.PUBLIC, "BaseEvent");
        generateJavadocForClass(baseEventClass.javadoc(), BASE_EVENT_JAVADOC, "Events");

        generateField(baseEventClass, new ConvertedProperty("update-type", eventTypeEnum.name(), BASE_EVENT_TYPE_JAVADOC), FunctionType.GETTER);
        generateTimecode("streamTimecode", "stream-timecode", BASE_EVENT_STREAM_JAVADOC);
        generateTimecode("recordingTimecode", "rec-timecode", BASE_EVENT_RECORDING_JAVADOC);
    }

    private void generateTimecode(String className, String path, String description) {
        JFieldVar timecode = baseEventClass.field(JMod.PRIVATE, String.class, className);
        timecode.annotate(JsonProperty.class).param(path);

        JMethod stringMethod = baseEventClass.method(JMod.PUBLIC, typeManager.getOptionalForType(String.class), generateFieldMethodName(className, "get"));
        stringMethod.body()._return(typeManager.getOptionalReturnForField(timecode));
        stringMethod.javadoc().add(description);

        String durationClassName = className.replace("Timecode", "Duration");
        JMethod durationMethod = baseEventClass.method(JMod.PUBLIC, typeManager.getOptionalForType(Duration.class), generateFieldMethodName(durationClassName, "get"));
        durationMethod.javadoc().add(description.replace("string", "duration"));

        JBlock body = durationMethod.body();
        body._if(timecode.eqNull())._then()._return(typeManager.getEmptyOptional());
        body._return(typeManager.getParseDuration(timecode));
    }

    public JDefinedClass getEventTypeEnum() {
        return eventTypeEnum;
    }

    public JDefinedClass getEventListenerClass() {
        return eventListenerClass;
    }

    public JDefinedClass getBaseEventClass() {
        return baseEventClass;
    }

    public JEnumConstant getEnumValue(String name) {
        return eventTypeEnum.enumConstant(generateEnumValue(name));
    }
}
