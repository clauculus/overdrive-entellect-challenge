package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.security.SecureRandom;
import java.util.*;

import static java.lang.Math.max;

public class Bot {

    //private static final int maxSpeed = 9;
    //private List<Integer> directionList = new ArrayList<>();

    private final Random random;
    private final GameState gameState;
    private final Car opponent;
    private final Car myCar;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command DECELERATE = new DecelerateCommand();
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
        List<Object> myCarBlocks = getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed);
        List<Object> leftLaneBlocks = new ArrayList<>();
        List<Object> rightLaneBlocks = new ArrayList<>();
        if (myCar.position.lane-1 >= 1){
            leftLaneBlocks = getBlocksInFront(myCar.position.lane-1, myCar.position.block, myCar.speed);
            int idxLast = leftLaneBlocks.size() - 1;
            leftLaneBlocks.remove(idxLast);
        }
        if (myCar.position.lane+1 <= 4) {
            rightLaneBlocks = getBlocksInFront(myCar.position.lane + 1, myCar.position.block, myCar.speed);
            int idxLast = rightLaneBlocks.size() - 1;
            rightLaneBlocks.remove(idxLast);
        }

        // Cek hit wall atau nggak, langsung menghindar kalau ya
        List<Object> tempBlock = getBlocksInFront(myCar.position.lane, myCar.position.block, 3);
        if (tempBlock.get(2) == Terrain.WALL || tempBlock.get(1) == Terrain.WALL) {
            myCarBlocks.clear();
            ArrayList<Object> choosingLane = chooseLane(myCarBlocks, leftLaneBlocks, rightLaneBlocks);
            if (choosingLane.get(0) == "LEFT_LANE") {
                return TURN_LEFT;
            }
            else if (choosingLane.get(0) == "RIGHT_LANE") {
                return TURN_RIGHT;
            }
            System.out.println("HIT WALL HIT WALL HIT WALL HIT WALL HIT WALL HIT WALL HIT WALL HIT WALL");
            System.out.println(choosingLane.get(1));
        }

        // Fix car if damage >= 3
        if (myCar.damage >= 3) {
            System.out.println("FIXFIXFIXFIXFIXFIXFIXFIXFIXFIXFIXFIXFIXFIXFIXFIXFIXFIXFIXFIXFIXFIXFIXFIX");
            return FIX;
        }

        // Kalau bakal ketemu mud, atau oil_spill, hindari dengan belok kanan atau kiri (kalau choice itu lebih baik drpd stay at myLane)
        if (myCarBlocks.contains(Terrain.MUD) || myCarBlocks.contains(Terrain.OIL_SPILL) || myCarBlocks.contains(Terrain.WALL)) {
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups) && !(myCarBlocks.get(myCarBlocks.size()-1) == Terrain.WALL || myCarBlocks.get(myCarBlocks.size()-1) == Terrain.MUD || myCarBlocks.get(myCarBlocks.size()-1) == Terrain.OIL_SPILL || isOpponent(myCar.position.lane, myCar.position.block + myCar.speed))) {
                System.out.println("LIZARD LIZARD LIZARD LIZARD LIZARD LIZARD LIZARD LIZARD LIZARD LIZARD LIZARD LIZARD LIZARD LIZARD ");
                return LIZARD;
            }
