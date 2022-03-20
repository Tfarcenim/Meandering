package tfar.meandering;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.DataSerializerEntry;

public class DataSerializerItemList {
    public static final IDataSerializer<NonNullList<ItemStack>> ITEM_LIST = new IDataSerializer<NonNullList<ItemStack>>() {

        public void write(PacketBuffer packetBuffer, NonNullList<ItemStack> itemStacks) {
            packetBuffer.writeInt(itemStacks.size());

            for (ItemStack itemStack : itemStacks) {
                packetBuffer.writeItemStack(itemStack);
            }

        }

        public NonNullList<ItemStack> read(PacketBuffer buf) {
            int length = buf.readInt();
            NonNullList<ItemStack> list = NonNullList.withSize(length, ItemStack.EMPTY);

            for(int i = 0; i < list.size(); ++i) {
                list.set(i, buf.readItemStack());
            }

            return list;
        }

        public NonNullList<ItemStack> copyValue(NonNullList<ItemStack> itemStacks) {
            NonNullList<ItemStack> list = NonNullList.withSize(itemStacks.size(), ItemStack.EMPTY);

            for(int i = 0; i < itemStacks.size(); ++i) {
                list.set(i, itemStacks.get(i).copy());
            }

            return list;
        }
    };

    public DataSerializerItemList() {
    }

    public static void register(RegistryEvent.Register<DataSerializerEntry> event) {
        DataSerializerEntry dataSerializerEntryItemList = new DataSerializerEntry(ITEM_LIST);
        dataSerializerEntryItemList.setRegistryName("item_list");
        event.getRegistry().register(dataSerializerEntryItemList);
    }
}