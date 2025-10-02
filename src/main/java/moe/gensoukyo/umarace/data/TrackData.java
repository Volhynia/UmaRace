package moe.gensoukyo.umarace.data;
import moe.gensoukyo.umarace.UmaRace;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class TrackData extends SavedData {
    public static final String DATA_NAME = UmaRace.MODID + "_tracks";
    private final Map<String, List<Vec3>> tracks = new HashMap<>();
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag, @NotNull HolderLookup.Provider provider) {
        CompoundTag tracksTag = new CompoundTag();
        tracks.forEach((name, waypoints) -> {
            ListTag waypointsTag = new ListTag();
            for (Vec3 point : waypoints) {
                CompoundTag pointTag = new CompoundTag();
                pointTag.putDouble("x", point.x);
                pointTag.putDouble("y", point.y);
                pointTag.putDouble("z", point.z);
                waypointsTag.add(pointTag);
            }
            tracksTag.put(name, waypointsTag);
        });
        compoundTag.put("tracks", tracksTag);
        return compoundTag;
    }
    public static TrackData load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        TrackData data = new TrackData();
        CompoundTag tracksTag = compoundTag.getCompound("tracks");
        for (String trackName : tracksTag.getAllKeys()) {
            List<Vec3> waypoints = new ArrayList<>();
            ListTag waypointsTag = tracksTag.getList(trackName, Tag.TAG_COMPOUND);
            for (int i = 0; i < waypointsTag.size(); i++) {
                CompoundTag pointTag = waypointsTag.getCompound(i);
                double x = pointTag.getDouble("x");
                double y = pointTag.getDouble("y");
                double z = pointTag.getDouble("z");
                waypoints.add(new Vec3(x, y, z));
            }
            data.tracks.put(trackName, waypoints);
        }
        return data;
    }
    public static TrackData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(TrackData::new, TrackData::load, null),
                DATA_NAME
        );
    }
    public void addTrack(String name, List<Vec3> waypoints) {
        tracks.put(name, waypoints);
        setDirty(); 
    }
    public Map<String, List<Vec3>> getAllTracks() {
        return tracks;
    }
}