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

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide()) {
            Player player = context.getPlayer();
            if (player == null) {
                return InteractionResult.FAIL;
            }
            if (player.isShiftKeyDown()) {
                player.sendSystemMessage(Component.literal("赛道定义完成！(未来将在此保存数据)"));
            } else {
                BlockPos clickedPos = context.getClickedPos();
                BlockPos waypointPos = clickedPos.above();
                player.sendSystemMessage(Component.literal(String.format("路径点已添加: %d, %d, %d", waypointPos.getX(), waypointPos.getY(), waypointPos.getZ())));
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
}