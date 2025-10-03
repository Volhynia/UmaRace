package moe.gensoukyo.umarace;
import com.mojang.logging.LogUtils;
import moe.gensoukyo.umarace.command.TrackCommand;
import moe.gensoukyo.umarace.item.TrackWandItem;
import moe.gensoukyo.umarace.network.PacketHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
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
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredItem<Item> TRACK_WAND = ITEMS.register("track_wand",
            () -> new TrackWandItem(new Item.Properties()));
    public UmaRace(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        ITEMS.register(modEventBus);
        // The PacketHandler is now an EventBusSubscriber, so it registers itself automatically.
        // We no longer need to manually register it to the event bus here if we add the annotation to it.
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("UmaRace Mod is setting up!");
        // We no longer need to call a register method here.
        // event.enqueueWork(PacketHandler::register); // REMOVE THIS LINE
    }
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(TRACK_WAND.get());
        }
    }
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TrackCommand.register(event.getDispatcher());
    }
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
}