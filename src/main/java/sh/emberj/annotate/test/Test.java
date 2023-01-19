package sh.emberj.annotate.test;

import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.alloy.AlloyHead;
import sh.emberj.annotate.alloy.AlloyTail;
import sh.emberj.annotate.alloy.Return;
import sh.emberj.annotate.alloy.args.AlloyReturned;
import sh.emberj.annotate.alloy.args.AlloyThis;
import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.entrypoint.Entrypoint;
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

        // AnnotateNetServer.registerNativeServerboundPacket(new
        // Identifier("annotate:test_a")).setHandler((data, ctx) -> {
        // Annotate.LOG.info("Got message from the client " + data.readString());
        // });

        // AnnotateNetClient.registerNativeClientboundPacket(new
        // Identifier("annotate:test_b")).setHandler((data, ctx) -> {
        // System.out.println(data.readString());
        // PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        // buf.writeString("Hello from the client <3");
        // AnnotateNetClient.sendNativeServerbound(new Identifier("annotate:test_a"),
        // data);
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

    // @AlloyHead(MinecraftServer.class)
    // public static void tick(@AlloyThis MinecraftServer _this, BooleanSupplier
    // shouldKeepTicking) {

    // if (_this.getPlayerManager().getPlayerList().size() != 0) {
    // done = true;
    // ServerPlayerEntity spe = _this.getPlayerManager().getPlayerList().get(0);
    // NetworkCallbacks.execute(spe, Test::clientCallback, "Hello, world!");
    // }
    // }

    // @Translation(key = "singleplayer", value = "Yay!", type = "menu.", namespace
    // = Translation.NO_NAMESPACE)
    // @Entrypoint(stage = AnnotateLoadStage.PRELAUNCH)
    // @Environment(EnvType.SERVER)
    // public static void onInit0() {
    // Annotate.LOG.info("On init 0!");
    // }

    // @Entrypoint(stage = AnnotateLoadStage.PREINIT, priority = 1)
    // public static void onInit1() {
    // Annotate.LOG.info("On init 1!");
    // }

    @Entrypoint
    public static void onInit2() {
        Annotate.LOG.info("On init 2!");
    }

    @Entrypoint(stage = AnnotateLoadStage.POSTINIT)
    public static void onInit3() {
        Annotate.LOG.info("On init 3!");
    }

    // @AlloyTail(MixinTarget.class)
    // public static void staticOne(@AlloyLocal(ordinal = 1) double local) {
    // Annotate.LOG.info("Static One Mixin! " + local);
    // // Annotate.LOG.info(info.getId());

    // }

    @AlloyTail(value = MixinTarget.class, cancellable = true)
    public static Return<String> staticTwo(String idk, int fbfb, @AlloyReturned String old) {
        Annotate.LOG.info("Static Two Mixin! Got idk = " + idk + " and fbfb = " + fbfb);
        Annotate.LOG.info("Intercepted return " + old);
        // return Return.cancel("cancelled!");
        return Return.resume();
    }

    @AlloyTail(MixinTarget.class)
    public static double memberOne(@AlloyThis MixinTarget _this, double abcde, @AlloyReturned double oldReturn) {
        Annotate.LOG.info("Member One Mixin! " + abcde);
        Annotate.LOG.info("State = " + _this.state);
        Annotate.LOG.info("Old Return = " + oldReturn);
        return 1111;
    }

    @AlloyHead(TitleScreen.class)
    public static void init() {
        System.out.println("==== TITLE SCREEN MIXIN ====");
        // System.exit(0);
    }
}
