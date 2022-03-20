package tfar.meandering.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.List;

public class IdleClientPlayer extends RemoteClientPlayerEntity {

    public IdleClientPlayer(ClientWorld world, GameProfile profile, List<ItemStack> equipment, List<ItemStack> armor, byte model) {
        super(world, profile);
        this.getDataManager().set(PLAYER_MODEL_FLAG,model);
        setItemStackToSlot(EquipmentSlotType.MAINHAND,equipment.get(0));
        setItemStackToSlot(EquipmentSlotType.OFFHAND,equipment.get(1));

        setItemStackToSlot(EquipmentSlotType.HEAD,armor.get(EquipmentSlotType.HEAD.getIndex()));
        setItemStackToSlot(EquipmentSlotType.CHEST,armor.get(EquipmentSlotType.CHEST.getIndex()));
        setItemStackToSlot(EquipmentSlotType.LEGS,armor.get(EquipmentSlotType.LEGS.getIndex()));
        setItemStackToSlot(EquipmentSlotType.FEET,armor.get(EquipmentSlotType.FEET.getIndex()));
    }
}
