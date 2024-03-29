package com.example.cs230;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;

/**
 * Responsible for the main menu window.
 *
 * @author Everybody
 */
public class ViewManager {
    private static final int WIDTH = 900;
    private static final int HEIGHT = 780;
    private static final String BACKGROUND_PATH = "treesBackground.png";
    private final BorderPane mainPane;
    private final Scene mainScene;
    private Stage mainStage;
    private static VBox buttonPane;
    private static VBox logoPane;
    private static StackPane subscenePane;
    private MainMenuSubScene creditsSubScene;
    private MainMenuSubScene helpSubScene;
    private MainMenuSubScene scoreSubScene;
    private MainMenuSubScene ninjaChooserSubScene;
    private MainMenuSubScene sceneToHide;
    private List<NinjaPicker> ninjaPickerList;
    private Ninja chosenNinja;
    private String currentPlayerProfile;
    private boolean isHidden = true;
    private HBox startButtons = new HBox();
    private boolean chooseButtonVisable = false;
    private boolean shownScore = false;

    /**
     * Creates a main menu window.
     */
    public ViewManager() throws FileNotFoundException {
        mainPane = new BorderPane();
        mainScene = new Scene(mainPane, WIDTH, HEIGHT);
    }

    /**
     * creates a new menu display.
     *
     * @param stage the current stage
     * @throws FileNotFoundException
     */
    public void createNewMenu(Stage stage) throws FileNotFoundException {
        this.mainStage = stage;
        this.mainStage.hide();
        mainStage.setScene(mainScene);
        createSubScenes();
        createBackground();
        createLogo();
        createMsgOfTheDay();
        createButtons();
        mainStage.show();
    }

    /**
     * Creates the sub scenes for the main menu.
     */
    private void createSubScenes() throws FileNotFoundException {
        subscenePane = new StackPane();
        subscenePane.setPadding(new Insets(20));
        creditsSubScene = new MainMenuSubScene();
        helpSubScene = new MainMenuSubScene();
        scoreSubScene = new MainMenuSubScene();
        ninjaChooserSubScene = new MainMenuSubScene();
        creditsSubScene.getPane().setAlignment(Pos.TOP_CENTER);
        helpSubScene.getPane().setAlignment(Pos.TOP_CENTER);
        scoreSubScene.getPane().setAlignment(Pos.TOP_CENTER);
        mainPane.setCenter(subscenePane);
        createPlayerCharacterChooserSubScene();
        createHelpText();
        createCreditsText();
        createScoresText();
    }

    /**
     * Moves sub scene to new position. If off-screen move to on-screen. If on-screen moves
     * off-screen.
     */
    private void addRemoveSubScene(MainMenuSubScene currentScene) {
        if (isHidden) {
            subscenePane.getChildren().add(currentScene.getPane());
            isHidden = false;
        } else {
            subscenePane.getChildren().remove(currentScene.getPane());
            isHidden = true;
        }
    }

    /**
     * If sub scene is hidden then move it.
     *
     * @param subScene The sub scene to be moved.
     */
    private void showSubScene(MainMenuSubScene subScene) {
        if (sceneToHide != null) {
            addRemoveSubScene(subScene);
        }
        addRemoveSubScene(subScene);
        sceneToHide = subScene;
    }

    /**
     * Creates the player character chooser.
     */
    private void createPlayerCharacterChooserSubScene() {
        Text playGame = new Text("PLAY GAME!");
        try {
            playGame.setFont(Font.loadFont(new FileInputStream(FONT_PATH), 50));
        } catch (FileNotFoundException e) {
            playGame.setFont(Font.font("Verdana", 50));
        }
        ninjaChooserSubScene.getPane().getChildren().add(playGame);
        ninjaChooserSubScene.getPane().getChildren().add(createCurrentProfile());
        ninjaChooserSubScene.getPane().getChildren().add(createPlayerProfilePicker());
        ninjaChooserSubScene.getPane().getChildren().add(createPlayerCharacterToChoose());
        ninjaChooserSubScene.getPane().getChildren().add(createButtonToStart());
    }

