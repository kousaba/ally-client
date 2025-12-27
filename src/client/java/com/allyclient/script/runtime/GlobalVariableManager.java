package com.allyclient.script.runtime;

import com.allyclient.AllyClientClient;
import net.minecraft.client.MinecraftClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;

public class GlobalVariableManager {
    private static final Map<String, Object> globals = new java.util.HashMap<>();
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static int ticks = 0;

    public static void update() {
        globals.put("fps", (double) mc.getCurrentFps());
        ticks++;

        // MinecraftClientから取得できるもの
        if (mc.player != null) {
            globals.put("x", mc.player.getX());
            globals.put("y", mc.player.getY());
            globals.put("z", mc.player.getZ());
            globals.put("yaw", (double) mc.player.getYaw());
            globals.put("pitch", (double) mc.player.getPitch());
            globals.put("hp", (double) mc.player.getHealth());
            globals.put("max_hp", (double) mc.player.getMaxHealth());
            globals.put("hunger", (double) mc.player.getHungerManager().getFoodLevel());
            globals.put("saturation", (double) mc.player.getHungerManager().getSaturationLevel());
            globals.put("air", (double) mc.player.getAir());
            globals.put("xp_level", (double) mc.player.experienceLevel);
            globals.put("xp_prog", (double) mc.player.experienceProgress);
            globals.put("speed", (double) Math.hypot(mc.player.getVelocity().x, mc.player.getVelocity().z) * 20);
            globals.put("gamemode", (mc.interactionManager != null ? mc.interactionManager.getCurrentGameMode().getId() : 0));
            globals.put("biome", mc.world.getBiome(mc.player.getBlockPos()).getKey().get().getValue().getPath());
            globals.put("light", (double) mc.world.getLightLevel(mc.player.getBlockPos()));
            globals.put("time", (double) (mc.world.getTimeOfDay() % 24000));
            globals.put("ping", mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()) != null ? mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()).getLatency() : 0);
            globals.put("held_item", mc.player.getMainHandStack().getItem().getName().getString());
            globals.put("held_item_stack", mc.player.getMainHandStack().getCount());
            globals.put("held_item_durability", (double) (mc.player.getMainHandStack().getMaxDamage() - mc.player.getMainHandStack().getDamage()));
            globals.put("held_item_sub", mc.player.getOffHandStack().getItem().getName().getString());
            globals.put("held_item_sub_stack", mc.player.getOffHandStack().getItem().getName().getString());
            globals.put("held_item_sub_durability", (double) (mc.player.getOffHandStack().getMaxDamage() - mc.player.getOffHandStack().getDamage()));
            globals.put("dimension", mc.world.getRegistryKey().getValue().getPath());
            // 必要に応じて変数を追加
        }
        // CPSなど
        // CPS
        // REAL_TIME
        LocalDateTime now = LocalDateTime.now();
        globals.put("real_time_year", now.getYear());
        globals.put("real_time_month", now.getMonthValue());
        globals.put("real_time_day", now.getDayOfMonth());
        globals.put("real_time_hour", now.getHour());
        globals.put("real_time_minute", now.getMinute());
        globals.put("real_time_second", now.getSecond());
        globals.put("real_time_millisecond", (double) now.get(ChronoField.MILLI_OF_SECOND));
        globals.put("real_time_nanosecond", (double) now.getNano());
        globals.put("real_time_weekday", now.getDayOfWeek().getValue());
        // Direction
        String direction;
        float yaw = mc.player != null ? mc.player.getYaw() % 360 : 0;
        if (yaw < 0) yaw += 360;
        if (yaw >= 315 || yaw < 45) direction = "S";
        else if (yaw >= 45 && yaw < 135) direction = "W";
        else if (yaw >= 135 && yaw < 225) direction = "N";
        else direction = "E";
        globals.put("direction", direction);
        // Session Time
        globals.put("session_time", (double) ((System.currentTimeMillis() - AllyClientClient.startTime) / 1000L));
        // System Info
        // 1 tick
        double cpuLoad = AllyClientClient.osBean.getCpuLoad() * 100.0;
        globals.put("system_cpu_load", (cpuLoad >= 0) ? cpuLoad : 0.0);
        globals.put("system_cpu_current_frequencies", (double) AllyClientClient.cpu.getCurrentFreq()[0]);
        // 1 sec
        if(ticks % (20 * 6) == 0){
            long freeMemory = Runtime.getRuntime().freeMemory();
            long memory = Runtime.getRuntime().totalMemory();
            globals.put("system_memory_free", (double) (freeMemory / (1024L * 1024L)));
            globals.put("system_memory_used", (double) ((memory - freeMemory) / (1024L * 1024L)));
            globals.put("system_memory_total", (double) (memory / (1024L * 1024L)));
            globals.put("system_os_thread_count", AllyClientClient.os.getThreadCount());
        }
        // 3 sec
        if(ticks % (60 * 6) == 0){
            globals.put("system_cpu_load_ticks", AllyClientClient.cpu.getSystemCpuLoadTicks().length > 0 ? (double) AllyClientClient.cpu.getSystemCpuLoadTicks()[0] : 0.0);
            globals.put("system_disk_reads", (double) AllyClientClient.disk.getReads());
            globals.put("system_disk_read_bytes", (double) AllyClientClient.disk.getReadBytes());
            globals.put("system_disk_writes", (double) AllyClientClient.disk.getWrites());
            globals.put("system_disk_write_bytes", (double) AllyClientClient.disk.getWriteBytes());
            globals.put("system_os_uptime", (double) AllyClientClient.os.getSystemUptime());
        }
        // 10 sec
        if(ticks % (20 * 10 * 6) == 0){
            globals.put("system_memory_available_bytes", (double) AllyClientClient.ram.getAvailable());
            globals.put("system_memory_virtual_memory", (double) AllyClientClient.ram.getVirtualMemory().getVirtualMax());
            globals.put("system_disk_transfer_time", (double) AllyClientClient.disk.getTransferTime());
        }
        // 20 sec
        if(ticks % (20 * 20 * 6) == 0){
            globals.put("system_cpu_load_avg", (double) AllyClientClient.cpu.getSystemLoadAverage(1)[0]);
            globals.put("system_os_process_count", AllyClientClient.os.getProcessCount());
            globals.put("system_os_processes", (double) AllyClientClient.os.getProcesses().size());
        }
        if(ticks % (20 * 60 * 6) == 0){
            globals.put("system_cpu_context_switches", (double) AllyClientClient.cpu.getContextSwitches());
            globals.put("system_cpu_interrupts", (double) AllyClientClient.cpu.getInterrupts());
            ticks = 0;
        }
    }
    public static Map<String, Object> getGlobals() {
        return globals;
    }
}
