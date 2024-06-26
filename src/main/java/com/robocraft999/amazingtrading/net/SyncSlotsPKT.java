package com.robocraft999.amazingtrading.net;

import com.robocraft999.amazingtrading.api.capabilities.IResourceItemProvider;
import com.robocraft999.amazingtrading.client.gui.shop.ShopInventory;
import com.robocraft999.amazingtrading.client.gui.shop.ShopMenu;
import com.robocraft999.amazingtrading.registry.ATCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;

public record SyncSlotsPKT(Map<Integer, ItemStack> stacksToSync, IResourceItemProvider.TargetUpdateType updateTargets) implements ITNPacket {
    @Override
    public void handle(NetworkEvent.Context context) {
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = player.clientLevel;
        if (level != null && player != null) {
            level.getCapability(ATCapabilities.RESOURCE_ITEM_CAPABILITY).ifPresent(cap -> {
                cap.receiveSlots(stacksToSync);
                if (updateTargets != IResourceItemProvider.TargetUpdateType.NONE && player.containerMenu instanceof ShopMenu container) {
                    //Update targets in case total available RP is now different
                    ShopInventory shopInventory = container.shopInventory;
                    if (updateTargets == IResourceItemProvider.TargetUpdateType.ALL) {
                        shopInventory.updateClientTargets();
                    } else {//If needed
                        shopInventory.checkForUpdates();
                    }
                }
            });
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(stacksToSync.size());
        for (Map.Entry<Integer, ItemStack> entry : stacksToSync.entrySet()) {
            buffer.writeVarInt(entry.getKey());
            buffer.writeItem(entry.getValue());
        }
        buffer.writeEnum(updateTargets);
    }

    public static SyncSlotsPKT decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        Map<Integer, ItemStack> syncedStacks = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            syncedStacks.put(buffer.readVarInt(), buffer.readItem());
        }
        return new SyncSlotsPKT(syncedStacks, buffer.readEnum(IResourceItemProvider.TargetUpdateType.class));
    }
}
