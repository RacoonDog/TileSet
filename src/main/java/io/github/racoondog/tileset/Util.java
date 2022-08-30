package io.github.racoondog.tileset;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class Util {
    public static @NotNull Identifier id(final String path) {
        return new Identifier(TileSet.MODID, path);
    }

    /**
     * Greatest common divisor
     */
    public static int gcd(int a, int b) {
        if (a == 0) return b;

        while (b != 0) {
            if (a > b) a = a - b;
            else b = b - a;
        }

        return a;
    }

    /**
     * Lowest common multiple
     */
    public static int lcm(int a, int b) {
        return a * (b / gcd(a, b));
    }

    @Environment(EnvType.CLIENT)
    public static SideInfo info(String path) {
        return new SideInfo(id(path));
    }

    @Environment(EnvType.CLIENT)
    public static class SideInfo {
        public final SpriteIdentifier spriteIdentifier;
        public final int resolution;

        public SideInfo(Identifier texture, Identifier atlas, int resolution) {
            this.spriteIdentifier = new SpriteIdentifier(atlas, texture);
            this.resolution = resolution;
        }

        public SideInfo(Identifier texture, Identifier atlas) {
            this(texture, atlas, 16);
        }

        public SideInfo(Identifier texture, int resolution) {
            this(texture, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, resolution);
        }

        public SideInfo(Identifier texture) {
            this(texture, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, 16);
        }
    }
}
