package nl.harm27.obs.websocket.generator.generators.generic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.helger.jcodemodel.*;
import nl.harm27.obs.websocket.generator.datamodel.shared.ConvertedProperty;

import java.util.Optional;

public abstract class GenericClassGenerator extends GenericGenerator {
    protected final TypeManager typeManager;

    protected GenericClassGenerator(TypeManager typeManager) {
        this.typeManager = typeManager;
    }

    protected void generateProperty(JDefinedClass targetClass, ConvertedProperty property, FunctionType functionType) throws JCodeModelException, UnknownTypeException {
        if (property.isSingleLevel()) {
            generateField(targetClass, property, functionType);
        } else {
            generateSubClass(targetClass, property, functionType);
        }
    }

    private void generateSubClass(JDefinedClass targetClass, ConvertedProperty property, FunctionType functionType) throws JCodeModelException, UnknownTypeException {
        JDefinedClass rootClass = getRootClass(targetClass);

        Optional<JDefinedClass> foundSubClass = rootClass.classes().stream().filter(sub -> property.getClassName().equalsIgnoreCase(sub.name())).findFirst();
        if (foundSubClass.isPresent())
            return;

        JDefinedClass subClass = rootClass._class(JMod.PUBLIC, property.getClassName());
        generateField(targetClass, new ConvertedProperty(property, subClass.name()), functionType);
        for (ConvertedProperty convertedProperty : property.getProperties()) {
            generateProperty(subClass, convertedProperty, functionType);
        }
    }

    protected JFieldVar generateField(JDefinedClass targetClass, ConvertedProperty property, FunctionType functionType) throws JCodeModelException, UnknownTypeException {
        AbstractJType fieldType = typeManager.getType(targetClass, property.getName(), property.getType(), property.getDescription());

        JFieldVar fieldVar = targetClass.field(JMod.PRIVATE, fieldType, property.getFieldName());
        fieldVar.annotate(JsonProperty.class).param(property.getName());

        if (functionType.hasGetter()) {
            JMethod getterMethod = generateGetter(targetClass, fieldType, fieldVar, property);
            generateAdditionalGetters(targetClass, getterMethod);
        }

        if (functionType.hasSetter())
            generateSetter(targetClass, fieldType, fieldVar, property);

        return fieldVar;
    }

    private void generateAdditionalGetters(JDefinedClass targetClass, JMethod getterMethod) {
        AbstractJClass matchClass = findParentClass(targetClass);
        if (matchClass == null)
            return;

        if ("GetVersion".equalsIgnoreCase(matchClass.name()) &&
                ("getAvailableRequests".equalsIgnoreCase(getterMethod.name()) ||
                        "getSupportedImageExportFormats".equalsIgnoreCase(getterMethod.name()))) {
            convertStringToList(targetClass, getterMethod);
        }
    }

    private void convertStringToList(JDefinedClass targetClass, JMethod getterMethod) {
        JMethod method = targetClass.method(JMod.PUBLIC, typeManager.getListPrimitiveType("string"), String.format("%sAsList", getterMethod.name()));
        method.body()._return(typeManager.getArraysAsList(JExpr._this().invoke(getterMethod).invoke("split").arg("\\.")));
    }

    private void generateSetter(JDefinedClass targetClass, AbstractJType fieldType, JFieldVar fieldVar, ConvertedProperty property) {
        JMethod method = targetClass.method(JMod.PUBLIC, typeManager.getVoidType(), property.getMethodName("set"));
        method.javadoc().add(property.getDescription());

        JVar param = method.param(fieldType, property.getFieldName());
        method.body().add(JExpr._this().ref(fieldVar).assign(param));
    }

    private JMethod generateGetter(JDefinedClass targetClass, AbstractJType fieldType, JFieldVar fieldVar, ConvertedProperty property) {
        String methodPrefix = "get";
        if (typeManager.isBoolean(fieldType))
            methodPrefix = "is";

        AbstractJType returnType = fieldType;
        if (property.isOptional())
            returnType = typeManager.getOptionalForType(fieldType);

        JMethod method = targetClass.method(JMod.PUBLIC, returnType, property.getMethodName(methodPrefix));
        method.javadoc().add(property.getDescription());

        if (property.isOptional())
            method.body()._return(typeManager.getOptionalReturnForField(fieldVar));
        else
            method.body()._return(fieldVar);

        return method;
    }

}
