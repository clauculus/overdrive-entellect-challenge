package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Integer> directionList = new ArrayList<>();

    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;
    private final static Command FIX = new FixCommand();

    
    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;

        directionList.add(-1);
        directionList.add(1);
    }

    // Bagian Modif
    // Lane sb.y, blocks sb.x
    private boolean canUseOil(Car myCar) {
        return (myCar.PowerUps.OIL) //gimanasi cara akses ada powerups ato engga??
                && (opponent.position.lane == myCar.position.lane)
                && (opponent.position.block < myCar.position.block);
    }

    private boolean canUseEMP(Car myCar) {
        return (myCar.PowerUps.EMP)
                //perlu cek ga kalo mau pake EMP? atau langsung dipake aja yg penting mobil lawan didepan?
                //eh tapi bisa jadi tbtb lawan pakai accelerate terus dia jd didepan???
                //bikin kondisi kalau di lane 1 dan 4?
                && (opponent.position.lane = myCar.position.lane)
                && (opponent.position.lane = myCar.position.lane - 1) 
                && (opponent.position.lane = myCar.position.lane - 1)
                && (opponent.position.block > myCar.position.block);
    }

    private boolean canUseTweet(Car myCar) {
        return (myCar.powerups[].OIL); //gimana cara cek arraynya TT
    }

    public Command run() {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;

        if(canUseEMP(myCar)) {
            //Rugi kalau lawannya pindah lane yg diluar EMP
            return EmpCommand();
        }

        else if(canUseOil(myCar)) {
            //Rugi kalau lawannya pindah lane
            return OilCommand();
        }

        else if(canUseTweet(myCar)){
            //Tweet berlaku utk next round
            //Kemungkinan speed lawan : 15,9,8,6,5,3
            //Cek speed mobil lawan berapa biar bisa prediksi tarok cybertrucksnya dimana
            //Asumsi : dia ambilnya posisi mobil musuh sebelum pindah pada round itu(?)
            //Rugi kl misalnya musuh pakai command accelerate/pindah lane
            if(opponent.speed == 15) {
                return TweetCommand(opponent.position.lane, opponent.position.block + 17)
            }
            else if (opponent.speed == 9) {
                return TweetCommand(opponent.position.lane, opponent.position.block + 11)
            }
            else if (opponent.speed == 8) {
                return TweetCommand(opponent.position.lane, opponent.position.block + 10)
            }
            else if (opponent.speed == 6) {
                return TweetCommand(opponent.position.lane, opponent.position.block + 8)
            }
            else if (opponent.speed == 5) {
                return TweetCommand(opponent.position.lane, opponent.position.block + 7)
            }
            else { //opponent.speed = 3
                return TweetCommand(opponent.position.lane, opponent.position.block + 5)
            }

        }

        //Fungsi buat cari powerups(?)
        private Position nearestPowerUps() {
            myCar.powerups[]
        }

        //Dari Entelectnya
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block);
        if (myCar.damage >= 5) {
            return new FixCommand();
        }
        if (blocks.contains(Terrain.MUD)) {
            int i = random.nextInt(directionList.size());
            return new ChangeLaneCommand(directionList.get(i));
        }
        return new AccelerateCommand();

        
    }

    //Dari Entelectnya
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
        return blocks;
    }

}
