package com.allyclient.settings.impl;


import com.allyclient.module.FeatureModule;

public class SprintModule extends FeatureModule {
    public SprintModule() {
        super("ToggleSprint", true);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        // 前に進もうとしているか？ (Wキーなど)
        boolean movingForward = mc.player.input.hasForwardMovement();

        // すでに走っている、スニーク中、空腹、壁にぶつかっている等の除外判定は
        // Minecraft本体の setSprinting がある程度やってくれますが、
        // 強制的にフラグを立てることでダッシュを維持させます。

        if (movingForward && !mc.player.isSneaking() && !mc.player.horizontalCollision && mc.player.getHungerManager().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }
    }
}