    /**
     * Creates the text about what profile is currently chosen.
     *
     * @return The text of the currently chosen profile.
     */
    private Text createCurrentProfile() {
        Text currentProfileText = new Text("Please Select Profile!");
        if (AllProfile.getNameList() != null) {
            currentProfileText = new Text("Profile: " + currentPlayerProfile);
        }
        currentProfileText.setFont(Font.font("Arial", 25));
        return currentProfileText;
    }

    /**
     * Creates the row of buttons associated with making/ choosing a profile.
     *
     * @return the HBox of profile buttons.
     */
    private HBox createPlayerProfilePicker() {
        MainMenuButton newProfileButton = createNewProfileButton();
        MainMenuButton chooseProfileButton = chooseProfileButton();
        MainMenuButton deleteProfileButton = deleteProfileButton();

        HBox profilePrickerPane = new HBox(newProfileButton);
        profilePrickerPane.getChildren().add(chooseProfileButton);
        profilePrickerPane.getChildren().add(deleteProfileButton);

        profilePrickerPane.setSpacing(20);
        profilePrickerPane.setAlignment(Pos.CENTER);
        return profilePrickerPane;
    }

    /**
     * Creates the chose profile button along with it acton of choosing a profile.
     *
     * @return The chose profile button.
     */
    private MainMenuButton chooseProfileButton() {
        AllProfile.loadProfile();
        MainMenuButton chooseProfileButton = new MainMenuButton("Choose\nProfile");
        chooseProfileButton.setOnAction(e -> {
            if (AllProfile.getAllNamesInProfiles().size() == 0) {
                Text error = new Text("Error no profiles have been made!");
                error.setFont(Font.font("Arial", 25));
                ninjaChooserSubScene.getPane().getChildren().set(1, error);
            } else {
                ArrayList<String> arrayData = AllProfile.getNameList();

                List<String> dialogData = arrayData;

                ChoiceDialog<String> dialog = new ChoiceDialog<>(dialogData.get(0), dialogData);
                dialog.setTitle("Select Profile");
                dialog.setHeaderText("Select your choice");
                Optional<String> result = dialog.showAndWait();
                String selected;
                if (result.isPresent()) {
                    selected = result.get();
                    for (int i = 0; i < AllProfile.getNameList().size(); i++) {
                        if (selected.equals(AllProfile.getNameList().get(i))) {
                            currentPlayerProfile = AllProfile.getNameList().get(i);
                            ninjaChooserSubScene.getPane().getChildren()
                                    .set(1, createCurrentProfile());
                        }
                    }
                    if (!chooseButtonVisable) {
                        chooseButtonVisable = true;
                        startButtons.getChildren().add(levelSelectionButton());
                    }
                }
                System.out.println("Selection: " + currentPlayerProfile);
            }
        });
        return chooseProfileButton;
    }

    /**
     * creates a delete profile button.
     *
     * @return a button to choose profile to delete
     */
    private MainMenuButton deleteProfileButton() {
        AllProfile.loadProfile();
        MainMenuButton chooseProfileButton = new MainMenuButton("Delete\nProfile");
        chooseProfileButton.setOnAction(e -> {
            if (AllProfile.getAllNamesInProfiles().size() == 0) {
                Text error = new Text("Error no profiles have been made!");
                error.setFont(Font.font("Arial", 25));
                ninjaChooserSubScene.getPane().getChildren().set(1, error);
            } else {
                ArrayList<String> arrayData = AllProfile.getNameList();

                List<String> dialogData = arrayData;

                ChoiceDialog<String> dialog = new ChoiceDialog<>(dialogData.get(0), dialogData);
                dialog.setTitle("Select Profile");
                dialog.setHeaderText("Select your choice");
                Optional<String> result = dialog.showAndWait();
                String selected;
                if (result.isPresent()) {
                    selected = result.get();
                    AllProfile.deleteProfile(selected);
                }
                System.out.println("Deleted: " + currentPlayerProfile);
            }
        });
        return chooseProfileButton;
    }

