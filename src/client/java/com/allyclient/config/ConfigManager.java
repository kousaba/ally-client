package com.allyclient.config;

import com.allyclient.module.Module;
import com.allyclient.module.ModuleManager;
import com.allyclient.module.HudModule;
import com.allyclient.settings.Setting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class ConfigManager {
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("allyclient.json").toFile();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save(){
        JsonObject root = new JsonObject();
        JsonObject modulesJson = new JsonObject();

        for(Module module : ModuleManager.modules){
            JsonObject moduleData = new JsonObject();

            moduleData.addProperty("enabled", module.enabled);

            if(module instanceof HudModule hudModule){
                moduleData.addProperty("x", hudModule.x);
                moduleData.addProperty("y", hudModule.y);
            }

            JsonObject settingJson = new JsonObject();
            for(Setting setting : module.settings){
                settingJson.add(setting.name, setting.save());
            }
            moduleData.add("settings", settingJson);

            modulesJson.add(module.name, moduleData);
        }
        root.add("modules", modulesJson);

        try(FileWriter writer = new FileWriter(CONFIG_FILE)){
            GSON.toJson(root, writer);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void load(){
        if(!CONFIG_FILE.exists()) return;
        try(FileReader reader = new FileReader(CONFIG_FILE)){
            JsonObject root = GSON.fromJson(reader, JsonObject.class);

            if(!root.has("modules")) return;
            JsonObject modulesJson = root.getAsJsonObject("modules");

            for (Module module : ModuleManager.modules) {
                if (modulesJson.has(module.name)) {
                    JsonObject moduleData = modulesJson.getAsJsonObject(module.name);

                    // 1. 基本データ読み込み
                    if (moduleData.has("enabled")) {
                        // 最初にenabledの状態によって、onEnable/onDisableを呼び出す
                        // これがないとTimeChangerとかFullbrightが正しく動作しない
                        boolean isEnabled = moduleData.get("enabled").getAsBoolean();
                        module.enabled = isEnabled;
                        if (isEnabled) {
                            module.onEnable();
                        } else {
                            module.onDisable();
                        }
                    }
                    if(module instanceof HudModule hudModule){
                        if (moduleData.has("x")) hudModule.x = moduleData.get("x").getAsInt();
                        if (moduleData.has("y")) hudModule.y = moduleData.get("y").getAsInt();
                    }

                    // 2. 詳細設定読み込み
                    if (moduleData.has("settings")) {
                        JsonObject settingsJson = moduleData.getAsJsonObject("settings");
                        for (Setting setting : module.settings) {
                            if (settingsJson.has(setting.name)) {
                                // 各設定クラスに実装した load() を呼ぶ
                                setting.load(settingsJson.get(setting.name));
                            }
                        }
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
