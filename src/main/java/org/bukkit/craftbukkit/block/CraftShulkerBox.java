package org.bukkit.craftbukkit.block;

import com.destroystokyo.paper.loottable.CraftLootableBlockInventory;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.tileentity.TileEntityShulkerBox;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.inventory.Inventory;

public class CraftShulkerBox extends CraftLootable<TileEntityShulkerBox> implements ShulkerBox, CraftLootableBlockInventory {

    public CraftShulkerBox(final Block block) {
        super(block, TileEntityShulkerBox.class);
    }

    public CraftShulkerBox(final Material material, final TileEntityShulkerBox te) {
        super(material, te);
    }

    @Override
    public Inventory getSnapshotInventory() {
        return new CraftInventory(this.getSnapshot());
    }

    @Override
    public Inventory getInventory() {
        if (!this.isPlaced()) {
            return this.getSnapshotInventory();
        }

        return new CraftInventory(this.getTileEntity());
    }

    @Override
    public DyeColor getColor() {
        net.minecraft.block.Block block = CraftMagicNumbers.getBlock(this.getType());

        return DyeColor.getByWoolData((byte) ((BlockShulkerBox) block).color.getMetadata());
    }
}
