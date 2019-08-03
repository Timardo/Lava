package org.lavapowered.lava.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.player.PlayerTeleportEvent;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LavaEventHandler
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEnderTeleportEvent(EnderTeleportEvent e) {
        if (e.getEntityLiving() instanceof EntityPlayerMP) {
            CraftPlayer player = (CraftPlayer) ((EntityPlayerMP)e.getEntityLiving()).getBukkitEntity();
            Location location = new Location(e.getEntityLiving().getEntityWorld().getWorld(), e.getTargetX(), e.getTargetY(), e.getTargetZ(), player.getLocation().getYaw(), player.getLocation().getPitch());

            PlayerTeleportEvent teleEvent = new PlayerTeleportEvent(player, player.getLocation(), location, PlayerTeleportEvent.TeleportCause.ENDER_PEARL);
            Bukkit.getPluginManager().callEvent(teleEvent);
            e.setTargetX(teleEvent.getTo().getX());
            e.setTargetY(teleEvent.getTo().getY());
            e.setTargetZ(teleEvent.getTo().getZ());

            if (teleEvent.isCancelled()) {
                e.setCanceled(true);
            }
        }
        
        else if (e.getEntityLiving() instanceof EntityShulker) {
            org.bukkit.event.entity.EntityTeleportEvent teleport = new org.bukkit.event.entity.EntityTeleportEvent(e.getEntityLiving().getBukkitEntity(), e.getEntityLiving().getBukkitEntity().getLocation(), new org.bukkit.Location(e.getEntityLiving().world.getWorld(), e.getTargetX(), e.getTargetY(), e.getTargetZ()));
            Bukkit.getPluginManager().callEvent(teleport);
            e.setTargetX(teleport.getTo().getX());
            e.setTargetY(teleport.getTo().getY());
            e.setTargetZ(teleport.getTo().getZ());
            
            if (teleport.isCancelled()) {
                e.setCanceled(true);
            }
        }
    }
}
