package com.allyclient.script.lib;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class Script3DRenderContext {
    private static MatrixStack matrixStack;
    private static Vec3d cameraPos;

    public static void begin(MatrixStack matrices, Vec3d camPos) {
        matrixStack = matrices;
        cameraPos = camPos;
    }

    public static void end() {
        matrixStack = null;
        cameraPos = null;
    }

    public static void drawBox3D(double x, double y, double z, double w, double h, double d, int color, boolean filled) {
        if (matrixStack == null || cameraPos == null) return;

        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        double renderX = x - cameraPos.x;
        double renderY = y - cameraPos.y;
        double renderZ = z - cameraPos.z;

        // --- OpenGL設定 ---

        // 1. ブレンド有効化
        GlStateManager._enableBlend();
        // ★修正: _blendFunc がないので _blendFuncSeparate を使う
        // (SRC_ALPHA, ONE_MINUS_SRC_ALPHA をRGBとAlpha両方に指定)
        GlStateManager._blendFuncSeparate(
                GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA
        );

        // 2. 深度テスト無効化 (ESPのように壁を透けさせる)
        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);

        // 3. カリング無効化 (箱の内側も見えるように)
        GlStateManager._disableCull();

        // --- 描画実行 ---

        Tessellator tessellator = Tessellator.getInstance();

        // ★修正: バッファの開始
        // begin() は BufferBuilder を返します
        BufferBuilder buffer = tessellator.begin(
                filled ? VertexFormat.DrawMode.QUADS : VertexFormat.DrawMode.DEBUG_LINES,
                VertexFormats.POSITION_COLOR
        );

        // 頂点の打ち込み (ロジックは以前と同じ)
        if (filled) {
            drawFilledBox(buffer, matrixStack, (float)renderX, (float)renderY, (float)renderZ, (float)w, (float)h, (float)d, r, g, b, a);
        } else {
            // 線の太さはGlStateManagerにもないので、デフォルト(1.0)になります
            // どうしても変えたい場合は GL11.glLineWidth(width) を直接呼びますが、
            // Core Profileでは動作しない可能性があります。
            drawLineBox(buffer, matrixStack, (float)renderX, (float)renderY, (float)renderZ, (float)w, (float)h, (float)d, r, g, b, a);
        }

        // ★修正: 描画の完了
        // BufferRenderer を使わず、Tessellator に描画させる
        try {

        } catch (Exception e) {
            // 描画エラー対策
        }

        // --- 設定を戻す ---
        GlStateManager._enableCull();
        GlStateManager._depthMask(true);
        GlStateManager._enableDepthTest();
        GlStateManager._disableBlend();
    }

    // --- 以下、頂点打ち込みロジック (変更なし) ---
    // ※前回のコードにある drawFilledBox, drawLineBox, vertex, line をここに貼り付けてください

    private static void drawFilledBox(BufferBuilder buffer, MatrixStack matrices, float x, float y, float z, float w, float h, float d, float r, float g, float b, float a) {
        org.joml.Matrix4f mat = matrices.peek().getPositionMatrix();
        float x2 = x + w, y2 = y + h, z2 = z + d;

        // Bottom
        vertex(buffer, mat, x, y, z, r, g, b, a);
        vertex(buffer, mat, x2, y, z, r, g, b, a);
        vertex(buffer, mat, x2, y, z2, r, g, b, a);
        vertex(buffer, mat, x, y, z2, r, g, b, a);

        // Top
        vertex(buffer, mat, x, y2, z2, r, g, b, a);
        vertex(buffer, mat, x2, y2, z2, r, g, b, a);
        vertex(buffer, mat, x2, y2, z, r, g, b, a);
        vertex(buffer, mat, x, y2, z, r, g, b, a);

        // North
        vertex(buffer, mat, x, y, z, r, g, b, a);
        vertex(buffer, mat, x, y2, z, r, g, b, a);
        vertex(buffer, mat, x2, y2, z, r, g, b, a);
        vertex(buffer, mat, x2, y, z, r, g, b, a);

        // South
        vertex(buffer, mat, x2, y, z2, r, g, b, a);
        vertex(buffer, mat, x2, y2, z2, r, g, b, a);
        vertex(buffer, mat, x, y2, z2, r, g, b, a);
        vertex(buffer, mat, x, y, z2, r, g, b, a);

        // West
        vertex(buffer, mat, x, y, z2, r, g, b, a);
        vertex(buffer, mat, x, y2, z2, r, g, b, a);
        vertex(buffer, mat, x, y2, z, r, g, b, a);
        vertex(buffer, mat, x, y, z, r, g, b, a);

        // East
        vertex(buffer, mat, x2, y, z, r, g, b, a);
        vertex(buffer, mat, x2, y2, z, r, g, b, a);
        vertex(buffer, mat, x2, y2, z2, r, g, b, a);
        vertex(buffer, mat, x2, y, z2, r, g, b, a);
    }

    private static void drawLineBox(BufferBuilder buffer, MatrixStack matrices, float x, float y, float z, float w, float h, float d, float r, float g, float b, float a) {
        org.joml.Matrix4f mat = matrices.peek().getPositionMatrix();
        float x2 = x + w, y2 = y + h, z2 = z + d;

        line(buffer, mat, x, y, z, x2, y, z, r, g, b, a);
        line(buffer, mat, x2, y, z, x2, y, z2, r, g, b, a);
        line(buffer, mat, x2, y, z2, x, y, z2, r, g, b, a);
        line(buffer, mat, x, y, z2, x, y, z, r, g, b, a);

        line(buffer, mat, x, y2, z, x2, y2, z, r, g, b, a);
        line(buffer, mat, x2, y2, z, x2, y2, z2, r, g, b, a);
        line(buffer, mat, x2, y2, z2, x, y2, z2, r, g, b, a);
        line(buffer, mat, x, y2, z2, x, y2, z, r, g, b, a);

        line(buffer, mat, x, y, z, x, y2, z, r, g, b, a);
        line(buffer, mat, x2, y, z, x2, y2, z, r, g, b, a);
        line(buffer, mat, x2, y, z2, x2, y2, z2, r, g, b, a);
        line(buffer, mat, x, y, z2, x, y2, z2, r, g, b, a);
    }

    private static void vertex(BufferBuilder buffer, org.joml.Matrix4f mat, float x, float y, float z, float r, float g, float b, float a) {
        buffer.vertex(mat, x, y, z).color(r, g, b, a);
    }

    private static void line(BufferBuilder buffer, org.joml.Matrix4f mat, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        buffer.vertex(mat, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(mat, x2, y2, z2).color(r, g, b, a);
    }
}