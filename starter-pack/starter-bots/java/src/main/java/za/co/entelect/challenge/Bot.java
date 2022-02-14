package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.security.SecureRandom;
import java.util.*;

import static java.lang.Math.max;

public class Bot {

    private static final int maxSpeed = 9;
    //private List<Integer> directionList = new ArrayList<>();

    private final Random random;
    private final GameState gameState;
    private final Car opponent;
    private final Car myCar;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();
    private final static Command DO_NOTHING = new DoNothingCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot(Random random, GameState gameState) {
        this.random = new SecureRandom();
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;

//        directionList.add(-1);
//        directionList.add(1);
    }

    public Command run() {
        List<Object> myCarBlocks = getBlocksInFront(myCar.position.lane, myCar.position.block);
        List<Object> leftLaneBlocks = new ArrayList<>();
        List<Object> rightLaneBlocks = new ArrayList<>();
        if (myCar.position.lane-1 >= 1){
            leftLaneBlocks = getBlocksInFront(myCar.position.lane-1, myCar.position.block);
            int idxLast = leftLaneBlocks.size() - 1;
            leftLaneBlocks.remove(idxLast);
        }
        if (myCar.position.lane+1 <= 4) {
            rightLaneBlocks = getBlocksInFront(myCar.position.lane + 1, myCar.position.block);
            int idxLast = rightLaneBlocks.size() - 1;
            rightLaneBlocks.remove(idxLast);
        }

        // Fix car if damage >= 3
        if (myCar.damage >= 3) {
            return FIX;
        }

        if (myCarBlocks.contains(Terrain.TWEET) || myCarBlocks.contains(Terrain.MUD) || myCarBlocks.contains(Terrain.OIL_SPILL) || (myCarBlocks.contains(Terrain.WALL) && !hasPowerUp(PowerUps.LIZARD, myCar.powerups))) {
            ArrayList<Object> choosingLane = chooseLane(myCarBlocks, leftLaneBlocks, rightLaneBlocks);
            if (choosingLane.get(0) == "MY_LANE") {
                if (isAccelerateValid()) {
                    return ACCELERATE;
                } else {
                    return DO_NOTHING;
                }
            }
            else if (choosingLane.get(0) == "LEFT_LANE") {
                return TURN_LEFT;
            }
            else if (choosingLane.get(0) == "RIGHT_LANE") {
                return TURN_RIGHT;
            }
            System.out.println(choosingLane.get(1));
        }
        if (myCarBlocks.contains(Terrain.WALL) && hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
            ArrayList<Object> choosingLane = chooseLane(myCarBlocks, leftLaneBlocks, rightLaneBlocks);
            if (choosingLane.get(0) == "MY_LANE") {
                return LIZARD;
            }
            else if (choosingLane.get(0) == "LEFT_LANE") {
                return TURN_LEFT;
            }
            else if (choosingLane.get(0) == "RIGHT_LANE") {
                return TURN_RIGHT;
            }
        }
        
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            return BOOST;
        }

        if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
            List<Lane[]> map = gameState.lanes;
            int opponentFinalBlock = opponent.position.block + opponent.speed;
            for (int addition = 1; addition <= opponent.speed; addition++) {
                if (map.get(opponent.position.lane)[opponentFinalBlock + addition].terrain == Terrain.EMPTY) {
                    return new TweetCommand(opponent.position.lane, opponentFinalBlock + addition);
                }
            }
        }
        if (hasPowerUp(PowerUps.OIL, myCar.powerups) && (opponent.position.lane == myCar.position.lane) && opponent.position.block < myCar.position.block) {
            return OIL;
        }
        if (hasPowerUp(PowerUps.EMP, myCar.powerups) && ((opponent.position.lane == myCar.position.lane) || (opponent.position.lane == myCar.position.lane-1) || (opponent.position.lane == myCar.position.lane+1))) {
            return EMP;
        }

        return DO_NOTHING;
    }

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    private Integer getMaxSpeed() {
        if (myCar.damage == 5) {
            return 0;
        }
        else if (myCar.damage == 4) {
            return 3;
        }
        else if (myCar.damage == 3) {
            return 6;
        }
        else if (myCar.damage == 2) {
            return 8;
        }
        else {
            return 9;
        }
    }

    private Boolean isAccelerateValid() {
        return ((myCar.speed < getMaxSpeed()) && (myCar.speed < 15));
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
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        //  System.out.println("HALO");
        //  System.out.println(blocks);
        return blocks;
    }

    private ArrayList<Object> chooseLane(List<Object> myLane, List<Object> leftLane, List<Object> rightLane) {
        ArrayList<Object> list = new ArrayList <>();

        if (leftLane.size() != 0 || rightLane.size() != 0) {
            int myLaneScore = 0;
            int leftLaneScore = 0;
            int rightLaneScore = 0;

            if (leftLane.size() != 0) {
                if (leftLane.contains(Terrain.MUD)) {
                    leftLaneScore -= 3;
                }
                if (leftLane.contains(Terrain.OIL_SPILL)) {
                    leftLaneScore -= 4;
                }
                if (leftLane.contains(Terrain.BOOST) || leftLane.contains(Terrain.OIL_POWER) || leftLane.contains(Terrain.TWEET) || leftLane.contains(Terrain.EMP) || leftLane.contains(Terrain.LIZARD)) {
                    leftLaneScore += 4;
                }
            }
            if (rightLane.size() != 0) {
                if (rightLane.contains(Terrain.MUD)) {
                    rightLaneScore -= 3;
                }
                if (rightLane.contains(Terrain.OIL_SPILL)) {
                    rightLaneScore -= 4;
                }
                if (rightLane.contains(Terrain.BOOST) || rightLane.contains(Terrain.OIL_POWER) || rightLane.contains(Terrain.TWEET) || rightLane.contains(Terrain.EMP) || rightLane.contains(Terrain.LIZARD)) {
                    rightLaneScore += 4;
                }
            }
            if (myLane.size() != 0) {
                if (myLane.contains(Terrain.MUD)) {
                    myLaneScore -= 3;
                }
                if (myLane.contains(Terrain.OIL_SPILL)) {
                    myLaneScore -= 4;
                }
                if (myLane.contains(Terrain.BOOST) || myLane.contains(Terrain.OIL_POWER) || myLane.contains(Terrain.TWEET) || myLane.contains(Terrain.EMP) || myLane.contains(Terrain.LIZARD)) {
                    myLaneScore += 4;
                }
            }

            if (myLaneScore >= leftLaneScore && myLaneScore >= rightLaneScore) {
                list.add("MY_LANE");
                list.add(myLaneScore);
            }
            else if (rightLaneScore > myLaneScore && rightLaneScore > leftLaneScore) {
                list.add("RIGHT_LANE");
                list.add(rightLaneScore);
            }
            else if (leftLaneScore > myLaneScore && leftLaneScore > rightLaneScore) {
                list.add("LEFT_LANE");
                list.add(leftLaneScore);
            }
        }
        else {
            int myLaneScore = 0;
            if (leftLane.contains(Terrain.MUD)) {
                myLaneScore -= 3;
            }
            if (leftLane.contains(Terrain.OIL_SPILL)) {
                myLaneScore -= 4;
            }
            if (leftLane.contains(Terrain.BOOST) || leftLane.contains(Terrain.OIL_POWER) || leftLane.contains(Terrain.TWEET) || leftLane.contains(Terrain.EMP) || leftLane.contains(Terrain.LIZARD)) {
                myLaneScore += 4;
            }
            list.add("MY_LANE");
            list.add(myLaneScore);
        }
        return list;
    }
}
