package methods;

import org.liquidbot.osrs.api.LiquidScript;
import java.util.concurrent.Callable;

public class Wait {

    private static void log(String info) {
        LiquidScript.log("WAIT METHODS: " + info);
    }

    private static int variance() {
        return org.liquidbot.osrs.api.util.Random.nextInt(75, 150) / 100;
    }

    public static void sleep(int time) {
        try {
            Thread.sleep(time * variance());
        } catch (InterruptedException e) {
            log(e.getMessage());
        }
    }

    public static Boolean dynamic(Callable<Boolean> c, int f, int t) {
        try {
            for (int i = 0; i < t; i++) {
                if (!c.call()) {
                    Wait.sleep(f);
                } else {
                    break;
                }
            }
            return c.call();
        } catch (Exception e) {
            log(e.getMessage());
        }
        return false;
    }
}
