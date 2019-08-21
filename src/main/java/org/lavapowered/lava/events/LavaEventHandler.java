package org.lavapowered.lava.events;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.lavapowered.lava.util.Utils;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.world.BlockEvent.MultiPlaceEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LavaEventHandler {
    
    public static boolean waitForDropEvent = false;
    public static ITextComponent[] chatMessage;
    public static TargetReason targetReason;
    public static EntityLivingBase originalTarget;
    
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
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerTosssEvent(ItemTossEvent e) {
        Player player = (Player)e.getPlayer().getBukkitEntity();
        CraftItem drop = new CraftItem((CraftServer)Bukkit.getServer(), e.getEntityItem());

        PlayerDropItemEvent event = new PlayerDropItemEvent(player, drop);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            e.setCanceled(true);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDeathEvent(LivingDeathEvent e) {
        if (e.getEntityLiving() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)e.getEntityLiving();
            
            if (player.dead) {
                e.setCanceled(true);
                return;
            }
            
            //This is just preparation since forge has divided events
            boolean dropItems = !player.world.getGameRules().getBoolean("keepInventory") && !player.isSpectator(); //used in death event

            if (dropItems) { //we will drop items, prepare and wait for PlayerDropsEvent
                waitForDropEvent = true;
                return;
            }

            //no item drops, let's just call the death event
            ITextComponent chatmessage = player.getCombatTracker().getDeathMessage();

            String deathmessage = chatmessage.getFormattedText();
            org.bukkit.event.entity.PlayerDeathEvent deathEvent = CraftEventFactory.callPlayerDeathEvent(player, new java.util.ArrayList<>(player.inventory.getSizeInventory()), deathmessage, true);
            chatMessage = CraftChatMessage.fromString(deathEvent.getDeathMessage());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerDropEvent(PlayerDropsEvent e) {
        if (e.getEntityPlayer() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)e.getEntityLiving();
            
            if (waitForDropEvent) {
                waitForDropEvent = false;
                List<org.bukkit.inventory.ItemStack> loot = new java.util.ArrayList<>(player.inventory.getSizeInventory());
            
                for (EntityItem item : e.getDrops()) {
                    loot.add(CraftItemStack.asCraftMirror(item.getItem())); //add loot from captured loot
                }

                String deathMessage = player.getCombatTracker().getDeathMessage().getFormattedText();
                org.bukkit.event.entity.PlayerDeathEvent deathEvent = CraftEventFactory.callPlayerDeathEvent(player, loot, deathMessage, false);
                deathMessage = deathEvent.getDeathMessage();

                if (player.world.getGameRules().getBoolean("showDeathMessages"))
                {
                    Team team = player.getTeam();
                    
                    if (team != null && team.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS)
                    {
                        if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS)
                        {
                            player.mcServer.getPlayerList().sendMessageToAllTeamMembers(player, player.getCombatTracker().getDeathMessage());
                        }
                        else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM)
                        {
                            player.mcServer.getPlayerList().sendMessageToTeamOrAllPlayers(player, player.getCombatTracker().getDeathMessage());
                        }
                    }
                    else
                    {
                        player.mcServer.getPlayerList().sendMessage(CraftChatMessage.fromString(deathMessage));
                    }
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityTravelToDimensionEvent(EntityTravelToDimensionEvent e) {
        if (e.getEntity() instanceof EntityPlayerMP) {
            if (((EntityPlayerMP)e.getEntity()).isPlayerSleeping()) {
                e.setCanceled(true); //Craftbukkit stuff - SPIGOT-3154
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingSetAttackTargetEvent(LivingSetAttackTargetEvent e) {
        TargetReason reason = targetReason;
        EntityLivingBase originalTarget = this.originalTarget;
        this.originalTarget = null;
        targetReason = null; //reset now to save some space with returns
        
        if (reason == TargetReason.DONTCALLEVENT) return; //called from CB
        EntityLivingBase entity = e.getEntityLiving();
        EntityLivingBase target = e.getTarget();
        
        if (((EntityLiving)entity).getAttackTarget() == target) return; //the target didn't change, skip
        if (reason == TargetReason.UNKNOWN && ((EntityLiving) entity).getAttackTarget() != null && target == null) {
            reason = ((EntityLiving) entity).getAttackTarget().isEntityAlive() ? TargetReason.FORGOT_TARGET : TargetReason.TARGET_DIED;
        }
        
        if (reason == TargetReason.UNKNOWN) {
            ; //nothing yet TODO
        }
        
        CraftLivingEntity ctarget = null;
        
        if (target != null) {
            ctarget = (CraftLivingEntity) target.getBukkitEntity();
        }
        EntityTargetLivingEntityEvent event = new EntityTargetLivingEntityEvent(entity.getBukkitEntity(), ctarget, reason);
        Bukkit.getPluginManager().callEvent(event);
        
        if (event.isCancelled()) {
            ((EntityLiving)entity).setAttackTarget(originalTarget); //reset the target to previous entity
        }

        if (event.getTarget() != null) {
            target = ((CraftLivingEntity) event.getTarget()).getHandle();
        } else {
            target = null;
        }
        ((EntityLiving)entity).setAttackTarget(target); //override the attack target
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onWorldLoad(WorldEvent.Load e) {
        Bukkit.getPluginManager().callEvent(new WorldLoadEvent(e.getWorld().getWorld()));
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMultiPlaceEvent(MultiPlaceEvent e) {
        int[] loc = e.getPlayer().clickedBlock;
        List<BlockState> blocks = new ArrayList<BlockState>();
        
        for (BlockSnapshot snapshot : e.getReplacedBlockSnapshots()) //Convert snapshots to block states TODO make a convenient method for conversion in Utils?
            blocks.add(new CraftBlockState(snapshot));
        
        BlockMultiPlaceEvent event = CraftEventFactory.callBlockMultiPlaceEvent(e.getWorld(), e.getPlayer(), e.getHand(), blocks, loc[0], loc[1], loc[2]);
        
        if (event.isCancelled() || !event.canBuild())
            e.setCanceled(true);
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlaceEvent(PlaceEvent e) {
        int[] loc = e.getPlayer().clickedBlock != null ? e.getPlayer().clickedBlock : Utils.toIntArr(e.getBlockSnapshot()); //handle lily pads as they are not going through ForgeHooks
        BlockPlaceEvent event = CraftEventFactory.callBlockPlaceEvent(e.getWorld(), e.getPlayer(), e.getHand(), new CraftBlockState(e.getBlockSnapshot()), loc[0], loc[1], loc[2]);
        
        if (event.isCancelled() || !event.canBuild())
            e.setCanceled(true);
    }
}
