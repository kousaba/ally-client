package com.allyclient.gui.widget;

import com.allyclient.settings.impl.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class AllySliderWidget extends ClickableWidget {
    private final NumberSetting setting;
    private boolean dragging = false;

    public AllySliderWidget(int x, int y, int width, int height, NumberSetting setting) {
        super(x, y, width, height, Text.of(""));
        this.setting = setting;
        updateMessage();
    }

    private void updateMessage() {
        // 小数点第2位まで表示 (例: Smooth: 0.15)
        String val = String.format("%.2f", setting.getValue());
        this.setMessage(Text.of(setting.name + ": " + val));
    }

    private void updateValue(double mouseX) {
        // マウス位置(0.0~1.0) を計算
        double percent = (mouseX - getX()) / (double) this.width;
        percent = MathHelper.clamp(percent, 0.0, 1.0);

        // パーセントを実際の値(min~max)に変換
        double range = setting.getMax() - setting.getMin();
        double newValue = setting.getMin() + (range * percent);

        setting.setValue(newValue);
        updateMessage();
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // 背景 (暗いグレー)
        context.fill(getX(), getY(), getX() + width, getY() + height, 0xFF202020);
        context.drawStrokedRectangle(getX(), getY(), width, height, 0xFFFFFFFF);

        // つまみ (Filled Bar) の計算
        double range = setting.getMax() - setting.getMin();
        double percent = (setting.getValue() - setting.getMin()) / range;
        int barWidth = (int) (this.width * percent);

        // つまみ部分 (緑色)
        context.fill(getX() + 1, getY() + 1, getX() + barWidth, getY() + height - 1, 0xFF22AA22);

        // 文字描画
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, this.getMessage(), getX() + width / 2, getY() + (height - 8) / 2, 0xFFFFFFFF);
    }

    @Override
    public void onClick(Click click, boolean isDouble) {
        updateValue(click.x());
        this.dragging = true;
    }

    @Override
    public void onRelease(Click click) {
        this.dragging = false;
    }

    @Override
    public void onDrag(Click click, double deltaX, double deltaY) {
        if (this.dragging) {
            updateValue(click.x());
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        return;
    }
}
