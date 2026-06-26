package com.tuservidor.vanillaplus;

import com.tuservidor.vanillaplus.command.HomeCommands;
import com.tuservidor.vanillaplus.command.TpaCommands;
import com.tuservidor.vanillaplus.command.UnlockCommand;
import com.tuservidor.vanillaplus.combat.CombatModifierListener;
import com.tuservidor.vanillaplus.combat.PotionListener;
import com.tuservidor.vanillaplus.combat.ProjectileListener;
import com.tuservidor.vanillaplus.config.ConfigManager;
import com.tuservidor.vanillaplus.config.MessageManager;
import com.tuservidor.vanillaplus.drop.DropManager;
import com.tuservidor.vanillaplus.fishing.FishingManager;
import com.tuservidor.vanillaplus.item.ItemFactory;
import com.tuservidor.vanillaplus.plant.PlantManager;
import com.tuservidor.vanillaplus.recipe.CraftingGuardListener;
import com.tuservidor.vanillaplus.recipe.RecipeRegistry;
import com.tuservidor.vanillaplus.set.SetManager;
import com.tuservidor.vanillaplus.teleport.HomeManager;
import com.tuservidor.vanillaplus.teleport.TpaManager;
import com.tuservidor.vanillaplus.unlock.UnlockManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class VanillaPlusPlugin extends JavaPlugin {
    private ConfigManager configManager;
    private MessageManager messageManager;
    private ItemFactory itemFactory;
    private UnlockManager unlockManager;
    private RecipeRegistry recipeRegistry;
    private SetManager setManager;
    private HomeManager homeManager;
    private TpaManager tpaManager;
    private PlantManager plantManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.load();
        messageManager = new MessageManager(configManager);
        itemFactory = new ItemFactory();

        unlockManager = new UnlockManager(this);
        recipeRegistry = new RecipeRegistry(this, itemFactory);
        recipeRegistry.registerAll();
        unlockManager.setRecipeRegistry(recipeRegistry);

        setManager = new SetManager(this, configManager, itemFactory);
        homeManager = new HomeManager(this, configManager, messageManager, unlockManager, itemFactory);
        tpaManager = new TpaManager(this, configManager, messageManager, unlockManager);
        plantManager = new PlantManager(this, configManager, unlockManager, itemFactory);
        plantManager.start();

        registerEvents();
        registerCommands();
        getLogger().info("VanillaPlusProgression cargado correctamente.");
    }

    @Override
    public void onDisable() {
        if (setManager != null) setManager.shutdown();
        if (homeManager != null) homeManager.save();
        if (tpaManager != null) tpaManager.shutdown();
        if (plantManager != null) plantManager.shutdown();
        getLogger().info("VanillaPlusProgression apagado correctamente.");
    }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        register(pm, unlockManager);
        register(pm, new CraftingGuardListener(recipeRegistry, unlockManager, messageManager, itemFactory));
        register(pm, setManager);
        register(pm, new CombatModifierListener(setManager));
        register(pm, new ProjectileListener(setManager));
        register(pm, new PotionListener(this, setManager));
        register(pm, new DropManager(configManager, unlockManager, itemFactory));
        register(pm, new FishingManager(this, configManager, unlockManager, itemFactory));
        register(pm, homeManager);
        register(pm, tpaManager);
        register(pm, plantManager);
    }

    private void register(PluginManager pm, Listener listener) {
        pm.registerEvents(listener, this);
    }

    private void registerCommands() {
        UnlockCommand unlockCommand = new UnlockCommand(unlockManager, messageManager);
        setTabExecutor("unlockpp", unlockCommand);
        HomeCommands homeCommands = new HomeCommands(homeManager);
        setTabExecutor("sethome", homeCommands);
        setTabExecutor("home", homeCommands);
        setTabExecutor("delhome", homeCommands);
        setTabExecutor("homes", homeCommands);
        TpaCommands tpaCommands = new TpaCommands(tpaManager);
        setTabExecutor("tpa", tpaCommands);
        setTabExecutor("tpaccept", tpaCommands);
        setTabExecutor("tpdeny", tpaCommands);
    }

    private void setTabExecutor(String commandName, TabExecutor executor) {
        PluginCommand command = getCommand(commandName);
        if (command == null) {
            getLogger().warning("Comando no encontrado en plugin.yml: " + commandName);
            return;
        }
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    public ConfigManager configManager() { return configManager; }
    public MessageManager messageManager() { return messageManager; }
    public ItemFactory itemFactory() { return itemFactory; }
    public UnlockManager unlockManager() { return unlockManager; }
    public RecipeRegistry recipeRegistry() { return recipeRegistry; }
    public SetManager setManager() { return setManager; }
}
