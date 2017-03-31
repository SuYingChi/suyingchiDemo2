package com.ihs.booster.utils;

import com.ihs.commons.utils.HSLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

/**
 * Created by ihandysoft on 4/10/16.
 */
public class CPUUtil {
    public static final int CPU_TEMPERATURE_LEVEL_1 = 20;
    public static final int CPU_TEMPERATURE_LEVEL_2 = 29;
    public static final int CPU_TEMPERATURE_LEVEL_3 = 40;
    public static final int CPU_TEMPERATURE_LEVEL_4 = 50;

    private static final int CPU_TEMPERATURE_PATH_NOT_INIT = -1;
    private static final int CPU_TEMPERATURE_PATH_NOT_FOUND = -2;

    public static final int BASE_ADDED_NUMBER_FOR_BATTERY = 5;
    public static final double BASE_NUMBER_FOR_RANDOM = 30.0;

    private static int cpuTemperaturePathIndex = CPU_TEMPERATURE_PATH_NOT_INIT;
    private static final String[] CPU_TEMPERATURE_FILE_PATH = {
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/devices/virtual/thermal/thermal_zone1/temp",
            "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
            "/sys/devices/system/cpu/cpu0/cpufreq/FakeShmoo_cpu_temp",
            "/sys/class/i2c-adapter/i2c-4/4-004c/temperature",
            "/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/temperature",
            "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
            "/sys/devices/platform/tegra_tmon/temp1_input",
            "/sys/kernel/debug/tegra_thermal/temp_tj",
            "/sys/devices/platform/s5p-tmu/temperature",
            "/sys/class/hwmon/hwmon0/device/temp1_input",
            "/sys/devices/platform/s5p-tmu/curr_temp", "/sys/htc/cpu_temp",
            "/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/ext_temperature",
            "/sys/devices/platform/tegra-tsensor/tsensor_temperature"};

    // 降温后要制造cpu温度下降的假象
    // 返回cool down后的降低的温度，这个温度将维持一段时间
    public static int[] generateFakeCoolDownTemperature(float cooledRatio) {
        Random generator = new Random();

        // 初始化温度，至少将要降低1度
        int currentTemperature = (int) getCpuTemperature();
        int randomDown = (int) ((generator.nextFloat() * 6 * cooledRatio) / 2 + 1.0f);

        HSLog.d("cooledRatio:" + cooledRatio);
        int afterCooledTemperature;

        if (currentTemperature < CPU_TEMPERATURE_LEVEL_1) {
            afterCooledTemperature = currentTemperature - randomDown;
        } else if (currentTemperature >= CPU_TEMPERATURE_LEVEL_1 && currentTemperature < CPU_TEMPERATURE_LEVEL_2) {
            afterCooledTemperature = CPU_TEMPERATURE_LEVEL_1 - randomDown;
        } else if (currentTemperature >= CPU_TEMPERATURE_LEVEL_2 && currentTemperature < CPU_TEMPERATURE_LEVEL_3) {
            afterCooledTemperature = CPU_TEMPERATURE_LEVEL_2 - randomDown;
        } else if (currentTemperature >= CPU_TEMPERATURE_LEVEL_3 && currentTemperature < CPU_TEMPERATURE_LEVEL_4) {
            afterCooledTemperature = CPU_TEMPERATURE_LEVEL_3 - randomDown;
        } else {
            afterCooledTemperature = CPU_TEMPERATURE_LEVEL_4 - randomDown;
        }

        int decreasedTemperature = currentTemperature - afterCooledTemperature;
        return new int[]{decreasedTemperature, afterCooledTemperature};
    }

    public static float getCpuTemperature() {
        return 35f;
    }


    private static void initTempIndex() {
        for (cpuTemperaturePathIndex = 0; cpuTemperaturePathIndex < CPU_TEMPERATURE_FILE_PATH.length; cpuTemperaturePathIndex++) {
            if (readFileInt(CPU_TEMPERATURE_FILE_PATH[cpuTemperaturePathIndex]) <= 0) {
                break;
            }
        }

        if (cpuTemperaturePathIndex >= CPU_TEMPERATURE_FILE_PATH.length) {
            cpuTemperaturePathIndex = CPU_TEMPERATURE_PATH_NOT_FOUND;
            HSLog.d("没有成功获取到cpu温度数组的index!");
        }
    }

    private static int readFileInt(String path) {
        File file = new File(path);

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            return Integer.parseInt(reader.readLine());
        } catch (IOException | NumberFormatException e) {
            HSLog.d("读取cpu温度文件失败!");
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        return -1;
    }
}
