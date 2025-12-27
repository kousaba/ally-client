package com.allyclient.gui.screen;

import com.allyclient.settings.impl.CodeSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.Text;

public class ScriptEditorScreen extends Screen {

    private final Screen parent;
    private final CodeSetting setting;
    private EditBoxWidget editBox;

    public ScriptEditorScreen(Screen parent, CodeSetting setting) {
        super(Text.of("Script Editor"));
        this.parent = parent;
        this.setting = setting;
    }

    @Override
    protected void init() {
        // 1. 複数行入力ボックス (EditBoxWidget) を作成
        // 引数: TextRenderer, x, y, width, height, placeholder, message
        this.editBox = new EditBoxWidget.Builder()
                .x(20)                   // X座標
                .y(50)                   // Y座標
                .placeholder(Text.of("Code")) // プレースホルダー（空のときに表示される薄い文字）
                // .hasBackground(true)  // 背景の有無など（デフォルトtrueなので省略可）
                .build(
                        this.textRenderer,   // 第1引数: フォントレンダラー
                        this.width - 40,     // 第2引数: 幅 (Width)
                        this.height - 100,   // 第3引数: 高さ (Height)
                        Text.of("Code Editor") // 第4引数: ナレーター用のメッセージ
                );

        // 現在のコードをセット
        this.editBox.setText(setting.getValue());

        // 最大文字数を増やす (重要！)
        this.editBox.setMaxLength(10000);

        // 画面に追加
        this.addDrawableChild(editBox);

        // 初期フォーカスを当てる
        this.setInitialFocus(editBox);


        // 2. 保存して戻るボタン
        this.addDrawableChild(ButtonWidget.builder(Text.of("Save & Close"), btn -> {
                    // 設定に値を保存
                    setting.setValue(editBox.getText());
                    // 親画面に戻る
                    this.client.setScreen(parent);
                })
                .dimensions(this.width / 2 - 105, this.height - 40, 100, 20)
                .build());

        // 3. キャンセルボタン
        this.addDrawableChild(ButtonWidget.builder(Text.of("Cancel"), btn -> {
                    this.client.setScreen(parent);
                })
                .dimensions(this.width / 2 + 5, this.height - 40, 100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xCC000000);

        // タイトル
        context.drawCenteredTextWithShadow(this.textRenderer, "Editing: " + setting.name, this.width / 2, 20, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    // 画面を閉じるときにも一応保存する場合 (ESCキー対策)
    @Override
    public void removed() {
        // setting.setValue(editBox.getText()); // お好みで有効化してください
        super.removed();
    }
}