package moe.gensoukyo.umarace.manager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.util.*;
public class TrackCreationManager {
    private final Map<UUID, List<Vec3>> temporaryTracks = new HashMap<>();
    private static final TrackCreationManager INSTANCE = new TrackCreationManager();
    private TrackCreationManager() {}
    public static TrackCreationManager getInstance() {
        return INSTANCE;
    }
    public void addWaypoint(Player player, Vec3 waypoint) {
        temporaryTracks.computeIfAbsent(player.getUUID(), k -> new ArrayList<>()).add(waypoint);
    }
    public List<Vec3> getWaypoints(Player player) {
        return temporaryTracks.getOrDefault(player.getUUID(), Collections.emptyList());
    }
    public List<Vec3> clearAndGetWaypoints(Player player) {
        return temporaryTracks.remove(player.getUUID());
    }
}