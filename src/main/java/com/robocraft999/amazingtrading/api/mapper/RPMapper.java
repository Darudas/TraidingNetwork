package com.robocraft999.amazingtrading.api.mapper;

import com.robocraft999.amazingtrading.resourcepoints.mapper.IRPMapper;
import com.robocraft999.amazingtrading.resourcepoints.nss.NormalizedSimpleStack;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents that this class should be loaded as an {@link IRPMapper )} from {@link NormalizedSimpleStack} to {@link Long}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RPMapper {

    /**
     * Gets the priority of this {@link RPMapper}. This is used when loading the list of rp mappers. The higher this number is the earlier it gets ran.
     *
     * @return Sort priority of this {@link RPMapper}
     */
    int priority() default 0;

    /**
     * Array of modids that are required for this {@link RPMapper} to be loaded, empty String or array for no dependencies.
     *
     * @return array of modids.
     */
    String[] requiredMods() default "";

    /**
     * Used to on a static field of a class annotated with {@link RPMapper} to represent the field is an instance of an {@link RPMapper}. This instance will then be
     * used instead of attempting to create a new instance of the class.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Instance {}
}