//            else if (myCarBlocks.get(myCarBlocks.size()-1) == Terrain.WALL || myCarBlocks.get(myCarBlocks.size()-1) == Terrain.MUD || myCarBlocks.get(myCarBlocks.size()-1) == Terrain.OIL_SPILL || isOpponent(myCar.position.lane, myCar.position.block + myCar.speed)) {
//                myCarBlocks = getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed);
//                if (myCarBlocks.get(myCarBlocks.size()-1) == Terrain.WALL || myCarBlocks.get(myCarBlocks.size()-1) == Terrain.MUD || myCarBlocks.get(myCarBlocks.size()-1) == Terrain.OIL_SPILL || isOpponent(myCar.position.lane, myCar.position.block + myCar.speed)) {
//                    return DECELERATE;
//                }
//            }
            else {
                ArrayList<Object> choosingLane = chooseLane(myCarBlocks, leftLaneBlocks, rightLaneBlocks);
                System.out.println("HAAAAAI~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                System.out.println(choosingLane);
                if (choosingLane.get(0) == "MY_LANE") {
                    if (isAccelerateValid()) {
                        myCarBlocks = getBlocksInFront(myCar.position.lane, myCar.position.block, getNextSpeed());
                        if (myCarBlocks.get(myCarBlocks.size()-1) == Terrain.WALL || myCarBlocks.get(myCarBlocks.size()-1) == Terrain.MUD || myCarBlocks.get(myCarBlocks.size()-1) == Terrain.OIL_SPILL || isOpponent(myCar.position.lane, myCar.position.block + getNextSpeed())) {
                            System.out.println("DECELERATE DECELERATE DECELERATE DECELERATE DECELERATE DECELERATE DECELERATE DECELERATE ");
                            return DECELERATE;
                        }
                        System.out.println("ACCELERATE ACCELERATE ACCELERATE ACCELERATE ACCELERATE ACCELERATE ACCELERATE ACCELERATE ");
                        return ACCELERATE;
                    }
//                    else {
//                        System.out.println("NOTHING NOTHING NOTHING NOTHING NOTHING NOTHING NOTHING NOTHING NOTHING NOTHING NOTHING ");
//                        return DO_NOTHING;
//                    }
                }
                else if (choosingLane.get(0) == "LEFT_LANE") {
                    System.out.println("LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT ");
                    return TURN_LEFT;
                }
                else if (choosingLane.get(0) == "RIGHT_LANE") {
                    System.out.println("RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT ");
                    return TURN_RIGHT;
                }
                System.out.println("CHOOSING LANE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println(choosingLane.get(1));
            }

        }

        // Kalau ada powerups boost, langsung pakai
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            System.out.println("BOOST BOOST BOOST BOOST BOOST BOOST BOOST BOOST BOOST BOOST BOOST BOOST BOOST BOOST ");
            return BOOST;
        }

        if (hasPowerUp(PowerUps.TWEET, myCar.powerups) && checkOpPos()) {
            int opponentFinalBlock = opponent.position.block + opponent.speed + 1;
            System.out.println("13111111111111111111111111111111111111111111111111111111111111131");
            System.out.println(opponentFinalBlock);
            List<Object> tempOpBlock = getBlocksInFront(opponent.position.lane, opponentFinalBlock, opponent.speed);
            System.out.println(tempOpBlock);
            int cnt = 1;
            while (tempOpBlock.get(cnt) != Terrain.EMPTY && cnt < tempOpBlock.size()) {
                cnt+=1;
            }
            System.out.print("TWEET TWEET TWEET TWEET TWEET TWEET TWEET TWEET TWEET TWEET TWEET ");
            System.out.print(opponent.position.lane);
            System.out.print(" ");
            System.out.println(opponentFinalBlock + cnt);
            return new TweetCommand(opponent.position.lane, opponentFinalBlock + cnt);
//            List<Lane[]> map = gameState.lanes;
//            int opponentFinalBlock = opponent.position.block + opponent.speed;
//            for (int addition = 1; addition <= opponent.speed; addition++) {
//                if (map.get(opponent.position.lane)[opponentFinalBlock + addition].terrain == Terrain.EMPTY) {
//                    return new TweetCommand(opponent.position.lane, opponentFinalBlock + addition);
//                }
//            }
        }
        if (hasPowerUp(PowerUps.OIL, myCar.powerups) && (opponent.position.lane == myCar.position.lane) && opponent.position.block < myCar.position.block) {
            System.out.println("OIL OIL OIL OIL OIL OIL OIL OIL OIL OIL OIL OIL OIL OIL OIL OIL OIL OIL OIL OIL OIL ");
            return OIL;
        }
        if (hasPowerUp(PowerUps.EMP, myCar.powerups) && ((opponent.position.lane == myCar.position.lane) || (opponent.position.lane == myCar.position.lane-1) || (opponent.position.lane == myCar.position.lane+1))) {
            System.out.println("EMP EMP EMP EMP EMP EMP EMP EMP EMP EMP EMP EMP EMP EMP EMP EMP EMP EMP EMP EMP EMP ");
            return EMP;
        }

        if (isAccelerateValid()) {
            System.out.println("ACCELERATE ACCELERATE ACCELERATE ACCELERATE ACCELERATE ACCELERATE ACCELERATE ACCELERATE ");
            return ACCELERATE;
        }
        else {
            System.out.println("NOTHING NOTHING NOTHING NOTHING NOTHING NOTHING NOTHING NOTHING NOTHING NOTHING NOTHING ");
            return DO_NOTHING;
        }
    }

    private Boolean checkOpPos() {
        return (opponent.position.block+opponent.speed >= myCar.position.block+myCar.speed - 4 && opponent.position.block+opponent.speed <= myCar.position.block+myCar.speed + 15);
    }

    private Boolean isOpponent(int lane, int block) {
        return (lane == opponent.position.lane && block == opponent.position.block + opponent.speed);
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
    
    private Integer getNextSpeed() {
        if (myCar.speed == 0) {
            return 3;
        }
        else if (myCar.speed == 3) {
            return 5;
        }
        else if (myCar.speed == 5) {
            return 6;
        }
        else if (myCar.speed == 6) {
            return 8;
        }
        else {
            return 9;
        }
    }

    private Integer getPrevSpeedMud(int spd) {
        if (spd == 3 || spd == 6) {
            return 3;
        }
        else if (spd == 8) {
            return 6;
        }
        else if (spd == 9) {
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
    private List<Object> getBlocksInFront(int lane, int block, int spd) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + spd; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }

        return blocks;
    }

    private ArrayList<Object> chooseLane(List<Object> myLane, List<Object> leftLane, List<Object> rightLane) {
        ArrayList<Object> list = new ArrayList <>();

        if (leftLane.size() != 0 || rightLane.size() != 0) {
            int myLaneSpeed = -99;
            int leftLaneSpeed = -99;
            int rightLaneSpeed = -99;

            if (leftLane.size() != 0) {
                if (leftLane.contains(Terrain.MUD) || leftLane.contains(Terrain.OIL_SPILL)) {
                    if (leftLaneSpeed == -99) {
                        leftLaneSpeed = getPrevSpeedMud(myCar.speed);
                    }
                    else {
                        leftLaneSpeed = getPrevSpeedMud(leftLaneSpeed);
                    }
                }
                if (leftLane.contains(Terrain.BOOST) ) {
                    leftLaneSpeed = getMaxSpeed();
                }
            }
            if (rightLane.size() != 0) {
                if (rightLane.contains(Terrain.MUD) || rightLane.contains(Terrain.OIL_SPILL)) {
                    if (rightLaneSpeed == -99) {
                        rightLaneSpeed = getPrevSpeedMud(myCar.speed);
                    }
                    else {
                        rightLaneSpeed = getPrevSpeedMud(rightLaneSpeed);
                    }
                }
                if (rightLane.contains(Terrain.BOOST) ) {
                    rightLaneSpeed = getMaxSpeed();
                }
            }
            if (myLane.size() != 0) {
                if (myLane.contains(Terrain.MUD) || myLane.contains(Terrain.OIL_SPILL)) {
                    if (myLaneSpeed == -99) {
                        myLaneSpeed = getPrevSpeedMud(myCar.speed);
                    }
                    else {
                        myLaneSpeed = getPrevSpeedMud(myLaneSpeed);
                    }
                }
                if (myLane.contains(Terrain.BOOST) ) {
                    myLaneSpeed = getMaxSpeed();
                }
            }

            if (myLaneSpeed >= leftLaneSpeed && myLaneSpeed >= rightLaneSpeed) {
                list.add("MY_LANE");
                list.add(myLaneSpeed);
            }
            else if (rightLaneSpeed > myLaneSpeed && rightLaneSpeed > leftLaneSpeed) {
                list.add("RIGHT_LANE");
                list.add(rightLaneSpeed);
            }
            else if (leftLaneSpeed > myLaneSpeed && leftLaneSpeed > rightLaneSpeed) {
                list.add("LEFT_LANE");
                list.add(leftLaneSpeed);
            }
        }
        else {
            int myLaneSpeed = -99;
            if (myLane.contains(Terrain.MUD) || myLane.contains(Terrain.OIL_SPILL)) {
                if (myLaneSpeed == -99) {
                    myLaneSpeed = getPrevSpeedMud(myCar.speed);
                }
                else {
                    myLaneSpeed = getPrevSpeedMud(myLaneSpeed);
                }
            }
            if (myLane.contains(Terrain.BOOST) ) {
                myLaneSpeed = getMaxSpeed();
            }

            list.add("MY_LANE");
            list.add(myLaneSpeed);
        }
        return list;
    }
}
