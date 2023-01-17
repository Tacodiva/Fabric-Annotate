package sh.emberj.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// @HandleMetaAnnotation(TranslationAnnotationHandler.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Translation {

    public static final String TYPE_BLOCK = "block.";
    public static final String TYPE_ITEM = "item.";
    public static final String TYPE_ITEM_GROUP = "itemGroup.";
    public static final String TYPE_FLUID = "fluid.";
    public static final String TYPE_SOUND_EVENT = "sound_event.";
    public static final String TYPE_STATUS_EFFECT = "effect.";
    public static final String TYPE_ENCHANTMENT = "enchantment.";
    public static final String TYPE_ENTITY = "entity.";
    public static final String TYPE_BIOME = "boime.";
    public static final String TYPE_STAT = "stat.";

    public static final String NO_NAMESPACE = "<none>";

    public String key();

    public String value();

    public String type() default "";

    public String namespace() default "";

    // public static class TranslationManager {
    //     private TranslationManager() {
    //     }

    //     private static Map<String, String> _translations = new HashMap<>();

    //     public static void addTranslation(String key, String value) {
    //         if (_translations == null)
    //             throw new IllegalStateException("Cannot add translations after translations have already been loaded!");
    //         _translations.put(key, value);
    //     }

    //     public static void applyTranslations(Map<String, String> translations) {
    //         for (Entry<String, String> translation : _translations.entrySet()) {
    //             translations.put(translation.getKey(), translation.getValue());
    //         }
    //         _translations = null;
    //     }
    // }

    // @AnnotateScan
    // public static class TranslationAnnotationHandler extends AnnotationHandler {

    //     public TranslationAnnotationHandler() {
    //         super(FabricLoadStage.PREINIT, 0);
    //     }

    //     @Override
    //     public void handle(AnnotationInfo annotation) throws AnnotateException {
    //         if (!annotation.isOfClass(Translation.class))
    //             return;
    //         String key = annotation.annotation().getStringParam("key");
    //         String value = annotation.annotation().getStringParam("value");
    //         String namespace = annotation.annotation().getStringParam("namespace");
    //         String type = annotation.annotation().getStringParam("type");

    //         namespace = AnnotateIdentifier.resolveNamespace(namespace, annotation.type().getMod());

    //         String langKey;
    //         if (namespace.equals(NO_NAMESPACE)) {
    //             langKey = type + key;
    //         } else {
    //             langKey = type + namespace + "." + key;
    //         }
    //         TranslationManager.addTranslation(langKey, value);
    //     }
    // }
}
