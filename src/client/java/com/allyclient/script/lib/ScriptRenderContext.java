package com.allyclient.script.lib;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ScriptRenderContext {
    private static DrawContext currentContext;
    private static int offsetX;
    private static int offsetY;

    public static void begin(DrawContext context, int x, int y) {
        currentContext = context;
        offsetX = x;
        offsetY = y;
    }

    public static void end() {
        currentContext = null;
    }

    public static void drawRect(double x, double y, double w, double h, int color) {
        if (currentContext == null) return;

        currentContext.fill(
                (int)(offsetX + x),
                (int)(offsetY + y),
                (int)(offsetX + x + w),
                (int)(offsetY + y + h),
                color
        );
    }

    public static void drawText(String text, double x, double y, int color, boolean shadow, double scale) {
        if (currentContext == null) return;

        // MatrixStackを保存
        currentContext.getMatrices().pushMatrix();

        // 1. 位置を移動 (Z軸を 1.0f にして、四角形より手前に表示させる)
        currentContext.getMatrices().translate((float)(offsetX + x), (float)(offsetY + y));

        // 2. サイズ変更
        currentContext.getMatrices().scale((float)scale, (float)scale);

        // 3. 描画 (座標はすでに translate で移動済みなので、ここでは 0, 0 を指定する！)
        if (shadow) {
            currentContext.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, text, 0, 0, color);
        } else {
            currentContext.drawText(MinecraftClient.getInstance().textRenderer, text, 0, 0, color, false);
        }

        // MatrixStackを復元
        currentContext.getMatrices().popMatrix();
    }

    public static void drawItem(String itemId, double x, double y, double scale){
        if(currentContext == null) return;
        if(!itemId.contains(":")) itemId = "minecraft:" + itemId; // minecraft:がなくても動くようにする
        Item item = Registries.ITEM.get(Identifier.of(itemId));
        if(item == null) return;
        ItemStack stack = new ItemStack(item);
        currentContext.getMatrices().pushMatrix();
        currentContext.getMatrices().translate((float)(offsetX + x), (float)(offsetY + y));
        currentContext.getMatrices().scale((float)scale, (float)scale);
        currentContext.drawItem(stack, 0, 0);
        currentContext.getMatrices().popMatrix();

    }
}
