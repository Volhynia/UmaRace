// --- FILE_PATH: moe/gensoukyo/umarace/UmaRace.java ---
package moe.gensoukyo.umarace;

import com.mojang.logging.LogUtils;
import moe.gensoukyo.umarace.item.TrackWandItem; // 导入我们的新物品类
import net.neoforged.bus.api.SubscribeEvent;
import org.slf4j.Logger;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(UmaRace.MODID)
public class UmaRace {
    public static final String MODID = "umarace";
    public static final Logger LOGGER = LogUtils.getLogger();

    // 我们只需要注册物品，所以保留物品的DeferredRegister
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // 注册我们的赛道定义工具
    public static final DeferredItem<Item> TRACK_WAND = ITEMS.register("track_wand",
            () -> new TrackWandItem(new Item.Properties()));

    public UmaRace(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        // 将物品注册器注册到事件总线
        ITEMS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        // 添加到创造模式物品栏的监听器
        modEventBus.addListener(this::addCreative);

        // 注册配置文件
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("UmaRace Mod is setting up!");
    }

    // 将我们的物品添加到创造模式物品栏
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // 将其添加到“工具与实用设备”标签页
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(TRACK_WAND.get());
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
}