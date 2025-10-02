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
            List<Vec3> waypoints = TrackCreationManager.getInstance().getWaypoints(player);
            if (waypoints.size() < 2) {
                source.sendFailure(Component.literal("错误: 赛道至少需要2个路径点才能保存。"));
                return 0;
            }
            TrackData trackData = TrackData.get(player.serverLevel());
            trackData.addTrack(name, waypoints);
            TrackCreationManager.getInstance().clearAndGetWaypoints(player);
            source.sendSuccess(() -> Component.literal("赛道 '" + name + "' 已保存，包含 " + waypoints.size() + " 个路径点。"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("错误: 只有玩家才能执行此命令。"));
            return 0;
        }
    }
    private static int clearTemporaryTrack(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            List<Vec3> clearedWaypoints = TrackCreationManager.getInstance().clearAndGetWaypoints(player);
            if (clearedWaypoints == null || clearedWaypoints.isEmpty()) {
                source.sendSuccess(() -> Component.literal("没有需要清除的临时路径点。"), false);
            } else {
                source.sendSuccess(() -> Component.literal("已清除 " + clearedWaypoints.size() + " 个临时路径点。"), true);
            }
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("错误: 只有玩家才能执行此命令。"));
            return 0;
        }
    }
}