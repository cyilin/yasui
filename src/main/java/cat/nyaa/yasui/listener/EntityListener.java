package cat.nyaa.yasui.listener;


import cat.nyaa.yasui.Yasui;
import cat.nyaa.yasui.config.Operation;
import cat.nyaa.yasui.other.ModuleType;
import cat.nyaa.yasui.other.NoAIType;
import cat.nyaa.yasui.other.Utils;
import cat.nyaa.yasui.task.ChunkTask;
import cat.nyaa.yasui.task.TPSMonitor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import java.util.Map;

public class EntityListener implements Listener {
    private Yasui plugin;

    public EntityListener(Yasui pl) {
        pl.getServer().getPluginManager().registerEvents(this, pl);
        this.plugin = pl;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent event) {
        Map<ModuleType, Operation> map = TPSMonitor.worldLimits.get(event.getEntity().getWorld().getName());
        if (map != null) {
            Operation entity_culler = map.get(ModuleType.entity_culler);
            Operation entity_ai_suppressor = map.get(ModuleType.entity_ai_suppressor);
            if (entity_culler != null || entity_ai_suppressor != null) {
                ChunkTask task = ChunkTask.getOrCreateTask(event.getLocation().getChunk());
                if (entity_culler != null && entity_culler.entity_culler_per_chunk_limit <= task.LivingEntityCount && !entity_culler.entity_culler_excluded_type.contains(event.getEntityType())) {
                    event.setCancelled(true);
                    return;
                }
                Utils.setAI(event.getEntity(), entity_ai_suppressor == null, entity_ai_suppressor);
                task.LivingEntityCount++;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() != null && event.getEntity() instanceof LivingEntity) {
            Map<ModuleType, Operation> l = TPSMonitor.worldLimits.get(event.getEntity().getWorld().getName());
            Operation o = l.get(ModuleType.entity_ai_suppressor);
            Entity entity = event.getEntity();
            if (o != null && o.entity_ai_suppresse_method == NoAIType.NO_TARGETING
                    && !o.entity_ai_suppressor_exclude_type.contains(entity.getType())
                    && !(o.entity_ai_suppressor_excluded_tagged && entity.getCustomName() != null)
                    && !(o.entity_ai_suppressor_excluded_tamed && entity instanceof Tameable && ((Tameable) entity).getOwner() != null)) {
                event.setCancelled(true);
            }
        }
    }
}