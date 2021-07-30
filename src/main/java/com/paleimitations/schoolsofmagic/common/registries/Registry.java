package com.paleimitations.schoolsofmagic.common.registries;

import com.paleimitations.schoolsofmagic.References;
import com.paleimitations.schoolsofmagic.SchoolsOfMagicMod;
import com.paleimitations.schoolsofmagic.common.config.Config;
import com.paleimitations.schoolsofmagic.common.data.loottables.LootInjecter;
import com.paleimitations.schoolsofmagic.common.data.capabilities.book_data.BookDataProvider;
import com.paleimitations.schoolsofmagic.common.network.*;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = References.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, References.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, References.MODID);
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, References.MODID);
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, References.MODID);
    public static final DeferredRegister<ContainerType<?>> CONTAINER_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, References.MODID);

    public static final  ItemGroup EQUIPMENT_TAB = (new ItemGroup("schoolsOfMagicEquipment") {
        @OnlyIn(Dist.CLIENT)
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemRegistry.APPRENTICE_WAND_1.get());
        }
    }).setRecipeFolderName("schools_of_magic_equipment");

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new Registry());
        MinecraftForge.EVENT_BUS.register(new BookDataProvider.Events());
        MinecraftForge.EVENT_BUS.register(new LootInjecter());
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MagicSchoolRegistry.init();
        MagicElementRegistry.init();
        Config.init();
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        TILE_ENTITY_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        CONTAINER_TYPES.register(modEventBus);
        ItemRegistry.register();
        BlockRegistry.register();
        TileEntityRegistry.register();
        RecipeRegistry.register();
        ContainerRegistry.register();
        SpellRegistry.init();
        BookPageRegistry.init();
        PacketHandler.registerMessages();
        TeaRegistry.register();
    }

    public static void init() {
        TeaIngredientRegistry.register();
        CapabilityRegistry.init();
        QuestRegistry.init();
        SchoolsOfMagicMod.getProxy().preInit();
        SchoolsOfMagicMod.getProxy().postInit();
        BlockRegistry.init();
        ItemRegistry.init();
    }

    @SubscribeEvent
    public static void registerSounds(final RegistryEvent.Register<SoundEvent> event) {
        System.out.println("Registered SoundEvents");
        try {
            for (Field f : SoundRegistry.class.getDeclaredFields()) {
                Object obj = f.get(null);
                if (obj instanceof SoundEvent) {
                    event.getRegistry().register((SoundEvent) obj);
                } else if (obj instanceof SoundEvent[]) {
                    for (SoundEvent soundEvent : (SoundEvent[]) obj) {
                        event.getRegistry().register(soundEvent);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}