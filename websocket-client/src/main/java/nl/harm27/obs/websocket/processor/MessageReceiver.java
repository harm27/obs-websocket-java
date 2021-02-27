package nl.harm27.obs.websocket.processor;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.harm27.obs.websocket.ListenerRegistry;
import nl.harm27.obs.websocket.api.base.BaseEvent;
import nl.harm27.obs.websocket.api.base.BaseResponse;
import nl.harm27.obs.websocket.api.base.EventType;
import nl.harm27.obs.websocket.listener.EventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageReceiver {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final ListenerRegistry listenerRegistry;
    private final Map<String, Class<?>> messageResponseTypes;
    private final Map<String, Consumer<BaseResponse>> messageCallbacks;
    private final ObjectMapper objectMapper;

    public MessageReceiver(ListenerRegistry listenerRegistry) {
        this.listenerRegistry = listenerRegistry;
        messageResponseTypes = new HashMap<>();
        messageCallbacks = new HashMap<>();
        objectMapper = new ObjectMapper().
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).
                setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE).
                setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    public void receiveMessage(String data) {
        try {
            JsonNode jsonNode = objectMapper.readTree(data);
            if (jsonNode.has("message-id"))
                handleResponse(jsonNode.get("message-id").asText(), data);
            else if (jsonNode.has("update-type"))
                handleEvent(jsonNode.get("update-type").asText(), data);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Failed to parse object", e);
        }
    }

    private void handleEvent(String eventType, String data) throws JsonProcessingException {
        EventType foundEventType = getEventTypeFromString(eventType);
        if (foundEventType == null)
            return;

        Class<?> eventClass = listenerRegistry.getClassForEvent(foundEventType);
        if (eventClass == null)
            return;

        BaseEvent baseEvent = (BaseEvent) objectMapper.readValue(data, eventClass);
        for (EventListener eventListener : listenerRegistry.getListenersForEventType(baseEvent.getUpdateType())) {
            CompletableFuture.runAsync(() -> eventListener.callEvent(baseEvent));
        }
    }

    private EventType getEventTypeFromString(String eventType) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(eventType);
        return objectMapper.readValue(json, EventType.class);
    }

    private void handleResponse(String messageId, String data) throws JsonProcessingException {
        Class<?> responseType = messageResponseTypes.remove(messageId);
        BaseResponse baseResponse = (BaseResponse) objectMapper.readValue(data, responseType);

        Consumer<BaseResponse> callbackConsumer = messageCallbacks.remove(messageId);
        CompletableFuture.runAsync(() -> callbackConsumer.accept(baseResponse));
    }

    public void addMessage(String messageId, Class<?> responseType, Consumer<BaseResponse> responseConsumer) {
        messageResponseTypes.put(messageId, responseType);
        messageCallbacks.put(messageId, responseConsumer);
    }
}
