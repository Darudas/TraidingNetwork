package com.robocraft999.traidingnetwork.resourcepoints.conversion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds deserialized custom conversions. <a href="https://gist.github.com/williewillus/9ebb0d04329526e31564">Full grammar specification</a>
 */
public class CustomConversionFile {

    public boolean replace = false;
    public String comment;
    public final Map<String, ConversionGroup> groups = new HashMap<>();
    public final FixedValues values = new FixedValues();

    public static CustomConversionFile merge(CustomConversionFile left, CustomConversionFile right) {
        if (right.replace) {
            return right;
        }

        for (Map.Entry<String, ConversionGroup> e : right.groups.entrySet()) {
            left.groups.merge(e.getKey(), e.getValue(), ConversionGroup::merge);
        }

        left.values.merge(right.values);
        return left;
    }

    public void write(File file) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file)) {
            CustomConversionMapper.GSON.toJson(this, fileWriter);
        }
    }
}
