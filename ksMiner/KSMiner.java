package ksMiner;

import methods.Wait;

import org.liquidbot.osrs.api.Manifest;
import org.liquidbot.osrs.api.LiquidScript;
import org.liquidbot.osrs.api.SkillCategory;
import org.liquidbot.osrs.api.methods.data.*;
import org.liquidbot.osrs.api.methods.data.movement.Camera;
import org.liquidbot.osrs.api.methods.interactive.GameEntities;
import org.liquidbot.osrs.api.methods.interactive.Players;
import org.liquidbot.osrs.api.wrapper.GameObject;
import org.liquidbot.osrs.api.wrapper.Item;

@Manifest(
        scriptName = "KS Miner",
        description = "Mines ore, and drops!",
        category = SkillCategory.MINING,
        author = "KickStarter",
        version = 0.1001
)
public class KSMiner extends LiquidScript {

    private static int oreItemId, pickId;
    private static final int[] ORE_ROCK_IDS = { 7172, 7173, 7174 };

    @Override
    public void onStart() {
        log("Welcome to KS Miner");

        // TODO GUI
        oreItemId = 438;
        pickId = 1265;

        // Stops if tools/level requirements are not valid
        if(check()) {
            log("Pick-axe has been found on your person, started successfully!");
        } else {
            log("No pick-axes were found, stopping...");
            stop();
        }

    }

    @Override
    public void onStop() {
        log("Thanks for using KS Miner!");
        // TODO Print gained level information and screen-shots
    }

    @Override
    public int operate() {
        switch (state()) {
            case MINE: {
                GameObject oreRock = GameEntities.getNearest(ORE_ROCK_IDS);
                if(oreRock != null) {
                    if (oreRock.isOnScreen()) {
                        if (oreRock.interact("Mine", "Rocks")) {
                            final int invCount = Inventory.getAllItems().length;
                            log("Waiting until ore is in inventory");
                            Wait.dynamic(() -> invCount != Inventory.getAllItems().length, 200, 50);
                        }
                    } else {
                        Camera.turnAngleTo(oreRock);
                    }
                } else {
                    log("Problem loading ore rock.");
                }
                break;
            }
            case DROP: {
                Item[] ores = Inventory.getAllItems(item -> item.getId() == oreItemId);
                for (Item item : ores) {
                    if (item.interact("Drop")) {
                        int invCount = Inventory.getAllItems().length;
                        Wait.dynamic(() -> invCount != Inventory.getAllItems().length, 200, 50);
                    }
                }
                break;
            }
            case WAIT: {
                log("Waiting until player is idle");
                Wait.dynamic(this::isIdle, 200, 50);
                break;
            }
        }
        return 200;
    }

    private state state() {
        if(Inventory.isFull()) {
            return state.DROP;
        } else if(isIdle()) {
            return state.MINE;
        }
        return state.WAIT;
    }

    private enum state {
        // Script based upon a finite-state-machine
        MINE, DROP, WAIT
    }

    private boolean isIdle() {
        return !Players.getLocal().isMoving() && Players.getLocal().getAnimation() == -1;
        // TODO Implement into separate methods class
    }

    private boolean check() {
        return Inventory.contains(pickId) || Equipment.contains(pickId);
        // TODO Check level requirement
    }
}
