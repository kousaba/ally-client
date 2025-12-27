package com.allyclient.gui.widget;

import com.allyclient.script.parser.AllyLexer;
import com.allyclient.settings.impl.CodeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class AllyCodeEditorWidget extends ClickableWidget {
    private String text = "";
    private int cursor = 0;
    private int scrollOffset = 0;
    private final TextRenderer textRenderer;
    private boolean isFocused = false;
    private final CodeSetting setting;

    private int frameTick = 0;

    public AllyCodeEditorWidget(TextRenderer textRenderer, int x, int y, int width, int height, CodeSetting setting){
        super(x,y,width,height, Text.of("Code Editor"));
        this.textRenderer = textRenderer;
        this.setting = setting;
        this.text = setting.getValue();
        this.cursor = text.length();
    }

    public void setText(String text){
        this.text = text;
        if(this.cursor > text.length()) this.cursor = text.length();
    }

    public String getText(){
        return text;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // 背景
        context.fill(getX(), getY(), getX() + width, getY() + height, 0xFF101010);
        context.drawStrokedRectangle(getX(), getY(), width, height, isFocused ? 0xFFFFFFFF : 0xFFAAAAAA);

        context.enableScissor(getX() + 2, getY() + 2, getX() + width - 2, getY() + height - 2);

        // --- テキスト描画 ---
        try {
            AllyLexer lexer = new AllyLexer(text);
            List<AllyLexer.Token> tokens = lexer.tokenize();

            int startX = getX() + 4;
            int drawX = startX;
            int drawY = getY() + 4 - (scrollOffset * 10);

            for (AllyLexer.Token token : tokens) {
                if (token.type() == AllyLexer.TokenType.NEWLINE) {
                    drawX = startX;
                    drawY += 10; // 1行の高さ
                    continue;
                }

                String val = token.value();
                int color = getTokenColor(token.type());

                // トークンを描画
                context.drawText(textRenderer, val, drawX, drawY, color, false);
                drawX += textRenderer.getWidth(val);
            }
        } catch (Exception e) {
            // エラー時は白文字でベタ書き
            context.drawText(textRenderer, text, getX() + 4, getY() + 4 - (scrollOffset * 10), 0xFFFFFFFF, false);
        }

        // --- カーソル描画 ---
        if (isFocused && (System.currentTimeMillis() % 1000 < 500)) {
            int[] pos = getCursorPos();
            int cx = pos[0];
            int cy = pos[1];

            // 範囲内なら描画
            if (cy >= getY() && cy < getY() + height) {
                // カーソルは縦棒 (幅1px, 高さ9px)
                context.fill(cx, cy, cx + 1, cy + 9, 0xFFFFFFFF);
            }
        }

        context.disableScissor();
    }

    private int getTokenColor(AllyLexer.TokenType type) {
        return switch (type) {
            case IF, ELSE, FUNC, LET, RETURN -> 0xFF569CD6; // 青 (キーワード)
            case STRING -> 0xFFCE9178; // オレンジ (文字列)
            case NUMBER -> 0xFFB5CEA8; // 薄緑 (数字)
            case COLOR -> 0xFFDCDCAA; // 黄色 (カラーコード)
            case VARIABLE -> 0xFF9CDCFE; // 水色 (システム変数)
            case IDENTIFIER -> 0xFF9CDCFE; // 水色 (ユーザー変数)
            default -> 0xFFD4D4D4; // グレー/白
        };
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (!isFocused) return false;

        // ★修正ポイント: input.key() で正しいキーコードを取得
        int keyCode = input.key();

        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            handleEnter();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (cursor > 0) {
                text = text.substring(0, cursor - 1) + text.substring(cursor);
                cursor--;
                // ★設定への保存を追加しておくと安心
                if(this.setting != null) this.setting.setValue(this.text);
            }
            return true;
        }
        // カーソル移動
        else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if (cursor > 0) cursor--;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (cursor < text.length()) cursor++;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_UP) {
            moveCursorUp();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
            moveCursorDown();
            return true;
        }

        ensureCursorVisible();

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!isFocused) return false;

        String str = input.asString();

        // 文字の挿入
        text = text.substring(0, cursor) + str + text.substring(cursor);

        // カーソルを進める (絵文字などが混ざっても大丈夫なように length() を足す)
        cursor += str.length();

        // 設定にリアルタイムで保存するならこれを追加
        if (this.setting != null) {
            this.setting.setValue(this.text);
        }

        ensureCursorVisible();

        return true;
    }

    // オートインデントのロジック
    private void handleEnter() {
        // 1. 直前の行を取得
        int lastNewLine = text.lastIndexOf('\n', cursor - 1);
        int lineStart = (lastNewLine == -1) ? 0 : lastNewLine + 1;
        String currentLine = text.substring(lineStart, cursor);

        // 2. 先頭の空白数をカウント
        int spaces = 0;
        for (char c : currentLine.toCharArray()) {
            if (c == ' ') spaces++;
            else break;
        }

        // 3. もし行末が '{' ならインデントを増やす (4つ)
        if (currentLine.trim().endsWith("{")) {
            spaces += 4;
        }

        // 4. 改行とスペースを挿入
        StringBuilder insertion = new StringBuilder("\n");
        for (int i = 0; i < spaces; i++) insertion.append(" ");

        text = text.substring(0, cursor) + insertion + text.substring(cursor);
        cursor += insertion.length();
    }

    // --- マウス操作 ---
    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (this.isMouseOver(click.x(), click.y())) {
            this.isFocused = true;
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            return true;
        } else {
            this.isFocused = false;
        }
        return false;
    }

    // ホイールスクロール
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isFocused) {
            scrollOffset -= verticalAmount;
            if (scrollOffset < 0) scrollOffset = 0;
            return true;
        }
        return false;
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {}

    private int[] getCursorPos() {
        String content = this.text;
        // カーソル位置までの文字列を取得
        if (cursor > content.length()) cursor = content.length();
        if (cursor < 0) cursor = 0;

        String beforeCursor = content.substring(0, cursor);

        // 改行の数を数えてY座標を決める
        // split("\n", -1) は、末尾が改行の場合も空文字を含めるために必要
        String[] lines = beforeCursor.split("\n", -1);

        int lineIndex = lines.length - 1;
        String currentLine = lines[lineIndex];

        // 行の幅を計算してX座標を決める
        // " " (スペース) の幅もしっかり計算させる
        int x = getX() + 4 + textRenderer.getWidth(currentLine);
        int y = getY() + 4 + (lineIndex * 10) - (scrollOffset * 10);

        return new int[]{x, y};
    }

    private void moveCursorUp() {
        if (cursor == 0) return;

        // 1. 現在のカーソル位置の前にある「最後の改行」を探す
        int lineStart = text.lastIndexOf('\n', cursor - 1);

        if (lineStart == -1) {
            // 前に改行がない = 1行目にいるということなので、行頭(0)に移動
            cursor = 0;
            return;
        }

        // 2. 現在の行での「左端からの文字数(カラム)」を計算
        // lineStartは改行文字の位置なので、+1 した場所が行の始まり
        int currentColumn = cursor - (lineStart + 1);

        // 3. 「前の行」の開始位置を探す
        // lineStartの一つ前から、さらに遡って改行を探す
        int prevLineStart = text.lastIndexOf('\n', lineStart - 1);

        // prevLineStart が -1 なら「前の行」は1行目ということ
        // 1行目の開始位置は 0 なので、計算しやすいように調整
        int actualPrevLineStart = (prevLineStart == -1) ? 0 : prevLineStart + 1;

        // 4. 前の行の長さを計算
        // 前の行の終わりは、現在の行の始まり(lineStart)
        int prevLineLength = lineStart - actualPrevLineStart;

        // 5. 移動先を決定
        // 「同じカラム」に移動したいが、前の行が短い場合は「前の行の末尾」に合わせる
        int newColumn = Math.min(currentColumn, prevLineLength);

        cursor = actualPrevLineStart + newColumn;

        ensureCursorVisible();
    }

    private void moveCursorDown() {
        // 現在位置より後ろに改行があるか探す
        int lineEnd = text.indexOf('\n', cursor);

        if (lineEnd == -1) {
            // 後ろに改行がない = 最終行にいるので、テキスト末尾に移動
            cursor = text.length();
            return;
        }

        // 1. 現在の行の開始位置を探す (カラム計算用)
        int lineStart = text.lastIndexOf('\n', cursor - 1);
        int currentColumn = cursor - (lineStart + 1);

        // 2. 次の行の開始位置
        int nextLineStart = lineEnd + 1;

        // 3. 次の行の終わりを探す (長さ計算用)
        int nextLineEnd = text.indexOf('\n', nextLineStart);
        if (nextLineEnd == -1) {
            nextLineEnd = text.length();
        }

        // 4. 次の行の長さ
        int nextLineLength = nextLineEnd - nextLineStart;

        // 5. 移動先決定
        int newColumn = Math.min(currentColumn, nextLineLength);

        cursor = nextLineStart + newColumn;

        ensureCursorVisible();
    }

    private void ensureCursorVisible() {
        // 1. カーソルが何行目にあるか計算
        String beforeCursor = text.substring(0, Math.min(cursor, text.length()));
        // 行数 = 改行の数
        int cursorLine = 0;
        for (char c : beforeCursor.toCharArray()) {
            if (c == '\n') cursorLine++;
        }

        // 2. 画面に表示できる行数を計算 (高さ / 1行10px)
        // 上下の余白(4pxずつ)を引いて計算
        int visibleLines = (this.height - 8) / 10;

        // 3. スクロール調整
        // カーソルが「上」にはみ出ている場合
        if (cursorLine < scrollOffset) {
            scrollOffset = cursorLine;
        }
        // カーソルが「下」にはみ出ている場合
        else if (cursorLine >= scrollOffset + visibleLines) {
            scrollOffset = cursorLine - visibleLines + 1;
        }
    }
}
