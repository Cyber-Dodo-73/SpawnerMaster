package fr.cyberdodo.spawnerMaster.listener;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.EntityType;

public class SpawnerPlaceListener implements Listener {

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent event) {
        // Vérifier si le bloc placé est un spawner
        if (event.getBlockPlaced().getType() == Material.SPAWNER) {
            ItemStack item = event.getItemInHand();
            ItemMeta meta = item.getItemMeta();

            if (meta instanceof BlockStateMeta) {
                BlockStateMeta blockStateMeta = (BlockStateMeta) meta;
                BlockState state = blockStateMeta.getBlockState();

                if (state instanceof CreatureSpawner) {
                    CreatureSpawner spawner = (CreatureSpawner) event.getBlockPlaced().getState();
                    CreatureSpawner itemSpawner = (CreatureSpawner) state;

                    // Appliquer le type de mob du spawner depuis l'item au spawner placé
                    spawner.setSpawnedType(itemSpawner.getSpawnedType());
                    spawner.update();

                    // Optionnel : Envoyer un message au joueur
                    // event.getPlayer().sendMessage("§aVous avez placé un spawner de " + itemSpawner.getSpawnedType().name().toLowerCase() + " !");
                }
            }
        }
    }
}
