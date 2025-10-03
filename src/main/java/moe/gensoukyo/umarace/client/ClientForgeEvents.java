package moe.gensoukyo.umarace.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import moe.gensoukyo.umarace.UmaRace;
import moe.gensoukyo.umarace.manager.TrackCreationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import java.util.List;
@EventBusSubscriber(modid = UmaRace.MODID, value = Dist.CLIENT)
public class ClientForgeEvents {
    private static final float TRACK_WIDTH = 18.0f;
    private static final List<Vec3> AXIS_DIRECTIONS = List.of(
            new Vec3(1, 0, 0), new Vec3(-1, 0, 0),
            new Vec3(0, 0, 1), new Vec3(0, 0, -1),
            new Vec3(1, 0, 1).normalize(), new Vec3(1, 0, -1).normalize(),
            new Vec3(-1, 0, 1).normalize(), new Vec3(-1, 0, -1).normalize()
    );
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Player player = Minecraft.getInstance().player;
        if (player == null || !player.isHolding(UmaRace.TRACK_WAND.get())) return;

        // Use the client-side data cache instead of the server-side manager
        ClientTrackCreationData ctd = ClientTrackCreationData.getInstance();
        List<Vec3> controlPoints = ctd.getPointsToRender();

        if (controlPoints.isEmpty()) return;
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-camPos.x(), -camPos.y(), -camPos.z());
        PoseStack.Pose pose = poseStack.last();
        if (controlPoints.size() == 1) {
            Vec3 p1 = controlPoints.get(0);
            float guideLineLength = 128.0f;
            for (Vec3 dir : AXIS_DIRECTIONS) {
                Vec3 end = p1.add(dir.scale(guideLineLength));
                drawLine(vertexConsumer, pose, p1, end, 0.5f, 0.5f, 0.5f, 0.4f);
            }
        }
        for (int i = 0; i < controlPoints.size(); i++) {
            float r = (i == 0) ? 0.0f : 1.0f;
            float g = (i == 1) ? 1.0f : 0.5f;
            float b = (i == 2) ? 1.0f : 0.0f;
            drawBox(vertexConsumer, pose, controlPoints.get(i), 0.3f, r, g, b, 1.0f);
        }
        if (controlPoints.size() == 2) {
            drawLine(vertexConsumer, pose, controlPoints.get(0), controlPoints.get(1), 1.0f, 1.0f, 0.0f, 1.0f);
        }
        if (controlPoints.size() == 3) {
            // We need to re-calculate the waypoints on the client for rendering
            // For this, we can make generateStadiumWaypoints public and accessible
            // Or better, we can copy a simplified version if it becomes complex
            // For now, let's assume direct access for simplicity.
            TrackCreationManager tcm = TrackCreationManager.getInstance();
            List<Vec3> centerWaypoints = tcm.generateStadiumWaypoints(controlPoints);
            if (!centerWaypoints.isEmpty()) {
                for (int i = 0; i < centerWaypoints.size(); i++) {
                    Vec3 p1 = centerWaypoints.get(i);
                    Vec3 p2 = centerWaypoints.get((i + 1) % centerWaypoints.size());
                    Vec3 tangent = p2.subtract(p1).normalize();
                    Vec3 normal = tangent.cross(new Vec3(0, 1, 0)).normalize().scale(TRACK_WIDTH / 2.0);
                    Vec3 p1_inner = p1.subtract(normal);
                    Vec3 p2_inner = p2.subtract(normal);
                    Vec3 p1_outer = p1.add(normal);
                    Vec3 p2_outer = p2.add(normal);
                    drawLine(vertexConsumer, pose, p1_inner, p2_inner, 1.0f, 1.0f, 1.0f, 0.8f);
                    drawLine(vertexConsumer, pose, p1_outer, p2_outer, 1.0f, 1.0f, 1.0f, 0.8f);
                }
            }
        }
        poseStack.popPose();
        bufferSource.endBatch(RenderType.lines());
    }
    private static void drawLine(VertexConsumer consumer, PoseStack.Pose pose, Vec3 start, Vec3 end, float r, float g, float b, float a) {
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        Vector4f startPos = new Vector4f((float)start.x, (float)start.y, (float)start.z, 1.0f).mul(poseMatrix);
        Vector4f endPos = new Vector4f((float)end.x, (float)end.y, (float)end.z, 1.0f).mul(poseMatrix);
        Vector3f normal = new Vector3f(0.0f, 1.0f, 0.0f).mul(normalMatrix);
        consumer.addVertex(startPos.x(), startPos.y(), startPos.z())
                .setColor(r, g, b, a)
                .setNormal(normal.x(), normal.y(), normal.z());
        consumer.addVertex(endPos.x(), endPos.y(), endPos.z())
                .setColor(r, g, b, a)
                .setNormal(normal.x(), normal.y(), normal.z());
    }
    private static void drawBox(VertexConsumer consumer, PoseStack.Pose pose, Vec3 center, float size, float r, float g, float b, float a) {
        float half = size / 2.0f;
        Vec3 p1 = center.add(-half, -half, -half);
        Vec3 p2 = center.add(half, -half, -half);
        Vec3 p3 = center.add(half, -half, half);
        Vec3 p4 = center.add(-half, -half, half);
        Vec3 p5 = center.add(-half, half, -half);
        Vec3 p6 = center.add(half, half, -half);
        Vec3 p7 = center.add(half, half, half);
        Vec3 p8 = center.add(-half, half, half);
        drawLine(consumer, pose, p1, p2, r, g, b, a);
        drawLine(consumer, pose, p2, p3, r, g, b, a);
        drawLine(consumer, pose, p3, p4, r, g, b, a);
        drawLine(consumer, pose, p4, p1, r, g, b, a);
        drawLine(consumer, pose, p5, p6, r, g, b, a);
        drawLine(consumer, pose, p6, p7, r, g, b, a);
        drawLine(consumer, pose, p7, p8, r, g, b, a);
        drawLine(consumer, pose, p8, p5, r, g, b, a);
        drawLine(consumer, pose, p1, p5, r, g, b, a);
        drawLine(consumer, pose, p2, p6, r, g, b, a);
        drawLine(consumer, pose, p3, p7, r, g, b, a);
        drawLine(consumer, pose, p4, p8, r, g, b, a);
    }
}