package io.github.racoondog.tileset.client;

import io.github.racoondog.tileset.Util;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ModelProvider implements ModelResourceProvider {
    private static final Identifier BASEMENT_MODEL = Util.id("block/basement");
    private static final Identifier CELLAR_MODEL = Util.id("block/cellar");

    @Override
    public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) {
        if (resourceId.equals(BASEMENT_MODEL)) {
            return new TwoTiledBlockModel(new Util.SideInfo(Util.id("block/basement_side"), 8), Util.info("block/basement_top"), Util.info("block/basement_top"));
        } else if (resourceId.equals(CELLAR_MODEL)) {
            return new TwoTiledBlockModel(Util.info("block/cellar_side"), Util.info("block/basement_top"), Util.info("block/basement_top"));
        }
        return null;
    }
}
