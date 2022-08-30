package io.github.racoondog.tileset;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;

public class TileSet implements ModInitializer {
    public static final String MODID = "tileset";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final Block BASEMENT = register("basement", FabricBlockSettings.copyOf(Blocks.BRICKS));
    public static final Block CELLAR = register("cellar", FabricBlockSettings.copyOf(Blocks.BRICKS));

    @Override
    public void onInitialize() {
    }

    private static Block register(String path, AbstractBlock.Settings settings) {
        final Identifier id = Util.id(path);
        final Block block = new Block(settings);
        Registry.register(Registry.BLOCK, id, block);
        Registry.register(Registry.ITEM, id, new BlockItem(block, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS)));
        return block;
    }

    private static <T extends Block> T register(String path, T block) {
        final Identifier id = Util.id(path);
        Registry.register(Registry.BLOCK, id, block);
        Registry.register(Registry.ITEM, id, new BlockItem(block, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS)));
        return block;
    }
}
