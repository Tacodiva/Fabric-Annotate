package sh.emberj.annotate.core;

import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

public class AnnotateIdentifier {

    private AnnotateIdentifier() {}

    /**
     * @param str The string
     * @return True if a string is null or empty or only contains blank characters.
     */
    private static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    /**
     * Converts camel case (camelCase) and pascal case (PascalCase) into snake case
     * (snake_case).
     * 
     * @param text The camel case string
     * @return The snake case string
     */
    private static String camelCaseToSnakeCase(String text) {
        if (text.length() == 0) return "";

        StringBuilder sb = new StringBuilder();
        int lastPart = 0;
        boolean wasUpper = Character.isUpperCase(text.charAt(0));

        for (int i = 1; i < text.length(); i++) {
            boolean isUpper = Character.isUpperCase(text.charAt(i));
            if (isUpper && !wasUpper) {
                sb.append(text.substring(lastPart, i).toLowerCase());
                sb.append("_");
                lastPart = i;
            }
            wasUpper = isUpper;
        }

        sb.append(text.substring(lastPart, text.length()).toLowerCase());

        return sb.toString();
    }

    /**
     * Creates an {@link net.minecraft.util.Identifier identifier} from it's string
     * representation, using the mod's id and class name as the default namespace
     * and path respectively.
     * 
     * @param id   The string representation of the id. Like "minecraft:diamond"
     * @param type The type this id should take it's defaults from
     * @return The identifier with the defaults applied where required
     * @throws AnnotateException If the identifier is invalid
     */
    public static Identifier createIdentifier(String id, AnnotatedType type) throws AnnotateException {
        if (isBlank(id)) return createIdentifier(null, null, type);
        int idx = id.indexOf(Identifier.NAMESPACE_SEPARATOR);
        if (idx == -1) return createIdentifier(null, id, type);
        return createIdentifier(id.substring(0, idx), id.substring(idx + 1, id.length()), type);
    }

    /**
     * Creates an {@link net.minecraft.util.Identifier identifier} from it's
     * namespace and path strings using the mod's id and class name as the default
     * namespace and path respectively.
     * 
     * @param namespace The namespace of the id
     * @param path      The path of the id
     * @param type      The type this id should take it's defaults from
     * @return The identifier with the defaults applied where required
     * @throws AnnotateException If the identifier is invalid
     */
    public static Identifier createIdentifier(String namespace, String path, AnnotatedType type)
            throws AnnotateException {
        if (isBlank(namespace)) {
            namespace = type.getMod().getId();
        }

        if (isBlank(path)) {
            path = camelCaseToSnakeCase(type.getRawType().getSimpleName());
        }

        try {
            return new Identifier(namespace, path);
        } catch (InvalidIdentifierException e) {
            throw new AnnotateException(e.getMessage(), type);
        }
    }

}