package moe.gensoukyo.umarace.network.packet;

import moe.gensoukyo.umarace.UmaRace;
import moe.gensoukyo.umarace.client.ClientTrackCreationData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SyncControlPointsPacket(List<Vec3> controlPoints) implements CustomPacketPayload {

    public static final Type<SyncControlPointsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(UmaRace.MODID, "sync_control_points"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncControlPointsPacket> STREAM_CODEC = StreamCodec.ofMember(
            SyncControlPointsPacket::write,
            SyncControlPointsPacket::new
    );

    // Constructor for decoding from the buffer
    public SyncControlPointsPacket(final RegistryFriendlyByteBuf buf) {
        this(buf.readList(b -> new Vec3(b.readDouble(), b.readDouble(), b.readDouble())));
    }

    // Method for encoding to the buffer.
    // This is NOT an override, so we remove the annotation.
    public void write(final RegistryFriendlyByteBuf buf) {
        buf.writeCollection(controlPoints, (b, vec) -> {
            b.writeDouble(vec.x());
            b.writeDouble(vec.y());
            b.writeDouble(vec.z());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final SyncControlPointsPacket msg, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientTrackCreationData.getInstance().setControlPoints(msg.controlPoints());
        });
    }
}