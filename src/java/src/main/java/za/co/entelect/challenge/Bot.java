package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
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
        // Mengambil sejumlah blok di depan bot
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block);

        ArrayList<Object> comp;
        ArrayList<Object> res;
        String best = "DO_NOTHING";

        // Menghindari tabrakan dengan lawan, bisa dengan berbelok ke kanan atau kiri
        if (checkOpponentCollision()) {
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
//                System.out.println("TURN_LEFT");
                return C_TURN_LEFT;
            } else if (best.equals("TURN_RIGHT")) {
//                System.out.println("TURN_RIGHT");
                return C_TURN_RIGHT;
            }
        }

        // Memperbaiki mobil dan mengurangi damage apabila damage menyebabkan kecepatan bot nol
        if (myCar.speed == 0 && myCar.damage == 5) {
            return C_FIX;
        }

        // Mengeksekusi command yang terbatas apabila sedang menggunakan boost
        // Memilih jalur yang paling memberikan kecepatan maksimum, lokasi terjauh, powerups terbanyak, damage paling sedikit, dan score terbanyak
        if (myCar.boosting) {
            List<Object> blocksBoost = getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed);
            int myLaneObs = countObstacles(blocksBoost);
            int bestChoiceLane = 0;

            // Lane saat ini ditandai dengan nol, lane di sebelah kiri -1, lane di sebelah kanan 1
            if (myCar.position.lane > 1) {
                blocksBoost = getBlocksInFront(myCar.position.lane-1, myCar.position.block, myCar.speed);
                blocksBoost.remove(blocksBoost.size()-1);
                int temp = countObstacles(blocksBoost);
                if (temp < myLaneObs) {
                    bestChoiceLane = -1;
                    myLaneObs = temp;
                }
            }
            if (myCar.position.lane < 4) {
                blocksBoost = getBlocksInFront(myCar.position.lane+1, myCar.position.block, myCar.speed);
                blocksBoost.remove(blocksBoost.size()-1);
                int temp = countObstacles(blocksBoost);
                if (temp < myLaneObs) {
                    bestChoiceLane = 1;
                    myLaneObs = temp;
                }
            }

            if (bestChoiceLane == -1) {
                return C_TURN_LEFT;
            }
            else if (bestChoiceLane == 0) {
                return C_DO_NOTHING;
            }
            else {
                return C_TURN_RIGHT;
            }
        }

        // Algoritma Greedy memilih command terbaik untuk memenangkan permainan Overdrive
        // Fungsi objektif:
        // Memaksimumkan kecepatan, Memaksimumkan lokasi, Memaksimumkan perolehan powerups, Meminimumkan perolehan damage, dan Memaksimumkan perolehan score

        // Memilih command terbaik bila tidak ada kejadian yang perlu dikecualikan
        // Command terlebih dahulu dicek apakah valid atau tidak
        // Command default adalah DO_NOTHING
        comp = whatIfDoNothing(blocks);
        best = "DO_NOTHING";
