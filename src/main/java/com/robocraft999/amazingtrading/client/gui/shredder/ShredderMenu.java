package com.robocraft999.amazingtrading.client.gui.shredder;

import com.robocraft999.amazingtrading.client.gui.menu.ATContainerMenu;
import com.robocraft999.amazingtrading.client.gui.shredder.slots.SlotInput;
import com.robocraft999.amazingtrading.registry.ATMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ShredderMenu extends ATContainerMenu {
    public final ShredderInventory shredderInventory;
    protected final List<Slot> inputSlots = new ArrayList<>();
    public final BlockPos blockPos;

    public ShredderMenu(Inventory playerInv, int i, BlockPos blockPos) {
        super(ATMenuTypes.SHREDDER_MENU.get(), playerInv, i);
        this.shredderInventory = new ShredderInventory(playerInv.player);
        this.blockPos = blockPos;
        initSlots();
    }

    protected void initSlots(){
        this.addSlot(new SlotInput(shredderInventory, 0, 203, 20));
        //this.addSlot(new SlotConsume(shredderInventory, 9, 107, 97));
        addPlayerInventory(8, 51);
    }

    @Override
    protected @NotNull Slot addSlot(@NotNull Slot slot) {
        if (slot instanceof SlotInput input) {
            inputSlots.add(input);
        }
        return super.addSlot(slot);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
        /*Slot currentSlot = tryGetSlot(i);
        ItemStack slotStack = currentSlot.getItem();
        ItemStack stackToInsert = slotStack;
        long points = ResourcePointHelper.getRPSellValue(stackToInsert);
        if (points > 0){
            if (shredderInventory.isServer()) {
                BigInteger pointsBigInt = BigInteger.valueOf(points);
                shredderInventory.addResourcePoints(pointsBigInt.multiply(BigInteger.valueOf(stackToInsert.getCount())));
                AmazingTrading.LOGGER.info("t"+shredderInventory.provider.getPoints());
                player.level().getCapability(TNCapabilities.RESOURCE_ITEM_CAPABILITY).ifPresent(provider -> {
                    if (provider.getSlotsHandler() instanceof RItemStackHandler handler && !handler.hasFreeSlot(stackToInsert)){
                        handler.enlarge();
                    }
                    ItemHandlerHelper.insertItemStacked(provider.getSlotsHandler(), stackToInsert.copy(), false);
                    provider.sync();
                });
            }
            currentSlot.set(ItemStack.EMPTY);
        }

        return ItemStack.EMPTY;*/
    }
}
