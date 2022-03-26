package tfar.meandering.world;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import tfar.meandering.IdlePlayer;
import tfar.meandering.Meandering;
import tfar.meandering.net.PacketHandler;
import tfar.meandering.net.S2CMeanderingPacket;
import tfar.meandering.net.S2COtherMeanderingPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MSavedData extends WorldSavedData {

    private final Map<UUID, Pair<UUID, Vector3d>> active = new HashMap<>();

    public MSavedData(String name) {
        super(name);
    }

    @Override
    public void read(CompoundNBT nbt) {
        active.clear();
        CompoundNBT nbt1 = nbt.getCompound(Meandering.MODID);

        for (String s : nbt1.keySet()) {
            UUID pUUID = UUID.fromString(s);
            CompoundNBT nbt2 = nbt1.getCompound(s);
            Vector3d vector3d = new Vector3d(nbt2.getDouble("x"),nbt2.getDouble("y"),nbt2.getDouble("z"));
            UUID eUUID = nbt2.getUniqueId("uuid");
            active.put(pUUID,Pair.of(eUUID,vector3d));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        CompoundNBT nbt1 = new CompoundNBT();

        for (Map.Entry<UUID, Pair<UUID, Vector3d>> entry : active.entrySet()) {
            CompoundNBT nbt2 = new CompoundNBT();
            nbt2.putUniqueId("uuid",entry.getValue().getFirst());
            Vector3d vec = entry.getValue().getSecond();
            nbt2.putDouble("x",vec.x);
            nbt2.putDouble("y",vec.y);
            nbt2.putDouble("z",vec.z);
            nbt1.put(entry.getKey().toString(),nbt2);
        }

        compound.put(Meandering.MODID,nbt1);
        return compound;
    }

    public boolean isActive(ServerPlayerEntity player) {
        UUID uuid = player.getGameProfile().getId();
        return active.containsKey(uuid);
    }

    public Vector3d center(ServerPlayerEntity player) {
        UUID uuid = player.getGameProfile().getId();
        return active.containsKey(uuid) ? active.get(uuid).getSecond() : null;
    }

    public Entity getDummy(ServerPlayerEntity player) {
        UUID uuid = player.getGameProfile().getId();

        if (active.containsKey(uuid)) {
            UUID dummyUUID = active.get(uuid).getFirst();
            return ((ServerWorld) player.world).getEntityByUuid(dummyUUID);
        }
        return null;
    }

    public void enable(ServerPlayerEntity player) {
        IdlePlayer entity = new IdlePlayer(Meandering.IDLE_PLAYER, player.world);
        UUID uuid = player.getGameProfile().getId();
        entity.setPlayerUUID(uuid);
        //entity.set(death.getPlayerName());
        //entity.setEquipment(player.);

        for (EquipmentSlotType type : EquipmentSlotType.values()) {
            entity.setItemStackToSlot(type,player.getItemStackFromSlot(type));
        }

        entity.setPlayerModelFlag(player.getDataManager().get(IdlePlayer.getPLAYER_MODEL_FLAG()));
        entity.setPositionAndRotation(player.getPosX(), player.getPosY(), player.getPosZ(), 0, 0);

        player.abilities.allowFlying = true;
        player.abilities.isFlying = true;
        player.setOnGround(false);
        active.put(uuid, Pair.of(entity.getUniqueID(),entity.getPositionVec()));
        player.world.addEntity(entity);
        PacketHandler.sendToTrackingClients(new S2COtherMeanderingPacket(true, player.getGameProfile().getId()),player);
        player.setForcedPose(Pose.STANDING);
        player.sendPlayerAbilities();
        markDirty();
    }

    public void disable(ServerPlayerEntity player) {
        UUID uuid = player.getGameProfile().getId();
        player.abilities.allowFlying = false;
        player.abilities.isFlying = false;

        Entity entity = player.getServerWorld().getEntityByUuid(active.get(uuid).getFirst());
        if (entity != null) {
            player.setPositionAndUpdate(entity.getPosX(), entity.getPosY(), entity.getPosZ());
            entity.remove();
        }
        active.remove(uuid);
        player.setForcedPose(null);
        player.sendPlayerAbilities();
        PacketHandler.sendToTrackingClients(new S2COtherMeanderingPacket(false, player.getGameProfile().getId()),player);
        markDirty();
    }

    public void login(ServerPlayerEntity player) {
        for (Map.Entry<UUID, Pair<UUID, Vector3d>> entry : active.entrySet()) {

            UUID pUUID = entry.getKey();

            if (pUUID.equals(player.getGameProfile().getId())) {
                PacketHandler.sendToClient(new S2CMeanderingPacket(true,entry.getValue().getSecond()),player);

                player.abilities.allowFlying = true;
                player.abilities.isFlying = true;
                player.setOnGround(false);

                player.sendPlayerAbilities();
            } else {
                PacketHandler.sendToClient(new S2COtherMeanderingPacket(true,entry.getKey()),player);
            }
        }
    }
}
