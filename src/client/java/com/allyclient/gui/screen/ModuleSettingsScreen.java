package com.allyclient.gui.screen;

import com.allyclient.gui.widget.AllyCodeEditorWidget;
import com.allyclient.gui.widget.AllyColorWidget;
import com.allyclient.gui.widget.AllySliderWidget;
import com.allyclient.module.Module;
import com.allyclient.settings.Setting;
import com.allyclient.settings.impl.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class ModuleSettingsScreen extends Screen {

    private final Screen parent; // 戻るための親画面 (ConfigScreen)
    private final Module module; // 編集対象のモジュール

    public ModuleSettingsScreen(Screen parent, Module module) {
        super(Text.of(module.name + " Settings"));
        this.parent = parent;
        this.module = module;
    }

    @Override
    protected void init() {
        super.init();

        int startY = 50; // 設定項目の開始Y座標
        int itemHeight = 30; // 項目の間隔
        int centerX = this.width / 2;

        // 設定リストをループして、型に応じたウィジェットを配置
        for (int i = 0; i < module.settings.size(); i++) {
            Setting setting = module.settings.get(i);
            int currentY = startY + (i * itemHeight);

            if (setting instanceof CodeSetting codeSetting) {
                // 専用エディタを表示

                int availableHeight = this.height - currentY - 60;
                int editorHeight = Math.max(availableHeight, 100);
                AllyCodeEditorWidget editor = new AllyCodeEditorWidget(
                        this.textRenderer,
                        centerX - 150,
                        currentY,
                        300,
                        editorHeight,
                        codeSetting
                );


                this.addDrawableChild(editor);
            }
            else if (setting instanceof BooleanSetting boolSetting) {
                // トグルボタンを作成
                ButtonWidget button = ButtonWidget.builder(
                                Text.of(boolSetting.name + ": " + (boolSetting.isEnabled() ? "ON" : "OFF")),
                                btn -> {
                                    // クリック時の処理: 値を反転
                                    boolSetting.toggle();
                                    // ボタンの文字を更新
                                    btn.setMessage(Text.of(boolSetting.name + ": " + (boolSetting.isEnabled() ? "ON" : "OFF")));
                                }
                        )
                        .dimensions(centerX - 100, currentY, 200, 20) // 位置とサイズ (x, y, w, h)
                        .build();

                // 画面に追加 (これをすると描画とクリック判定が自動化される)
                this.addDrawableChild(button);
            }

            // --- 2. StringSetting (文字入力) の場合 ---
            else if (setting instanceof StringSetting stringSetting) {
                // テキストボックスを作成
                TextFieldWidget textField = new TextFieldWidget(
                        this.textRenderer,
                        centerX - 100, currentY, 200, 20,
                        Text.of(stringSetting.name)
                );

                // 初期値を入れる
                textField.setText(stringSetting.getValue());
                // 文字数制限 (必要なら)
                textField.setMaxLength(128);

                // 文字が変わるたびに設定値を更新するリスナー
                textField.setChangedListener(text -> {
                    stringSetting.setValue(text);
                });

                this.addDrawableChild(textField);
            }

            else if (setting instanceof NumberSetting numberSetting){
                AllySliderWidget slider = new AllySliderWidget(
                        centerX - 100, currentY, 200, 20,
                        numberSetting
                );
                this.addDrawableChild(slider);
            }

            else if(setting instanceof ColorSetting colorSetting){
                AllyColorWidget colorWidget = new AllyColorWidget(
                        this.textRenderer,
                        centerX - 100, currentY, 200, 20,
                        colorSetting
                );
                this.addDrawableChild(colorWidget);
            }
        }

        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), btn -> {
            this.client.setScreen(this.parent); // 親画面に戻る
        }).dimensions(centerX - 50, this.height - 40, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 背景を暗くする (ワールド内なら半透明、タイトル画面なら土背景)
        // this.renderBackground(context, mouseX, mouseY, delta);

        // タイトル表示
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        // ラベル表示 (テキストボックスの上に「何の設定か」名前を出すなど)
        int startY = 50;
        int itemHeight = 30;

        for (int i = 0; i < module.settings.size(); i++) {
            Setting setting = module.settings.get(i);
            int currentY = startY + (i * itemHeight);

            // StringSettingの場合は、ボックスの上に名前を表示してあげる
            if (setting instanceof StringSetting) {
                context.drawTextWithShadow(
                        this.textRenderer,
                        setting.name,
                        this.width / 2 - 100,
                        currentY - 10, // ボックスの少し上
                        0xAAAAAA
                );
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void removed(){
        super.removed();
        com.allyclient.config.ConfigManager.save();
    }
}