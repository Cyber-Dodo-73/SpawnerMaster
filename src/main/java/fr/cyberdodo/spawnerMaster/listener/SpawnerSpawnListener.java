package fr.cyberdodo.spawnerMaster.listener;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class SpawnerSpawnListener implements Listener {

    private final Random random = new Random();
    private final JavaPlugin plugin;

    // Map pour garder une trace des spawners actifs et de leurs tâches
    private final Map<Location, BukkitTask> activeSpawners = new HashMap<>();

    public SpawnerSpawnListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        // Annuler le spawn par défaut
        event.setCancelled(true);

        // Obtenir le type de mob du spawner
        EntityType entityType = event.getEntityType();

        // Obtenir la position du spawner
        Location spawnerLocation = event.getSpawner().getLocation();

        // Vérifier si une tâche est déjà en cours pour ce spawner
        if (activeSpawners.containsKey(spawnerLocation)) {
            return; // Ne pas démarrer une nouvelle tâche si une est déjà en cours
        }

        // Obtenir le monde du spawner
        World world = spawnerLocation.getWorld();
        if (world == null) {
            return;
        }

        // Jouer le son de préparation au spawn
        world.playSound(spawnerLocation, Sound.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, 1.0f, 1.0f);

        // Lancer les particules de flammes tournant autour du spawner pendant 2 secondes
        BukkitTask task = new BukkitRunnable() {

            double angle = 0;
            int count = 0;
            final int maxCount = 40; // 40 ticks (2 secondes)

            @Override
            public void run() {
                if (count >= maxCount) {
                    // Arrêter les particules et spawner les mobs
                    spawnMobs(entityType, spawnerLocation);
                    activeSpawners.remove(spawnerLocation);
                    cancel();
                    return;
                }

                // Générer les particules autour du spawner
                double radius = 1.5; // Rayon du cercle autour du bloc

                // Générer plusieurs particules pour un effet plus fluide
                for (int i = 0; i < 10; i++) {
                    angle += Math.PI / 16;

                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);

                    // Positionner les particules autour du centre du bloc du spawner
                    Location particleLocation = spawnerLocation.clone().add(0.5 + x, 0.5, 0.5 + z);

                    world.spawnParticle(Particle.FLAME, particleLocation, 0, 0, 0, 0);
                }

                count++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // Démarre immédiatement, répète chaque tick

        // Stocker la tâche dans la Map
        activeSpawners.put(spawnerLocation, task);
    }

    private void spawnMobs(EntityType entityType, Location spawnerLocation) {
        World world = spawnerLocation.getWorld();
        if (world == null) {
            return;
        }

        // Nombre de mobs à spawner selon les probabilités
        int mobCount = getRandomMobCount();

        // Stocker les positions déjà utilisées pour éviter les doublons
        Set<Vector> usedPositions = new HashSet<>();

        for (int i = 0; i < mobCount; i++) {
            Location spawnLocation = getRandomValidLocationAroundSpawner(spawnerLocation, usedPositions);

            if (spawnLocation != null) {
                // Ajouter la position à l'ensemble des positions utilisées
                usedPositions.add(spawnLocation.toVector());

                // Spawner le mob à la position choisie
                world.spawnEntity(spawnLocation, entityType);

                // Jouer un son à la position du mob
                world.playSound(spawnLocation, Sound.BLOCK_TRIAL_SPAWNER_DETECT_PLAYER, 1.0f, 1.0f);

                // Particules à la position du mob
                world.spawnParticle(Particle.CLOUD, spawnLocation, 10, 0.2, 0.5, 0.2, 0.1);
            }
        }
    }

    private int getRandomMobCount() {
        double chance = random.nextDouble() * 100; // Génère un nombre entre 0 et 100
        if (chance < 8.0) { // 8% de chance
            return 6 + random.nextInt(6); // Retourne 6 à 11
        } else {
            return 3 + random.nextInt(5); // Retourne 3 à 7
        }
    }

    private Location getRandomValidLocationAroundSpawner(Location spawnerLocation, Set<Vector> usedPositions) {
        Location spawnLocation = null;
        int attempts = 0;
        while (attempts < 10) {
            attempts++;

            // Générer des offsets aléatoires entre -5 et 5
            double offsetX = -5 + random.nextDouble() * 10;
            double offsetZ = -5 + random.nextDouble() * 10;

            // Créer la nouvelle position
            Location candidateLocation = spawnerLocation.clone().add(offsetX, 0, offsetZ);

            // Arrondir les positions pour éviter les positions trop proches
            candidateLocation.setX(candidateLocation.getBlockX() + 0.5);
            candidateLocation.setZ(candidateLocation.getBlockZ() + 0.5);

            // Garder la même hauteur que le spawner
            candidateLocation.setY(spawnerLocation.getY());

            // Vérifier que la position n'a pas déjà été utilisée
            if (usedPositions.contains(candidateLocation.toVector())) {
                continue;
            }

            // Vérifier que le bloc à la position est de l'air
            if (!candidateLocation.getBlock().getType().isAir()) {
                continue;
            }

            // Vérifier que le bloc au-dessus est de l'air
            Location blockAbove = candidateLocation.clone().add(0, 1, 0);
            if (!blockAbove.getBlock().getType().isAir()) {
                continue;
            }

            // Optionnel : Vérifier que les blocs adjacents ne sont pas solides
            boolean obstructed = false;
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) continue;
                    Location adjacentBlock = candidateLocation.clone().add(x, 0, z);
                    if (!adjacentBlock.getBlock().getType().isAir()) {
                        obstructed = true;
                        break;
                    }
                }
                if (obstructed) break;
            }
            if (obstructed) {
                continue;
            }

            // La position est valide
            spawnLocation = candidateLocation;
            break;
        }
        return spawnLocation;
    }


    // Méthode pour annuler la tâche de spawn lorsque le spawner est cassé
    public boolean cancelSpawnTask(Location spawnerLocation) {
        BukkitTask task = activeSpawners.remove(spawnerLocation);
        if (task != null) {
            task.cancel();
            return true;
        }
        return false;
    }
}
