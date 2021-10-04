package me.william278.signsearch;

import de.themoep.minedown.MineDown;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.logging.Level;

public class SearchCommand implements CommandExecutor {

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
                for (int x = 0; x <= 15; x++) {
                    for (int z = 0; z <= 15; z++) {
                        for (int y = location.getBlockY() - 20; (y < location.getBlockY() + 30 || y < chunkSnapshot.getHighestBlockYAt(x, z)); y++) {
                            try {
                                if (y < 0) {
                                    y = 0;
                                }
                                final BlockData blockData = chunkSnapshot.getBlockData(x, y, z);
                                if (blockData instanceof org.bukkit.block.data.type.Sign || blockData instanceof WallSign) {
                                    int finalY = y;
                                    int finalX = x;
                                    int finalZ = z;
                                    Bukkit.getScheduler().runTask(plugin, () -> results.add(player.getWorld().getChunkAt(chunkSnapshot.getX(), chunkSnapshot.getZ()).getBlock(finalX, finalY, finalZ).getLocation()));
                                }
                            } catch (ArrayIndexOutOfBoundsException ignored) {} catch (Exception e) {
                                plugin.getLogger().log(Level.WARNING, "An exception occurred performing a sign search", e);
                            }
                        }
                    }
                }
            }
            Bukkit.getScheduler().runTask(plugin, () -> findText(results, player, search));
        });
    }

    private void findText(HashSet<Location> results, Player player, String search) {
        if (results.isEmpty()) {
            player.spigot().sendMessage(new MineDown("[Couldn't find any nearby signs.](#ff3300)").toComponent());
            return;
        }
        double distance = 100000D;
        Location finalLocation = null;
        for (Location l : results) {
            if (l.getWorld() == null) {
                continue;
            }
            Block block = l.getWorld().getBlockAt(l);
            BlockState blockState = block.getState();
            Sign signBlock = (Sign) blockState;
            for (String s : signBlock.getLines()) {
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
            player.spigot().sendMessage(new MineDown("[Found a matching sign](#00fb9a) [" + truncate(distance) + "](#00fb9a bold) [blocks away, at \\(" + truncate(finalLocation.getX())
                    + ", " + truncate(finalLocation.getY()) + ", " + truncate(finalLocation.getZ()) + "\\)](#00fb9a)").toComponent());
            return;
        }
        player.spigot().sendMessage(new MineDown("[Couldn't find any nearby signs.](#ff3300)").toComponent());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (args.length >= 1) {
                StringBuilder searchText = new StringBuilder();
                int chunkRadius = 2;
                for (String argument : args) {
                    if (argument.contains("-radius:")) {
                        try {
                            chunkRadius = Integer.parseInt(argument.split(":")[1]);
                        } catch (NumberFormatException e) {
                            searchText.append(argument).append(" ");
                        }
                    } else {
                        searchText.append(argument).append(" ");
                    }
                }
                String search = StringUtils.removeEnd(searchText.toString(), " ");
                player.spigot().sendMessage(new MineDown("[Searching for matching signs...](gray)").toComponent());
                search(search, player.getLocation(), player, chunkRadius);
            } else {
                player.spigot().sendMessage(new MineDown("[Error:](#ff3300) [Incorrect syntax. Usage: " + command.getUsage() + "](#ff7e5e)").toComponent());
            }
            return true;
        }
        return false;
    }
}
