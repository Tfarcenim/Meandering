package tfar.meandering.net;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import tfar.meandering.client.MeanderingClient;

import java.util.UUID;
import java.util.function.Supplier;


public class S2COtherMeanderingPacket {

    private final boolean meandering;
    private final UUID uuid;

    public S2COtherMeanderingPacket(boolean meandering,UUID uuid) {
        this.meandering = meandering;
        this.uuid = uuid;
    }


    //decode
    public S2COtherMeanderingPacket(PacketBuffer buf) {
        meandering = buf.readBoolean();
        uuid = buf.readUniqueId();
    }

    public void encode(PacketBuffer buf) {
        buf.writeBoolean(meandering);
        buf.writeUniqueId(uuid);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        Minecraft.getInstance().enqueue(() -> MeanderingClient.handle(meandering,uuid,null));
        ctx.get().setPacketHandled(true);
    }
}

