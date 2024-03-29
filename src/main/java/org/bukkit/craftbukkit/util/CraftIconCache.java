package org.bukkit.craftbukkit.util;

import org.bukkit.util.CachedServerIcon;

public class CraftIconCache implements CachedServerIcon {
    public final String value;

    // Paper start
    @Override
    public String getData() {
        return value;
    }
    // Paper end

    public CraftIconCache(final String value) {
        this.value = value;
    }
}
