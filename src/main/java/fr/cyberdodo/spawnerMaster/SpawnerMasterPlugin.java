package fr.cyberdodo.spawnerMaster;


import fr.cyberdodo.spawnerMaster.listener.SpawnerBreakListener;
import fr.cyberdodo.spawnerMaster.listener.SpawnerSpawnListener;
import fr.cyberdodo.spawnerMaster.listener.SpawnerPlaceListener;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnerMasterPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Créer une instance de SpawnerSpawnListener
        SpawnerSpawnListener spawnerSpawnListener = new SpawnerSpawnListener(this);

        // Enregistrer les Listeners
        getServer().getPluginManager().registerEvents(spawnerSpawnListener, this);
        getServer().getPluginManager().registerEvents(new SpawnerBreakListener(spawnerSpawnListener), this);
        getServer().getPluginManager().registerEvents(new SpawnerPlaceListener(), this);
    }

    @Override
    public void onDisable() {
        // Code qui s'exécute à l'arrêt du plugin
    }
}