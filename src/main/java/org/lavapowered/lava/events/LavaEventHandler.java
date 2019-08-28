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
import org.bukkit.craftbukkit.util.LazyPlayerSet;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.lavapowered.lava.internal.Lava;
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
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.world.BlockEvent.MultiPlaceEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LavaEventHandler {
    
    public static boolean waitForDropEvent = false; //Used in death event
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
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRightClickItem(RightClickItem e) {
        if (!(e.getEntityPlayer() instanceof EntityPlayerMP)) return; //TODO log error as this should be only fake player or smth like that, implement?
        EntityPlayerMP player = (EntityPlayerMP)e.getEntityPlayer();
        EnumHand enumHand = e.getHand();
        ItemStack itemStack = player.getHeldItem(enumHand);
        //Craftbukkit stuff - Raytrace to look for 'rogue armswings'
        float f1 = player.rotationPitch;
        float f2 = player.rotationYaw;
        double d0 = player.posX;
        double d1 = player.posY + (double)player.getEyeHeight();
        double d2 = player.posZ;
        Vec3d vec3d = new Vec3d(d0, d1, d2);
        float f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
        float f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
        float f5 = -MathHelper.cos(-f1 * 0.017453292F);
        float f6 = MathHelper.sin(-f1 * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d3 = player.interactionManager.getGameType() == GameType.CREATIVE ? 5.0D : 4.5D;
        Vec3d vec3d1 = vec3d.addVector((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
        RayTraceResult movingobjectposition = player.world.rayTraceBlocks(vec3d, vec3d1, false);

        boolean cancelled;
        if (movingobjectposition == null || movingobjectposition.typeOfHit != net.minecraft.util.math.RayTraceResult.Type.BLOCK) {
            PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, itemStack, enumHand);
            cancelled = event.useItemInHand() == Event.Result.DENY;
        } else {
            if (player.interactionManager.firedInteract) {
                player.interactionManager.firedInteract = false;
                cancelled = player.interactionManager.interactResult;
            } else {
                PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, movingobjectposition.getBlockPos(), movingobjectposition.sideHit, itemStack, true, enumHand);
                cancelled = event.useItemInHand() == Event.Result.DENY;
            }
        }
        
        if (cancelled) {
            e.setCancellationResult(EnumActionResult.PASS);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerChatEvent(ServerChatEvent e) { //TODO this is not complete nor tested! Remove the whole Async thing? How do the recipients work? Check this later
        String message = e.getMessage();
        boolean async = e.isAsync();
        Player playerCB = e.getPlayer().getBukkitEntity();
        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(async, playerCB, message, new LazyPlayerSet(Lava.getServer()));
        Bukkit.getPluginManager().callEvent(event);

        if (PlayerChatEvent.getHandlerList().getRegisteredListeners().length != 0) {
            // Evil plugins still listening to deprecated event
            final PlayerChatEvent queueEvent = new PlayerChatEvent(playerCB, event.getMessage(), event.getFormat(), event.getRecipients());
            queueEvent.setCancelled(event.isCancelled());
            
            if (async) {
                Waitable waitable = new Waitable() {
                    @Override
                    protected Object evaluate() {
                        org.bukkit.Bukkit.getPluginManager().callEvent(queueEvent);

                        if (queueEvent.isCancelled()) return null;

                        String message = String.format(queueEvent.getFormat(), queueEvent.getPlayer().getDisplayName(), queueEvent.getMessage());
                        Lava.getServer().console.sendMessage(message); //Log the message to the console too
                        
                        if (((LazyPlayerSet) queueEvent.getRecipients()).isLazy())
                            for (EntityPlayerMP player : Lava.getServer().getPlayerList().getPlayers())
                                player.sendMessage(new TextComponentTranslation("chat.type.text", playerCB.getDisplayName(), ForgeHooks.newChatWithLinks(message)));
                        else
                            for (Player player : queueEvent.getRecipients())
                                ((CraftPlayer)player).getHandle().sendMessage(new TextComponentTranslation("chat.type.text", playerCB.getDisplayName(), ForgeHooks.newChatWithLinks(message)));
                        
                        return null;
                    }
                };
                
                Lava.getServer().processQueue.add(waitable);
                e.setCanceled(true); //this event is going to be executed asynchronously, cancel the event and wait for execution by Bukkit TODO this is just testing behavior!
                
                try {
                    waitable.get();
                } catch (InterruptedException exc) {
                    Thread.currentThread().interrupt(); // This is proper habit for java. If we aren't handling it, pass it on!
                } catch (java.util.concurrent.ExecutionException exc) {
                    throw new RuntimeException("Exception processing chat event", exc.getCause());
                }
            }
            else { //the event is synchronous, so just set the component with links again
                org.bukkit.Bukkit.getPluginManager().callEvent(queueEvent);

                if (queueEvent.isCancelled()) {
                    e.setCanceled(true);
                    return;
                }

                String msg = String.format(queueEvent.getFormat(), queueEvent.getPlayer().getDisplayName(), queueEvent.getMessage());
                e.setComponent(new TextComponentTranslation("chat.type.text", playerCB.getDisplayName(), ForgeHooks.newChatWithLinks(msg)));
            }
        } else { //WOOHOO no evil plugins
            if (event.isCancelled()) {
                e.setCanceled(true);
                return;
            }

            message = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
            e.setComponent(new TextComponentTranslation("chat.type.text", playerCB.getDisplayName(), ForgeHooks.newChatWithLinks(message))); //TODO prob doubles the player name
        }
    }
}
