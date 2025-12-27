package com.allyclient;

import com.allyclient.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

import java.util.Map;

public class AllyClientClient implements ClientModInitializer {
	public static long startTime;
	public static SystemInfo si;
	public static CentralProcessor cpu;
	public static CentralProcessor.ProcessorIdentifier cpuId;
	public static GraphicsCard gpu;
	public static GlobalMemory ram;
	public static HWDiskStore disk;
	public static OperatingSystem os;
	public static Sensors sensors;
	public static com.sun.management.OperatingSystemMXBean osBean =
			(com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
	public static Map<String, Object> globalScriptVariables = new java.util.HashMap<>();
	@Override
	public void onInitializeClient() {
		startTime = System.currentTimeMillis();
		si = new SystemInfo();
		cpu = si.getHardware().getProcessor();
		cpuId = cpu.getProcessorIdentifier();
		gpu = si.getHardware().getGraphicsCards().getFirst();
		ram = si.getHardware().getMemory();
		disk = si.getHardware().getDiskStores().getFirst();
		os = si.getOperatingSystem();
		// sensors = si.getHardware().getSensors();
		setGlobalScriptVariables();
		ModuleManager.init();
		com.allyclient.config.ConfigManager.load();
	}

	private void setGlobalScriptVariables(){
		globalScriptVariables.put("system_memory_all", (double) ram.getTotal());
		globalScriptVariables.put("system_memory_page_size", (double) AllyClientClient.ram.getPageSize());
		globalScriptVariables.put("system_memory_manufacturer", AllyClientClient.ram.getPhysicalMemory().getFirst().getManufacturer());
		globalScriptVariables.put("system_memory_memory_type", AllyClientClient.ram.getPhysicalMemory().getFirst().getMemoryType());
		globalScriptVariables.put("system_memory_clock_speed", (double) AllyClientClient.ram.getPhysicalMemory().getFirst().getClockSpeed());
		globalScriptVariables.put("system_cpu_cores", (double) Runtime.getRuntime().availableProcessors());
		globalScriptVariables.put("system_cpu_name", AllyClientClient.cpuId.getName());
		globalScriptVariables.put("system_cpu_vendor", AllyClientClient.cpuId.getVendor());
		globalScriptVariables.put("system_cpu_micro_architecture", AllyClientClient.cpuId.getMicroarchitecture());
		globalScriptVariables.put("system_cpu_frequency", (double) AllyClientClient.cpuId.getVendorFreq() / 1_000_000_000.0); // GHz
		globalScriptVariables.put("system_cpu_physical_cores", (double) AllyClientClient.cpu.getPhysicalProcessorCount());
		globalScriptVariables.put("system_cpu_logical_cores", (double) AllyClientClient.cpu.getLogicalProcessorCount());
		globalScriptVariables.put("system_cpu_physical_package_count", (double) AllyClientClient.cpu.getPhysicalPackageCount());
		globalScriptVariables.put("system_cpu_max_frequency", (double) AllyClientClient.cpu.getMaxFreq());
		globalScriptVariables.put("system_cpu_identifier", AllyClientClient.cpuId.getIdentifier());
		globalScriptVariables.put("system_cpu_model", AllyClientClient.cpuId.getModel());
		globalScriptVariables.put("system_cpu_family", AllyClientClient.cpuId.getFamily());
		globalScriptVariables.put("system_cpu_stepping", AllyClientClient.cpuId.getStepping());
		globalScriptVariables.put("system_cpu_processor_id", AllyClientClient.cpuId.getProcessorID());
		globalScriptVariables.put("system_cpu_is_64bit", AllyClientClient.cpuId.isCpu64bit() ? 1.0 : 0.0);
		globalScriptVariables.put("system_gpu_name", AllyClientClient.gpu.getName());
		globalScriptVariables.put("system_gpu_vendor", AllyClientClient.gpu.getVendor());
		globalScriptVariables.put("system_gpu_device_id", AllyClientClient.gpu.getDeviceId());
		globalScriptVariables.put("system_gpu_vram_mb", (double) AllyClientClient.gpu.getVRam() / (1024.0 * 1024.0)); // MB
		globalScriptVariables.put("system_gpu_vram", (double) AllyClientClient.gpu.getVRam() / (1024.0 * 1024.0 * 1024.0)); // GB
		globalScriptVariables.put("system_gpu_version_info", AllyClientClient.gpu.getVersionInfo());
		globalScriptVariables.put("system_disk_model", AllyClientClient.disk.getModel());
		globalScriptVariables.put("system_disk_name", AllyClientClient.disk.getName());
		globalScriptVariables.put("system_disk_size", (double) AllyClientClient.disk.getSize() / (1024.0 * 1024.0 * 1024.0)); // GB
		globalScriptVariables.put("system_disk_serial", AllyClientClient.disk.getSerial());
		globalScriptVariables.put("system_os_family", AllyClientClient.os.getFamily());
		globalScriptVariables.put("system_os_manufacturer", AllyClientClient.os.getManufacturer());
		globalScriptVariables.put("system_os_version", AllyClientClient.os.getVersionInfo().getVersion());
		globalScriptVariables.put("system_os_bitness", AllyClientClient.os.getBitness());
		globalScriptVariables.put("system_os_process_id", (double) AllyClientClient.os.getProcessId());
	}
}