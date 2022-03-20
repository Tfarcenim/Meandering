package tfar.meandering.net;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import tfar.meandering.Meandering;

public class PacketHandler {
    public static SimpleChannel INSTANCE;

    public static void registerMessages() {
        int id = 0;

        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Meandering.MODID, Meandering.MODID), () -> "1.0", s -> true, s -> true);

        INSTANCE.registerMessage(id++, C2SKeybindPacket.class,
                C2SKeybindPacket::encode,
                C2SKeybindPacket::new,
                C2SKeybindPacket::handle);

        INSTANCE.registerMessage(id++, S2CMeanderingPacket.class,
                S2CMeanderingPacket::encode,
                S2CMeanderingPacket::new,
                S2CMeanderingPacket::handle);

        INSTANCE.registerMessage(id++, S2COtherMeanderingPacket.class,
                S2COtherMeanderingPacket::encode,
                S2COtherMeanderingPacket::new,
                S2COtherMeanderingPacket::handle);
    }

    public static void sendToClient(Object packet, ServerPlayerEntity player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),packet);
    }

    public static void sendToTrackingClients(Object packet, ServerPlayerEntity player) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),packet);
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }
}
