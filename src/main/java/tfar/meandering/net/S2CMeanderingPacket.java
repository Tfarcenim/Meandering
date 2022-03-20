package tfar.meandering.net;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import tfar.meandering.client.MeanderingClient;

import java.util.function.Supplier;


public class S2CMeanderingPacket {

    private final boolean meandering;

    public S2CMeanderingPacket(boolean meandering) {
        this.meandering = meandering;
    }


    //decode
    public S2CMeanderingPacket(PacketBuffer buf) {
        meandering = buf.readBoolean();
    }

    public void encode(PacketBuffer buf) {
        buf.writeBoolean(meandering);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        Minecraft.getInstance().enqueue(() -> MeanderingClient.selfMeandering = meandering);
        ctx.get().setPacketHandled(true);
    }
}

