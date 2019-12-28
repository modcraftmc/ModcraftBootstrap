package fr.modcraft.launcher;

import fr.modcraft.launcher.alert.AlertBuilder;
import fr.modcraft.launcher.downloader.DownloaderManager;
import fr.modcraft.launcher.maintenance.MaintenanceManager;
import fr.modcraft.launcher.utils.JavaUtils;
import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.external.ClasspathConstructor;
import fr.theshark34.openlauncherlib.external.ExternalLaunchProfile;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.util.GameDirGenerator;
import fr.theshark34.openlauncherlib.util.CrashReporter;
import fr.theshark34.openlauncherlib.util.explorer.ExploredDirectory;
import fr.theshark34.openlauncherlib.util.explorer.Explorer;
import fr.theshark34.supdate.SUpdate;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;

public class Bootstrap extends Application {

    public static final File DIR = new File(GameDirGenerator.createGameDir("modcraft/"), "Launcher/beta");
    private static CrashReporter crashReporter = new CrashReporter("Bootstrap-Crash", new File(DIR, "crash/"));

    private static ProgressBar progressBar = new ProgressBar();
    public static Stage stage;

    @Override
    public void start(Stage stage) {
        JavaUtils.getArchitecture();
        JavaUtils.getVersion();

        this.stage = stage;

        StackPane root = new StackPane();
        Scene scene = new Scene(root);
        stage.setTitle("ModcraftMC");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("resources/favicon.png")));
        stage.setScene(scene);
        stage.setMaxHeight(60);
        stage.setMaxWidth(300);

        stage.setOnCloseRequest(event -> System.exit(0));

        stage.setResizable(false);
        stage.show();
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 2);
        initComponents(root);

        MaintenanceManager maintenanceManager = new MaintenanceManager();
        maintenanceManager.checkIfMaintenance();

        if (maintenanceManager.isMaintenance()) {
            AlertBuilder alertBuilder = new AlertBuilder("Maintenance", maintenanceManager.getInfos(), AlertBuilder.ButtonsType.JUST_OK, Alert.AlertType.ERROR);
            alertBuilder.show();
            if (maintenanceManager.isExit()) {
                System.exit(0);
            }
        }

        if (!JavaUtils.getArchitecture().contains("64")) {
            DownloaderManager.askToDownload();
        } else {
            new Thread(Bootstrap::start).start();
        }

    }

    private void initComponents(StackPane root) {
        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(30);

        root.getStylesheets().add(getClass().getResource("resources/bar.css").toExternalForm());
        root.getChildren().addAll(progressBar);
    }


    public static void start() {

        try {
            doUpdate();
        } catch (Exception e) {
            crashReporter.catchError(e, "Impossible de mettre à jour le Launcher !");
        }

        try {
            Thread.sleep(2000);
            launchLauncher();
        } catch (LaunchException | InterruptedException e) {
            crashReporter.catchError(e, "Impossible de lancer le Launcher !");
        }
    }

    public static void doUpdate() throws Exception {

        SUpdate su = new SUpdate("http://v1.modcraftmc.fr:300", DIR);
        su.getServerRequester().setRewriteEnabled(true);
        su.start();
    }

    private static void launchLauncher() throws LaunchException {
        ClasspathConstructor constructor = new ClasspathConstructor();

        ExploredDirectory gameDir = Explorer.dir(DIR);
        constructor.add(gameDir.get("launcher.jar"));
        ExternalLaunchProfile profile = new ExternalLaunchProfile("fr.modcraft.launcher.ModcraftLauncher", constructor.make());
        ExternalLauncher launcher = new ExternalLauncher(profile);

        Process p = launcher.launch();
        try {
            Platform.runLater(() -> Bootstrap.stage.hide());
            p.waitFor();
        } catch (InterruptedException localInterruptedException) {

        }
        System.exit(0);
    }

    public static ProgressBar getProgressBar() {
        return progressBar;
    }

    public static void setTitle(String text) {
        Platform.runLater(() -> stage.setTitle(text));
    }
}
