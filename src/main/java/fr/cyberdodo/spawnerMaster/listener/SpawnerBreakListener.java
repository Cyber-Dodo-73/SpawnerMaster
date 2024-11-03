package fr.cyberdodo.spawnerMaster.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class SpawnerBreakListener implements Listener {

    private final SpawnerSpawnListener spawnerSpawnListener;

    public SpawnerBreakListener(SpawnerSpawnListener spawnerSpawnListener) {
        this.spawnerSpawnListener = spawnerSpawnListener;
    }

    @EventHandler
    public void onSpawnerBreak(BlockBreakEvent event) {
        // Vérifier si le bloc cassé est un spawner
        if (event.getBlock().getType() == Material.SPAWNER) {
            Location spawnerLocation = event.getBlock().getLocation();

            // Annuler la tâche de spawn si elle existe
            spawnerSpawnListener.cancelSpawnTask(spawnerLocation);


            // Vérifier si le joueur utilise un outil avec Silk Touch
            if (event.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH) && !event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                // Annuler le drop par défaut du spawner
                event.setExpToDrop(0);
                event.setDropItems(false);

                // Obtenir le type de mob du spawner
                CreatureSpawner spawner = (CreatureSpawner) event.getBlock().getState();
                EntityType spawnerType = spawner.getSpawnedType();

                // Créer un item spawner avec le type de mob associé
                ItemStack spawnerItem = new ItemStack(Material.SPAWNER, 1);
                BlockStateMeta meta = (BlockStateMeta) spawnerItem.getItemMeta();

                if (meta != null) {
                    CreatureSpawner spawnerState = (CreatureSpawner) meta.getBlockState();
                    spawnerState.setSpawnedType(spawnerType);
                    meta.setBlockState(spawnerState);
                    spawnerItem.setItemMeta(meta);
                }

                // Droper le spawner à la position du bloc cassé
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), spawnerItem);
            }
        }
    }
}
