package com.proxlu.game;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.Files;
// import com.proxlu.game.SpaceGDX; 

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("SpaceGDX Jam");
		// config.addIcon("icons/ic_launcher_32.png", Files.FileType.Internal);

                config.setWindowedMode(1280, 720);
		new Lwjgl3Application(new SpaceGDX(), config);
	}
}
