package fr.modcraft.launcher;

import javafx.application.Application;

public class Main {

    public static void main(String[] args) {
        System.out.println("Starting ModcraftLauncher bootstrap");
        Application.launch(Bootstrap.class, args);

    }
}
