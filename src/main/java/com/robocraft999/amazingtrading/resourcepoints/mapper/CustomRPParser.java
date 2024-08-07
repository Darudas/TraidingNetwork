package com.robocraft999.amazingtrading.resourcepoints.mapper;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.robocraft999.amazingtrading.AmazingTrading;
import com.robocraft999.amazingtrading.Config;
import com.robocraft999.amazingtrading.resourcepoints.nss.NSSItem;
import com.robocraft999.amazingtrading.resourcepoints.nss.NSSSerializer;
import com.robocraft999.amazingtrading.resourcepoints.nss.NormalizedSimpleStack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class CustomRPParser {

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(NormalizedSimpleStack.class, NSSSerializer.INSTANCE).setPrettyPrinting().create();
    private static final File CONFIG = Config.CONFIG_DIR.resolve("custom_rp.json").toFile();

    //Note: Neither this nor CustomRPEntry can be records due to gson not supporting creating records yet
    public static class CustomRPFile {

        public final List<CustomRPEntry> entries;

        public CustomRPFile(List<CustomRPEntry> entries) {
            this.entries = entries;
        }
    }

    public static class CustomRPEntry {

        public final NormalizedSimpleStack item;
        public final long rp;

        private CustomRPEntry(NormalizedSimpleStack item, long rp) {
            this.item = item;
            this.rp = rp;
        }

        @Override
        public boolean equals(Object o) {
            return o == this || o instanceof CustomRPEntry && item.equals(((CustomRPEntry) o).item) && rp == ((CustomRPEntry) o).rp;
        }

        @Override
        public int hashCode() {
            int result = item != null ? item.hashCode() : 0;
            result = 31 * result + (int) (rp ^ (rp >>> 32));
            return result;
        }
    }

    public static CustomRPFile currentEntries;
    private static boolean dirty = false;

    public static void init() {
        flush();

        if (!CONFIG.exists()) {
            try {
                if (CONFIG.createNewFile()) {
                    writeDefaultFile();
                }
            } catch (IOException e) {
                AmazingTrading.LOGGER.error(LogUtils.FATAL_MARKER, "Exception in file I/O: couldn't create custom configuration files.");
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG))) {
            currentEntries = GSON.fromJson(reader, CustomRPFile.class);
            currentEntries.entries.removeIf(e -> !(e.item instanceof NSSItem) || e.rp < 0);
        } catch (IOException | JsonParseException e) {
            AmazingTrading.LOGGER.error(LogUtils.FATAL_MARKER, "Couldn't read custom rp file", e);
            currentEntries = new CustomRPFile(new ArrayList<>());
        }
    }

    private static NormalizedSimpleStack getNss(String str) {
        return NSSSerializer.INSTANCE.deserialize(str);
    }

    public static void addToFile(String toAdd, long rp) {
        NormalizedSimpleStack nss = getNss(toAdd);
        CustomRPEntry entry = new CustomRPEntry(nss, rp);
        int setAt = -1;
        for (int i = 0; i < currentEntries.entries.size(); i++) {
            if (currentEntries.entries.get(i).item.equals(nss)) {
                setAt = i;
                break;
            }
        }
        if (setAt == -1) {
            currentEntries.entries.add(entry);
        } else {
            currentEntries.entries.set(setAt, entry);
        }
        dirty = true;
    }

    public static boolean removeFromFile(String toRemove) {
        NormalizedSimpleStack nss = getNss(toRemove);
        Iterator<CustomRPEntry> iter = currentEntries.entries.iterator();
        boolean removed = false;
        while (iter.hasNext()) {
            if (iter.next().item.equals(nss)) {
                iter.remove();
                dirty = true;
                removed = true;
            }
        }
        return removed;
    }

    public static void flush() {
        if (dirty) {
            try {
                Files.asCharSink(CONFIG, Charsets.UTF_8).write(GSON.toJson(currentEntries));
            } catch (IOException e) {
                AmazingTrading.LOGGER.error("Failed to write custom RP file", e);
            }
            dirty = false;
        }
    }

    private static void writeDefaultFile() {
        JsonObject elem = (JsonObject) GSON.toJsonTree(new CustomRPFile(new ArrayList<>()));
        elem.add("__comment", new JsonPrimitive("Use the in-game commands to edit this file"));
        try {
            Files.asCharSink(CONFIG, Charsets.UTF_8).write(GSON.toJson(elem));
        } catch (IOException e) {
            AmazingTrading.LOGGER.error("Failed to write default custom RP file", e);
        }
    }
}
