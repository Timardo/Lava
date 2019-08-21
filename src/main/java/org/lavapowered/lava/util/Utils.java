package org.lavapowered.lava.util;

import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.block.CraftBlockState;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class Utils {
    
    /*
     * A helper method for replacing an ItemStack outside of code where it is declared, mostly used in events
     */
    public static void setItemStack(ItemStack original, ItemStack replacement) {
        if (original == null || replacement == null) return;
        original.setItem(replacement.getItem());
        original.setCount(replacement.getCount());
        original.setItemDamage(replacement.getItemDamage());
        original.setTagCompound(replacement.getTagCompound());
        if (!ItemStack.areItemStacksEqual(original, replacement)) System.out.println("Something is wrong with these itemstacks: Original: "
                + "" + original.toString() + " Replacement: " + replacement.toString() + " If it happens too often, open an issue on GitHub ASAP!");
        //this should NEVER happen, if it will happen too often, we will have to set capabilities too (which requires another accessor made to be public probably) TODO remove when it is fully tested
    }
    
    /*
     * Converts BlockSnapshot to its location in int array TODO is this really needed here?
     */
    public static int[] toIntArr(BlockSnapshot blockSnapshot) {
        BlockPos pos = blockSnapshot.getPos();
        return new int[] {pos.getX(), pos.getY(), pos.getZ()};
    }
}
