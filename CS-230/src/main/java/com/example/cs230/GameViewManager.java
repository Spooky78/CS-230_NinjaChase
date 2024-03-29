package com.example.cs230;

import java.io.IOException;
import java.util.*;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Responsible for the main game window.
 *
 * @author Everyone
 */
public class GameViewManager {
    private static final int GAME_WIDTH = 600;
    private static final int GAME_HEIGHT = 600;
    private static final int SECOND = 1000;
    private static final int ADDED_TIME = 5;
    private final HBox topRow = new HBox();
    private final BorderPane gamePlayPane = new BorderPane();
    private final ArrayList<StackPane> allAssassinStacks = new ArrayList<>();
    private final ArrayList<FlyingAssassin> allAssassins = new ArrayList<>();
    private ArrayList<NPC> allThieves = new ArrayList<>();
    private ArrayList<SmartThief> allSmartThieves = new ArrayList<>();
    private ArrayList<FloorFollowingThief> allFFThieves = new ArrayList<>();
    private final ArrayList<Coin> allCoins = new ArrayList<>();
    private final ArrayList<Clock> allClock = new ArrayList<>();
    private final ArrayList<Lever> allLever = new ArrayList<>();
    private final ArrayList<Gate> allGates = new ArrayList<>();
    private final ArrayList<Bomb> allBomb = new ArrayList<>();
    private final ArrayList<Item> allCollectableItems = new ArrayList<>();
    private final ArrayList<Item> bombDestroyCanDestroy = new ArrayList<>();
    private boolean isLose = false;
    private final boolean isTimerEnd = false;
    private VBox gamePane;
    private int currentLevel;
    private Scene gameScene;
    private Stage gameStage;
    private Stage menuStage;
    private AnimationTimer gameTimer;
    private Player currentPlayer;
    private StackPane currentPlayerStack;
    private Board currentBoard;
    private Door door;
    private Gate goldenGate;
    private Gate silverGate;
    private GameOverViewManager gameOver;
    private Ninja chosenNinja;
    private String level;
    private Time time;
    private boolean bombTImerStart = false;
    private ArrayList<Bomb> bombsToRemove = new ArrayList<>();
    private String name;


    /**
     * Creates a GameViewManager.
     */
    public GameViewManager() {
        initializeStage();
    }

    /**
     * Creates a new game.
     *
     * @param stage       The previous stage (usually menuStage).
     * @param chosenNinja The player character.
     */
    public void createNewGame(Stage stage, Ninja chosenNinja, String levelPath, String name) {
        this.level = levelPath;
        this.chosenNinja = chosenNinja;
        this.menuStage = stage;
        if (name != null) {
            this.name = name;
        }
        this.menuStage.hide();
        gameOver = new GameOverViewManager();
        createBackground();
        createBoard();
        createDoor();
        createClock();
        createGoldenGate();
        createSilverGate();
        createLever();
        createCoins();
        createBomb();
        createFloorFollowingThief();
        createSmartThief();
        createAssassin();
        createPlayer(chosenNinja);

        if (currentBoard.getProfileName() != null) {
            this.name = currentBoard.getProfileName();
        }
        if (levelPath.equals("/Levels/LastSave.txt")) {
            int levelInt = currentBoard.getLevel();
            level = "/Levels/Level0"+levelInt+".txt";
        }
       currentPlayer.setScore(currentBoard.getScore());

        time = new Time(currentBoard.getSeconds());
        updateTopRow();
        createGameLoop();
        topRow.setAlignment(Pos.CENTER_RIGHT);
        gamePane.getChildren().add(topRow);
        gamePane.getChildren().add(gamePlayPane);
        gameStage.show();
    }

