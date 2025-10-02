package moe.gensoukyo.umarace.item;
import moe.gensoukyo.umarace.manager.TrackCreationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
public class TrackWandItem extends Item {
    public TrackWandItem(Properties properties) {
        super(properties);
    }
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null || level.isClientSide()) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            player.sendSystemMessage(Component.literal("请使用 /track save <name> 或 /track clear 来管理赛道。"));
            return InteractionResult.SUCCESS;
        }
        TrackCreationManager tcm = TrackCreationManager.getInstance();
        BlockPos clickedPos = context.getClickedPos();
        Vec3 waypoint = Vec3.atCenterOf(clickedPos.above());
        tcm.addControlPoint(player, waypoint); 
        int count = tcm.getControlPoints(player).size();
        player.sendSystemMessage(Component.literal(String.format("体育场赛道: 已设置内侧边缘控制点 %d/3.", count)));
        if (count == 1) {
            player.sendSystemMessage(Component.literal("提示: 第1点为内侧直道起点. 请沿引导线方向点击第2点."));
        } else if (count == 2) {
            player.sendSystemMessage(Component.literal("提示: 第2点已自动对齐为直线终点. 请点击第3点确定弯道方向."));
        } else if (count == 3) {
            player.sendSystemMessage(Component.literal("提示: 第3点为内侧弯道顶点. 赛道范围已显示!"));
            player.sendSystemMessage(Component.literal("再次点击将重置控制点."));
            player.sendSystemMessage(Component.literal("使用 /track save <name> 保存赛道."));
        }
        return InteractionResult.SUCCESS;
    }
}