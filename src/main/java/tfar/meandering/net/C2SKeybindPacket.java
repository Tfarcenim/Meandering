package tfar.meandering.net;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import tfar.meandering.Meandering;

import java.util.function.Supplier;


public class C2SKeybindPacket {

  public C2SKeybindPacket(){}


  //decode
  public C2SKeybindPacket(PacketBuffer buf) {

  }

  public void encode(PacketBuffer buf) {

  }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
      ServerPlayerEntity player = ctx.get().getSender();
      if (player == null) return;
      ctx.get().enqueueWork(()-> Meandering.handle(player));
      ctx.get().setPacketHandled(true);
    }
}

