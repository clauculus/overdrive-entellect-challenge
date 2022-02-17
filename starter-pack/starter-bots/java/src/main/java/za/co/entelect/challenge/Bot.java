package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.State;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

public class Bot {

    private static final int maxSpeed = 9;

    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;

    private final static int MINIMUM_SPEED = 0;
    private final static int SPEED_STATE_1 = 3;
    private final static int INITIAL_SPEED = 5;
    private final static int SPEED_STATE_2 = 6;
    private final static int SPEED_STATE_3 = 8;
    private final static int MAXIMUM_SPEED = 9;
    private final static int BOOST_SPEED = 15;

    private final static Command C_ACCELERATE = new AccelerateCommand();
    private final static Command C_DECELERATE = new DecelerateCommand();
    private final static Command C_LIZARD = new LizardCommand();
    private final static Command C_OIL = new OilCommand();
    private final static Command C_BOOST = new BoostCommand();
    private final static Command C_EMP = new EmpCommand();
    private final static Command C_FIX = new FixCommand();
    private final static Command C_DO_NOTHING = new DoNothingCommand();

    private final static Command C_TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command C_TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;
    }

    public Command run() {
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block);

        ArrayList<Object> comp;
        ArrayList<Object> res;
        String best = "DO_NOTHING";

        if (myCar.state == State.HIT_WALL || myCar.state == State.HIT_MUD ||
            myCar.state == State.HIT_CYBER_TRUCK || myCar.state == State.HIT_OIL) {
            comp = whatIfTurnLeft();
            if (Boolean.TRUE.equals(comp.get(0))) {
                best = "TURN_LEFT";
            }
            res = whatIfTurnRight();
            if (Boolean.TRUE.equals(res.get(0))) {
                comp = stateCompare(comp, res);
                if (comp.equals(res)) {
                    best = "TURN_RIGHT";
                }
            }

            if (best.equals("TURN_LEFT")) {
                return C_TURN_LEFT;
            }
            else if (best.equals("TURN_RIGHT")) {
                return C_TURN_RIGHT;
            }
        }

        comp = whatIfDoNothing(blocks);
        best = "DO_NOTHING";
        res = whatIfAccelerate(blocks);
        if (Boolean.TRUE.equals(res.get(0))) {
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "ACCELERATE";
            }
        }
        res = whatIfDecelerate(blocks);
        if (Boolean.TRUE.equals(res.get(0))) {
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "DECELERATE";
            }
        }
        res = whatIfTurnLeft();
        if (Boolean.TRUE.equals(res.get(0))) {
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "TURN_LEFT";
            }
        }
        res = whatIfTurnRight();
        if (Boolean.TRUE.equals(res.get(0))) {
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "TURN_RIGHT";
            }
        }
        res = whatIfUseBoost(blocks);
        if (Boolean.TRUE.equals(res.get(0))) {
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "USE_BOOST";
            }
        }
        res = whatIfUseOil(blocks);
        if (Boolean.TRUE.equals(res.get(0))) {
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "USE_OIL";
            }
        }
        res = whatIfUseLizard(blocks);
        if (Boolean.TRUE.equals(res.get(0))) {
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "USE_LIZARD";
            }
        }
        res = whatIfUseEmp(blocks);
        if (Boolean.TRUE.equals(res.get(0))) {
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "USE_EMP";
            }
        }
        res = whatIfDecelerate(blocks);
        if (Boolean.TRUE.equals(res.get(0))) {
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "DECELERATE";
            }
        }
        res = whatIfFix();
        if (Boolean.TRUE.equals(res.get(0))) {
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "FIX";
            }
        }
        res = whatIfUseTweet(blocks);
        if (Boolean.TRUE.equals(res.get(0))) {
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "USE_TWEET";
            }
        }

        if (best.equals("DO_NOTHING")) {
            return C_DO_NOTHING;
        }
        else if (best.equals("ACCELERATE")) {
            return C_ACCELERATE;
        }
        else if (best.equals("DECELERATE")) {
            return C_DECELERATE;
        }
        else if (best.equals("TURN_LEFT")) {
            return C_TURN_LEFT;
        }
        else if (best.equals("TURN_RIGHT")) {
            return C_TURN_RIGHT;
        }
        else if (best.equals("USE_BOOST")) {
            return C_BOOST;
        }
        else if (best.equals("USE_OIL")) {
            return C_OIL;
        }
        else if (best.equals("USE_LIZARD")) {
            return C_LIZARD;
        }
        else if (best.equals("USE_TWEET")) {
            int xy = searchTweetPos();
            if (xy != -1){
                return new TweetCommand(opponent.position.lane, xy);
            }
        }
        else if (best.equals("USE_EMP")) {
            return C_EMP;
        }
        else if (best.equals("FIX")) {
            return C_FIX;
        }

        return C_DO_NOTHING;
    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed + 1; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                blocks.add(laneList[i].terrain);
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

    private Integer getMaxDamageSpeed(int damage) {
        if (damage == 5) {
            return 0;
        }
        else if (damage == 4) {
            return 3;
        }
        else if (damage == 3) {
            return 6;
        }
        else if (damage == 2) {
            return 8;
        }
        else {
            return 9;
        }
    }

    private Integer getPrevSpeed(int spd) {
        if (spd == SPEED_STATE_1 || spd == SPEED_STATE_2 || spd == INITIAL_SPEED) {
            return SPEED_STATE_1;
        }
        else if (spd == SPEED_STATE_3) {
            return SPEED_STATE_2;
        }
        else if (spd == MAXIMUM_SPEED) {
            return SPEED_STATE_3;
        }
        else if (spd == BOOST_SPEED) {
            return MAXIMUM_SPEED;
        }
        else {
            return MAXIMUM_SPEED;
        }
    }

    private Integer getDecSpeed(int spd) {
        if (spd == SPEED_STATE_1) {
            return MINIMUM_SPEED;
        }
        else if (spd == INITIAL_SPEED) {
            return SPEED_STATE_1;
        }
        else if (spd == SPEED_STATE_2) {
            return SPEED_STATE_1;
        }
        else if (spd == SPEED_STATE_3) {
            return SPEED_STATE_2;
        }
        else if (spd == MAXIMUM_SPEED) {
            return SPEED_STATE_3;
        }
        else if (spd == BOOST_SPEED) {
            return MAXIMUM_SPEED;
        }
        else {
            return MINIMUM_SPEED;
        }
    }

    private Integer getNextSpeed(int spd) {
        if (spd == MINIMUM_SPEED) {
            return SPEED_STATE_1;
        }
        else if (spd == SPEED_STATE_1 || spd == INITIAL_SPEED) {
            return SPEED_STATE_2;
        }
        else if (spd == SPEED_STATE_2) {
            return SPEED_STATE_3;
        }
        else if (spd == SPEED_STATE_3) {
            return MAXIMUM_SPEED;
        }
        else {
            return MAXIMUM_SPEED;
        }
    }

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<Object> stateCompare(ArrayList<Object> one, ArrayList<Object> two) {
        Integer val1 = (Integer) one.get(2);
        Integer val2 = (Integer) two.get(2);
        if (Integer.compare(val1, val2) == 1) {
            return one;
        }
        else if (Integer.compare(val1, val2) == -1) {
            return two;
        }
        else {
            val1 = (Integer) one.get(1);
            val2 = (Integer) two.get(1);
            if (Integer.compare(val1, val2) == 1) {
                return one;
            }
            else if (Integer.compare(val1, val2) == -1) {
                return two;
            }
            else {
                val1 = (Integer) one.get(4);
                val2 = (Integer) two.get(4);
                if (Integer.compare(val1, val2) == -1) {
                    return one;
                }
                else if (Integer.compare(val1, val2) == 1) {
                    return two;
                }
                else {
                    val1 = (Integer) one.get(3);
                    val2 = (Integer) two.get(3);
                    if (Integer.compare(val1, val2) == -1) {
                        return one;
                    }
                    else if (Integer.compare(val1, val2) == 1) {
                        return two;
                    }
                    else {
                        val1 = (Integer) one.get(5);
                        val2 = (Integer) two.get(5);
                        if (Integer.compare(val1, val2) == -1) {
                            return one;
                        }
                        else if (Integer.compare(val1, val2) == 1) {
                            return two;
                        }
                    }
                }
            }
        }
        return one;
    }

    private Integer searchTweetPos() {
        int xy = -1;
        int opSpeed = opponent.speed;
        int opBlock = opponent.position.block + opSpeed;
        List<Object> tempBlock = getBlocksInFront(opponent.position.lane, opponent.position.block);
        for (int i = 0; i <= opSpeed; i++) {
            if (tempBlock.get(i+1) == Terrain.FINISH) {
                break;
            }
            if (tempBlock.get(i) == Terrain.EMPTY) {
                xy = opBlock + i;
                break;
            }
        }
        return xy;
    }

    private ArrayList<Object> whatIfDoNothing(List<Object> blocks) {
        boolean isValid = true;
        int locationForward = myCar.position.block + myCar.speed;
        int speedAfter = myCar.speed;
        int damageAfter = myCar.damage;
        int pu = 0;
        int scoreAddition = 0;
        ArrayList<Object> ret = new ArrayList <>();
        int batas = Math.min(Math.min(myCar.speed, Bot.maxSpeed), blocks.size());

        for (int i = 0; i <= batas; i++) {
            if (blocks.get(i+1) == Terrain.FINISH && i < batas) {
                break;
            }
            if (blocks.get(i) == Terrain.MUD) {
                scoreAddition -= 3;
                speedAfter = getPrevSpeed(speedAfter);
                damageAfter += 1;
            }
            else if (blocks.get(i) == Terrain.OIL_SPILL) {
                scoreAddition -= 4;
                speedAfter = getPrevSpeed(speedAfter);
                damageAfter += 1;
            }
            else if (blocks.get(i) == Terrain.WALL) {
                damageAfter += 2;
                speedAfter = 3;
            }
            else if (blocks.get(i) == Terrain.OIL_POWER || blocks.get(i) == Terrain.BOOST ||
                    blocks.get(i) == Terrain.LIZARD || blocks.get(i) == Terrain.TWEET ||
                    blocks.get(i) == Terrain.EMP) {
                scoreAddition += 4;
                pu += 1;
            }
        }

        ret.add(isValid);
        ret.add(locationForward);
        ret.add(speedAfter);
        ret.add(damageAfter);
        ret.add(pu);
        ret.add(scoreAddition);

        return ret;
    }

    private ArrayList<Object> whatIfAccelerate(List<Object> blocks) {
        boolean isValid = true;
        int locationForward = myCar.position.block + getNextSpeed(myCar.speed);
        int speedAfter = getNextSpeed(myCar.speed);
        int damageAfter = myCar.damage;
        int pu = 0;
        int scoreAddition = 0;
        ArrayList<Object> ret = new ArrayList <>();
        int batas = Math.min(Math.min(speedAfter, Bot.maxSpeed), blocks.size());

        if (myCar.speed >= getMaxDamageSpeed(myCar.damage)) {
            isValid = false;
            speedAfter = myCar.speed;
            locationForward = myCar.position.block;
        }
        else {
            for (int i = 0; i <= batas; i++) {
                if (blocks.get(i+1) == Terrain.FINISH && i < batas) {
                    break;
                }
                if (blocks.get(i) == Terrain.MUD) {
                    scoreAddition -= 3;
                    speedAfter = getPrevSpeed(speedAfter);
                    damageAfter += 1;
                }
                else if (blocks.get(i) == Terrain.OIL_SPILL) {
                    scoreAddition -= 4;
                    speedAfter = getPrevSpeed(speedAfter);
                    damageAfter += 1;
                }
                else if (blocks.get(i) == Terrain.WALL) {
                    damageAfter += 2;
                    speedAfter = 3;
                }
                else if (blocks.get(i) == Terrain.OIL_POWER || blocks.get(i) == Terrain.BOOST ||
                        blocks.get(i) == Terrain.LIZARD || blocks.get(i) == Terrain.TWEET ||
                        blocks.get(i) == Terrain.EMP) {
                    scoreAddition += 4;
                    pu += 1;
                }
            }
        }

        ret.add(isValid);
        ret.add(locationForward);
        ret.add(speedAfter);
        ret.add(damageAfter);
        ret.add(pu);
        ret.add(scoreAddition);

        return ret;
    }

    private ArrayList<Object> whatIfDecelerate(List<Object> blocks) {
        boolean isValid = true;
        int locationForward = myCar.position.block + getDecSpeed(myCar.speed);
        int speedAfter = getDecSpeed(myCar.speed);
        int damageAfter = myCar.damage;
        int pu = 0;
        int scoreAddition = 0;
        ArrayList<Object> ret = new ArrayList <>();
        int batas = Math.min(Math.min(speedAfter, Bot.maxSpeed), blocks.size());

        if (myCar.speed <= MINIMUM_SPEED) {
            isValid = false;
            speedAfter = myCar.speed;
            locationForward = myCar.position.block;
        }
        else {
            for (int i = 0; i <= batas; i++) {
                if (blocks.get(i+1) == Terrain.FINISH && i < batas) {
                    break;
                }
                if (blocks.get(i) == Terrain.MUD) {
                    scoreAddition -= 3;
                    speedAfter = getPrevSpeed(speedAfter);
                    damageAfter += 1;
                }
                else if (blocks.get(i) == Terrain.OIL_SPILL) {
                    scoreAddition -= 4;
                    speedAfter = getPrevSpeed(speedAfter);
                    damageAfter += 1;
                }
                else if (blocks.get(i) == Terrain.WALL) {
                    damageAfter += 2;
                    speedAfter = 3;
                }
                else if (blocks.get(i) == Terrain.OIL_POWER || blocks.get(i) == Terrain.BOOST ||
                        blocks.get(i) == Terrain.LIZARD || blocks.get(i) == Terrain.TWEET ||
                        blocks.get(i) == Terrain.EMP) {
                    scoreAddition += 4;
                    pu += 1;
                }
            }
        }

        ret.add(isValid);
        ret.add(locationForward);
        ret.add(speedAfter);
        ret.add(damageAfter);
        ret.add(pu);
        ret.add(scoreAddition);

        return ret;
    }

    private ArrayList<Object> whatIfTurnLeft() {
        boolean isValid = true;
        int locationForward = myCar.position.block + myCar.speed - 1;
        int speedAfter = myCar.speed;
        int damageAfter = myCar.damage;
        int pu = 0;
        int scoreAddition = 0;
        ArrayList<Object> ret = new ArrayList <>();

        if (myCar.position.lane == 1) {
            isValid = false;
            locationForward = myCar.position.block;
        }
        else {
            List<Object> blocks = getBlocksInFront(myCar.position.lane-1, myCar.position.block);
            blocks.remove(blocks.size()-1);
            int batas = Math.min(Math.min(myCar.speed, Bot.maxSpeed), blocks.size()-1);
            for (int i = 0; i < batas; i++) {
                if (blocks.get(i+1) == Terrain.FINISH && i < batas - 1) {
                    break;
                }
                if (blocks.get(i) == Terrain.MUD) {
                    scoreAddition -= 3;
                    speedAfter = getPrevSpeed(speedAfter);
                    damageAfter += 1;
                }
                else if (blocks.get(i) == Terrain.OIL_SPILL) {
                    scoreAddition -= 4;
                    speedAfter = getPrevSpeed(speedAfter);
                    damageAfter += 1;
                }
                else if (blocks.get(i) == Terrain.WALL) {
                    damageAfter += 2;
                    speedAfter = 3;
                }
                else if (blocks.get(i) == Terrain.OIL_POWER || blocks.get(i) == Terrain.BOOST ||
                        blocks.get(i) == Terrain.LIZARD || blocks.get(i) == Terrain.TWEET ||
                        blocks.get(i) == Terrain.EMP) {
                    scoreAddition += 4;
                    pu += 1;
                }
            }
        }

        ret.add(isValid);
        ret.add(locationForward);
        ret.add(speedAfter);
        ret.add(damageAfter);
        ret.add(pu);
        ret.add(scoreAddition);

        return ret;
    }

    private ArrayList<Object> whatIfTurnRight() {
        boolean isValid = true;
        int locationForward = myCar.position.block + myCar.speed - 1;
        int speedAfter = myCar.speed;
        int damageAfter = myCar.damage;
        int pu = 0;
        int scoreAddition = 0;
        ArrayList<Object> ret = new ArrayList <>();

        if (myCar.position.lane == 4) {
            isValid = false;
            locationForward = myCar.position.block;
        }
        else {
            List<Object> blocks = getBlocksInFront(myCar.position.lane+1, myCar.position.block);
            blocks.remove(blocks.size()-1);
            int batas = Math.min(Math.min(myCar.speed, Bot.maxSpeed), blocks.size()-1);
            for (int i = 0; i < batas; i++) {
                if (blocks.get(i+1) == Terrain.FINISH && i < batas -1) {
                    break;
                }
                if (blocks.get(i) == Terrain.MUD) {
                    scoreAddition -= 3;
                    speedAfter = getPrevSpeed(speedAfter);
                    damageAfter += 1;
                }
                else if (blocks.get(i) == Terrain.OIL_SPILL) {
                    scoreAddition -= 4;
                    speedAfter = getPrevSpeed(speedAfter);
                    damageAfter += 1;
                }
                else if (blocks.get(i) == Terrain.WALL) {
                    damageAfter += 2;
                    speedAfter = 3;
                }
                else if (blocks.get(i) == Terrain.OIL_POWER || blocks.get(i) == Terrain.BOOST ||
                        blocks.get(i) == Terrain.LIZARD || blocks.get(i) == Terrain.TWEET ||
                        blocks.get(i) == Terrain.EMP) {
                    scoreAddition += 4;
                    pu += 1;
                }
            }
        }

        ret.add(isValid);
        ret.add(locationForward);
        ret.add(speedAfter);
        ret.add(damageAfter);
        ret.add(pu);
        ret.add(scoreAddition);

        return ret;
    }

    private ArrayList<Object> whatIfUseBoost(List<Object> blocks) {
        boolean isValid = true;
        int locationForward = myCar.position.block + BOOST_SPEED;
        int speedAfter = BOOST_SPEED;
        int damageAfter = myCar.damage;
        int pu = 0;
        int scoreAddition = 4;
        ArrayList<Object> ret = new ArrayList <>();
        int batas = Math.min(Math.min(speedAfter, Bot.maxSpeed), blocks.size());

        if (!hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            isValid = false;
            locationForward = myCar.position.block + myCar.speed;
            speedAfter = myCar.speed;
            scoreAddition = 0;
        }
        else {
            for (int i = 0; i <= batas; i++) {
                if (blocks.get(i+1) == Terrain.FINISH && i < batas) {
                    break;
                }
                if (blocks.get(i) == Terrain.MUD) {
                    scoreAddition -= 3;
                    speedAfter = getPrevSpeed(speedAfter);
                    damageAfter += 1;
                }
                else if (blocks.get(i) == Terrain.OIL_SPILL) {
                    scoreAddition -= 4;
                    speedAfter = getPrevSpeed(speedAfter);
                    damageAfter += 1;
                }
                else if (blocks.get(i) == Terrain.WALL) {
                    damageAfter += 2;
                    speedAfter = 3;
                }
                else if (blocks.get(i) == Terrain.OIL_POWER || blocks.get(i) == Terrain.BOOST ||
                        blocks.get(i) == Terrain.LIZARD || blocks.get(i) == Terrain.TWEET ||
                        blocks.get(i) == Terrain.EMP) {
                    scoreAddition += 4;
                    pu += 1;
                }
            }
        }

        ret.add(isValid);
        ret.add(locationForward);
        ret.add(speedAfter);
        ret.add(damageAfter);
        ret.add(pu);
        ret.add(scoreAddition);

        return ret;
    }

    private ArrayList<Object> whatIfUseOil(List<Object> blocks) {
        boolean isValid = true;
        int locationForward = myCar.position.block + myCar.speed;
        int speedAfter = myCar.speed;
        int damageAfter = myCar.damage;
        int pu = 0;
        int scoreAddition = 4;
        ArrayList<Object> ret = new ArrayList <>();
        int batas = Math.min(Math.min(myCar.speed, Bot.maxSpeed), blocks.size());

        if (!hasPowerUp(PowerUps.OIL, myCar.powerups)) {
            isValid = false;
            scoreAddition = 0;
        }
        else {
            for (int i = 0; i <= batas; i++) {
                if (blocks.get(i+1) == Terrain.FINISH && i < batas) {
                    break;
                }
                if (blocks.get(i) == Terrain.MUD) {
                    scoreAddition -= 3;
                    speedAfter = getPrevSpeed(speedAfter);
                    damageAfter += 1;
                }
                else if (blocks.get(i) == Terrain.OIL_SPILL) {
                    scoreAddition -= 4;
                    speedAfter = getPrevSpeed(speedAfter);
                    damageAfter += 1;
                }
                else if (blocks.get(i) == Terrain.WALL) {
                    damageAfter += 2;
                    speedAfter = 3;
                }
                else if (blocks.get(i) == Terrain.OIL_POWER || blocks.get(i) == Terrain.BOOST ||
                        blocks.get(i) == Terrain.LIZARD || blocks.get(i) == Terrain.TWEET ||
                        blocks.get(i) == Terrain.EMP) {
                    scoreAddition += 4;
                    pu += 1;
                }
            }
        }

        ret.add(isValid);
        ret.add(locationForward);
        ret.add(speedAfter);
        ret.add(damageAfter);
        ret.add(pu);
        ret.add(scoreAddition);

        return ret;
    }

    private ArrayList<Object> whatIfUseTweet(List<Object> blocks) {
        boolean isValid = true;
        int locationForward = myCar.position.block + myCar.speed;
        int speedAfter = myCar.speed;
        int damageAfter = myCar.damage;
        int pu = 0;
        int scoreAddition = 4;
        ArrayList<Object> ret = new ArrayList <>();
        int batas = Math.min(Math.min(myCar.speed, Bot.maxSpeed), blocks.size());

        if (!hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
            isValid = false;
            scoreAddition = 0;
        }
        else {
            for (int i = 0; i <= batas; i++) {
                if (blocks.get(i+1) == Terrain.FINISH && i < batas) {
                    break;
                }
                if (blocks.get(i) == Terrain.MUD) {
                    scoreAddition -= 3;
                    speedAfter = getPrevSpeed(speedAfter);
                    damageAfter += 1;
                }
                else if (blocks.get(i) == Terrain.OIL_SPILL) {
                    scoreAddition -= 4;
                    speedAfter = getPrevSpeed(speedAfter);
                    damageAfter += 1;
                }
                else if (blocks.get(i) == Terrain.WALL) {
                    damageAfter += 2;
                    speedAfter = 3;
                }
                else if (blocks.get(i) == Terrain.OIL_POWER || blocks.get(i) == Terrain.BOOST ||
                        blocks.get(i) == Terrain.LIZARD || blocks.get(i) == Terrain.TWEET ||
                        blocks.get(i) == Terrain.EMP) {
                    scoreAddition += 4;
                    pu += 1;
                }
            }
        }

        ret.add(isValid);
        ret.add(locationForward);
        ret.add(speedAfter);
        ret.add(damageAfter);
        ret.add(pu);
        ret.add(scoreAddition);

        return ret;
    }

    private ArrayList<Object> whatIfUseLizard(List<Object> blocks) {
        boolean isValid = true;
        int locationForward = myCar.position.block + myCar.speed;
        int speedAfter = myCar.speed;
        int damageAfter = myCar.damage;
        int pu = 0;
        int scoreAddition = 4;
        ArrayList<Object> ret = new ArrayList <>();

        if (!hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
            isValid = false;
            scoreAddition = 0;
        }
        else {
            int finishterrain = blocks.indexOf(Terrain.FINISH);
            if ((myCar.speed < finishterrain || finishterrain == -1) && myCar.speed <= Bot.maxSpeed) {
                //finish masih di depan atau nggak ada finish, jadi pake lizard biasa
                //hitung damage, pu, speed yg di gain di locationforward
                if (blocks.get(myCar.speed) == Terrain.MUD) {
                    scoreAddition -= 3;
                    damageAfter += 1;
                }
                else if (blocks.get(myCar.speed) == Terrain.OIL_SPILL) {
                    scoreAddition -= 4;
                    damageAfter += 1;
                }
                else if (blocks.get(myCar.speed) == Terrain.WALL) {
                    damageAfter += 2;
                    speedAfter = 3;
                }
                else if (blocks.get(myCar.speed) == Terrain.OIL_POWER || blocks.get(myCar.speed) == Terrain.BOOST ||
                        blocks.get(myCar.speed) == Terrain.LIZARD || blocks.get(myCar.speed) == Terrain.TWEET ||
                        blocks.get(myCar.speed) == Terrain.EMP) {
                    scoreAddition += 4;
                    pu += 1;
                }
            } // else finish dilompati, nggak ngapa-ngapain langsung finish
        }

        ret.add(isValid);
        ret.add(locationForward);
        ret.add(speedAfter);
        ret.add(damageAfter);
        ret.add(pu);
        ret.add(scoreAddition);

        return ret;
    }

    private ArrayList<Object> whatIfUseEmp(List<Object> blocks) {
        boolean isValid = true;
        int locationForward = myCar.position.block + myCar.speed;
        int speedAfter = myCar.speed;
        int damageAfter = myCar.damage;
        int pu = 0;
        int scoreAddition = 4;
        ArrayList<Object> ret = new ArrayList <>();
        int batas = Math.min(Math.min(myCar.speed, Bot.maxSpeed), blocks.size());

        if (!hasPowerUp(PowerUps.EMP, myCar.powerups) || Math.abs(opponent.position.lane-myCar.position.lane) > 1) {
            isValid = false;
            scoreAddition = 0;
        }
        else {
            for (int i = 0; i <= batas; i++) {
                if (blocks.get(i+1) == Terrain.FINISH && i < batas) {
                    break;
                }
                if (blocks.get(i) == Terrain.MUD) {
                    scoreAddition -= 3;
                    speedAfter = getPrevSpeed(speedAfter);
                    damageAfter += 1;
                }
                else if (blocks.get(i) == Terrain.OIL_SPILL) {
                    scoreAddition -= 4;
                    speedAfter = getPrevSpeed(speedAfter);
                    damageAfter += 1;
                }
                else if (blocks.get(i) == Terrain.WALL) {
                    damageAfter += 2;
                    speedAfter = 3;
                }
                else if (blocks.get(i) == Terrain.OIL_POWER || blocks.get(i) == Terrain.BOOST ||
                        blocks.get(i) == Terrain.LIZARD || blocks.get(i) == Terrain.TWEET ||
                        blocks.get(i) == Terrain.EMP) {
                    scoreAddition += 4;
                    pu += 1;
                }
            }
        }

        ret.add(isValid);
        ret.add(locationForward);
        ret.add(speedAfter);
        ret.add(damageAfter);
        ret.add(pu);
        ret.add(scoreAddition);

        return ret;
    }

    private ArrayList<Object> whatIfFix() {
        boolean isValid = true;
        int locationForward = myCar.position.block;
        int speedAfter = getMaxDamageSpeed(myCar.damage-2);
        int damageAfter = myCar.damage - 2;
        int pu = 0;
        int scoreAddition = 0;
        ArrayList<Object> ret = new ArrayList <>();

        if (myCar.damage < 2) {
            isValid = false;
            damageAfter = myCar.damage;
            speedAfter = myCar.speed;
        }

        ret.add(isValid);
        ret.add(locationForward);
        ret.add(speedAfter);
        ret.add(damageAfter);
        ret.add(pu);
        ret.add(scoreAddition);

        return ret;
    }

}