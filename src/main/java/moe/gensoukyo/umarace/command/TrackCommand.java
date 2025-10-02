package moe.gensoukyo.umarace.command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import moe.gensoukyo.umarace.data.TrackData;
import moe.gensoukyo.umarace.manager.TrackCreationManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import java.util.List;
public class TrackCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("track")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("save")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> saveTrack(context.getSource(), StringArgumentType.getString(context, "name")))))
                .then(Commands.literal("clear")
                        .executes(context -> clearTemporaryTrack(context.getSource())))
        );
    }

    private static int saveTrack(CommandSourceStack source, String name) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            TrackCreationManager tcm = TrackCreationManager.getInstance();
            List<Vec3> controlPoints = tcm.getControlPoints(player);

            if (controlPoints.size() < 3) {
                source.sendFailure(Component.literal("错误: 需要设置3个控制点才能保存赛道。"));
                return 0;
            }

            // 在服务器端最终生成路径点
            List<Vec3> waypoints = tcm.generateStadiumWaypoints(controlPoints);
            if (waypoints.isEmpty()) {
                source.sendFailure(Component.literal("错误: 无效的控制点，无法生成赛道 (例如半径过小或点重合)。"));
                return 0;
            }

            TrackData trackData = TrackData.get(player.serverLevel());
            trackData.addTrack(name, waypoints);

            tcm.clearAndGetControlPoints(player); // 清除临时点

            source.sendSuccess(() -> Component.literal("体育场赛道 '" + name + "' 已保存，包含 " + waypoints.size() + " 个路径点。"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("错误: 只有玩家才能执行此命令。"));
            return 0;
        }
    }

    private static int clearTemporaryTrack(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            TrackCreationManager tcm = TrackCreationManager.getInstance();
            List<Vec3> clearedPoints = tcm.clearAndGetControlPoints(player);

            if (clearedPoints == null || clearedPoints.isEmpty()) {
                source.sendSuccess(() -> Component.literal("没有需要清除的临时控制点。"), false);
            } else {
                source.sendSuccess(() -> Component.literal("已清除 " + clearedPoints.size() + " 个临时控制点。"), true);
            }
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("错误: 只有玩家才能执行此命令。"));
            return 0;
        }
    }
}