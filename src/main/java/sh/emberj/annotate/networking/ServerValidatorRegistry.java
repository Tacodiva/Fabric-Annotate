package sh.emberj.annotate.networking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.Instance;
import sh.emberj.annotate.registry.FreezableRegistry;
import sh.emberj.annotate.registry.Registry;

@Registry
public class ServerValidatorRegistry extends FreezableRegistry<IServerValidator> {
    public static final Identifier ANNOTATE_CHANNEL = new Identifier("annotate", "main");
    public static final int ANNOTATE_QUERY_ID = 807380476;
    public static final byte VERSION_ID = 1;

    @Instance
    public static final ServerValidatorRegistry INSTANCE = new ServerValidatorRegistry();
    public static final String ID = "annotate:server_validator";

    private final Map<Identifier, IServerValidator> _VALIDATOR_MAP;
    private List<IServerValidator> _validators;

    private ServerValidatorRegistry() {
        super(new Identifier(ID), IServerValidator.class);
        _VALIDATOR_MAP = new HashMap<>();
    }

    @Override
    public void register(Identifier key, IServerValidator value) throws AnnotateException {
        ensureNotFrozen();
        _VALIDATOR_MAP.put(key, value);        
    }

    public void freeze() {
        super.freeze();
        _validators = _VALIDATOR_MAP.values().stream().sorted((a, b) -> a.getIdentifier().compareTo(b.getIdentifier()))
                .toList();
    }

    public Packet<?> createValidationPacket() {
        ensureFrozen();
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeByte(VERSION_ID);
        buf.writeVarInt(_VALIDATOR_MAP.size());
        for (IServerValidator validator : _validators) {
            buf.writeIdentifier(validator.getIdentifier());
            validator.writeValidationData(buf);
        }

        return new LoginQueryRequestS2CPacket(ANNOTATE_QUERY_ID, ANNOTATE_CHANNEL, buf);
    }

    @Environment(EnvType.CLIENT)
    public boolean verifyValidationPacket(PacketByteBuf buf) {
        ensureFrozen();

        if (buf.readByte() != VERSION_ID) {
            Annotate.LOG.error("Failed to validate server. Wrong version id.");
            return false;
        }

        int validatorCount = buf.readVarInt();

        if (validatorCount != _validators.size())
            return false;

        for (int i = 0; i < validatorCount; i++) {
            Identifier validatorId = buf.readIdentifier();
            IServerValidator validator = _validators.get(i);
            if (!validator.getIdentifier().equals(validatorId))
                return false;
            if (!validator.validateData(buf))
                return false;
        }

        return true;
    }
}
