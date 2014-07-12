package methods;

import ksMiner.KSMiner;
import org.liquidbot.osrs.api.LiquidScript;
import org.liquidbot.osrs.api.enums.Tab;
import org.liquidbot.osrs.api.methods.data.*;
import org.liquidbot.osrs.api.methods.input.Mouse;
import org.liquidbot.osrs.api.wrapper.Item;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Quick {

    private static void log(String info) {
        LiquidScript.log("QUICK METHODS: " + info);
    }

    public static boolean drop(Item item) {
        if (!Tab.INVENTORY.isOpen() && !Tab.INVENTORY.open()) {
            log("Failed to open inventory tab");
            return false;
        }
        if (!item.isValid()) {
            log("Item is not valid: " + item.getName());
            return false;
        }

        Point itemRandomPoint = new Point(item.getCentralPoint().getLocation().x + LiquidScript.random(-20, 20),
                item.getCentralPoint().getLocation().y + LiquidScript.random(-15, -5));
        Mouse.click(itemRandomPoint, false);
        if (Wait.dynamic(org.liquidbot.osrs.api.methods.data.Menu::isOpen, 25, 50)) {
            if (!org.liquidbot.osrs.api.methods.data.Menu.getActions().contains("Drop")) {
                log("Could not find drop action, on item: " + item.getName());
                return false;
            }
            if (org.liquidbot.osrs.api.methods.data.Menu.contains("->")) {
                org.liquidbot.osrs.api.methods.data.Menu.interact("Cancel");
            }
            if (org.liquidbot.osrs.api.methods.data.Menu.getActions().size() == 4 && item.getIndex() < 24) {
                Point menuPoint = new Point(Mouse.getLocation().x, Mouse.getLocation().y + LiquidScript.random(35, 45));
                Mouse.hop(menuPoint);
                Mouse.click(true);
                return true;
            } else if (org.liquidbot.osrs.api.methods.data.Menu.interact("Drop")) {
                return true;
            }
        } else {
            log("Failed to right-click item: " + item.getName());
        }
        return false;
    }

    public static boolean drop(Item[] item) {
        ArrayList<Item> itemsToDrop = new ArrayList<>();
        Collections.addAll(itemsToDrop, item);
        Iterator<Item> iterator = itemsToDrop.stream().iterator();
        while (iterator.hasNext()) {
            if (KSMiner.isRunning) {
                drop(iterator.next());
                continue;
            }
            return false;
        }
        return true;
    }

    public static boolean dropAll() {
        return drop(Inventory.getAllItems());
    }

    public static void dropAllExcept(Item[] notToDrop) {
        if (notToDrop.length == 0) {
            // The items that have been passed to be dropped, are not in the inventory.
            dropAll();
            return;
        }
        Item[] itemArray = new Item[28];
        for (int j = 0; j < itemArray.length; j++) {
            for (Item i : Inventory.getAllItems()) {
                for (Item e : notToDrop) {
                    if (!i.getName().equalsIgnoreCase(e.getName())) {
                        itemArray[j] = e;
                    }
                }
            }
        }
        drop(itemArray);
    }
}
