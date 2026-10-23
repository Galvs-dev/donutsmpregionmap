package com.donutregions;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

public class DonutRegionsClient implements ClientModInitializer {
    public static KeyBinding openMapKey;
    public static KeyBinding toggleHudKey;
    private static boolean hudEnabled = true;

    @Override
    public void onInitializeClient() {
        openMapKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.donutregions.map", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, "category.donutregions"));
        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.donutregions.toggle_hud", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.donutregions"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMapKey.wasPressed()) {
                if (client.currentScreen == null && client.player != null) {
                    client.setScreen(new RegionMapScreen());
                }
            }
            while (toggleHudKey.wasPressed()) {
                hudEnabled = !hudEnabled;
            }
        });

        HudRenderCallback.EVENT.register(DonutRegionsClient::renderHud);
    }

    /** Player X/Z in overworld-equivalent coordinates (Nether is multiplied by 8). */
    public static double[] playerXZ(MinecraftClient mc) {
        double scale = (mc.world != null && mc.world.getRegistryKey() == World.NETHER) ? 8.0 : 1.0;
        return new double[]{mc.player.getX() * scale, mc.player.getZ() * scale};
    }

    private static void renderHud(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!hudEnabled || mc.player == null || mc.options.hudHidden) return;

        double[] p = playerXZ(mc);
        RegionMap.Result r = RegionMap.lookup(p[0], p[1]);

        String line1 = r == null ? "Outside region map" : "Region " + r.id() + " - " + r.region().displayName;
        String line2 = String.format("X: %d  Z: %d", (int) Math.floor(p[0]), (int) Math.floor(p[1]));
        int color = r == null ? 0xFFAAAAAA : r.region().color;

        int x = 6, y = 6;
        int w = Math.max(mc.textRenderer.getWidth(line1), mc.textRenderer.getWidth(line2)) + 8;
        ctx.fill(x - 2, y - 2, x + w, y + 24, 0x90000000);
        ctx.fill(x - 2, y - 2, x, y + 24, color);
        ctx.drawTextWithShadow(mc.textRenderer, line1, x + 4, y, color);
        ctx.drawTextWithShadow(mc.textRenderer, line2, x + 4, y + 12, 0xFFFFFFFF);
    }
}
