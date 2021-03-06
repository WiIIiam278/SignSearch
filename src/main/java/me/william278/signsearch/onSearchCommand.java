package me.william278.signsearch;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;

public class onSearchCommand implements CommandExecutor {

    private String truncate(double dbl) {
        String truncatedDouble = Double.toString(dbl);
        return truncatedDouble.split("\\.")[0];
    }

    private static final SignSearch plugin = SignSearch.getInstance();

    private void faceDirection(Player player, Location target) {
        target.setX(target.getX() + 0.5);
        target.setY(target.getY() + 0.5);
        target.setZ(target.getZ() + 0.5);
        Vector dir = target.clone().subtract(player.getEyeLocation()).toVector();
        Location loc = player.getLocation().setDirection(dir);
        player.teleport(loc);
    }

    private void search(String search, Location location, Player player, int chunkRadius) {
        HashSet<Location> results = new HashSet<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            HashSet<ChunkSnapshot> checkingChunks = new HashSet<>();
            for (int chunkCheckX = location.getChunk().getX() - (chunkRadius);
                 chunkCheckX <= location.getChunk().getX() + (chunkRadius);
                 chunkCheckX++) {
                for (int chunkCheckZ = location.getChunk().getZ() - (chunkRadius);
                     chunkCheckZ <= location.getChunk().getZ() + (chunkRadius);
                     chunkCheckZ++) {
                    checkingChunks.add(player.getWorld().getChunkAt(chunkCheckX, chunkCheckZ).getChunkSnapshot());
                }
            }

            for (ChunkSnapshot chunkSnapshot : checkingChunks) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = location.getBlockY() - 20; (y < location.getBlockY() + 30 || y < chunkSnapshot.getHighestBlockYAt(x, z)); y++) {
                            if (y < 0) { y = 0; }
                            if (chunkSnapshot.getBlockData(x, y, z) instanceof org.bukkit.block.data.type.Sign) {
                                int finalY = y;
                                int finalX = x;
                                int finalZ = z;
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    results.add(player.getWorld().getChunkAt(chunkSnapshot.getX(), chunkSnapshot.getZ()).getBlock(finalX, finalY, finalZ).getLocation());
                                });
                            }
                        }
                    }
                }
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
               findText(results, player, search);
            });
        });
    }

    private void findText(HashSet<Location> results, Player player, String search) {
        if (results.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Couldn't find any nearby signs.");
            return;
        }
        double distance = 100000D;
        Location finalLocation = null;
        for (Location l : results) {
            Block block = l.getWorld().getBlockAt(l);
            BlockState blockState = block.getState();
            Sign sign = (Sign) blockState;
            for (String s : sign.getLines()) {
                if (s.toLowerCase().contains(search.toLowerCase())) {
                    double tmpDistance = player.getLocation().distance(l);
                    if (tmpDistance < distance) {
                        distance = tmpDistance;
                        finalLocation = l;
                    }
                }
            }
        }
        if (finalLocation != null) {
            faceDirection(player, finalLocation);
            player.sendMessage(ChatColor.GREEN + "Found a sign " + truncate(distance) + " blocks away containing \"" + search + "\" at (" + truncate(finalLocation.getX()) +
                    ", " + truncate(finalLocation.getY()) + ", " + truncate(finalLocation.getZ()) + ")");
            return;
        }
        player.sendMessage(ChatColor.RED + "Couldn't find any signs with \"" + search + "\"");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length >= 1) {
                StringBuilder searchText = new StringBuilder();
                for (String argument : args) {
                    searchText.append(argument).append(" ");
                }
                String search = StringUtils.removeEnd(searchText.toString(), " ");
                player.sendMessage(ChatColor.GRAY + "Searching...");
                search(search, player.getLocation(), player, 2);
            } else {
                player.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "Invalid syntax. Usage: " + command.getUsage());
            }
            return true;
        }
        return false;
    }
}
