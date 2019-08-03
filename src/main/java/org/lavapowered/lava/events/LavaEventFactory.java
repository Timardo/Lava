package org.lavapowered.lava.events;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.TreeType;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.EntityInteractEvent;
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
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
		org.bukkit.block.Block block = worldIn.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
		CraftBlockState state = (CraftBlockState) block.getState();
		state.setTypeId(net.minecraft.block.Block.getIdFromBlock(blockState.getBlock()));
        state.setRawData((byte) 0);
        
		BlockGrowEvent event = new BlockGrowEvent(block, state);
        Bukkit.getPluginManager().callEvent(event);
        
        return !event.isCancelled();
	}

	public static boolean onFarmlandTrample(World world, BlockPos pos, IBlockState state, float fallDistance, Entity entity) {
		org.bukkit.event.Cancellable cancellable;
		
		if (entity instanceof EntityPlayer) {
            cancellable = CraftEventFactory.callPlayerInteractEvent((EntityPlayer) entity, org.bukkit.event.block.Action.PHYSICAL, pos, null, null, null);
        } else {
            cancellable = new EntityInteractEvent(entity.getBukkitEntity(), world.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
            world.getServer().getPluginManager().callEvent((EntityInteractEvent) cancellable);
        }

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

    public static void registerEvents()
    {
        MinecraftForge.EVENT_BUS.register(new LavaEventHandler());
    }

    public static int handleItemPickupEvent(EntityItem entityItem, EntityPlayer player)
    {
        ItemStack itemstack = entityItem.getItem(); //preparation
        int canHold = player.inventory.canHold(itemstack);
        int remaining = itemstack.getCount() - canHold;

        if (entityItem.pickupDelay <= 0 && canHold > 0) {
            //itemstack.setCount(canHold);
            // Call legacy event
            org.bukkit.event.player.PlayerPickupItemEvent playerEvent = new org.bukkit.event.player.PlayerPickupItemEvent((org.bukkit.entity.Player) player.getBukkitEntity(), (org.bukkit.entity.Item) entityItem.getBukkitEntity(), remaining);
            playerEvent.setCancelled(!player.thisisatest);
            entityItem.world.getServer().getPluginManager().callEvent(playerEvent);
            if (playerEvent.isCancelled()) {
                return 0;
            }

            // Call newer event afterwards
            org.bukkit.event.entity.EntityPickupItemEvent entityEvent = new org.bukkit.event.entity.EntityPickupItemEvent((org.bukkit.entity.Player) player.getBukkitEntity(), (org.bukkit.entity.Item) entityItem.getBukkitEntity(), remaining);
            entityEvent.setCancelled(!player.thisisatest);
            entityItem.world.getServer().getPluginManager().callEvent(entityEvent);
            if (entityEvent.isCancelled()) {
                return 0;
            }

            //itemstack.setCount(canHold + remaining);

            // Possibly < 0; fix here so we do not have to modify code below
            entityItem.pickupDelay = 0;
        }
        return 1;
    }

}
