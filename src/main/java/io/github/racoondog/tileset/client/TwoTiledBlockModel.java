package io.github.racoondog.tileset.client;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.github.racoondog.tileset.TileSet;
import io.github.racoondog.tileset.Util;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class TwoTiledBlockModel implements UnbakedModel, BakedModel, FabricBakedModel {
    private Mesh[] MESHES;
    private int x;
    private int y;
    private int z;
    private final Util.SideInfo SIDE_SPRITE_INFO;
    private final Util.SideInfo TOP_SPRITE_INFO;
    private final Util.SideInfo BOTTOM_SPRITE_INFO;
    private Sprite SIDE_SPRITE;

    public TwoTiledBlockModel(Util.SideInfo side, Util.SideInfo top, Util.SideInfo bottom) {
        this.SIDE_SPRITE_INFO = side;
        this.TOP_SPRITE_INFO = top;
        this.BOTTOM_SPRITE_INFO = bottom;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<net.minecraft.util.math.random.Random> randomSupplier, RenderContext context) {
        int i = Math.floorMod(pos.getX(), x);
        i += Math.floorMod(pos.getZ(), z) * x;
        i += Math.floorMod(pos.getY(), y) * x * z;
        context.meshConsumer().accept(this.MESHES[i]);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<net.minecraft.util.math.random.Random> randomSupplier, RenderContext context) {
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, net.minecraft.util.math.random.Random random) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        return this.SIDE_SPRITE;
    }

    @Override
    public ModelTransformation getTransformation() {
        return null;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return null;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return Lists.newArrayList(this.SIDE_SPRITE_INFO.spriteIdentifier, this.TOP_SPRITE_INFO.spriteIdentifier, this.BOTTOM_SPRITE_INFO.spriteIdentifier);
    }

    private static final float STEP = 0.015625f;
    private void setSprite(QuadEmitter emitter, Sprite sprite, int xPush, int yPush, int resolution) {
        float step = STEP / (16.0f / resolution);

        float minU = sprite.getMinU() + step * xPush;
        float minV = sprite.getMinV() + step * yPush;
        float maxU = minU + step;
        float maxV = minV + step;

        emitter.sprite(0, 0, maxU, maxV);
        emitter.sprite(1, 0, maxU, minV);
        emitter.sprite(2, 0, minU, minV);
        emitter.sprite(3, 0, minU, maxV);
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        this.SIDE_SPRITE = textureGetter.apply(this.SIDE_SPRITE_INFO.spriteIdentifier);
        final Sprite TOP_SPRITE = textureGetter.apply(this.TOP_SPRITE_INFO.spriteIdentifier);
        final Sprite BOTTOM_SPRITE = textureGetter.apply(this.BOTTOM_SPRITE_INFO.spriteIdentifier);

        int sideWidth = this.SIDE_SPRITE.getWidth() / this.SIDE_SPRITE_INFO.resolution;
        int sideHeight = this.SIDE_SPRITE.getHeight() / this.SIDE_SPRITE_INFO.resolution;

        int topWidth = TOP_SPRITE.getWidth() / this.TOP_SPRITE_INFO.resolution;
        int topHeight = TOP_SPRITE.getHeight() / this.TOP_SPRITE_INFO.resolution;

        if (topWidth != BOTTOM_SPRITE.getWidth() / this.BOTTOM_SPRITE_INFO.resolution || topHeight != BOTTOM_SPRITE.getHeight() / this.BOTTOM_SPRITE_INFO.resolution) {
            TileSet.LOGGER.warn("Model {} has differently sized top and bottom textures.", modelId); //Math would be too complicated for my smooth brain
            return null;
        }

        x = Util.lcm(topWidth, sideWidth);
        y = sideHeight;
        z = Util.lcm(topHeight, sideWidth);

        int meshCount = x * y * z;

        this.MESHES = new Mesh[meshCount];

        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        MeshBuilder builder = renderer.meshBuilder();

        for (int i = 0; i < meshCount; i++) {
            QuadEmitter emitter = builder.getEmitter();

            for (Direction direction : Direction.values()) {
                emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);

                if (direction == Direction.UP) {
                    this.setSprite(emitter, TOP_SPRITE, i % x % topWidth, i / x % z % topHeight, TOP_SPRITE_INFO.resolution);
                } else if (direction == Direction.DOWN) {
                    this.setSprite(emitter, BOTTOM_SPRITE, i % x % topWidth, i / x % z % topHeight, BOTTOM_SPRITE_INFO.resolution);
                } else if (direction == Direction.WEST || direction == Direction.EAST) {
                    this.setSprite(emitter, SIDE_SPRITE, i / x % z % sideWidth, i / x / z % y % sideHeight, SIDE_SPRITE_INFO.resolution);
                } else {
                    this.setSprite(emitter, SIDE_SPRITE, i % x % sideWidth, i / x / z % y % sideHeight, SIDE_SPRITE_INFO.resolution);
                }

                emitter.spriteColor(0, -1, -1, -1, -1);
                emitter.emit();
            }
            this.MESHES[i] = builder.build();
        }

        return this;
    }
}