    /**
     * Creates new profile button algong with it action of naking new profile.
     *
     * @return The new profile button.
     */
    private MainMenuButton createNewProfileButton() {
        TextInputDialog nameInputDialog = new TextInputDialog("Enter a name");
        nameInputDialog.setContentText("Name: ");
        nameInputDialog.setHeaderText("Create New Player Profile!");

        MainMenuButton newProfileButton = new MainMenuButton("New\nProfile");

        newProfileButton.setOnAction(e -> {
            Optional<String> result = nameInputDialog.showAndWait();
            String newProfileName = "none";
            if (result.isPresent()) {
                newProfileName = result.get();
                currentPlayerProfile = newProfileName;
                ninjaChooserSubScene.getPane().getChildren().set(1, createCurrentProfile());
                AllProfile.addName(newProfileName);
            }
        });
        return newProfileButton;
    }

    /**
     * creates a button for level selection.
     *
     * @return a choose level button
     */
    private MainMenuButton levelSelectionButton() {
        if (currentPlayerProfile != null) {
            List<String> dialogData = AllProfile.getLevelSelection(currentPlayerProfile);
            MainMenuButton chooseLevelButton = new MainMenuButton("Choose\nLevel");
            chooseLevelButton.setOnAction(e -> {
                if (dialogData.size() == 0) {
                    Text error = new Text("Error no level found!");
                    error.setFont(Font.font("Arial", 25));
                    ninjaChooserSubScene.getPane().getChildren().set(1, error);
                } else {
                    ChoiceDialog<String> dialog = new ChoiceDialog<>(dialogData.get(0), dialogData);
                    dialog.setTitle("Select Level");
                    dialog.setHeaderText("Select your choice");
                    Optional<String> result = dialog.showAndWait();
                    String selected;
                    if (result.isPresent() && chosenNinja != null) {
                        selected = result.get();
                        //System.out.println(selected);
                        char levelChar = selected.charAt(6);
                        //System.out.println(levelChar);
                        int level = Integer.parseInt(String.valueOf(levelChar));
                        String levelString = "/Levels/Level0" + level + ".txt";
                        GameViewManager newGame = new GameViewManager();
                        newGame.createNewGame(mainStage, chosenNinja, levelString, currentPlayerProfile);
                    } else if (result.isPresent() && chosenNinja == null) {
                        Text error = new Text("YOU NEED TO SELECT CHARACTER");
                        error.setFont(Font.font("Arial", 25));
                        ninjaChooserSubScene.getPane().getChildren().set(1, error);
                    }
                    System.out.println("Level selection: " + currentPlayerProfile);
                }
            });
            return chooseLevelButton;
        }
        return null;
    }

    /**
     * Creates the start button.
     *
     * @return The start button.
     */
    private HBox createButtonToStart() {
        if (currentPlayerProfile != null) {
            startButtons.getChildren().add(levelSelectionButton());
        }
        startButtons.setSpacing(20);
        startButtons.setAlignment(Pos.CENTER);
        MainMenuButton startButton = new MainMenuButton("START\nGAME");
        startButtons.getChildren().add(startButton);
        startButton.setOnAction(actionEvent -> {
            if (chosenNinja != null && currentPlayerProfile != null) {
                GameViewManager gameManager = new GameViewManager();
                gameManager.createNewGame(mainStage, chosenNinja, "/Levels/Level00.txt", currentPlayerProfile);
            } else if (chosenNinja == null && currentPlayerProfile != null) {
                Text error = new Text("YOU NEED TO SELECT CHARACTER");
                error.setFont(Font.font("Arial", 25));
                ninjaChooserSubScene.getPane().getChildren().set(1, error);
            } else if (chosenNinja != null && currentPlayerProfile == null) {
                Text error = new Text("YOU NEED TO SELECT A PROFILE");
                error.setFont(Font.font("Arial", 25));
                ninjaChooserSubScene.getPane().getChildren().set(1, error);
            }
        });

        return startButtons;
    }

