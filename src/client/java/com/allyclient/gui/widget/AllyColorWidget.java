package com.allyclient.gui.widget;

import com.allyclient.settings.impl.ColorSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public class AllyColorWidget extends ClickableWidget {
    private final ColorSetting setting;
    private final TextFieldWidget hexField;
    public AllyColorWidget(TextRenderer textRenderer, int x, int y, int width, int height, ColorSetting setting) {
        super(x, y, width, height, Text.of(""));
        this.setting = setting;

        // 色コード入力用のテキストボックスを作成
        // 位置はウィジェットの右側半分くらいを使う
        int fieldWidth = 70;
        this.hexField = new TextFieldWidget(
                textRenderer,
                x + width - fieldWidth - 25, // プレビューの左側に配置
                y + (height - 16) / 2,
                fieldWidth,
                16,
                Text.of("Hex")
        );

        // 初期値をセット (int -> #RRGGBB文字列)
        this.hexField.setText(toHexString(setting.getValue()));
        this.hexField.setMaxLength(9); // #AARRGGBB まで対応

        // 入力が変わった時の処理
        this.hexField.setChangedListener(text -> {
            try {
                int color = parseHexString(text);
                this.setting.setValue(color);
            } catch (Exception ignored) {
                // 不正な文字列のときは無視
            }
        });
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. ラベル (左寄せ)
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, setting.name, getX(), getY() + (height - 8) / 2, 0xFFFFFFFF);

        // 2. テキストボックス描画
        hexField.setX(getX() + width - 70 - 25); // 位置調整
        hexField.setY(getY() + (height - 16) / 2);
        hexField.render(context, mouseX, mouseY, delta);

        // 3. プレビューボックス (一番右)
        int boxSize = 20;
        int boxX = getX() + width - boxSize;
        int boxY = getY() + (height - boxSize) / 2;

        // 透明部分がわかるようにチェッカーボード柄 (市松模様) を背景に描く
        drawCheckerboard(context, boxX, boxY, boxSize, boxSize);

        // 現在の色で塗りつぶし
        context.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, setting.getValue());

        // 枠線
        context.drawStrokedRectangle(boxX, boxY, boxSize, boxSize, 0xFFAAAAAA);
    }

    // --- 以下、入力イベントをテキストボックスに委譲する ---

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        boolean TextClicked = hexField.mouseClicked(click, doubled);
        if(TextClicked){
            hexField.setFocused(true);
            return true;
        }else{
            hexField.setFocused(false);
        }
        int boxSize = 20;
        int boxX = getX() + width - boxSize;
        int boxY = getY() + (height - boxSize) / 2;

        // click.x, click.y を使う
        if (click.x() >= boxX && click.x() <= boxX + boxSize && click.y() >= boxY && click.y() <= boxY + boxSize) {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            // 虹色設定があればここで切り替え
            // setting.toggleChroma();
            return true;
        }
        return false;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public boolean keyPressed(KeyInput input) {
        return hexField.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput chr) {
        return hexField.charTyped(chr);
    }

    // --- ヘルパーメソッド ---

    // 背景の市松模様を描く (透明度対応のため)
    private void drawCheckerboard(DrawContext context, int x, int y, int w, int h) {
        context.fill(x, y, x + w, y + h, 0xFFFFFFFF); // 白背景
        int checkSize = 5;
        for (int i = 0; i < w; i += checkSize) {
            for (int j = 0; j < h; j += checkSize) {
                if ((i / checkSize + j / checkSize) % 2 == 1) {
                    // グレーの四角
                    int drawW = Math.min(checkSize, w - i);
                    int drawH = Math.min(checkSize, h - j);
                    context.fill(x + i, y + j, x + i + drawW, y + j + drawH, 0xFFCCCCCC);
                }
            }
        }
    }

    // int -> "#AARRGGBB"
    private String toHexString(int color) {
        return String.format("#%08X", color);
    }

    // "#RRGGBB" or "#AARRGGBB" -> int
    private int parseHexString(String text) {
        if (text.startsWith("#")) text = text.substring(1);
        long val = Long.parseLong(text, 16);
        if (text.length() <= 6) val |= 0xFF000000; // アルファがないなら不透明に
        return (int) val;
    }
}
