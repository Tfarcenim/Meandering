package tfar.meandering;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.HandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class IdlePlayer extends LivingEntity {

    private static final DataParameter<Optional<UUID>> ID = EntityDataManager.createKey(IdlePlayer.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<String> NAME = EntityDataManager.createKey(IdlePlayer.class, DataSerializers.STRING);
    private static final DataParameter<Byte> MODEL = EntityDataManager.createKey(IdlePlayer.class, DataSerializers.BYTE);

    public IdlePlayer(EntityType<? extends IdlePlayer> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void registerData() {
        super.registerData();
        dataManager.register(ID, Optional.empty());
        dataManager.register(NAME, "name");
        dataManager.register(MODEL,(byte)0);
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        if (compound.hasUniqueId("player_uuid")) {
            setPlayerUUID(compound.getUniqueId("player_uuid"));
        }

        if (compound.contains("ArmorItems", 9)) {
            ListNBT listnbt = compound.getList("ArmorItems", 10);

            for(int i = 0; i < this.inventoryArmor.size(); ++i) {
                this.inventoryArmor.set(i, ItemStack.read(listnbt.getCompound(i)));
            }
        }

        if (compound.contains("HandItems", 9)) {
            ListNBT listnbt1 = compound.getList("HandItems", 10);

            for(int j = 0; j < this.inventoryHands.size(); ++j) {
                this.inventoryHands.set(j, ItemStack.read(listnbt1.getCompound(j)));
            }
        }
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        if (getPlayerUUID().isPresent()) {
            compound.putUniqueId("player_uuid",getPlayerUUID().get());
        }
        ListNBT listnbt = new ListNBT();

        for(ItemStack itemstack : this.inventoryArmor) {
            CompoundNBT compoundnbt = new CompoundNBT();
            if (!itemstack.isEmpty()) {
                itemstack.write(compoundnbt);
            }

            listnbt.add(compoundnbt);
        }

        compound.put("ArmorItems", listnbt);
        ListNBT listnbt1 = new ListNBT();

        for(ItemStack itemstack1 : this.inventoryHands) {
            CompoundNBT compoundnbt1 = new CompoundNBT();
            if (!itemstack1.isEmpty()) {
                itemstack1.write(compoundnbt1);
            }

            listnbt1.add(compoundnbt1);
        }

        compound.put("HandItems", listnbt1);
        ListNBT listnbt2 = new ListNBT();

    }

    @Override
    public boolean isSpectator() {
        return false;
    }
    private final NonNullList<ItemStack> inventoryHands = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> inventoryArmor = NonNullList.withSize(4, ItemStack.EMPTY);

    @Override
    public List<ItemStack> getArmorInventoryList() {
        return inventoryArmor;
    }

    @Override
    public List<ItemStack> getHeldEquipment() {
        return this.inventoryHands;
    }

    @Override
    public ItemStack getItemStackFromSlot(EquipmentSlotType slotIn) {
        if (slotIn == EquipmentSlotType.MAINHAND) {
            return inventoryHands.get(0);
        } else if (slotIn == EquipmentSlotType.OFFHAND) {
            return inventoryHands.get(1);
        } else {
            return slotIn.getSlotType() == EquipmentSlotType.Group.ARMOR ? inventoryArmor.get(slotIn.getIndex()) : ItemStack.EMPTY;
        }
    }

    @Override
    public void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack) {
        if (slotIn == EquipmentSlotType.MAINHAND) {
            this.playEquipSound(stack);
            this.inventoryHands.set(0, stack);
        } else if (slotIn == EquipmentSlotType.OFFHAND) {
            this.playEquipSound(stack);
            this.inventoryHands.set(1, stack);
        } else if (slotIn.getSlotType() == EquipmentSlotType.Group.ARMOR) {
            this.playEquipSound(stack);
            this.inventoryArmor.set(slotIn.getIndex(), stack);
        }
    }

    @Override
    public HandSide getPrimaryHand() {
        return HandSide.RIGHT;
    }

    public Optional<UUID> getPlayerUUID() {
        return dataManager.get(ID);
    }

    public void setPlayerUUID(UUID uuid) {
        if (uuid == null) {
            dataManager.set(ID, Optional.empty());
        } else {
            dataManager.set(ID, Optional.of(uuid));
        }
    }

    public String getPlayerName() {
        return null;
    }

    public byte getPlayerModelFlag() {
        return dataManager.get(MODEL);
    }

    public void setPlayerModelFlag(byte model) {
        dataManager.set(MODEL, model);
    }


    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {

        if (!world.isRemote) {
            PlayerEntity player = getPlayerUUID().map(uuid -> getServer().getPlayerList().getPlayerByUUID(uuid)).orElse(null);

            if (player != null) {
                return player.attackEntityFrom(source, amount);
            }
        }
        return super.attackEntityFrom(source, amount);
    }

    public static AttributeModifierMap.MutableAttribute registerAttributes() {
        return PlayerEntity.func_234570_el_();
    }

    public static DataParameter<Byte> getPLAYER_MODEL_FLAG() {
        return Dummy.getPLAYER_MODEL_FLAG();
    }

    public abstract static class Dummy extends PlayerEntity {

        public static DataParameter<Byte> getPLAYER_MODEL_FLAG() {
            return PlayerEntity.PLAYER_MODEL_FLAG;
        }

        public Dummy(World p_i241920_1_, BlockPos p_i241920_2_, float p_i241920_3_, GameProfile p_i241920_4_) {
            super(p_i241920_1_, p_i241920_2_, p_i241920_3_, p_i241920_4_);
        }
    }
}
