package org.lavapowered.lava.events;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import org.lavapowered.lava.util.Utils;

import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.BlockReed;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class LavaEventFactory {
    
	public static boolean handleGrowEvent(World worldIn, BlockPos pos, IBlockState blockState) {
	    
		if (blockState.getBlock() instanceof BlockCactus && ((Integer)blockState.getValue(BlockCactus.AGE)).intValue() == 15) {
			return postGrowEvent(worldIn, pos, blockState);
		}
		
		else if (blockState.getBlock() instanceof BlockCocoa) {
			return postGrowEvent(worldIn, pos, blockState);
		}
		
		else if (blockState.getBlock() instanceof BlockCrops) {
			return postGrowEvent(worldIn, pos, blockState);
		}
		
		else if (blockState.getBlock() instanceof BlockNetherWart) {
			return postGrowEvent(worldIn, pos, blockState);
		}
		
		else if (blockState.getBlock() instanceof BlockReed) {
			return postGrowEvent(worldIn, pos.up(), blockState);
		}
		
		return true;
	}
	
	private static boolean postGrowEvent(World worldIn, BlockPos pos, IBlockState blockState) {
		Block block = worldIn.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
		CraftBlockState state = (CraftBlockState) block.getState();
		state.setTypeId(net.minecraft.block.Block.getIdFromBlock(blockState.getBlock()));
        state.setRawData((byte) 0);
        
		BlockGrowEvent event = new BlockGrowEvent(block, state);
        Bukkit.getPluginManager().callEvent(event);
        
        return !event.isCancelled();
	}
	
	public static boolean onFarmlandTrample(World world, BlockPos pos, IBlockState state, float fallDistance, Entity entity) {
		Cancellable cancellable;
		
		if (entity instanceof EntityPlayer) {
            cancellable = CraftEventFactory.callPlayerInteractEvent((EntityPlayer) entity, Action.PHYSICAL, pos, null, null, null);
        } else {
            cancellable = new EntityInteractEvent(entity.getBukkitEntity(), world.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
        }
		
		world.getServer().getPluginManager().callEvent((EntityInteractEvent) cancellable);
		
        if (cancellable.isCancelled()) {
            return false;
        }
        
        if (CraftEventFactory.callEntityChangeBlockEvent(entity, pos, Blocks.DIRT, 0).isCancelled()) {
            return false;
        }
        
        return true;
	}
	
	public static boolean setTreeType(final Random rand) {
		BlockSapling.treeType = rand.nextInt(10) == 0 ? TreeType.BIG_TREE : TreeType.TREE;
		return true;
	}
	
    public static void registerEvents() {
        MinecraftForge.EVENT_BUS.register(new LavaEventHandler());
    }
    
    public static int handleItemPickupEvent(EntityItem entityItem, EntityPlayer player) {
        ItemStack itemstack = entityItem.getItem(); //preparation
        int canHold = player.inventory.canHold(itemstack);
        int remaining = itemstack.getCount() - canHold;
        
        if (entityItem.pickupDelay <= 0 && canHold > 0) {
            //itemstack.setCount(canHold);
            // Call legacy event
            PlayerPickupItemEvent playerEvent = new PlayerPickupItemEvent((Player) player.getBukkitEntity(), (Item) entityItem.getBukkitEntity(), remaining);
            playerEvent.setCancelled(!player.canPickUpLootCB);
            entityItem.world.getServer().getPluginManager().callEvent(playerEvent);
            if (playerEvent.isCancelled()) {
                return 0;
            }
            
            // Call newer event afterwards
            EntityPickupItemEvent entityEvent = new EntityPickupItemEvent((Player) player.getBukkitEntity(), (Item) entityItem.getBukkitEntity(), remaining);
            entityEvent.setCancelled(!player.canPickUpLootCB);
            entityItem.world.getServer().getPluginManager().callEvent(entityEvent);
            if (entityEvent.isCancelled()) {
                return 0;
            }
            
            //itemstack.setCount(canHold + remaining);
            
            // Possibly < 0; fix here
            entityItem.pickupDelay = 0;
        }
        return 1;
    }
    
    public static boolean allowPlayerPickupArrow(EntityPlayer entityIn, EntityArrow entityArrow, ItemStack itemStack) {
        EntityItem item = new EntityItem(entityArrow.world, entityArrow.posX, entityArrow.posY, entityArrow.posZ, itemStack);
        PlayerPickupArrowEvent event = new PlayerPickupArrowEvent((Player) entityIn.getBukkitEntity(), new CraftItem(entityArrow.world.getServer(), entityArrow, item), (Arrow) entityArrow.getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }
    
    public static void handleEggHatching(EntityEgg entityEgg, boolean hatching, byte hatches) {
        EntityType hatchingType = EntityType.CHICKEN;
        Entity shooter = entityEgg.getThrower();
        
        if (shooter instanceof net.minecraft.entity.player.EntityPlayer) {
            PlayerEggThrowEvent event = new PlayerEggThrowEvent((Player) shooter.getBukkitEntity(), (org.bukkit.entity.Egg) entityEgg.getBukkitEntity(), hatching, hatches, hatchingType);
            entityEgg.world.getServer().getPluginManager().callEvent(event);
            
            if (!event.isHatching()) {
                return;
            }
            
            for (int k = 0; k < event.getNumHatches(); ++k) {
                Entity entity = entityEgg.world.getWorld().createEntity(new Location(entityEgg.world.getWorld(), entityEgg.posX, entityEgg.posY, entityEgg.posZ, entityEgg.rotationYaw, 0.0F), hatchingType.getEntityClass());
                
                if (entity.getBukkitEntity() instanceof Ageable) {
                    ((Ageable) entity.getBukkitEntity()).setBaby();
                }
                
                entityEgg.world.getWorld().addEntity(entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.EGG);
            }
        }
    }

    public static boolean onBucketFill(EntityPlayer playerIn, ItemStack itemstack, BlockPos blockpos, boolean isWater, ItemBucket itemBucket) {
        PlayerBucketFillEvent event = CraftEventFactory.callPlayerBucketFillEvent(playerIn, blockpos.getX(), blockpos.getY(), blockpos.getZ(), null, itemstack, isWater ? Items.WATER_BUCKET : Items.LAVA_BUCKET);
        if (event.isCancelled()) return true;
        itemBucket.eventStack = CraftItemStack.asNMSCopy(event.getItemStack());
        return false;
    }
}
