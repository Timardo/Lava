package org.lavapowered.lava.wrapper;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.Command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

/**
 * Simple command wrapper for registering Bukkit commands to Forge environment correctly.
 * 
 * @author Timardo
 *
 */
public class BukkitCommandWrapper implements ICommand {
    private final Command bukkitReference; //holds the bukkit command
    private final String actualName; //holds the actual name of commnad (can be one of it's aliases)
    
    public BukkitCommandWrapper(Command bukkitCommand, String name) {
        this.bukkitReference = bukkitCommand;
        this.actualName = name;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }

    @Override
    public String getName() {
        return this.bukkitReference.getName();
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return this.bukkitReference.getUsage();
    }

    @Override
    public List<String> getAliases() {
        return this.bukkitReference.getAliases();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        this.bukkitReference.execute(sender.getCommandSenderEntity().getBukkitEntity(), this.actualName, args);
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return this.bukkitReference.testPermission(sender.getCommandSenderEntity().getBukkitEntity());
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        return this.bukkitReference.tabComplete(sender.getCommandSenderEntity().getBukkitEntity(), this.actualName, args, new Location(sender.getEntityWorld().getWorld(), targetPos.getX(), targetPos.getY(), targetPos.getZ()));
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }
    
}