    /**
     * Saving the game while middle leave.
     * @throws IOException throws when can't red file.
     */
    public void saveGave() throws IOException {
        if (currentPlayer.isSaveKeyPressed()) {
            SaveFile saveFile = new SaveFile(currentPlayer, allAssassins, allFFThieves,
                    allSmartThieves, allCoins, allLever, allGates, allBomb, allClock, door,
                    time.getCurrentTime(), currentPlayer.getScore(), currentBoard, name, currentLevel);
            saveFile.printAll();
            allThieves.clear();
            allSmartThieves.clear();
            System.gc();
            SaveViewManager saveViewManager = new SaveViewManager();
            saveViewManager.createSave(gameStage, currentPlayer);
            time.isKilled();
            isLose = true;
        }
    }

    /**
     * Check if the tile can be stepped.
     * @param coords the coords
     * @return true if it is able to step
     */
    public boolean checkNonSteppableTile(int[] coords) {
        for (Gate allGate : allGates) {
            if (allGate.getCoords()[0] == coords[0] && allGate.getCoords()[1] == coords[1]) {
                System.out.println("ON NON TILE GATE");
                return true;
            }
        }
        for (Bomb bomb : allBomb) {
            if (bomb.getCoords()[0] == coords[0] && bomb.getCoords()[1] == coords[1]) {
                System.out.println("ON NON TILE");
                return true;
            }
        }
        return false;
    }

    /**
     * Initializes the stage, scene, and pane.
     */
    private void initializeStage() {
        gamePane = new VBox();
        gameScene = new Scene(gamePane, GAME_WIDTH, GAME_HEIGHT);
        gameStage = new Stage();
        gameStage.setScene(gameScene);
    }

