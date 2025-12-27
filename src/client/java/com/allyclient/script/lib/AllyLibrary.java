package com.allyclient.script.lib;

import javax.swing.*;
import javax.xml.crypto.dsig.keyinfo.KeyName;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class AllyLibrary{
    private static final Map<String, Object> functions = new HashMap<>();

    static{
        register("int", args -> (int) asDouble(args.get(0)));
        register("float", args -> asDouble(args.get(0)));
        register("double", args -> asDouble(args.get(0)));
        register("string", args -> args.get(0).toString());
        register("abs", args -> Math.abs(asDouble(args.get(0))));
        register("sin", args -> Math.sin(asDouble(args.get(0))));
        register("cos", args -> Math.cos(asDouble(args.get(0))));
        register("tan", args -> Math.tan(asDouble(args.get(0))));
        register("round", args -> Math.round(asDouble(args.get(0))));
        register("floor", args -> Math.floor(asDouble(args.get(0))));
        register("ceil", args -> Math.ceil(asDouble(args.get(0))));
        register("sqrt", args -> Math.sqrt(asDouble(args.get(0))));
        register("max", args -> Math.max(asDouble(args.get(0)), asDouble(args.get(1))));
        register("min", args -> Math.min(asDouble(args.get(0)), asDouble(args.get(1))));
        register("pow", args -> Math.pow(asDouble(args.get(0)), asDouble(args.get(1))));
        register("random", args -> Math.random());
        register("log10", args -> Math.log10(asDouble(args.get(0))));
        register("clamp", args -> {
            double val = asDouble(args.get(0));
            double min = asDouble(args.get(1));
            double max = asDouble(args.get(2));
            return Math.max(min, Math.min(max, val));
        });
        register("rgb", args -> {
            int r = (int) asDouble(args.get(0));
            int g = (int) asDouble(args.get(1));
            int b = (int) asDouble(args.get(2));
            return 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
        });
        register("rgba", args -> {
            int r = (int) asDouble(args.get(0));
            int g = (int) asDouble(args.get(1));
            int b = (int) asDouble(args.get(2));
            int a = (int) asDouble(args.get(3));
            return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
        });
        register("chroma", args -> {
            double speed = asDouble(args.get(0));
            if (speed <= 0) speed = 1.0;
            long time = System.currentTimeMillis();
            float hue = (time % (long)(speed * 1000)) / (float)(speed * 1000);
            return Color.HSBtoRGB(hue, 1.0f, 1.0f);
        });
        register("lerp_color", args -> {
            int c1 = asColor(args.get(0));
            int c2 = asColor(args.get(1));
            double ratio = Math.max(0, Math.min(1, asDouble(args.get(2))));

            int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
            int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;

            int a = (int) (a1 + (a2 - a1) * ratio);
            int r = (int) (r1 + (r2 - r1) * ratio);
            int g = (int) (g1 + (g2 - g1) * ratio);
            int b = (int) (b1 + (b2 - b1) * ratio);
            return (a << 24) | (r << 16) | (g << 8) | b;
        });
        register("draw_rect", args -> {
            double x = asDouble(args.get(0));
            double y = asDouble(args.get(1));
            double w = asDouble(args.get(2));
            double h = asDouble(args.get(3));
            int color = asColor(args.get(4));
            ScriptRenderContext.drawRect(x, y, w, h, color);
            return null;
        });
        register("draw_text", args -> {
            String text = args.get(0).toString();
            double x = asDouble(args.get(1));
            double y = asDouble(args.get(2));
            int color = asColor(args.get(3));
            double scale = (args.size() > 4) ? asDouble(args.get(4)) : 1.0;
            ScriptRenderContext.drawText(text, x, y, color, true, scale);
            return null;
        });
        register("draw_item", args -> {
            String itemId = args.get(0).toString();
            double x = asDouble(args.get(1));
            double y = asDouble(args.get(2));
            double scale = (args.size() > 3) ? asDouble(args.get(3)) : 1.0;
            ScriptRenderContext.drawItem(itemId, x, y, scale);
            return null;
        });
        register("get_inventory", args -> {
            java.util.List<String> items = new ArrayList<>();
            MinecraftClient mc = MinecraftClient.getInstance();
            if(mc.player != null){
                PlayerInventory inv = mc.player.getInventory();
                for(ItemStack stack : inv.getMainStacks()){
                    if(!stack.isEmpty()){
                        items.add(Registries.ITEM.getId(stack.getItem()).toString());
                    } else {
                        items.add("air"); // 取得できなかったらair
                    }
                }
            }
            return items;
        });
        register("get_armor", args -> {
            java.util.List<String> items = new ArrayList<>();
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                PlayerInventory inv = mc.player.getInventory();
                for(int i = 36;i < 40;i++){
                    ItemStack stack = inv.getStack(i);
                    if(!stack.isEmpty()){
                        items.add(Registries.ITEM.getId(stack.getItem()).toString());
                    }else{
                        items.add("air");
                    }
                }
            }
            return items;
        });
        register("get_main_hand", args -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if(mc.player != null){
                ItemStack stack = mc.player.getMainHandStack();
                if(!stack.isEmpty()){
                    return Registries.ITEM.getId(stack.getItem()).toString();
                }
            }
            return "air";
        });
        register("get_off_hand", args -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if(mc.player != null){
                ItemStack stack = mc.player.getOffHandStack();
                if(!stack.isEmpty()){
                    return Registries.ITEM.getId(stack.getItem()).toString();
                }
            }
            return "air";
        });
        register("is_key_down", args -> {
            String keyName = args.get(0).toString().toUpperCase();
            if(keyName.equals("LMB")) return GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;
            if(keyName.equals("RMB")) return GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_2) == GLFW.GLFW_PRESS;
            int keyCode = getKeyCode(keyName);
            if(keyCode == -1) return false;
            Window handle = MinecraftClient.getInstance().getWindow();
            return InputUtil.isKeyPressed(handle, keyCode);
        });
        register("len", args -> {
            if(args.get(0) instanceof java.util.List list){
                return list.size();
            }
            return 0;
        });
        register("add" , args -> {
            if (args.get(0) instanceof java.util.List list) {
                list.add(args.get(1));
            }
            return null;
        });
        register("set", args -> {
            if (args.get(0) instanceof java.util.List list){
                int i = (int) asDouble(args.get(1));
                if(i >= 0 && i < list.size()){
                    list.set(i, args.get(2));
                }
            }
            return null;
        });
        register("get", args -> {
            if (args.get(0) instanceof java.util.List list){
                int i = (int) asDouble(args.get(1));
                if (i >= 0 && i < list.size()){
                    return list.get(i);
                }
            }
            return 0.0;
        });
        register("clear", args -> {
            if(args.get(0) instanceof java.util.List list) list.clear();
            return null;
        });
        register("play_sound", args -> {
            try{
                String id = args.get(0).toString();
                float pitch = (args.size() > 1) ? (float) asDouble(args.get(1)) : 1.0f;
                float volume = (args.size() > 2) ? (float) asDouble(args.get(2)) : 1.0f;

                if(!id.contains(":")) id = "minecraft:" + id; // minecraft:がなくても動くようにする
                Identifier soundId = Identifier.of(id);
                SoundEvent event = Registries.SOUND_EVENT.get(soundId);
                if(event != null){
                    // UI音として再生(座標などに関係なく聞こえるようになる)
                    MinecraftClient.getInstance().getSoundManager().play(
                            PositionedSoundInstance.master(event,pitch,volume)
                    );
                }
            } catch (Exception ignored){
                // 無視
            }
            return null;
        });
        register("show_toast", args -> {
            String title = args.get(0).toString();
            String message = (args.size() > 1) ? args.get(1).toString() : "";
            SystemToast toast = new SystemToast(
                    SystemToast.Type.PERIODIC_NOTIFICATION,
                    Text.of(title),
                    Text.of(message)
            );
            MinecraftClient.getInstance().getToastManager().add(toast);
            return null;
        });
        register("get_target", args -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.world == null) return null;

            Entity camera = mc.getCameraEntity();
            if (camera == null) return null;

            // 探索距離 (長めに設定)
            double range = 100.0;

            // 1. 視線の始点と終点を計算
            Vec3d start = camera.getCameraPosVec(1.0F);
            Vec3d rotation = camera.getRotationVec(1.0F);
            Vec3d end = start.add(rotation.multiply(range));

            // 2. 探索範囲のボックスを作成 (視線を含む大きな箱)
            Box searchBox = camera.getBoundingBox()
                    .stretch(rotation.multiply(range))
                    .expand(1.0, 1.0, 1.0);

            Entity closestEntity = null;
            double closestDistSq = range * range; // 最短距離の2乗

            // 3. 範囲内のエンティティを総当たりチェック
            for (Entity entity : mc.world.getOtherEntities(camera, searchBox)) {
                // スペクテイターは無視
                if (entity.isSpectator()) continue;

                // 生き物以外 (ドロップアイテムや矢など) を無視したい場合はここに追加
                // if (!(entity instanceof LivingEntity)) continue;

                // エンティティの当たり判定ボックスを取得 (少し余裕を持たせる)
                float margin = entity.getTargetingMargin();
                Box entityBox = entity.getBoundingBox().expand(margin);

                // 4. 視線ベクトルとボックスが交差しているか判定
                var hitResult = entityBox.raycast(start, end);

                if (hitResult.isPresent()) {
                    // 交差点までの距離を確認
                    double distSq = start.squaredDistanceTo(hitResult.get());

                    // 一番近いものを採用
                    if (distSq < closestDistSq) {
                        closestEntity = entity;
                        closestDistSq = distSq;
                    }
                }
            }

            // 見つかったエンティティをMapに変換して返す
            if (closestEntity != null) {
                return entityToMap(mc, closestEntity);
            }

            return null;
        });
        register("get_entities", args -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.world == null) return new ArrayList<>();

            double range = (args.size() > 0) ? asDouble(args.get(0)) : 20.0;
            java.util.List<Map<String, Object>> list = new ArrayList<>();

            Box box = mc.player.getBoundingBox().expand(range);
            for (Entity e : mc.world.getOtherEntities(mc.player, box)) {
                list.add(entityToMap(mc, e));
            }
            return list;
        });
    }

    private static void register(String name, FunctionImpl impl){
        functions.put(name, impl);
    }
    public static FunctionImpl get(String name){
        return (FunctionImpl) functions.get(name);
    }
    public static boolean has(String name){
        return functions.containsKey(name);
    }

    private static double asDouble(Object o){
        if(o instanceof Number n) return n.doubleValue();
        return 0.0;
    }

    private static int asColor(Object o) {
        if (o instanceof Number n) return n.intValue();
        if (o instanceof String s) {
            try {
                if (s.startsWith("#")) {
                    String hex = s.substring(1);
                    long val = Long.parseLong(hex, 16);
                    if (hex.length() <= 6) val |= 0xFF000000L;
                    return (int) val;
                }
                return Integer.parseInt(s);
            } catch (Exception e) { return 0xFFFFFFFF; }
        }
        return 0xFFFFFFFF;
    }

    private static int getKeyCode(String keyName){
        if(keyName.length() == 1){
            char c = keyName.charAt(0);
            if ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                return c;
            }
        }
        if(keyName.charAt(0) == 'F'){
            try{
                int fn = Integer.parseInt(keyName.substring(1));
                if(fn >= 1 && fn <= 12){
                    return GLFW.GLFW_KEY_F1 + (fn - 1);
                }
            } catch(Exception ignored){}
        }
        return switch(keyName){
            case "SPACE" -> GLFW.GLFW_KEY_SPACE;
            case "SHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "RSHIFT" -> GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "CTRL" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "ALT" -> GLFW.GLFW_KEY_LEFT_ALT;
            case "TAB" -> GLFW.GLFW_KEY_TAB;
            case "ENTER" -> GLFW.GLFW_KEY_ENTER;
            case "ESC" -> GLFW.GLFW_KEY_ESCAPE;
            case "UP" -> GLFW.GLFW_KEY_UP;
            case "DOWN" -> GLFW.GLFW_KEY_DOWN;
            case "LEFT" -> GLFW.GLFW_KEY_LEFT;
            case "RIGHT" -> GLFW.GLFW_KEY_RIGHT;
            case "BACKSPACE" -> GLFW.GLFW_KEY_BACKSPACE;
            case "DELETE" -> GLFW.GLFW_KEY_DELETE;
            case "INSERT" -> GLFW.GLFW_KEY_INSERT;
            case "HOME" -> GLFW.GLFW_KEY_HOME;
            case "END" -> GLFW.GLFW_KEY_END;
            case "PAGEUP" -> GLFW.GLFW_KEY_PAGE_UP;
            case "PAGEDOWN" -> GLFW.GLFW_KEY_PAGE_DOWN;
            default -> -1;
        };
    }
    private static Map<String, Object> entityToMap(MinecraftClient mc, Entity e) {
        Map<String, Object> map = new HashMap<>();

        map.put("name", e.getName().getString());
        map.put("type", e.getType().toString()); // "entity.minecraft.zombie" など
        map.put("x", e.getX());
        map.put("y", e.getY());
        map.put("z", e.getZ());

        // プレイヤーからの距離
        if (mc.player != null) {
            map.put("dist", (double) mc.player.distanceTo(e));
        }

        // 生き物ならHPも取得
        if (e instanceof LivingEntity living) {
            map.put("hp", (double) living.getHealth());
            map.put("max_hp", (double) living.getMaxHealth());
            map.put("armor", (double) living.getArmor());
        } else {
            map.put("hp", 0.0);
            map.put("max_hp", 0.0);
        }

        // プレイヤーならPingなども取れるかも
        if (e instanceof PlayerEntity player) {
            map.put("is_player", 1.0);
        } else {
            map.put("is_player", 0.0);
        }

        return map;
    }
}
