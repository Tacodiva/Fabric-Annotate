package sh.emberj.annotate.test;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.entrypoint.Entrypoint;
import sh.emberj.annotate.mixin.MixinMethodHead;
import sh.emberj.annotate.mixin.MixinMethodTail;
import sh.emberj.annotate.networking.callback.ClientboundCallbackContext;
import sh.emberj.annotate.networking.callback.NetCallback;
import sh.emberj.annotate.networking.callback.NetworkCallbacks;
import sh.emberj.annotate.networking.callback.ServerboundCallbackContext;
import sh.emberj.annotate.networking.serialization.Nothing;

// @AnnotateScan
public class Test {

    @Entrypoint
    public static void onInitialize() {
        AnimalRegistry.INSTANCE.get(new Identifier("annotate:pig")).makeNoise();
        AnimalRegistry.INSTANCE.get(new Identifier("annotate:sheep")).makeNoise();
        AnimalRegistry.INSTANCE.get(new Identifier("annotate:piglet")).makeNoise();

        // AnnotateNetServer.registerNativeServerboundPacket(new Identifier("annotate:test_a")).setHandler((data, ctx) -> {
        //     Annotate.LOG.info("Got message from the client " + data.readString());
        // });

        // AnnotateNetClient.registerNativeClientboundPacket(new Identifier("annotate:test_b")).setHandler((data, ctx) -> {
        //     System.out.println(data.readString());
        //     PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        //     buf.writeString("Hello from the client <3");
        //     AnnotateNetClient.sendNativeServerbound(new Identifier("annotate:test_a"), data);
        // });
    }

    @NetCallback(NetworkSide.CLIENTBOUND)
    public static void clientCallback(ClientboundCallbackContext ctx, String value) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString("<3");

        NetworkCallbacks.execute(Test::serverCallback);
    }

    @NetCallback(NetworkSide.SERVERBOUND)
    public static void serverCallback(ServerboundCallbackContext ctx, Nothing message) {
        Annotate.LOG.info("Received " + message + " from " + ctx.getPlayer().getGameProfile().getName());
        // Annotate.LOG.info("Received on the server " + message());
    }

    public static boolean done = false;

    @MixinMethodHead(value = MinecraftServer.class)
    public static void tick(MinecraftServer _this, BooleanSupplier shouldKeepTicking) {
        
        if (_this.getPlayerManager().getPlayerList().size() != 0) {
            done = true;
            ServerPlayerEntity spe = _this.getPlayerManager().getPlayerList().get(0);
            NetworkCallbacks.execute(spe, Test::clientCallback, "Hello, world!");
        }
    }

    // @Translation(key = "singleplayer", value = "Yay!", type = "menu.", namespace = Translation.NO_NAMESPACE)
    @Entrypoint(stage = AnnotateLoadStage.PRELAUNCH)
    @Environment(EnvType.SERVER)
    public static void onInit0() {
        Annotate.LOG.info("On init 0!");
    }

    @Entrypoint(stage = AnnotateLoadStage.PREINIT, priority = 1)
    public static void onInit1() {
        Annotate.LOG.info("On init 1!");
    }

    @Entrypoint
    public static void onInit2() {
        Annotate.LOG.info("On init 2!");
    }

    @Entrypoint(stage = AnnotateLoadStage.POSTINIT)
    public static void onInit3() {
        Annotate.LOG.info("On init 3!");
    }

    @MixinMethodHead(MixinTarget.class)
    public static void staticOne() {
        Annotate.LOG.info("Static One Mixin!");
    }

    @MixinMethodHead(MixinTarget.class)
    public static String staticTwo(String idk, int fbfb, CallbackInfo info) {
        Annotate.LOG.info("Static Two Mixin! Got idk = " + idk + " and fbfb = " + fbfb);
        return "cancelled!";
    }

    @MixinMethodTail(MixinTarget.class)
    public static double memberOne(MixinTarget _this, double abcde, CallbackInfoReturnable<String> cbi, double returnVal) {
        Annotate.LOG.info("Member One Mixin! " + abcde);
        Annotate.LOG.info("State = " + _this.state);
        Annotate.LOG.info("State = " + cbi.getId());
        Annotate.LOG.info("" + returnVal);    
        return 1111;
    }

    @MixinMethodHead(value = TitleScreen.class)
    public static void init() {
        System.out.println("==== TITLE SCREEN MIXIN ====");
        // System.exit(0);
    }
}
