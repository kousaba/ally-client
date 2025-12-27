package com.allyclient.gui.screen;

import com.allyclient.module.Module;
import com.allyclient.module.ModuleManager;
import com.allyclient.module.HudModule;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.client.input.KeyInput;  // 仮定
import org.lwjgl.glfw.GLFW; // キーコード用

public class ConfigScreen extends Screen {

    private double scrollOffset = 0;
    private HudModule draggingModule = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public ConfigScreen() {
        super(Text.of("Ally Config"));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void removed() {
        draggingModule = null;
        super.removed();
        com.allyclient.config.ConfigManager.save();
    }

    // ★改良点1: ゲームを一時停止しない (PvPクライアントの標準挙動)
    @Override
    public boolean shouldPause() {
        return false;
    }

    // ★改良点2: 引数が変わった render メソッド
    // summaryによると render(DrawContext, mouseX, mouseY, deltaTicks)
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        // 背景やHUD枠の描画処理（前回と同じ）
        renderHudBorders(context);
        renderWindow(context, mouseX, mouseY);

        super.render(context, mouseX, mouseY, deltaTicks);
    }

    // コードを見やすくするため、描画処理をメソッドに切り出しました
    private void renderHudBorders(DrawContext context) {
        for (Module m : ModuleManager.modules) {
            if (m.enabled && m instanceof HudModule hud) {
                context.drawStrokedRectangle(hud.x - 2, hud.y - 2, hud.width + 4, hud.height + 4, 0xAAFFFFFF);
            }
        }
    }

    private void renderWindow(DrawContext context, int mouseX, int mouseY) {
        int windowWidth = 400;
        int windowHeight = 300;
        int windowX = (this.width - windowWidth) / 2;
        int windowY = (this.height - windowHeight) / 2;

        // 背景
        context.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, 0xCC101010);
        context.drawStrokedRectangle(windowX, windowY, windowWidth, windowHeight, 0xFF00AAAA);
        context.drawText(this.textRenderer, "Ally Client Modules", windowX + 10, windowY + 10, -1, true);

        // リスト描画
        int itemHeight = 25;
        int startY = windowY + 40;
        int currentY = startY - (int)scrollOffset;

        for (Module m : ModuleManager.modules) {
            // カリング (簡易)
            if (currentY < windowY + 30 || currentY > windowY + windowHeight - 30) {
                currentY += itemHeight;
                continue;
            }

            // モジュール名
            context.drawText(this.textRenderer, m.name, windowX + 20, currentY + 8, -1, true);

            // Toggleボタン
            int btnX = windowX + 150;
            int btnY = currentY + 4;
            int btnW = 60;
            int btnH = 16;
            int btnColor = m.enabled ? 0xFF22CC22 : 0xFFCC2222;

            context.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnColor);
            String btnText = m.enabled ? "Enabled" : "Disabled";
            context.drawCenteredTextWithShadow(this.textRenderer, Text.of(btnText), btnX + btnW / 2, btnY + 4, -1);

            // Settingボタン
            int setX = windowX + 250;
            context.fill(setX, btnY, setX + btnW, btnY + btnH, 0xFF555555);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Setting"), setX + btnW / 2, btnY + 4, -1);

            currentY += itemHeight;
        }
    }

    // ★改良点3: 入力イベント (Clickオブジェクト対応)
    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        // Clickオブジェクトからデータを取り出す
        double mouseX = click.x();
        double mouseY = click.y();

        int button = click.buttonInfo().button();

        // ロジックメソッドへ委譲
        if (handleMouseClickedLogic(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    // ★改良点4: マウスリリース (ドラッグ終了)
    @Override
    public boolean mouseReleased(Click click) {
        this.draggingModule = null; // ドラッグ解除
        return super.mouseReleased(click);
    }

    // ★改良点5: キー入力 (ESCで閉じる処理など)
    @Override
    public boolean keyPressed(KeyInput input) {
        // input.key() で int型のキーコードが取れるはずです
        int keyCode = input.key();

        // ESCキーで閉じる (superの処理に任せてもいいが、明示的に書くならこう)
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
            this.close();
            return true;
        }

        return super.keyPressed(input);
    }

    // 内部ロジック (以前の mouseClicked の中身)
    private boolean handleMouseClickedLogic(double mouseX, double mouseY, int button) {
        int windowWidth = 400;
        int windowHeight = 300;
        int windowX = (this.width - windowWidth) / 2;
        int windowY = (this.height - windowHeight) / 2;

        // ウィンドウ内のクリック判定
        if (mouseX >= windowX && mouseX <= windowX + windowWidth &&
                mouseY >= windowY && mouseY <= windowY + windowHeight) {

            int itemHeight = 25;
            int startY = windowY + 40;
            int currentY = startY - (int)scrollOffset;

            for (Module m : ModuleManager.modules) {
                if (currentY < windowY + 30 || currentY > windowY + windowHeight - 30) {
                    currentY += itemHeight;
                    continue;
                }

                // Toggleボタン判定
                int btnX = windowX + 150;
                int btnY = currentY + 4;
                int btnW = 60;
                int btnH = 16;

                if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                    this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    m.toggle();
                    return true;
                }
                currentY += itemHeight;

                int setX = windowX + 250;

                // Settingボタンがクリックされたら
                if (mouseX >= setX && mouseX <= setX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                    this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    this.client.setScreen(new ModuleSettingsScreen(this, m));
                    return true;
                }
            }
        }
        // ウィンドウ外ならドラッグ開始判定
        else {
            for (Module m : ModuleManager.modules) {
                if(m instanceof HudModule hud){
                    if (hud.enabled && hud.isHovered(mouseX, mouseY)) {
                        this.draggingModule = hud;
                        this.dragOffsetX = (int)mouseX - hud.x;
                        this.dragOffsetY = (int)mouseY - hud.y;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= verticalAmount * 20;
        if (scrollOffset < 0) scrollOffset = 0;
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    // ★注意: mouseDraggedも変更されている可能性があります
    // もしエラーが出る場合は、引数を `(Drag drag)` 等に変更してください
    // boolean mouseDragged(Click click, double offsetX, double offsetY) {
    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY){
        if (this.draggingModule != null){
            this.draggingModule.x = (int)click.x() - this.dragOffsetX;
            this.draggingModule.y = (int)click.y() - this.dragOffsetY;
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }
}