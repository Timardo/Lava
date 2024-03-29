package org.bukkit.craftbukkit.entity;

import net.minecraft.entity.item.EntityXPOrb;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;

import javax.annotation.Nullable;
import java.util.UUID;

public class CraftExperienceOrb extends CraftEntity implements ExperienceOrb {
    public CraftExperienceOrb(CraftServer server, EntityXPOrb entity) {
        super(server, entity);
    }

    public int getExperience() {
        return getHandle().xpValue;
    }

    public void setExperience(int value) {
        getHandle().xpValue = value;
    }

    // Paper start

    @Nullable
    @Override
    public UUID getTriggerEntityId() {
        return getHandle().triggerEntityId;
    }

    @Nullable
    @Override
    public UUID getSourceEntityId() {
        return getHandle().sourceEntityId;
    }

    @Override
    public SpawnReason getSpawnReason() {
        return getHandle().reason;
    }
    // Paper end

    @Override
    public EntityXPOrb getHandle() {
        return (EntityXPOrb) entity;
    }

    @Override
    public String toString() {
        return "CraftExperienceOrb";
    }

    public EntityType getType() {
        return EntityType.EXPERIENCE_ORB;
    }
}
