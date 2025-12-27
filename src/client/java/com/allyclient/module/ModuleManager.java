package com.allyclient.module;

import com.allyclient.module.impl.*;
import com.allyclient.settings.impl.SprintModule;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final List<Module> modules = new ArrayList<>();

//    public static void init(){
//        modules.add(new FpsModule());
//        modules.add(new WatermarkModule());
//        modules.add(new CoordModule());
//        modules.add(new CpsModule());
//        modules.add(new ScriptModule("Script 1", 10, 100));
//        modules.add(new ScriptModule("Script 2", 10, 120));
//        modules.add(new ScriptModule("Script 3", 10, 140));
//    }
//
//    public static void render(DrawContext context){
//        for(HudModule module : modules){
//            if(module.enabled){
//                module.render(context);
//            }
//        }
//    }
    public static void init(){
        modules.add(new FpsModule());
        modules.add(new WatermarkModule());
        modules.add(new CoordModule());
        modules.add(new CpsModule());
        modules.add(new ScriptModule("Script 1", 10, 100));
        modules.add(new ScriptModule("Script 2", 10, 120));
        modules.add(new ScriptModule("Script 3", 10, 140));
        modules.add(new ScriptFeatureModule("Script Feature 1", ""));
        modules.add(new ZoomModule());
        modules.add(new FullbrightModule());
        modules.add(new SprintModule());
        modules.add(new TimeChangerModule());
        modules.add(new BlockOverlayModule());
        modules.add(new ScriptCanvasModule("Script Canvas 1", 10, 160, ""));
    }

    public static void onTick(){
        for(Module m : modules){
            if(m.enabled) m.onTick();
        }
    }

    public static void render(DrawContext context){
        for(Module m: modules){
            if(m.enabled && m instanceof HudModule hud){
                hud.render(context);
            }
        }
    }

    public static <T extends Module> T getModule(Class<T> clazz){
        for(Module m : modules){
            if(clazz.isInstance(m)){
                return clazz.cast(m);
            }
        }
        return null;
    }
}