    /**
     * Created game loop.
     */
    private void createGameLoop() {
        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                if (time.getCurrentTime() == 0 && !isLose) {
                    for (NPC allThieve : allThieves) {
                        allThieve.stopTimer();
                    }
                    allThieves.clear();
                    allSmartThieves.clear();
                    System.gc();
                    gameOver.createGameOver(gameStage, currentPlayer);
                    time.isKilled();
                    isLose = true;
                }

                updateAssassinCollission();
                updateCoinCollission();
                updateClockCollission();
                updateDoorCollission();
                updateGateLeverCollision();

                for (Bomb allBombs : allBomb) {
                    if (allBombs.isCollisionPlayer(currentPlayer.getPlayerCoords())) {
                        allBombs.countdown();
                        bombTImerStart = true;}
                    if (bombTImerStart){
                        bombExplode(allBombs);
                    }
                }
                for (Bomb bomb : bombsToRemove) {
                    allBomb.remove(bomb);
                }
                bombsToRemove.clear();

                for (FloorFollowingThief allFFThieve : allFFThieves) {
                    if (allFFThieve.isFinishDirection()) {
                        allFFThieve.move();
                    }
                }

                updateSmartThiefMovement();
                updateTopRow();
            }
        };
        gameTimer.start();
    }

    /**
     * Bomb is exploded and remove all adjacent items.
     * @param allBombs the current exploding bomb.
     */
    private void bombExplode(Bomb allBombs) {
        bombDestroyCanDestroy.remove(allBombs);
        int[] currentBomb = allBombs.getCoords();
        for (Item allitems : bombDestroyCanDestroy) {
            int[] currentItem = allitems.getCoords();
            if (allitems.getClass() == Bomb.class && (allitems.getCoords()[0] == currentBomb[0] ||
                    allitems.getCoords()[1] == currentBomb[1] )){
                ((Bomb) allitems).countdown();
                bombExplode((Bomb) allitems);
            }
            if (bombTImerStart && (allBombs.isExploded() && ((currentItem[0] == currentBomb[0] ||
                    currentItem[1] == currentBomb[1])))) {
                gamePlayPane.getChildren().remove(allBombs.getStackPane());
                bombsToRemove.add(allBombs);
                gamePlayPane.getChildren().remove(allitems.getStackPane());
                allCollectableItems.remove(allitems);
                if(allitems.getClass() == Coin.class) {
                    allCoins.remove(allitems);
                }
                if(allitems.getClass() == Lever.class) {
                    allLever.remove(allitems);
                }
            }
            if (allBombs.isExploded() && bombTImerStart) {
                gamePlayPane.getChildren().remove(allBombs.getStackPane());
                bombsToRemove.add(allBombs);
            }
        }
    }

    /**
     * Updates if something interacts with lever, meaning that gates of same colour are destroyed.
     */
    private void updateGateLeverCollision() {
        ArrayList<Lever> leverToRemove = new ArrayList<>();
        ArrayList<Gate> gateToRemove = new ArrayList<>();
        for (Gate goldenGate : allGates) {
            if (goldenGate.getCanPass() &&
                    goldenGate.isCollisionPlayer(currentPlayer.getPlayerCoords())) {
                currentPlayer.canMove = false;
                System.out.println("Error");
            }
        }

        for (Lever currentLever : allLever) {
            for (Gate allGate : allGates) {
                if (Objects.equals(currentLever.getLeverColour(), "GOLD")
                        && (Objects.equals(allGate.getColour(), "GOLD"))
                        && currentLever.isCollectedByPlayer(currentPlayer.getPlayerCoords())) {
                    gamePlayPane.getChildren().remove(currentLever.getStackPane());
                    gamePlayPane.getChildren().remove(allGate.getStackPane());
                    leverToRemove.add(currentLever);
                    gateToRemove.add(allGate);

                }
                if (Objects.equals(currentLever.getLeverColour(), "SILVER")
                        && (Objects.equals(allGate.getColour(), "SILVER"))
                        && currentLever.isCollectedByPlayer(currentPlayer.getPlayerCoords())) {
                    gamePlayPane.getChildren().remove(currentLever.getStackPane());
                    gamePlayPane.getChildren().remove(allGate.getStackPane());
                    leverToRemove.add(currentLever);
                    gateToRemove.add(allGate);
                }
                for (NPC allThieve : allThieves) {
                    if (Objects.equals(currentLever.getLeverColour(), "GOLD")
                            && (Objects.equals(allGate.getColour(), "GOLD"))
                            && currentLever.isLeverCollisionNPC(allThieve.getCoords())) {
                        gamePlayPane.getChildren().remove(currentLever.getStackPane());
                        gamePlayPane.getChildren().remove(allGate.getStackPane());
                        leverToRemove.add(currentLever);
                        gateToRemove.add(allGate);
                    }

                    if (Objects.equals(currentLever.getLeverColour(), "SILVER")
                            && (Objects.equals(allGate.getColour(), "SILVER"))
                            && currentLever.isLeverCollisionNPC(allThieve.getCoords())) {
                        gamePlayPane.getChildren().remove(currentLever.getStackPane());
                        gamePlayPane.getChildren().remove(allGate.getStackPane());
                        leverToRemove.add(currentLever);
                        gateToRemove.add(allGate);
                    }
                }
            }

        }

        for (Gate silverGate : gateToRemove) {
            System.out.println("DELETED GATE!");
            allGates.remove(silverGate);
        }
        gateToRemove.clear();

        for (Lever lever : leverToRemove) {
            allLever.remove(lever);
            allCollectableItems.remove(lever);
        }
        leverToRemove.clear();
    }

    /**
     * Updates if anything has interacted with door.
     */
    private void updateDoorCollission() {
        if (door.isCollectedByPlayer(currentPlayer.getPlayerCoords()) && allCoins.size() == 0
                && allLever.size() == 0 ) {
            door.setPicked();
            gamePlayPane.getChildren().remove(door.getDoorPane());
            currentLevel += 1;
            gamePlayPane.getChildren().clear();
            time.isKilled();
            for (NPC allThieve : allThieves) {
                allThieve.stopTimer();
            }
            WinScreenViewManager winScreen = new WinScreenViewManager();
            winScreen.createGameOver(gameStage, currentPlayer, chosenNinja, level, name,
                    currentPlayer.getScore(), time.getCurrentTime());
        }
    }

    /**
     * Updates if there has been interaction with clock object.
     */
    private void updateClockCollission() {
        ArrayList<Clock> clockToRemove = new ArrayList<>();
        for (Clock allClocks : allClock) {
            if (allClocks.isCollectedByPlayer(currentPlayer.getPlayerCoords())) {

                time.currentTime += ADDED_TIME;
                gamePlayPane.getChildren().remove(allClocks.getClockPane());
                clockToRemove.add(allClocks);
            }
            for (NPC allThieve : allThieves) {
                if (allClocks.isClockCollisionNPC(allThieve.getCoords())) {
                    time.currentTime -= ADDED_TIME;
                    gamePlayPane.getChildren().remove(allClocks.getClockPane());
                    clockToRemove.add(allClocks);
                }
            }
        }
        for (Clock clock : clockToRemove) {
            allClock.remove(clock);
            allCollectableItems.remove(clock);
        }
        clockToRemove.clear();
    }

    /**
     * Updates if anything has interacted with coin.
     */
    private void updateCoinCollission() {
        ArrayList<Coin> coinsToRemove = new ArrayList<>();
        for (Coin allCoin : allCoins) {
            if (allCoin.isCollisionPlayer(currentPlayer.getPlayerCoords())) {
                gamePlayPane.getChildren().remove(allCoin.getCoinStackPane());
                coinsToRemove.add(allCoin);
                currentPlayer.setScore(currentPlayer.getScore() + allCoin.getCoinScore());
            }
            for (NPC allThieve : allThieves) {
                if (allCoin.isCollisionNPC(allThieve.getCoords())) {
                    gamePlayPane.getChildren().remove(allCoin.getCoinStackPane());
                    coinsToRemove.add(allCoin);
                }
            }
        }
        for (Coin coin : coinsToRemove) {
            allCoins.remove(coin);
            allCollectableItems.remove(coin);
        }
        coinsToRemove.clear();
    }

    /**
     * Updates smart thief movement.
     */
    private void updateSmartThiefMovement() {
        for (int i = 0; i < allSmartThieves.size(); i++) {
            if (allSmartThieves.get(i).isReachedDoor()) {
                for (NPC allThieve : allThieves) {
                    allThieve.stopTimer();
                }
                allThieves.clear();
                allSmartThieves.clear();
                System.gc();
                gameOver.createGameOver(gameStage, currentPlayer);
                time.isKilled();
                isLose = true;
            }
            if (allSmartThieves.size() != 0) {
                if (allSmartThieves.get(i).isCollected()) {
                    allSmartThieves.get(i).setAllCollectableItems(allCollectableItems);
                    allSmartThieves.get(i).move();
                }
            }
        }
    }

    /**
     * It anything touches assassin they die.
     */
    private void updateAssassinCollission() {
        ArrayList<NPC> removeNPC = new ArrayList<>();
        for (FlyingAssassin allAssassin : allAssassins) {
            if (allAssassin.collidedPlayer(currentPlayer.getPlayerCoords(),
                    currentPlayerStack, gamePlayPane)) {
                gamePlayPane.getChildren().clear();
                time.isKilled();
                for (NPC allThieve : allThieves) {
                    allThieve.stopTimer();
                }
                allThieves.clear();
                allSmartThieves.clear();
                System.gc();
                gameOver.createGameOver(gameStage, currentPlayer);
                allAssassin.setLose();
            }
            for (NPC allThieve : allThieves) {
                if (allAssassin.collidedThief(allThieve.getCoords(),
                        allThieve.getStackPane(), gamePlayPane)) {
                    removeNPC.add(allThieve);
                }
            }
        }

        for (NPC npc : removeNPC) {
            allThieves.remove(npc);
            if (npc.getClass() == SmartThief.class) {
                allSmartThieves.remove((SmartThief) npc);
            }
            if (npc.getClass() == FloorFollowingThief.class) {
                allFFThieves.remove((FloorFollowingThief) npc);
            }
        }
    }

    /**
     * Create player on the board.
     * @param chosenNinja the chosen ninja
     */
    private void createPlayer(Ninja chosenNinja) {
        currentPlayer = new Player(gameScene, chosenNinja, currentBoard, this);
        currentPlayerStack = currentPlayer.getPlayerStack();

        int tileSize = currentBoard.getTileSize();
        currentPlayer.setMovementOffset(tileSize);

        int[] playerStart = currentBoard.getPlayerStartCoords();
        int playerStartX = playerStart[0];
        int playerStartY = playerStart[1];
        currentPlayerStack.setLayoutX(playerStartX * tileSize + (tileSize / 2.0));
        currentPlayerStack.setLayoutY(playerStartY * tileSize + (tileSize / 2.0));
        gamePlayPane.getChildren().add(currentPlayerStack);
    }

    /**
     * Create smart thief.
     */
    private void createSmartThief() {
        ArrayList<Integer> coords = currentBoard.getSmartThiefStartCoords();
        for (int i = 0; i < coords.size(); i += 2) {
            int[] currentCoords = new int[2];
            currentCoords[0] = coords.get(i);
            currentCoords[1] = coords.get(i + 1);
            StackPane currentStackPane = new StackPane();
            SmartThief currentSmartThief =
                    new SmartThief(currentBoard, currentCoords, currentStackPane, this);
            currentStackPane.getChildren().add(currentSmartThief.getSmartThief());
            allThieves.add(currentSmartThief);
            allSmartThieves.add(currentSmartThief);
            gamePlayPane.getChildren().add(currentStackPane);
        }
    }

    /**
     * Create floor following thief.
     */
    private void createFloorFollowingThief() {
        ArrayList<String> colours = currentBoard.getFloorFollowingThiefColours();
        ArrayList<Integer> coords = currentBoard.getFloorFollowingThiefStartCoords();
        for (int i = 0; i < colours.size(); i++) {
            int[] currentCoords = {coords.get(i * 2), coords.get(i * 2 + 1)};
            StackPane ffThiefStack = new StackPane();
            FloorFollowingThief currentThief =
                    new FloorFollowingThief(currentBoard, currentCoords, ffThiefStack, i, this);
            ffThiefStack.getChildren().add(currentThief.getffThief());
            allThieves.add(currentThief);
            allFFThieves.add(currentThief);
            gamePlayPane.getChildren().add(ffThiefStack);
        }
    }

    /**
     * Create Assassin.
     */
    private void createAssassin() {
        ArrayList<String> direction = currentBoard.getAssassinStartDirection();
        ArrayList<Integer> coords = currentBoard.getAssassinStartCoords();
        //Each iteration of loop creates new assassin.
        for (int i = 0; i < direction.size(); i += 1) {
            int[] currentCoords = {coords.get(i * 2), coords.get(i * 2 + 1)};
            StackPane currentStackPane = new StackPane();
            FlyingAssassin currentAssassin =
                    new FlyingAssassin(currentBoard, currentCoords, currentStackPane, i);
            allAssassinStacks.add(currentStackPane);
            allAssassins.add(currentAssassin);
            currentStackPane.getChildren().add(currentAssassin.getAssassin());
            gamePlayPane.getChildren().add(currentStackPane);
        }
    }

    /**
     * Create coins on board.
     */
    private void createCoins() {
        ArrayList<String> coinColor = currentBoard.getCoinColor();
        ArrayList<Integer> coords = currentBoard.getCoinCoords();
        //Each iteration of loop creates new bronze coin.
        for (int i = 0; i < coinColor.size(); i += 1) {
            int[] currentCoinCoords = {coords.get(i * 2), coords.get((i * 2) + 1)};
            Coin currentCoin = new Coin(coinColor.get(i), currentBoard, currentCoinCoords);
            allCoins.add(currentCoin);
            allCollectableItems.add(currentCoin);
            bombDestroyCanDestroy.add(currentCoin);
            gamePlayPane.getChildren().add(currentCoin.getCoinStackPane());
        }
    }

    /**
     * Create lever.
     */
    private void createLever() {
        ArrayList<String> colours = currentBoard.getLeverColours();
        ArrayList<Integer> positionCoords = currentBoard.getLeverCoords();
        for (int i = 0; i < colours.size(); i += 1) {
            int[] currentLeverCoords = {positionCoords.get(i * 2), positionCoords.get(i * 2 + 1)};
            Lever currentLever = new Lever(currentBoard, currentLeverCoords, colours.get(i));
            allLever.add(currentLever);
            allCollectableItems.add(currentLever);
            bombDestroyCanDestroy.add(currentLever);
            gamePlayPane.getChildren().add(currentLever.getStackPane());
        }
    }

    /**
     * Create the door(exit to level.
     */
    private void createDoor() {
        int[] positionCoords = currentBoard.getDoorCoords();
        door = new Door(currentBoard, positionCoords);
        allCollectableItems.add(door);
        gamePlayPane.getChildren().add(door.getDoorPane());
    }

    /**
     * Create clock.
     */
    private void createClock() {
        ArrayList<Integer> positionCoords = currentBoard.getClockCoords();
        for (int i = 0; i < positionCoords.size(); i += 2) {
            int[] positionCoords2 = {positionCoords.get(i), positionCoords.get(i + 1)};
            Clock clock = new Clock(currentBoard, positionCoords2);
            allClock.add(clock);
            bombDestroyCanDestroy.add(clock);
            allCollectableItems.add(clock);
            gamePlayPane.getChildren().add(clock.getClockPane());
        }
    }

    /**
     * Create gold gate.
     */
    private void createGoldenGate() {
        ArrayList<Integer> positionCoords = currentBoard.getGate1Coords();
        for (int i = 0; i < positionCoords.size(); i += 2) {
            int[] positionCoords2 = {positionCoords.get(i), positionCoords.get(i + 1)};
            goldenGate = new Gate("GOLD", currentBoard, positionCoords2);
            allGates.add(goldenGate);
            gamePlayPane.getChildren().add(goldenGate.getStackPane());
        }
    }

    /**
     * Create silver gate.
     */
    private void createSilverGate() {
        ArrayList<Integer> positionCoords = currentBoard.getGate2Coords();
        for (int i = 0; i < positionCoords.size(); i += 2) {
            int[] positionCoords2 = {positionCoords.get(i), positionCoords.get(i + 1)};
            silverGate = new Gate("SILVER", currentBoard, positionCoords2);
            allGates.add(silverGate);

            gamePlayPane.getChildren().add(silverGate.getStackPane());
        }
    }

    /**
     * Create bomb.
     */
    private void createBomb() {
        ArrayList<Integer> positionCoords = currentBoard.getBombCoords();
        for (int i = 0; i < positionCoords.size(); i += 2) {
            int[] positionCoords2 = {positionCoords.get(i), positionCoords.get(i + 1)};
            Bomb bomb = new Bomb(currentBoard, positionCoords2);
            allBomb.add(bomb);
            bombDestroyCanDestroy.add(bomb);
            gamePlayPane.getChildren().add(bomb.getStackPane());
        }
    }

    /**
     * Create board.
     */
    private void createBoard() {
        currentLevel = 0;
        currentBoard = new Board(level, GAME_WIDTH);
        gamePlayPane.setLeft(currentBoard.getBoardPane());
    }

    /**
     * Creates top row of game window, which contains time left.
     */
    private void updateTopRow() {
        topRow.setPadding(new Insets(20));
        topRow.setSpacing(20);
        topRow.getChildren().clear();
        Text timeCounter = new Text("Time Left: " + time.getCurrentTime());
        timeCounter.setFont(Font.font("Arial", 20));
        topRow.getChildren().add(timeCounter);

        Text playerScore = new Text("SCORE: " + currentPlayer.getScore());
        playerScore.setFont(Font.font("Arial", 20));
        topRow.getChildren().add(playerScore);


    }

    /**
     * Creates the background.
     */
    private void createBackground() {
        Background background = new Background(new BackgroundFill(
                Color.SANDYBROWN, CornerRadii.EMPTY, Insets.EMPTY));
        gamePane.setBackground(background);
    }

    /**
     *
     * @return array lists of all collectable items.
     */
    public ArrayList<Item> getAllCollectableItems() {
        return allCollectableItems;
    }

    /**
     * print door to the screen.
     * @return door object.
     */
    public Door getDoor() {
        return door;
    }

}
