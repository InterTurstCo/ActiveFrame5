package ru.intertrust.cm.core.gui.api.codegen;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import ru.intertrust.cm.core.gui.model.ComponentName;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

/**
 * Генератор реестра компонентов GUI. Так как в GWT отсутствует механизм аналогичный, Java Reflection, данный механизм
 * обеспечивает поиск компонентов GUI по имени и создание их новых экзмепляров.
 * @author Denis Mitavskiy
 *         Date: 22.07.13
 *         Time: 15:40
 */

public class ComponentRegistryGenerator extends Generator {
    public static final String REGISTRY_PACKAGE = "ru.intertrust.cm.core.gui.api.client";
    public static final String REGISTRY_CLASS_NAME = "ComponentRegistry";
    public static final String REGISTRY_IMPL_NAME = "ComponentRegistryImpl";

    @Override
    public String generate(TreeLogger treeLogger, GeneratorContext context, String typeName) throws UnableToCompleteException {
        Properties properties = getGuiProperties();
        TypeOracle typeOracle = context.getTypeOracle();

        final JClassType COMPONENT_TYPE = typeOracle.findType("ru.intertrust.cm.core.gui.api.client.Component");

        HashMap<String, String> componentClassesByName = new HashMap<String, String>(100);
        try {
            for (JClassType currentType : typeOracle.getTypes()) {
                String componentName = null;
                if (!isComponent(COMPONENT_TYPE, currentType)) {
                    continue;
                }
                ComponentName annotation = currentType.getAnnotation(ComponentName.class);
                if (annotation != null ) {
                    componentName = annotation.value();
                } else {
                    System.out.println(currentType + " is ignored as @ComponentName annotation is absent.");
                    continue;
                }

                String className = currentType.getQualifiedSourceName();
                if (componentClassesByName.containsKey(componentName)) {
                    String definedClassName = properties.getProperty(componentName);
                    if (definedClassName == null) {
                        String message = "Component " + componentName + " is not unique. Resolve name in gui.properties";
                        System.out.println("===========================================================================================");
                        System.out.println(message);
                        throw new IllegalArgumentException(message);
                    }
                    className = definedClassName;
                }
                componentClassesByName.put(componentName, className);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new UnableToCompleteException();
        }

        debugComponents(componentClassesByName);

        ClassSourceFileComposerFactory sourceWriterFactory = getFactory();
        sourceWriterFactory.addImplementedInterface(REGISTRY_PACKAGE + '.' + REGISTRY_CLASS_NAME);

        PrintWriter printWriter = context.tryCreate(treeLogger, REGISTRY_PACKAGE, REGISTRY_IMPL_NAME);
        if (printWriter == null) {
            return sourceWriterFactory.getCreatedClassName();
        }

        SourceWriter sourceWriter = sourceWriterFactory.createSourceWriter(context, printWriter);
        sourceWriter.println("private HashMap<String, Component> components;");
        sourceWriter.println(REGISTRY_IMPL_NAME + "() {");
        sourceWriter.indent();
        sourceWriter.println("components = new HashMap<String, Component>(100);");
        for (String componentName : componentClassesByName.keySet()) {
            String componentClass = componentClassesByName.get(componentName);
            sourceWriter.println("components.put( \"" + componentName + "\", new " + componentClass + "());");
        }
        sourceWriter.outdent();
        sourceWriter.println("}");

        sourceWriter.println("public <T extends Component> T get(String name) {");
        sourceWriter.indent();
        sourceWriter.println("Component obj = components.get(name);");
        sourceWriter.println("if (obj == null) {");
        sourceWriter.println("    return null;");
        sourceWriter.println("}");
        sourceWriter.println("return obj instanceof BaseComponent ? (T) ((BaseComponent) obj.createNew()).setName(name) : (T) obj.createNew();");
        sourceWriter.outdent();
        sourceWriter.println("}");
        sourceWriter.commit(treeLogger);
        return sourceWriterFactory.getCreatedClassName();
    }

    private ClassSourceFileComposerFactory getFactory() {
        ClassSourceFileComposerFactory sourceWriterFactory =
                new ClassSourceFileComposerFactory(REGISTRY_PACKAGE, REGISTRY_IMPL_NAME);
        sourceWriterFactory.addImport("java.util.HashMap");
        sourceWriterFactory.addImplementedInterface(REGISTRY_PACKAGE + ".ComponentRegistry");

        return sourceWriterFactory;
    }

    private boolean isComponent(JClassType componentType, JClassType currentType) {
        return !currentType.equals(componentType) && currentType.isAssignableTo(componentType) && !currentType.isAbstract() && currentType.isPublic() && currentType.isDefaultInstantiable();
    }

    private Properties getGuiProperties() throws UnableToCompleteException {
        Properties properties = new Properties();
        URL url =  ClassLoader.getSystemResource("gui.properties");
        try {
            properties.load(new FileInputStream(new File(url.getFile())));
        } catch (Throwable e) { // если файла нет, это равноценно пустому файлу
            System.out.println("=========================================== No gui.properties file found =============================");
            return properties;
        }
        return properties;
    }

    private void debugComponents(HashMap<String, String> componentClassesByName) {
        final ArrayList<String> componentNames = new ArrayList<>(componentClassesByName.keySet());
        Collections.sort(componentNames);
        System.out.println("============ Components ============");
        for (String name : componentNames) {
            System.out.println(name + "\t\t\t ===> " + componentClassesByName.get(name));
        }
    }
}

