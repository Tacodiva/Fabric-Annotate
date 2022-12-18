package sh.emberj.annotate.resource.mixins;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonElement;

import net.minecraft.resource.JsonDataLoader;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.Annotate;

@Mixin(JsonDataLoader.class)
public class JsonDataLoaderMixin {
    @Shadow
    @Final
    private String dataType;

    @Inject(at = @At("TAIL"), method = "prepare")
    public void prepareJsonData(CallbackInfoReturnable<Map<Identifier, JsonElement>> jsonElements) {
        Annotate.LOG.info("Loading JSON data from '" + dataType + "'.");
    }
}
/*
[14:32:12] [Worker-Main-25/INFO] (Annotate) Loading JSON data from 'predicates''.
[14:32:12] [Worker-Main-16/INFO] (Annotate) Loading JSON data from 'item_modifiers''.
[14:32:12] [Worker-Main-25/INFO] (Annotate) Loading JSON data from 'recipes''.
[14:32:12] [Worker-Main-23/INFO] (Annotate) Loading JSON data from 'loot_tables''.
[14:32:12] [Worker-Main-16/INFO] (Annotate) Loading JSON data from 'advancements''.
 */