package milkucha.trmt;

import milkucha.trmt.erosion.ErosionMapManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class TRMTPlugin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    public static TRMTPlugin INSTANCE;
    public static Logger LOGGER;

    @Override
    public void onEnable() {
        INSTANCE = this;
        LOGGER = this.getLogger();

        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        
        // Register this class as both the Executor and the Tab Completer
        this.getCommand("trmt").setExecutor(this);
        this.getCommand("trmt").setTabCompleter(this);

        ErosionMapManager.getInstance().loadState();
        LOGGER.info("[TRMT] Server-Side Plugin Initialized successfully.");
    }

    @Override
    public void onDisable() {
        ErosionMapManager.getInstance().saveState();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && 
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Block standingOn = event.getTo().getBlock().getRelative(BlockFace.DOWN);
        float stepWeight = event.getPlayer().isSprinting() ? 1.5f : 1.0f;

        ErosionMapManager.getInstance().onStep(
            standingOn, 
            stepWeight, 
            standingOn.getWorld().getFullTime()
        );
    }

    @EventHandler
    public void onBlockPlaceAttempt(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null || !event.getItem().getType().isBlock()) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        Block blockBelow = clickedBlock.getRelative(event.getBlockFace());
        Block targetedErosionBlock = blockBelow.getRelative(0, -1, 0);

        if (ErosionMapManager.getInstance().isErodedBlock(targetedErosionBlock.getLocation())) {
            event.setCancelled(true);
        }
    }

    // --- COMMAND HANDLING ---
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;
        if (!sender.hasPermission("trmt.admin")) {
            sender.sendMessage(ChatColor.RED + "You lack trmt.admin permissions.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setspeed":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /trmt setspeed <minSteps> <maxSteps>");
                    return true;
                }
                try {
                    double min = Double.parseDouble(args[1]);
                    double max = Double.parseDouble(args[2]);

                    if (min < 0 || max < 0 || max < min) {
                        sender.sendMessage(ChatColor.RED + "Invalid values. Max must be greater than min.");
                        return true;
                    }

                    getConfig().set("erosion-speed.min", min);
                    getConfig().set("erosion-speed.max", max);
                    saveConfig();

                    sender.sendMessage(ChatColor.GREEN + "[TRMT] Erosion speed thresholds updated to: " + min + " - " + max + " steps.");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Please provide valid numeric inputs.");
                }
                return true;

            case "reloadconfig":
                reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "[TRMT] Config reloaded.");
                return true;

            case "convert-to-vanilla":
                if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
                    ErosionMapManager.getInstance().convertAllErodedToVanilla();
                    sender.sendMessage(ChatColor.GREEN + "[TRMT] Wiped step memory cache.");
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "[TRMT] WARNING: This wipes step history. Blocks stay frozen as their current block variant.");
                    sender.sendMessage(ChatColor.GOLD + "Run /trmt convert-to-vanilla confirm to proceed.");
                }
                return true;

            case "eroded-chunks":
                int totalActive = ErosionMapManager.getInstance().getTrackingCount();
                sender.sendMessage(ChatColor.GREEN + "[TRMT] Tracking " + totalActive + " active unique block coordinates.");
                return true;
        }
        return false;
    }

    // --- AUTOFILL / TAB COMPLETION ---
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Create an empty list to store suggestions
        List<String> completions = new ArrayList<>();
        
        // Security check: Only suggest commands if they have permission
        if (!sender.hasPermission("trmt.admin")) {
            return completions;
        }

        // Handle the first argument: /trmt <arg>
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("setspeed", "reloadconfig", "convert-to-vanilla", "eroded-chunks");
            // Filters choices based on what the player has already typed so far
            StringUtil.copyPartialMatches(args[0], subCommands, completions);
            Collections.sort(completions);
            return completions;
        }

        // Handle the second argument: /trmt convert-to-vanilla <confirm>
        if (args.length == 2 && args[0].equalsIgnoreCase("convert-to-vanilla")) {
            List<String> confirmations = Collections.singletonList("confirm");
            StringUtil.copyPartialMatches(args[1], confirmations, completions);
            return completions;
        }

        // Suggest placeholder info text for setspeed arguments
        if (args.length == 2 && args[0].equalsIgnoreCase("setspeed")) {
            return Collections.singletonList("<minSteps>");
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("setspeed")) {
            return Collections.singletonList("<maxSteps>");
        }

        return completions;
    }
}