//        System.out.println("DO_NOTHING");

        // Membandingkan dengan ACCELERATE
        res = whatIfAccelerate(blocks);
        if (Boolean.TRUE.equals(res.get(0))) {
//            System.out.println("ACCELERATE");
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "ACCELERATE";
            }
        }

        // Membandingkan dengan DECELERATE
        res = whatIfDecelerate(blocks);
        if (Boolean.TRUE.equals(res.get(0))) {
//            System.out.println("DECELERATE");
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "DECELERATE";
            }
        }

        // Membandingkan dengan TURN_LEFT
        res = whatIfTurnLeft();
        if (Boolean.TRUE.equals(res.get(0))) {
//            System.out.println("TURN_LEFT");
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "TURN_LEFT";
            }
        }

        // Membandingkan dengan TURN_RIGHT
        res = whatIfTurnRight();
        if (Boolean.TRUE.equals(res.get(0))) {
//            System.out.println("TURN_RIGHT");
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "TURN_RIGHT";
            }
        }

        // Membandingkan dengan USE_BOOST
        res = whatIfUseBoost(blocks);
        if (Boolean.TRUE.equals(res.get(0))) {
//            System.out.println("USE_BOOST");
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "USE_BOOST";
            }
        }

        // Membandingkan dengan USE_OIL
        res = whatIfUseOil(blocks);
        if (Boolean.TRUE.equals(res.get(0))) {
//            System.out.println("USE_OIL");
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "USE_OIL";
            }
        }

        // Membandingkan dengan USE_LIZARD
        res = whatIfUseLizard();
        if (Boolean.TRUE.equals(res.get(0))) {
//            System.out.println("USE_LIZARD");
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "USE_LIZARD";
            }
        }

        // Membandingkan dengan USE_EMP
        res = whatIfUseEmp(blocks);
        if (Boolean.TRUE.equals(res.get(0))) {
//            System.out.println("USE_EMP");
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "USE_EMP";
            }
        }

        // Membandingkan dengan FIX
        res = whatIfFix();
        if (Boolean.TRUE.equals(res.get(0))) {
//            System.out.println("FIX");
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "FIX";
            }
        }

        // Membandingkan dengan USE_TWEET
        res = whatIfUseTweet(blocks);
        if (Boolean.TRUE.equals(res.get(0))) {
//            System.out.println("USE_TWEET");
            comp = stateCompare(comp, res);
            if (comp.equals(res)) {
                best = "USE_TWEET";
            }
        }

        // Mengeksekusi command yang telah dipilih
        if (best.equals("DO_NOTHING")) {
//            System.out.println("DO_NOTHING");
            return C_DO_NOTHING;
        }
        else if (best.equals("ACCELERATE")) {
//            System.out.println("ACCELERATE");
            return C_ACCELERATE;
        }
        else if (best.equals("DECELERATE")) {
//            System.out.println("DECELERATE");
            return C_DECELERATE;
        }
        else if (best.equals("TURN_LEFT")) {
//            System.out.println("TURN_LEFT");
            return C_TURN_LEFT;
        }
        else if (best.equals("TURN_RIGHT")) {
//            System.out.println("TURN_RIGHT");
            return C_TURN_RIGHT;
        }
        else if (best.equals("USE_BOOST")) {
//            System.out.println("USE_BOOST");
            return C_BOOST;
        }
        else if (best.equals("USE_OIL")) {
//            System.out.println("USE_OIL");
            return C_OIL;
        }
        else if (best.equals("USE_LIZARD")) {
//            System.out.println("USE_LIZARD");
            return C_LIZARD;
        }
        else if (best.equals("USE_TWEET")) {
//            System.out.println("USE_TWEET");
            // Meletakkan TWEET pada posisi tepat di depan bot lawan
            return new TweetCommand(opponent.position.lane, opponent.position.block+opponent.speed+1);
        }
        else if (best.equals("USE_EMP")) {
//            System.out.println("USE_EMP");
            return C_EMP;
        }
        else if (best.equals("FIX")) {
//            System.out.println("FIX");
            return C_FIX;
        }

        return C_DO_NOTHING;
    }

    // Mengambil blocks di depan bot sebanyak kecepatan maksimum bot
    private List<Object> getBlocksInFront(int lane, int block) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed + 1; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                if (laneList[i].terrain == Terrain.FINISH) {
                    blocks.add(laneList[i].terrain);
                }
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

    // Mengambil blocks di depan bot sebanyak spd
    private List<Object> getBlocksInFront(int lane, int block, int spd) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + spd + 1; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                if (laneList[i].terrain == Terrain.FINISH) {
                    blocks.add(laneList[i].terrain);
                }
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

    // Menghitung jumlah rintangan yang terdapat pada suatu lane
    private int countObstacles(List<Object> blocksBoost) {
        int obsInMyLane = Collections.frequency(blocksBoost, Terrain.MUD)
                + Collections.frequency(blocksBoost, Terrain.OIL_SPILL)
                + Collections.frequency(blocksBoost, Terrain.WALL);
        if (isThereOpponent()) {
            obsInMyLane += 1;
        }
        return obsInMyLane;
    }

    // Mengecek apakah sedang bertabrakan dengan lawan
    private boolean checkOpponentCollision() {
        return (opponent.position.lane == myCar.position.lane && opponent.position.block == myCar.position.block+1);
    }

    // Mengembalikan kecepatan maksimum berdasar damage dan kecepatan saat ini
    private int getMaxDamageSpeed(int damage) {
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

    // Mengembalikan kecepatan yang selevel lebih rendah daripada spd
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

    // Mengembalikan kecepatan yang selevel lebih rendah daripada spd apabila mengeksekusi command Decelerate
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

    // Mengembalikan kecepatan yang selevel lebih tinggi daripada spd
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

    // Mengecek apakah bot memiliki powerup powerUpToCheck
    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    // Membandingkan hasil eksekusi dua command, mengambil command yang memenuhi fungsi objektif dengan lebih baik
    // Memaksimumkan kecepatan, Memaksimumkan lokasi, Memaksimumkan perolehan powerups, Meminimumkan perolehan damage, dan Memaksimumkan perolehan score
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
                if (Integer.compare(val1, val2) == 1) {
                    return one;
                }
                else if (Integer.compare(val1, val2) == -1) {
                    return two;
                }
                else {
                    val1 = (Integer) one.get(3);
                    val2 = (Integer) two.get(3);
                    if (Integer.compare(val1, val2) == 1) {
                        return two;
                    }
                    else if (Integer.compare(val1, val2) == -1) {
                        return one;
                    }
                    else {
                        val1 = (Integer) one.get(5);
                        val2 = (Integer) two.get(5);
                        if (Integer.compare(val1, val2) == 1) {
                            return one;
                        }
                        else if (Integer.compare(val1, val2) == -1) {
                            return two;
                        }
                        else {
                            return two;
                        }
                    }
                }
            }
        }
    }

    // Mengecek valid atau tidak command DO_NOTHING, kecepatan yang dihasilkan setelah mengeksekusi command ini,
    // lokasi yang dicapai setelah command ini selesai dicapai, damage yang didapat dengan mengeksekusi command ini,
    // jumlah powerup yang didapatkan dengan mengeksekusi command ini, dan tambahan score yang didapatkan
    private ArrayList<Object> whatIfDoNothing(List<Object> blocks) {
        boolean isValid = true;
        int locationForward = myCar.position.block + myCar.speed;
        int speedAfter = myCar.speed;
        int damageAfter = myCar.damage;
        int pu = 0;
        int scoreAddition = 0;
        ArrayList<Object> ret = new ArrayList <>();
        int batas = Math.min(Math.min(myCar.speed, Bot.maxSpeed), blocks.size());

        for (int i = 1; i <= batas; i++) {
//            System.out.println(i);
            if (blocks.get(i) == Terrain.FINISH) {
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

    // Mengecek valid atau tidak command ACCELERATE, kecepatan yang dihasilkan setelah mengeksekusi command ini,
    // lokasi yang dicapai setelah command ini selesai dicapai, damage yang didapat dengan mengeksekusi command ini,
    // jumlah powerup yang didapatkan dengan mengeksekusi command ini, dan tambahan score yang didapatkan
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
            for (int i = 1; i <= batas; i++) {
//                System.out.println(i);
                if (blocks.get(i) == Terrain.FINISH) {
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

    // Mengecek valid atau tidak command ACCELERATE, kecepatan yang dihasilkan setelah mengeksekusi command ini,
    // lokasi yang dicapai setelah command ini selesai dicapai, damage yang didapat dengan mengeksekusi command ini,
    // jumlah powerup yang didapatkan dengan mengeksekusi command ini, dan tambahan score yang didapatkan
    private ArrayList<Object> whatIfDecelerate(List<Object> blocks) {
        boolean isValid = true;
        int locationForward = myCar.position.block + getDecSpeed(myCar.speed);
        int speedAfter = getDecSpeed(myCar.speed);
        int damageAfter = myCar.damage;
        int pu = 0;
        int scoreAddition = 0;
        ArrayList<Object> ret = new ArrayList <>();
        int batas = Math.min(Math.min(speedAfter, Bot.maxSpeed), blocks.size());

        if (myCar.speed <= SPEED_STATE_1) {
            isValid = false;
            speedAfter = myCar.speed;
            locationForward = myCar.position.block;
        }
        else { //Melakukan perhitungan berdasarkan blocks yang terdapat pada lane
            for (int i = 1; i <= batas; i++) {
//                System.out.println(i);
                if (blocks.get(i) == Terrain.FINISH) {
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

    // Mengecek valid atau tidak command TURN_LEFT, kecepatan yang dihasilkan setelah mengeksekusi command ini,
    // lokasi yang dicapai setelah command ini selesai dicapai, damage yang didapat dengan mengeksekusi command ini,
    // jumlah powerup yang didapatkan dengan mengeksekusi command ini, dan tambahan score yang didapatkan
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
            for (int i = 1; i < batas; i++) {
//                System.out.println(i);
                if (blocks.get(i) == Terrain.FINISH) {
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

    // Mengecek valid atau tidak command TURN_RIGHT, kecepatan yang dihasilkan setelah mengeksekusi command ini,
    // lokasi yang dicapai setelah command ini selesai dicapai, damage yang didapat dengan mengeksekusi command ini,
    // jumlah powerup yang didapatkan dengan mengeksekusi command ini, dan tambahan score yang didapatkan
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
            for (int i = 1; i < batas; i++) {
//                System.out.println(i);
                if (blocks.get(i) == Terrain.FINISH) {
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

    // Mengecek valid atau tidak command USE_BOOST, kecepatan yang dihasilkan setelah mengeksekusi command ini,
    // lokasi yang dicapai setelah command ini selesai dicapai, damage yang didapat dengan mengeksekusi command ini,
    // jumlah powerup yang didapatkan dengan mengeksekusi command ini, dan tambahan score yang didapatkan
    private ArrayList<Object> whatIfUseBoost(List<Object> blocks) {
        boolean isValid = true;
        int locationForward = myCar.position.block + BOOST_SPEED;
        int speedAfter = BOOST_SPEED;
        int damageAfter = myCar.damage;
        int pu = 0;
        int scoreAddition = 4;
        ArrayList<Object> ret = new ArrayList <>();
        int batas = Math.min(Math.min(speedAfter, Bot.maxSpeed), blocks.size());

        if (!hasPowerUp(PowerUps.BOOST, myCar.powerups) || myCar.boosting || blocks.contains(Terrain.MUD) || blocks.contains(Terrain.OIL_SPILL) || blocks.contains(Terrain.WALL)) {
            isValid = false;
            locationForward = myCar.position.block + myCar.speed;
            speedAfter = myCar.speed;
            scoreAddition = 0;
        }
        else {
            for (int i = 1; i <= batas; i++) {
//                System.out.println(i);
                if (blocks.get(i) == Terrain.FINISH) {
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

    // Mengecek valid atau tidak command USE_OIL, kecepatan yang dihasilkan setelah mengeksekusi command ini,
    // lokasi yang dicapai setelah command ini selesai dicapai, damage yang didapat dengan mengeksekusi command ini,
    // jumlah powerup yang didapatkan dengan mengeksekusi command ini, dan tambahan score yang didapatkan
    private ArrayList<Object> whatIfUseOil(List<Object> blocks) {
        boolean isValid = true;
        int locationForward = myCar.position.block + myCar.speed;
        int speedAfter = myCar.speed;
        int damageAfter = myCar.damage;
        int pu = 0;
        int scoreAddition = 4;
        ArrayList<Object> ret = new ArrayList <>();
        int batas = Math.min(Math.min(myCar.speed, Bot.maxSpeed), blocks.size());

        if (!hasPowerUp(PowerUps.OIL, myCar.powerups) || (opponent.position.lane != myCar.position.lane && opponent.position.block >= myCar.position.block)) {
            isValid = false;
            scoreAddition = 0;
        }
        else {
            for (int i = 1; i <= batas; i++) {
//                System.out.println(i);
                if (blocks.get(i) == Terrain.FINISH) {
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

    // Mengecek valid atau tidak command USE_TWEET, kecepatan yang dihasilkan setelah mengeksekusi command ini,
    // lokasi yang dicapai setelah command ini selesai dicapai, damage yang didapat dengan mengeksekusi command ini,
    // jumlah powerup yang didapatkan dengan mengeksekusi command ini, dan tambahan score yang didapatkan
    private ArrayList<Object> whatIfUseTweet(List<Object> blocks) {
        boolean isValid = true;
        int locationForward = myCar.position.block + myCar.speed;
        int speedAfter = myCar.speed;
        int damageAfter = myCar.damage;
        int pu = 0;
        int scoreAddition = 4;
        ArrayList<Object> ret = new ArrayList <>();
        int batas = Math.min(myCar.speed, blocks.size());

        if (!hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
            isValid = false;
            scoreAddition = 0;
        }
        else {
            for (int i = 1; i <= batas; i++) {
//                System.out.println(i);
                if (blocks.get(i) == Terrain.FINISH) {
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

    // Memeriksa apakah terdapat rintangan
    private boolean isThereObstacle (List<Object> blocks, int maxIdxCheck) {
        int i = 0;
        boolean foundObstacle = false;
        int opX = opponent.position.block + opponent.speed;
        int myX = myCar.position.block;
        int opY = opponent.position.lane;

        while (i <= maxIdxCheck && !foundObstacle) {
            if (blocks.get(i) == Terrain.WALL || blocks.get(i) == Terrain.MUD || blocks.get(i) == Terrain.OIL_SPILL ||
                (opY == myCar.position.lane && opX > myX && opX < myX+myCar.speed)) {
                foundObstacle = true;
            }
            i++;
        }
        return foundObstacle;
    }

    // Memeriksa apakah terdapat mobil lawan di depan bot myCar dalam jarak jangkauan myCar
    private boolean isThereOpponent () {
        boolean foundOpponent = false;
        int opX = opponent.position.block + opponent.speed;
        int myX = myCar.position.block;
        int opY = opponent.position.lane;

        if (opY == myCar.position.lane && opX > myX && opX < myX+myCar.speed) {
            foundOpponent = true;
        }

        return foundOpponent;
    }

    // Memeriksa apakah terdapat block FINISH 
    private boolean isThereFinish (List<Object> blocks) {
        return (blocks.contains(Terrain.FINISH));
    }


    // Mengecek valid atau tidak command USE_LIZARD, kecepatan yang dihasilkan setelah mengeksekusi command ini,
    // lokasi yang dicapai setelah command ini selesai dicapai, damage yang didapat dengan mengeksekusi command ini,
    // jumlah powerup yang didapatkan dengan mengeksekusi command ini, dan tambahan score yang didapatkan
    private ArrayList<Object> whatIfUseLizard() {
        boolean isValid = true;
        int locationForward = myCar.position.block + myCar.speed;
        int speedAfter = myCar.speed;
        int damageAfter = myCar.damage;
        int pu = 0;
        int scoreAddition = 4;
        ArrayList<Object> ret = new ArrayList <>();
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed);
        int batas = Math.min(myCar.speed, blocks.size()-1);

        if (!hasPowerUp(PowerUps.LIZARD, myCar.powerups) || !isThereObstacle(blocks, batas)) {
            isValid = false;
            scoreAddition = 0;
        }
        else {
//            System.out.println(batas);
            if (myCar.speed < blocks.size() && !isThereFinish(blocks)) {
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


    // Mengecek valid atau tidak command USE_EMP, kecepatan yang dihasilkan setelah mengeksekusi command ini,
    // lokasi yang dicapai setelah command ini selesai dicapai, damage yang didapat dengan mengeksekusi command ini,
    // jumlah powerup yang didapatkan dengan mengeksekusi command ini, dan tambahan score yang didapatkan
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
            for (int i = 1; i <= batas; i++) {
//                System.out.println(i);
                if (blocks.get(i) == Terrain.FINISH) {
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

    // Mengecek kecepatan yang dihasilkan setelah mengeksekusi command FIX,
    // lokasi yang dicapai setelah command ini selesai dicapai, damage yang didapat dengan mengeksekusi command ini,
    // jumlah powerup yang didapatkan dengan mengeksekusi command ini, dan tambahan score yang didapatkan
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