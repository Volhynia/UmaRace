package moe.gensoukyo.umarace.network;

import moe.gensoukyo.umarace.UmaRace;
import moe.gensoukyo.umarace.network.packet.SyncControlPointsPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = UmaRace.MODID)
public class PacketHandler {

    @SubscribeEvent
    public static void registerPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(UmaRace.MODID);

        // Use playToClient and provide the TYPE, the STREAM_CODEC, and the handler method
        registrar.playToClient(
                SyncControlPointsPacket.TYPE,
                SyncControlPointsPacket.STREAM_CODEC,
                SyncControlPointsPacket::handle
        );
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(ServerPlayer player, T message) {
        PacketDistributor.sendToPlayer(player, message);
    }
}