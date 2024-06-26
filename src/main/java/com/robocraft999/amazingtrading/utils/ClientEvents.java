package com.robocraft999.amazingtrading.utils;

import com.robocraft999.amazingtrading.client.gui.shop.ShopScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onAddTooltip(ItemTooltipEvent event){
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && ResourcePointHelper.doesItemHaveRP(stack) && event.getEntity() != null){
            var tooltip = event.getToolTip();
            if (Minecraft.getInstance().screen instanceof ShopScreen){
                //tooltip.add(Component.translatable(TNLang.KEY_GUI_SHIFT).withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
            } else {
                //tooltip.add(Component.literal("Value: " + ResourcePointHelper.getRPSellValue(stack)).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
                tooltip.add(Component.literal("Value: " + ResourcePointHelper.getRPSellValue(stack)).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
            }
        }
    }
}
