package nl.harm27.obs.websocket.generator.generators.requests;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.helger.jcodemodel.*;
import nl.harm27.obs.websocket.generator.datamodel.requests.Request;
import nl.harm27.obs.websocket.generator.datamodel.shared.ConvertedProperty;
import nl.harm27.obs.websocket.generator.generators.generic.FunctionType;
import nl.harm27.obs.websocket.generator.generators.generic.StringConstants;
import nl.harm27.obs.websocket.generator.generators.generic.TypeManager;
import nl.harm27.obs.websocket.generator.generators.generic.UnknownTypeException;

import java.util.Arrays;
import java.util.List;

public class RequestGenerator extends GenericRequestsGenerator {
    private static final List<String> NON_AUTHENTICATED_REQUESTS = Arrays.asList("authenticate", "getversion", "getauthrequired");
    private final JPackage requestCategoryPackageModel;
    private final RequestsBaseGenerator requestsBaseGenerator;
    private final Request request;

    public RequestGenerator(JPackage requestCategoryPackageModel, RequestsBaseGenerator requestsBaseGenerator, TypeManager typeManager, Request request) {
        super(requestsBaseGenerator, typeManager);
        this.requestCategoryPackageModel = requestCategoryPackageModel;
        this.requestsBaseGenerator = requestsBaseGenerator;
        this.request = request;
    }

    public GeneratedRequest generate() throws JCodeModelException, UnknownTypeException {
        String typeName = request.getName();
        JDefinedClass definedClass = requestCategoryPackageModel._class(typeName);
        definedClass.constructor(JMod.PRIVATE);
        generateJavadocForClass(definedClass.javadoc(), request.getDescription(), replaceTypeName(typeName), request.getSince(), request.getDeprecated());

        JDefinedClass builderClass = generateBuilderClass(request, definedClass);
        JDefinedClass responseClass = generateResponseClass(request, definedClass);
        JDefinedClass requestClass = generateRequestClass(request, requestsBaseGenerator.getEnumValue(typeName), definedClass, builderClass, responseClass);
        generateSendMessageMethod(builderClass, responseClass, requestClass);

        return new GeneratedRequest(request, builderClass, requestClass, responseClass);
    }

    private void generateSendMessageMethod(JDefinedClass builderClass, JDefinedClass responseClass, JDefinedClass requestClass) {
        JMethod sendMessageMethod = builderClass.method(JMod.PUBLIC, typeManager.getVoidType(), "sendMessage");
        sendMessageMethod.javadoc().add(StringConstants.BASE_BUILDER_SEND_MESSAGE_JAVADOC);
        JVar consumerVar = sendMessageMethod.param(typeManager.getConsumer(responseClass), "consumer");
        JBlock body = sendMessageMethod.body();
        JVar messageId = body.decl(typeManager.getPrimitiveType(StringConstants.STRING_TYPE), "messageId", JExpr.invoke("getNewMessageId"));
        JVar requestVar = body.decl(requestClass, "request", requestClass._new().arg(JExpr._this()).arg(messageId));
        JLambda lambda = new JLambda();
        JLambdaParam responseConsumerParam = lambda.addParam("responseConsumer");
        lambda.body().lambdaExpr(consumerVar.invoke("accept").arg(responseConsumerParam.castTo(responseClass)));
        body.add(JExpr.invoke("sendMessage").arg(requestVar).arg(lambda));
    }

    private JDefinedClass generateBuilderClass(Request request, JDefinedClass definedClass) throws JCodeModelException, UnknownTypeException {
        JDefinedClass builderClass = definedClass._class(JMod.PUBLIC | JMod.STATIC, "Builder")._extends(requestsBaseGenerator.getBaseBuilderClass());
        generateConstructor(builderClass);
        for (ConvertedProperty property : request.getParams()) {
            generateProperty(builderClass, property, FunctionType.BOTH);
        }
        return builderClass;
    }

    private JDefinedClass generateResponseClass(Request request, JDefinedClass definedClass) throws JCodeModelException, UnknownTypeException {
        JDefinedClass responseClass = definedClass._class(JMod.PUBLIC | JMod.STATIC, "Response")._extends(requestsBaseGenerator.getBaseResponseClass());
        for (ConvertedProperty property : request.getReturns()) {
            generateProperty(responseClass, property, FunctionType.GETTER);
        }
        return responseClass;
    }

    private JDefinedClass generateRequestClass(Request request, JEnumConstant requestEnumConstant, JDefinedClass definedClass, JDefinedClass builderClass, JDefinedClass responseClass) throws JCodeModelException {
        JDefinedClass requestClass = definedClass._class(JMod.PUBLIC | JMod.STATIC, "Request")._extends(requestsBaseGenerator.getBaseRequestClass());
        JFieldVar builderField = requestClass.field(JMod.PRIVATE, builderClass, "builder");
        builderField.annotate(JsonUnwrapped.class);

        generateRequestConstructor(requestEnumConstant, builderClass, requestClass, builderField);

        JMethod getResponseTypeMethod = requestClass.method(JMod.PUBLIC, typeManager.getAnyClassType(), "getResponseType");
        getResponseTypeMethod.javadoc().add(StringConstants.BASE_REQUEST_GET_RESPONSE_TYPE_METHOD_JAVADOC);
        getResponseTypeMethod.body()._return(responseClass.dotclass());

        JMethod getRequestNameMethod = requestClass.method(JMod.PUBLIC, typeManager.getPrimitiveType(StringConstants.STRING_TYPE), "getRequestName");
        getRequestNameMethod.javadoc().add(StringConstants.BASE_REQUEST_NAME_METHODE_JAVADOC);
        getRequestNameMethod.body()._return(definedClass.dotclass().invoke("getSimpleName"));

        if (NON_AUTHENTICATED_REQUESTS.contains(request.getName().toLowerCase())) {
            JMethod isAuthenticationRequiredMethod = requestClass.method(JMod.PUBLIC, typeManager.getPrimitiveType("boolean"), "isAuthenticationRequired");
            isAuthenticationRequiredMethod.annotate(Override.class);
            isAuthenticationRequiredMethod.javadoc().add(StringConstants.BASE_REQUEST_AUTHENTICATION_REQUIRED_METHOD);
            isAuthenticationRequiredMethod.body()._return(JExpr.lit(false));
        }
        return requestClass;
    }

    private void generateRequestConstructor(JEnumConstant requestEnumConstant, JDefinedClass builderClass, JDefinedClass requestClass, JFieldVar builderField) {
        JMethod constructor = requestClass.constructor(JMod.PRIVATE);
        JVar builderVar = constructor.param(builderClass, "builder");
        JVar messageIdVar = constructor.param(typeManager.getPrimitiveType(StringConstants.STRING_TYPE), "messageId");
        JBlock constructorBody = constructor.body();
        constructorBody.add(JExpr.invokeSuper().arg(requestEnumConstant).arg(messageIdVar));
        constructorBody.add(JExpr._this().ref(builderField).assign(builderVar));
    }

    private String replaceTypeName(String typeName) {
        if (typeName.equalsIgnoreCase(StringConstants.BROADCAST_CUSTOM_MESSAGE_FILTER))
            return typeName + "-1";
        return typeName;
    }
}
