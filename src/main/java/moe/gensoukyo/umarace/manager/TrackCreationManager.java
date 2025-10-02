package moe.gensoukyo.umarace.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TrackCreationManager {
    private final Map<UUID, List<Vec3>> temporaryControlPoints = new ConcurrentHashMap<>();
    private static final TrackCreationManager INSTANCE = new TrackCreationManager();

    private static final double TRACK_WIDTH = 18.0;
    private static final double HALF_WIDTH = TRACK_WIDTH / 2.0;

    private TrackCreationManager() {}
    public static TrackCreationManager getInstance() {
        return INSTANCE;
    }

    /**
     * 为玩家添加控制点，并根据点的数量执行不同逻辑（对齐、重置等）.
     * @param player 玩家
     * @param point  点击的点
     */
    public void addControlPoint(Player player, Vec3 point) {
        List<Vec3> points = temporaryControlPoints.computeIfAbsent(player.getUUID(), k -> new ArrayList<>());

        // 使用 switch 语句来清晰地管理状态
        switch (points.size()) {
            case 0:
                // 状态: 没有点 -> 添加第1个点
                points.add(point);
                break;
            case 1:
                // 状态: 有1个点 -> 添加第2个点 (需对齐)
                Vec3 p1 = points.get(0);
                Vec3 snappedP2 = snapPointToAxis(p1, point);
                points.add(snappedP2);
                break;
            case 2:
                // 状态: 有2个点 -> 添加第3个点
                points.add(point);
                break;
            default: // 状态: 有3个或更多点 -> 重置
                points.clear();
                points.add(point);
                break;
        }
    }

    private Vec3 snapPointToAxis(Vec3 p1, Vec3 rawP2) {
        Vec3 delta = rawP2.subtract(p1);
        double dx = delta.x;
        double dz = delta.z;
        double absDx = Math.abs(dx);
        double absDz = Math.abs(dz);

        double snappedX, snappedZ;

        // tan(22.5°) ≈ 0.414, tan(67.5°) ≈ 2.414
        if (absDx > absDz * 2.414) { // 接近水平
            snappedX = rawP2.x;
            snappedZ = p1.z;
        } else if (absDz > absDx * 2.414) { // 接近垂直
            snappedX = p1.x;
            snappedZ = rawP2.z;
        } else { // 接近对角线
            double avgDist = (absDx + absDz) / 2.0;
            snappedX = p1.x + Math.signum(dx) * avgDist;
            snappedZ = p1.z + Math.signum(dz) * avgDist;
        }

        return Vec3.atCenterOf(BlockPos.containing(snappedX, rawP2.y, snappedZ));
    }

    public List<Vec3> getControlPoints(Player player) {
        return temporaryControlPoints.getOrDefault(player.getUUID(), Collections.emptyList());
    }

    public List<Vec3> clearAndGetControlPoints(Player player) {
        return temporaryControlPoints.remove(player.getUUID());
    }

    public List<Vec3> generateStadiumWaypoints(List<Vec3> controlPoints) {
        // ... (此方法无需修改，保持原样)
        if (controlPoints.size() != 3) {
            return Collections.emptyList();
        }
        Vec3 p1_inner = controlPoints.get(0);
        Vec3 p2_inner = controlPoints.get(1);
        Vec3 p3_inner = controlPoints.get(2);
        double baseY = p1_inner.y;
        Vec3 p1_inner_flat = new Vec3(p1_inner.x, baseY, p1_inner.z);
        Vec3 p2_inner_flat = new Vec3(p2_inner.x, baseY, p2_inner.z);
        Vec3 p3_inner_flat = new Vec3(p3_inner.x, baseY, p3_inner.z);
        Vec3 straightVec = p2_inner_flat.subtract(p1_inner_flat);
        if (straightVec.lengthSqr() < 1e-6) return Collections.emptyList();
        Vec3 straightDir = straightVec.normalize();
        double straightLen = straightVec.length();
        Vec3 rightDir = straightDir.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 p2_to_p3 = p3_inner_flat.subtract(p2_inner_flat);
        Vec3 insideNormal = rightDir.scale(Math.signum(p2_to_p3.dot(rightDir)));
        double radius_inner = Math.abs(p2_to_p3.dot(rightDir));
        if (radius_inner < 1.0) return Collections.emptyList();
        double radius_center = radius_inner + HALF_WIDTH;
        Vec3 p1_center_flat = p1_inner_flat.subtract(insideNormal.scale(HALF_WIDTH));
        Vec3 p2_center_flat = p2_inner_flat.subtract(insideNormal.scale(HALF_WIDTH));
        Vec3 c1_center = p2_center_flat.add(insideNormal.scale(radius_center));
        Vec3 c2_center = p1_center_flat.add(insideNormal.scale(radius_center));
        Vec3 p_start_straight_2_center = p2_center_flat.add(insideNormal.scale(radius_center * 2));
        Vec3 p_end_straight_2_center = p1_center_flat.add(insideNormal.scale(radius_center * 2));
        List<Vec3> waypoints = new ArrayList<>();
        double segmentLength = 4.0;
        for (double d = 0; d < straightLen; d += segmentLength) {
            waypoints.add(p1_center_flat.add(straightDir.scale(d)));
        }
        waypoints.add(p2_center_flat);
        double curveLen = Math.PI * radius_center;
        int curvePoints = (int) Math.max(1, curveLen / segmentLength);
        for (int i = 1; i <= curvePoints; i++) {
            double angle = (Math.PI * i) / curvePoints;
            Vec3 pointOnCurve = c1_center
                    .add(insideNormal.scale(-Math.cos(angle) * radius_center))
                    .add(straightDir.scale(Math.sin(angle) * radius_center));
            waypoints.add(pointOnCurve);
        }
        for (double d = 0; d < straightLen; d += segmentLength) {
            waypoints.add(p_start_straight_2_center.add(straightDir.scale(-d)));
        }
        waypoints.add(p_end_straight_2_center);
        curveLen = Math.PI * radius_center;
        curvePoints = (int) Math.max(1, curveLen / segmentLength);
        for (int i = 1; i < curvePoints; i++) {
            double angle = Math.PI + (Math.PI * i) / curvePoints;
            Vec3 pointOnCurve = c2_center
                    .add(insideNormal.scale(-Math.cos(angle) * radius_center))
                    .add(straightDir.scale(Math.sin(angle) * radius_center));
            waypoints.add(pointOnCurve);
        }
        return waypoints;
    }
}