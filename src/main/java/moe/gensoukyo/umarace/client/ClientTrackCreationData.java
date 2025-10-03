package moe.gensoukyo.umarace.client;

import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientTrackCreationData {
    private static final ClientTrackCreationData INSTANCE = new ClientTrackCreationData();
    private List<Vec3> controlPointsToRender = new ArrayList<>();

    private ClientTrackCreationData() {}

    public static ClientTrackCreationData getInstance() {
        return INSTANCE;
    }

    public void setControlPoints(List<Vec3> points) {
        this.controlPointsToRender = new ArrayList<>(points);
    }

    public List<Vec3> getPointsToRender() {
        return Collections.unmodifiableList(this.controlPointsToRender);
    }
}