package com.example.cs230;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

//TODO: Change score into score[] HAVE FINISHED by adding 4 score txt file.
//TODO: Change high score table so it needs an input of level index
//TODO: Link with the game.

public class AllProfile {
    private static final String PROFILE_FILE_PATH = "CS-230/src/main/resources/Levels/Profile.txt";
    private static final int DEFAULT_LEVEL_UNLOCKED = 1;

    private static ArrayList<PlayerProfile> allProfile = new ArrayList<>();

    private static ArrayList<String> nameList = new ArrayList<>();
    private static ArrayList<String> stringOfLevelSelection = new ArrayList<>();



    //When the game started, it loads all the profile in the file path.
    public static void loadProfile() {
        allProfile.clear();
        nameList.clear();
        try {
            File playerProfile = new File(PROFILE_FILE_PATH);
            Scanner playerProfileReader = new Scanner(playerProfile);
            while (playerProfileReader.hasNextLine()) {
                String readLine = playerProfileReader.nextLine();
                String[] params = readLine.split(", ");
                String name = params[0];
                int unlockedLevel = Integer.parseInt(params[1]);
                allProfile.add(new PlayerProfile(name, unlockedLevel));
                nameList.add(name);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //if not existed, add a name as well as the profile index
    public static void addName(String name) {
        if (!nameList.contains(name)) {
            allProfile.add(new PlayerProfile(name, DEFAULT_LEVEL_UNLOCKED));
            nameList.add(name);
            writeProfileInTxt();
        } else if (nameList.contains(name)) {
            System.out.println("Sorry, the name has been created by someone else.");
        } else {
            System.out.println("Sorry, there seems to be some Errors.");
        }
    }



    //return string array list of names to the game panel
    public static ArrayList<String> getAllNamesInProfiles() {
        return nameList;
    }
    public static void deleteProfile(String name) {
        for (int i = 0; i < allProfile.size(); i++) {
            if (name.equals(allProfile.get(i).getName())) {
                allProfile.remove(i);
                nameList.remove(name);
            }
        }
        AllScore.deleteAllScoreCurrentName(name);
/*
        for (int i = 0; i < allScores.size(); i++) {
            if (name.equals(allScores.get(i).getPlayerName())) {
                allScores.remove(i);
                scoreList.remove(i);
            }
        }
 */
        writeProfileInTxt();
    }

    // a method to rewrite every profile
    // when adding/deleting the method, writeTxt has to be run so the txt of the profile have all the profile names.
    public static void writeProfileInTxt() {
        try {
            FileWriter playerProfile = new FileWriter(PROFILE_FILE_PATH);
            for (int i = 0; i < allProfile.size(); i++) {
                playerProfile.write(allProfile.get(i).getName() + ", " +
                        allProfile.get(i).getIsLevelUnlocked() + "\n");
            }
            playerProfile.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    //the level is updated once the level is completed.
    public static void updateLevel(String profileName, int levelIndex) {
        String playerName = profileName;
        for (int i = 0; i < allProfile.size(); i++) {
            if (playerName.equals(allProfile.get(i).getName())) {
                if (levelIndex > allProfile.get(i).getIsLevelUnlocked()) {
                    allProfile.get(i).setLevelUnlocked(levelIndex);
                }
            }
        }
    }


    public static ArrayList<String> getNameList() {
        return nameList;
    }

    public static ArrayList<String> getLevelSelection(String playerName) {
        stringOfLevelSelection.clear();
        for (int i = 0; i < allProfile.size(); i++) {
            if (playerName.equals(allProfile.get(i).getName())) {
                for (int j = 0; j < allProfile.get(i).getIsLevelUnlocked(); j++) {
                    stringOfLevelSelection.add("Level0" + j);
                }
            }
        }
        return stringOfLevelSelection;
    }
}
