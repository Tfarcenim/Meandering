package tfar.meandering.net;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;
import tfar.meandering.client.MeanderingClient;

import java.util.function.Supplier;


public class S2CMeanderingPacket {

    private final boolean meandering;
    private final double x,y,z;

    public S2CMeanderingPacket(boolean meandering,Vector3d vec) {
        this.meandering = meandering;
        x = vec.x;
        y = vec.y;
        z = vec.z;
    }


    //decode
    public S2CMeanderingPacket(PacketBuffer buf) {
        meandering = buf.readBoolean();
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
    }

    public void encode(PacketBuffer buf) {
        buf.writeBoolean(meandering);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        Minecraft.getInstance().enqueue(() -> {
            MeanderingClient.selfMeandering = meandering;
            MeanderingClient.center = new Vector3d(x,y,z);
        });
        ctx.get().setPacketHandled(true);
    }
}

