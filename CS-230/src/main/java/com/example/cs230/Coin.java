package com.example.cs230;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public class Coin extends  Item{
    private static final String BRONZE_COIN_PATH = "/Items/CoinBronze.png";
    private static final String SILVER_COIN_PATH = "/Items/CoinSilver.png";
    private static final String GOLD_COIN_PATH = "/Items/CoinGold.png";
    private static final String PLAT_COIN_PATH = "/Items/CoinPlat.png";

    private static final int BRONZE_COIN_SCORE = 1;
    private static final int SILVER_COIN_SCORE = 3;
    private static final int GOLD_COIN_SCORE = 5;
    private static final int PLAT_COIN_SCORE = 10;
    private static final int COIN_SIZE = 30;
    private StackPane coinStackPane = new StackPane();
    private Board gameBoard;
    private ImageView coin = new ImageView();
    private int[] coinPosition;
    private int coinScore;

    public Coin(String cointype, Board board, int[] position) {
        gameBoard = board;
        coinPosition = position;
        createItem(cointype, position);
    }

    protected void createItem(String cointype, int[] position) {

        switch (cointype) {
            case "BRONZE":
                Image bronzeImage = new Image(
                        Objects.requireNonNull(getClass().getResourceAsStream(BRONZE_COIN_PATH)));
                coin = new ImageView(bronzeImage);
                coinScore = BRONZE_COIN_SCORE;
                break;
            case "SILVER":
                Image silverImage = new Image(
                        Objects.requireNonNull(getClass().getResourceAsStream(SILVER_COIN_PATH)));
                coin = new ImageView(silverImage);
                coinScore = SILVER_COIN_SCORE;
                break;
            case "GOLD":
                Image goldImage = new Image(
                        Objects.requireNonNull(getClass().getResourceAsStream(GOLD_COIN_PATH)));
                coin = new ImageView(goldImage);
                coinScore = GOLD_COIN_SCORE;
                break;
            case "PLAT":
                Image platImage = new Image(
                        Objects.requireNonNull(getClass().getResourceAsStream(PLAT_COIN_PATH)));
                coin = new ImageView(platImage);
                coinScore = PLAT_COIN_SCORE;
                break;
        }
        coin.setFitWidth(COIN_SIZE);
        coin.setFitHeight(COIN_SIZE);
        coinStackPane.getChildren().add(coin);
        int tileSize = gameBoard.getTileSize();
        coinStackPane.setLayoutX((position[0] * tileSize) - (tileSize / 2));
        coinStackPane.setLayoutY((position[1] * tileSize) - (tileSize / 2));

    }

    public boolean isCollisionPlayer(int[] playerCoords) {
        if (playerCoords[0] +1 == coinPosition[0] && playerCoords[1] +1 == coinPosition[1]) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isCollisionNPC(int[] npcCoords) {
        if (npcCoords[0] == coinPosition[0] && npcCoords[1] == coinPosition[1]) {
            return true;
        } else {
            return false;
        }
    }

    public StackPane getCoinStackPane() {return coinStackPane;}

    public ImageView getCoin() {return coin;}
    public int getCoinScore() {return coinScore;}

    @Override
    protected StackPane getStackPane() {
        return coinStackPane;
    }

    @Override
    protected int[] getCoords() {
        return coinPosition;
    }
}
