// --- FILE_PATH: moe/gensoukyo/umarace/item/TrackWandItem.java ---
package moe.gensoukyo.umarace.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class TrackWandItem extends Item {

    public TrackWandItem(Properties properties) {
        super(properties);
    }

    /**
     * 当玩家用此物品右键点击方块时调用
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        // 确保逻辑只在服务器端运行，这是非常重要的原则
        if (!level.isClientSide()) {
            Player player = context.getPlayer();
            if (player == null) {
                return InteractionResult.FAIL;
            }

            // 检查玩家是否按下了Shift键
            if (player.isShiftKeyDown()) {
                // 如果按下了Shift，我们认为是“完成定义”的操作
                // TODO: 在这里添加保存赛道数据的逻辑
                player.sendSystemMessage(Component.literal("赛道定义完成！(未来将在此保存数据)"));
            } else {
                // 如果没有按下Shift，我们认为是“添加路径点”的操作
                BlockPos clickedPos = context.getClickedPos();
                // 我们使用点击位置的上方一格作为路径点，这样点位不会在地下
                BlockPos waypointPos = clickedPos.above();

                // TODO: 在这里添加记录路径点的逻辑
                player.sendSystemMessage(Component.literal(String.format("路径点已添加: %d, %d, %d", waypointPos.getX(), waypointPos.getY(), waypointPos.getZ())));
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
}