    /**
     * Creates the player character to choose.
     *
     * @return The HBox of the player character.
     */
    private HBox createPlayerCharacterToChoose() {
        HBox ninjaPickerBox = new HBox();
        ninjaPickerBox.setSpacing(20);
        ninjaPickerList = new ArrayList<>();
        for (Ninja ninja : Ninja.values()) {
            NinjaPicker ninjaToPick = new NinjaPicker(ninja);
            ninjaPickerList.add(ninjaToPick);
            ninjaPickerBox.getChildren().add(ninjaToPick);
            ninjaToPick.setOnMouseClicked(mouseEvent -> {
                for (NinjaPicker ship1 : ninjaPickerList) {
                    ship1.setIsBoxChosen(false);
                }
                ninjaToPick.setIsBoxChosen(true);
                chosenNinja = ninjaToPick.getNinja();
            });
        }
        ninjaPickerBox.setPadding(new Insets(20));
        ninjaPickerBox.setAlignment(Pos.CENTER);
        return ninjaPickerBox;
    }

    /**
     * get font from font file(.ttf)
     */
    private final String FONT_PATH;

    {
        try {
            FONT_PATH = String.valueOf(new File(ClassLoader.getSystemResource(
                    "kenvector_future.ttf").toURI()));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * creates a level selection.
     *
     * @return which level you can choose
     */
    private MainMenuButton choseScoreLevel() {
        //Score list is an array list
        List<String> dialogData = new ArrayList<>();

        //List<String> dialogData =  string array list
        dialogData.add("Level00");
        dialogData.add("Level01");
        dialogData.add("Level02");
        dialogData.add("Level03");

        MainMenuButton chooseScoreLevelButton = new MainMenuButton("Choose\nLevel");
        chooseScoreLevelButton.setOnAction(e -> {
            if (dialogData.size() == 0) {
                Text error = new Text("Error no level found!");
                error.setFont(Font.font("Arial", 25));
                ninjaChooserSubScene.getPane().getChildren().set(1, error);
            } else {

                ChoiceDialog<String> dialog = new ChoiceDialog<>(dialogData.get(0), dialogData);
                dialog.setTitle("Select Level");
                dialog.setHeaderText("Select your choice");
                Optional<String> result = dialog.showAndWait();
                String selected;
                if (result.isPresent()) {
                    selected = result.get();
                    switch (selected) {
                        case "Level00":
                            loadScores(0);
                            break;
                        case "Level01":
                            loadScores(1);
                            break;
                        case "Level02":
                            loadScores(2);
                            break;
                        case "Level03":
                            loadScores(3);
                            break;
                        default:
                            System.out.println("fuck");
                            break;
                    }
                }
            }
        });
        return chooseScoreLevelButton;
    }

    /**
     * Titles of score and only return when there is no score.
     */
    private void createScoresText() {
        Text scoreTitle = new Text("Score");
        try {
            scoreTitle.setFont(Font.loadFont(new FileInputStream(FONT_PATH), 60));
        } catch (FileNotFoundException e) {
            scoreTitle.setFont(Font.font("Arial", 60));
        }

        VBox scoreTit = new VBox();
        scoreTit.setAlignment(Pos.CENTER);
        scoreTit.getChildren().add(scoreTitle);
        scoreTit.getChildren().add(choseScoreLevel());
        VBox score = new VBox();
        scoreSubScene.getPane().getChildren().add(scoreTit);
        scoreSubScene.getPane().getChildren().add(score);
    }

    /**
     * loads the scores.
     *
     * @param levelIndex the index of the level
     */
    private void loadScores(int levelIndex) {
        VBox score = new VBox();
        AllScore.loadScore();
        AllScore.createAndSortScoreList(levelIndex);
        if (!shownScore) {
            for (int i = 0; i < AllScore.getTenHighScore().length; i++) {
                Text testText = new Text(AllScore.getTenHighScoreName(levelIndex)[i] + "   " + AllScore.getTenHighScore()[i]);
                score.getChildren().add(testText);
            }
            scoreSubScene.getPane().getChildren().add(score);
            shownScore = true;
        }

    }

    /**
     * Creates credits of who created the game.
     */
    private void createCreditsText() {
        Text creditTitle = new Text("Credits");
        try {
            creditTitle.setFont(Font.loadFont(new FileInputStream(FONT_PATH), 60));
        } catch (FileNotFoundException e) {
            creditTitle.setFont(Font.font("Arial", 60));
        }
        Text members = new Text("\n Ziming Dong, 2013509 \n Rex Lam, 2035415 \n Arran Pearce, 2010120 " +
                "\n Omar Sufer, 2123959 \n Dimitrios Koumaris, 2269608 \n Vic Lismanovica, 2108255 " +
                "\n Chrysis Pitsillides, 2009555");
        members.setFont(Font.font("Arial", 25));
        VBox creditTit = new VBox();
        creditTit.getChildren().add(creditTitle);
        creditTit.setAlignment(Pos.CENTER);
        VBox credit = new VBox();
        credit.getChildren().add(members);
        creditsSubScene.getPane().getChildren().add(creditTit);
        creditsSubScene.getPane().getChildren().add(credit);
    }

    /**
     * Creates messages of how to play the game.
     */
    private void createHelpText() {
        Text howToPlayTitle = new Text("How to play");
        VBox helpTitle = new VBox();
        try {
            howToPlayTitle.setFont(Font.loadFont(new FileInputStream(FONT_PATH), 40));
        } catch (FileNotFoundException e) {
            howToPlayTitle.setFont(Font.font("Arial", 40));
        }
        VBox instructions = new VBox();
        Text objective = new Text("Objective: \n");
        Text objInstruction = new Text(
                "Use the arrow keys to move around the room and collect all the variables, " +
                        "then escape  through the red door to the next level. \n\n"
        );
        Text how2Play = new Text("How to play: \n");
        Text playInstruction = new Text(
                "You can only move to tiles that are the same color as the tile you are on." +
                        "If you are blocked by a different color. You automatically jump" +
                        "to the next same-color tile. To move to a new color, go to a multicolored tile. \n"
        );
        Text items = new Text("Items: \n");
        Text itemsInstruction = new Text(
                "Keys, bombs and levers open locked doors. \n" +
                        "Coins get more score dependent on remaining time \n" +
                        "Clocks add time to the countdown timer. \n"
        );
        Text thieves = new Text("Thieves: \n");
        Text thievesInstruction = new Text(
                "Floor following thieves follows the colored edge of the floor. \n" +
                        "Smart Thieves collects the loot and reach the exit of the level before the player. " +
                        "When the Smart Thieves arrived first, you lost the game."
        );
        helpTitle.getChildren().add(howToPlayTitle);
        helpTitle.setAlignment(Pos.CENTER);
        objective.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 20));
        objective.setUnderline(true);
        objInstruction.setFont(Font.font("Arial", 15));
        how2Play.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 20));
        how2Play.setUnderline(true);
        playInstruction.setFont(Font.font("Arial", 15));
        items.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 20));
        items.setUnderline(true);
        itemsInstruction.setFont(Font.font("Arial", 15));
        thieves.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 20));
        thieves.setUnderline(true);
        thievesInstruction.setFont(Font.font("Arial", 15));
        objInstruction.setWrappingWidth(550);
        playInstruction.setWrappingWidth(550);
        itemsInstruction.setWrappingWidth(550);
        thievesInstruction.setWrappingWidth(550);
        instructions.getChildren().add(objective);
        instructions.getChildren().add(objInstruction);
        instructions.getChildren().add(how2Play);
        instructions.getChildren().add(playInstruction);
        instructions.getChildren().add(items);
        instructions.getChildren().add(itemsInstruction);
        instructions.getChildren().add(thieves);
        instructions.getChildren().add(thievesInstruction);
        helpSubScene.getPane().getChildren().add(helpTitle);
        helpSubScene.getPane().getChildren().add(instructions);
    }


    /**
     * Gets main stage.
     *
     * @return The main stage.
     */
    public Stage getMainStage() {
        return mainStage;
    }

    /**
     * Creates all main menu buttons.
     */
    private void createButtons() {
        buttonPane = new VBox();
        buttonPane.setSpacing(20);
        buttonPane.setAlignment(Pos.CENTER_LEFT);
        buttonPane.setPadding(new Insets(0, 20, 20, 20));
        mainPane.setLeft(buttonPane);
        createStartButton();
        createSavesButton();
        createScoreButton();
        createHelpButton();
        createCreditsButton();
        createExitButton();
    }

    /**
     * Creates the play button.
     */
    private void createStartButton() {
        MainMenuButton startButton = new MainMenuButton("PLAY");
        buttonPane.getChildren().add(startButton);
        startButton.setOnAction(actionEvent -> showSubScene(ninjaChooserSubScene));
    }

    /**
     * Creates the saves button.
     */
    private void createSavesButton() {
        MainMenuButton savesButton = new MainMenuButton("Saves");
        buttonPane.getChildren().add(savesButton);
        savesButton.setOnAction(actionEvent -> {
            chosenNinja = Ninja.BLACK;
            GameViewManager gameManager = new GameViewManager();
            gameManager.createNewGame(mainStage, chosenNinja, "/Levels/LastSave.txt", null);
        });
    }

    /**
     * Creates the score button.
     */
    private void createScoreButton() {
        MainMenuButton scoreButton = new MainMenuButton("Scores");
        buttonPane.getChildren().add(scoreButton);
        scoreButton.setOnAction(actionEvent -> showSubScene(scoreSubScene));
    }

    /**
     * Creates the help button.
     */
    private void createHelpButton() {
        MainMenuButton helpButton = new MainMenuButton("Help");
        buttonPane.getChildren().add(helpButton);
        helpButton.setOnAction(actionEvent -> showSubScene(helpSubScene));
    }

    /**
     * Creates the credits button.
     */
    private void createCreditsButton() {
        MainMenuButton creditsButton = new MainMenuButton("Credits");
        buttonPane.getChildren().add(creditsButton);
        creditsButton.setOnAction(actionEvent -> showSubScene(creditsSubScene));
    }

    /**
     * Creates the exit button, that exits the application.
     */
    private void createExitButton() {
        MainMenuButton exitButton = new MainMenuButton("Exit");
        buttonPane.getChildren().add(exitButton);
        exitButton.setOnAction(actionEvent -> mainStage.close());
    }

    /**
     * Sets the background to images.
     */
    private void createBackground() {
        Image backgroundImage = new Image(BACKGROUND_PATH, WIDTH, HEIGHT, false, false);
        BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(100, 100,
                true, true, true, true));
        mainPane.setBackground(new Background(background));
    }

    /**
     * Creates the game logo.
     */
    private void createLogo() {

        ImageView logo = new ImageView("logo.png");

        logo.setOnMouseEntered(mouseEvent -> logo.setEffect(new DropShadow()));
        logo.setOnMouseExited(mouseEvent -> logo.setEffect(null));

        logoPane = new VBox();
        logoPane.getChildren().add(logo);
        logoPane.setAlignment(Pos.TOP_CENTER);
        logoPane.setPadding(new Insets(30, 0, 0, 0));
        mainPane.setTop(logoPane);
    }


    /**
     * Displays message of the day and well-fitted under the game title
     * word size stays the same when resizing the game.
     */
    private void createMsgOfTheDay() {
        MsgOfTheDay m = new MsgOfTheDay();
        Text msgOfTheDay = new Text(m.getRequest());
        msgOfTheDay.setFont(Font.font("Arial", FontPosture.ITALIC, 18));
        msgOfTheDay.setTextAlignment(TextAlignment.CENTER);
        msgOfTheDay.setWrappingWidth(600);
        logoPane.getChildren().add(msgOfTheDay);
    }
}
