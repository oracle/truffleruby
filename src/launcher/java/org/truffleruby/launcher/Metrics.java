package org.truffleruby.launcher;

import org.graalvm.launcher.Launcher;

import java.lang.management.ManagementFactory;

public class Metrics {

    // These system properties are used before outside the SDK option system
    public static boolean METRICS_TIME;
    private static final boolean METRICS_MEMORY_USED_ON_EXIT =
            Boolean.getBoolean("truffleruby.metrics.memory_used_on_exit");

    public static void printTime(String id) {
        if (METRICS_TIME) {
            final long millis = System.currentTimeMillis();
            System.err.printf("%s %d.%03d%n", id, millis / 1000, millis % 1000);
        }
    }

    private static void printMemory() {
        // Memory stats aren't available in native.
        if (!Launcher.isAOT() && METRICS_MEMORY_USED_ON_EXIT) {
            for (int n = 0; n < 10; n++) {
                System.gc();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.err.printf("allocated %d%n", ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());
        }
    }

    static void end() {
        printTime("after-main");
        printMemory();
    }

    static void begin() {
        // Assigned here so it's available on SVM as well
        METRICS_TIME = Boolean.getBoolean("truffleruby.metrics.time");

        printTime("before-main");
    }
}
