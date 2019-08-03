package org.lavapowered.lava.internal;

import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

public class Lava {

    public static Logger LOGGER;
    private static final String NATIVE_VERSON = "v1_12_R1";
    private static final String NMS_PREFIX = "net/minecraft/server/";

    public static String getNativeVersion() {
        return NATIVE_VERSON;
    }

    public static String getNmsPrefix() {
        return NMS_PREFIX;
    }
}
