package com.robocraft999.amazingtrading.resourcepoints.nss;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NSSSerializer implements JsonSerializer<NormalizedSimpleStack>, JsonDeserializer<NormalizedSimpleStack> {

    public static final NSSSerializer INSTANCE = new NSSSerializer();

    public static final NSSCreator fakeCreator = NSSFake::create;

    public static final NSSCreator itemCreator = itemName -> {
        if (itemName.startsWith("#")) {
            return NSSItem.createTag(getResourceLocation(itemName.substring(1), "item tag"));
        }
        int nbtStart = itemName.indexOf('{');
        ResourceLocation resourceLocation = getResourceLocation(nbtStart == -1 ? itemName : itemName.substring(0, nbtStart), "item");
        if (nbtStart == -1) {
            return NSSItem.createItem(resourceLocation);
        }
        String nbtAsString = itemName.substring(nbtStart);
        try {
            return NSSItem.createItem(resourceLocation, TagParser.parseTag(nbtAsString));
        } catch (CommandSyntaxException e) {
            throw new JsonParseException("Malformed NBT compound", e);
        }
    };

    public static final NSSCreator fluidCreator = fluidName -> {
        if (fluidName.startsWith("#")) {
            return NSSFluid.createTag(getResourceLocation(fluidName.substring(1), "fluid tag"));
        }
        int nbtStart = fluidName.indexOf('{');
        ResourceLocation resourceLocation = getResourceLocation(nbtStart == -1 ? fluidName : fluidName.substring(0, nbtStart), "fluid");
        if (nbtStart == -1) {
            return NSSFluid.createFluid(resourceLocation);
        }
        String nbtAsString = fluidName.substring(nbtStart);
        try {
            return NSSFluid.createFluid(resourceLocation, TagParser.parseTag(nbtAsString));
        } catch (CommandSyntaxException e) {
            throw new JsonParseException("Malformed NBT compound", e);
        }
    };

    private static ResourceLocation getResourceLocation(String s, String type) throws JsonParseException {
        try {
            return new ResourceLocation(s);
        } catch (ResourceLocationException e) {
            throw new JsonParseException("Malformed " + type + " ID", e);
        }
    }

    private Map<String, NSSCreator> creators = Collections.emptyMap();

    public void setCreators(Map<String, NSSCreator> creators) {
        //Make the map be immutable
        this.creators = ImmutableMap.copyOf(creators);
    }

    @Override
    public NormalizedSimpleStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return deserialize(json.getAsString());
    }

    public NormalizedSimpleStack deserialize(String s) {
        if (s.contains("|")) {
            String[] parts = s.split("\\|");
            String key = parts[0];
            if (creators.containsKey(key)) {
                return creators.get(key).create(parts[1]);
            }
        }
        //Fallback to the item creator
        return itemCreator.create(s);
    }

    @Override
    public JsonElement serialize(NormalizedSimpleStack src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.json());
    }

    public static void init() {
        registerDefault("FAKE", fakeCreator);
        registerDefault("ITEM", itemCreator);
        registerDefault("FLUID", fluidCreator);

        var creators = new HashMap<String, NSSCreator>();
        creators.put("FAKE", fakeCreator);
        creators.put("ITEM", itemCreator);
        creators.put("FLUID", fluidCreator);
        INSTANCE.setCreators(creators);
    }

    private static void registerDefault(String key, NSSCreator creator) {
        //InterModComms.sendTo(AmazingTrading.MODID, IMCMethods.REGISTER_NSS_SERIALIZER, () -> new NSSCreatorInfo(key, creator));
    }
}