package nl.harm27.obs.websocket.generator.generators.generic;

import com.helger.jcodemodel.util.JCStringHelper;

import static java.lang.String.format;

public class StringUtil {
    private StringUtil() {
    }

    public static String generateValidFieldName(String name) {
        if (name.contains("-"))
            return generateValidName(name, '-', true);
        else if (name.contains("_"))
            return generateValidName(name, '_', true);
        else
            return lowerCaseFirstChar(name);
    }

    private static String generateValidName(String name, char separator, boolean firstWordNotCapitalized) {
        String[] nameParts = JCStringHelper.getExplodedArray(separator, name);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < nameParts.length; i++) {
            if (i == 0 && firstWordNotCapitalized)
                builder.append(nameParts[i]);
            else
                builder.append(capitalizeFirstChar(nameParts[i]));
        }
        return builder.toString();
    }

    public static String generateValidClassName(String name) {
        if (name.contains(" "))
            return generateValidName(name, ' ', false);
        else if (name.contains("-"))
            return generateValidName(name, '-', false);
        else
            return capitalizeFirstChar(name);
    }

    public static String generateValidMethodName(String name) {
        if (!name.contains(" "))
            return lowerCaseFirstChar(name);

        return generateValidName(name, ' ', true);
    }

    public static String capitalizeFirstChar(String name) {
        return format("%s%s", name.substring(0, 1).toUpperCase(), name.substring(1));
    }

    public static String lowerCaseFirstChar(String name) {
        return format("%s%s", name.substring(0, 1).toLowerCase(), name.substring(1));
    }

    public static String generateFieldMethodName(String name, String methodPrefix) {
        return format("%s%s", methodPrefix, capitalizeFirstChar(generateValidFieldName(name)));
    }

    public static String generateValidPackageName(String category) {
        return category.replace(" ", "");
    }

    public static String generateEnumValue(String name) {
        if (name.contains("_"))
            return name;
        return name.replaceAll("([A-Z])", "_$1").toUpperCase().replaceFirst("_", "");
    }
}
