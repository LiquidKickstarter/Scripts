package ksMiner;

import methods.Quick;
import methods.Wait;

import org.liquidbot.osrs.api.Manifest;
import org.liquidbot.osrs.api.LiquidScript;
import org.liquidbot.osrs.api.SkillCategory;
import org.liquidbot.osrs.api.enums.Tab;
import org.liquidbot.osrs.api.methods.data.*;
import org.liquidbot.osrs.api.methods.data.Menu;
import org.liquidbot.osrs.api.methods.data.movement.Camera;
import org.liquidbot.osrs.api.methods.input.Mouse;
import org.liquidbot.osrs.api.methods.interactive.GameEntities;
import org.liquidbot.osrs.api.methods.interactive.Players;
import org.liquidbot.osrs.api.methods.interactive.Widgets;
import org.liquidbot.osrs.api.util.Filter;
import org.liquidbot.osrs.api.wrapper.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

@Manifest(
        scriptName = "KS Miner",
        description = "Mines ore, and drops!",
        category = SkillCategory.MINING,
        author = "KickStarter",
        version = 0.1001
)
public class KSMiner extends LiquidScript {

    private static final int[] ORE_ROCK_IDS = {7172, 7173, 7174, 7175, 7176},
            POSSIBLE_EXPLODE_IDS = {13430, 13431, 13432, 13433, 13434, 13453};
    private static final String
            GEM_NAME = "Uncut",
            PICK_AXE_NAME = "pickaxe";

    public static Boolean isRunning;

    @Override
    public void onStart() {
        isRunning = true;

        log("Welcome to KS Miner");
        Mouse.setHumanInput(false);


        // Stops if tools/level requirements are not valid
        if (check()) {
            log("Pick-axe has been found on your person, started successfully!");
        } else {
            log("No pick-axes were found, stopping...");
            stop();
        }

        // TODO GUI
    }

    @Override
    public void onStop() {
        isRunning = false;
        log("Thanks for using KS Miner!");
        Mouse.setHumanInput(true);
        // TODO Print gained level information and screen-shots
    }

    @Override
    public int operate() {
        switch (state()) {
            case MINE: {
                GameObject oreRock = GameEntities.getNearest(ORE_ROCK_IDS);
                if (oreRock.isOnScreen()) {
                    if (oreRock.interact("Mine", "Rocks")) {
                        final int invCount = Inventory.getAllItems().length;
                        Wait.dynamic(() -> invCount != Inventory.getAllItems().length, 200, 50);
                    }
                } else {
                    Camera.turnAngleTo(oreRock);
                }
                break;
            }
            case DROP: {
                // Will drop all items except pick-axe, based off strings.
                Item[] notToDrop = Inventory.getAllItems(item ->
                        item.getName().contains(PICK_AXE_NAME));
                Quick.dropAllExcept(notToDrop);
                break;
            }
            case WAIT_IDLE: {
                Wait.dynamic(this::isIdle, 200, 50);
                break;
            }
            case WAIT_EXPLODE: {
                log("Explode visible");
                if (!isIdle()) {
                    Tile randomTile = new Tile(Players.getLocal().getLocation().getX() + random(-2,2),
                            Players.getLocal().getLocation().getY() + random(-2,2));
                    if (!randomTile.isOnScreen()) {
                        Camera.turnAngleTo(randomTile);
                    }
                    if (randomTile.interact("Walk here")) {
                        Wait.dynamic(() -> Players.getLocal().getLocation().equals(randomTile), 200, 20);
                    }
                }
                Wait.dynamic(() -> GameEntities.getAll(POSSIBLE_EXPLODE_IDS).length == 0, 200, 50);
            }
        }
        return 200;
    }

    private state state() {
        if (GameEntities.getAll(POSSIBLE_EXPLODE_IDS).length > 0) {
            return state.WAIT_EXPLODE;
        } else if (Inventory.isFull()) {
            return state.DROP;
        } else if (isIdle()) {
            return state.MINE;
        }
        return state.WAIT_IDLE;
    }

    private enum state {
        // Script based upon a finite-state-machine
        MINE, DROP, WAIT_IDLE, WAIT_EXPLODE
    }

    private boolean isIdle() {
        return !Players.getLocal().isMoving() && Players.getLocal().getAnimation() == -1;
        // TODO Implement into separate methods class
    }

    private boolean check() {
        if (!Tab.INVENTORY.isOpen() && !Tab.INVENTORY.open()) {
            log("Failed to open equipment tab");
            return false;
        }
        if (Inventory.getAllItems(item -> item.getName().contains(PICK_AXE_NAME)).length > 0) {
            return true;
        }
        if (!Tab.EQUIPMENT.isOpen() && !Tab.EQUIPMENT.open()) {
            log("Failed to open equipment tab");
            return false;
        }
        WidgetChild weaponSlot = Widgets.get(387).getChild(9);
        // Wait#dynamic added due to loading of component names
        if (!Wait.dynamic(() -> weaponSlot.getName().length() > 0, 200, 20)) {
            log("Unable build weapon slot name");
            return false;
        }
        if (Wait.dynamic(() ->
                weaponSlot.getName().substring(0, weaponSlot.getName().indexOf("<")).contains(PICK_AXE_NAME), 200, 20)) {
            Tab.INVENTORY.open();
            return true;
        }
        return false;
        // TODO Check level requirement
    }
}
