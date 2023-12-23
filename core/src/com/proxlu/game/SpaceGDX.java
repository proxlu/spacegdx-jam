package com.proxlu.game;

import java.util.Random;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.Timer;
import java.util.Iterator;

public class SpaceGDX extends ApplicationAdapter {
    SpriteBatch batch;
    Texture img, tNave, tMissile, tEnemy;

    private Sprite nave, missile;
    private float posX, posY, velocity, xMissile, yMissile;
    private boolean attack, gameover;
    private Array<Rectangle> enemies;
    private long lastEnemyTime;
    private int score, power, numEnemies, enemiesCleared;
    private FreeTypeFontGenerator generator;
    private FreeTypeFontGenerator.FreeTypeFontParameter parameter;
    private BitmapFont bitmap;
    private boolean paused = false;
    private Sound shotSound, damageSound, specialSound;
    private Music backgroundMusic;
    private boolean titleScreen = true;
    private Texture titleImage, specialStartImage, specialMiddleImage, specialEndImage, transparentImage;
    private boolean truncate = true;
    private boolean xPressed = true;
    private long specialStartTime = 0;
    private int specialStage = 1;
    private boolean open = true;
    private float spawn;
    Random random = new Random();

    @Override
    public void create() {
        batch = new SpriteBatch();
        img = new Texture("bg.png");
        tNave = new Texture("spaceship.png");
        nave = new Sprite(tNave);
        posX = 0;
        posY = (Gdx.graphics.getHeight() - nave.getHeight()) / 2;
        velocity = 10;
        tMissile = new Texture("missile.png");
        missile = new Sprite(tMissile);
        xMissile = posX;
        yMissile = posY;
        attack = false;
        tEnemy = new Texture("enemy.png");
        enemies = new Array<>();
        lastEnemyTime = 0;
        score = 0;
        power = 3;

        numEnemies = 799999999;
        generator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 30;
        parameter.borderWidth = 1;
        parameter.borderColor = Color.BLACK;
        parameter.color = Color.WHITE;
        bitmap = generator.generateFont(parameter);
        gameover = false;

        shotSound = Gdx.audio.newSound(Gdx.files.internal("shot.mp3"));
        damageSound = Gdx.audio.newSound(Gdx.files.internal("damage.mp3"));
        specialSound = Gdx.audio.newSound(Gdx.files.internal("special.mp3"));
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.play();
        titleImage = new Texture("title.jpg");
        specialStartImage = new Texture("start.png");
        specialMiddleImage = new Texture("middle.png");
        specialEndImage = new Texture("end.png");
        // transparentImage = new Texture("transparent.png");

    }

    // Pausas, não usado por padrão
    public void sleep() {
        try {
            Thread.sleep(5);
        }catch(InterruptedException e){}
    }

    // Função principal
    @Override
    public void render() {
        if (titleScreen) {
            // Tela de título
            batch.begin();
            batch.draw(titleImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

            // Calcula a posição para "Press Z" no centro da inferior-esquerda
            float pressZPosX = Gdx.graphics.getWidth() / 4f;
            float pressZPosY = Gdx.graphics.getHeight() / 4f;

            // Desenha "Press Z" na posição calculada
            bitmap.draw(batch, "PRESS Z", pressZPosX, pressZPosY);

            batch.end();
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.Z) || Gdx.input.isKeyJustPressed(Input.Keys.X)) {
                titleScreen = false;
            }
            return;
        }

        if(!paused && !gameover && (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER))){
            paused = true;
            specialStartTime = 0;
            specialStage = 1;
            open = true;
        }else if(paused){
            if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.Z) || Gdx.input.isKeyJustPressed(Input.Keys.X)){
                paused = false;
                truncate = true;
                xPressed = true;
            }
        }else if(gameover){
            if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.Z) || Gdx.input.isKeyJustPressed(Input.Keys.X)){
                restartGame();
                truncate = true;
	        xPressed = true;
            }
        }else if(specialStartTime > 0){
            // Exibe a imagem especial
            if(TimeUtils.timeSinceMillis(specialStartTime) < 200){
                if(specialStage == 1){
                    // System.out.println("1"); // Debug
                    batch.begin();
                    batch.draw(specialStartImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                    batch.end();
                    specialStage = 2;
	        }
	        return;
            }else if(TimeUtils.timeSinceMillis(specialStartTime) < 400){
                if(specialStage == 3){
                    // System.out.println("3"); // Debug
                    batch.begin();
                    batch.draw(specialMiddleImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                    batch.end();
                    specialStage = 4;
                }
                if(specialStage == 2){
                // System.out.println("2"); // Debug
                    specialStage = 3;
                }else{
                    return;
                }
            }else if(TimeUtils.timeSinceMillis(specialStartTime) < 600){
                if(specialStage == 5){
                    // System.out.println("5"); // Debug
                    batch.begin();
                    batch.draw(specialEndImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                    batch.end();
                    specialStage = 6;
                    // Adicione pontos com base no número de inimigos eliminados
                    damageSound.play(); // Toca o som de dano aos inimigos
                    int enemiesCleared = enemies.size;
                    enemies.clear();
                    score += enemiesCleared;
                }
                if(specialStage == 4){
                    // System.out.println("4"); // Debug
                    --power;
                    specialStage = 5;
                }else{
                    return;
                }
            }else if(TimeUtils.timeSinceMillis(specialStartTime) < 800){
	        if(specialStage == 7){
                    // System.out.println("7"); // Debug
                    batch.begin();
                    batch.draw(specialMiddleImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                    batch.end();
                    specialStage = 8;
	        }
                if(specialStage == 6){
                    // System.out.println("6"); // Debug
                    specialStage = 7;
                }else{
	            return;
                }
            }else if(TimeUtils.timeSinceMillis(specialStartTime) < 1000){
                if(specialStage == 9){
                    // System.out.println("9"); // Debug
                    batch.begin();
                    batch.draw(specialStartImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                    batch.end();
                    specialStage = 10;
	        }
                if(specialStage == 8){
                    // System.out.println("8"); // Debug
                    specialStage = 9;
                }else{
	            return;
                }
            }else{
                // Reinicializa a variável para a próxima vez que a tecla "X" for pressionada
                // System.out.println("10"); // Debug
	        specialStartTime = 0;
                specialStage = 1;
	        open = true;
            }
        }else{
            this.moveMissile();
            this.moveEnemies();
        }

        ScreenUtils.clear(1, 0, 0, 1);
        batch.begin();
        batch.draw(img, 0, 0);

        // Especial
        if (open && Gdx.input.isKeyPressed(Input.Keys.X) && power > 0 && !xPressed && !paused && !gameover) {
            xPressed = true;

            // Clareia
            specialStartTime = TimeUtils.millis(); // Inicia o temporizador
            specialSound.play();
            open = false;

        // Reseta a variável quando a tecla é solta
        }else if(open && !Gdx.input.isKeyPressed(Input.Keys.X)){
            xPressed = false;
        }

        // Desenha os elementos do jogo
        if (!gameover && !paused) {

            if (attack) {
                batch.draw(missile, xMissile, yMissile);
            }
            batch.draw(nave, posX, posY);

            for (Rectangle enemy : enemies) {
                batch.draw(tEnemy, enemy.x, enemy.y);
            }
            bitmap.draw(batch, "Score: " + score, 20, Gdx.graphics.getHeight() - 20);
            bitmap.draw(batch, "Power: " + power, Gdx.graphics.getWidth() - 150, Gdx.graphics.getHeight() - 20);
        }else if (paused){
            bitmap.draw(batch, "PAUSE", Gdx.graphics.getWidth() / 2 - 50, Gdx.graphics.getHeight() / 2);
        }else{
            bitmap.draw(batch, "Score: " + score, 20, Gdx.graphics.getHeight() - 20);
            bitmap.draw(batch, "GAME OVER", Gdx.graphics.getWidth() - 150, Gdx.graphics.getHeight() - 20);
        }
        batch.end();
    }

    //
    @Override
    public void dispose() {
        batch.dispose();
        img.dispose();
        tNave.dispose();
        tMissile.dispose();
        tEnemy.dispose();
        backgroundMusic.dispose();
        shotSound.dispose();
        damageSound.dispose();
        specialSound.dispose();
        generator.dispose();
        specialStartImage.dispose();
        specialMiddleImage.dispose();
        specialEndImage.dispose();
        // transparentImage.dispose();

        }

    // Chamado para reiniciar jogo após gameover
    private void restartGame() {
        gameover = false;
        score = 0;
        power = 3;
        posX = 0;
        posY = (Gdx.graphics.getHeight() - nave.getHeight()) / 2;
        enemies.clear();
    }

    // Lida com o movimento da nave
    private void moveNave(){
        float diagonalVelocity = velocity * Gdx.graphics.getDeltaTime() / 1.4f;

        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            if( posX < Gdx.graphics.getWidth() - nave.getWidth() ){
                posX += velocity;
            }
        }
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            if( posX > 0 ){
            posX -= velocity;
            }
        }
        if(Gdx.input.isKeyPressed(Input.Keys.UP)){
            if( posY < Gdx.graphics.getHeight() - nave.getHeight() ){
                posY += velocity;
            }
        }
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
            if( posY > 0 ){
                posY -= velocity;
            }
        }
    }

    // Lida com o movimento do missil
    private void moveMissile(){
        if(Gdx.input.isKeyPressed(Input.Keys.Z) && !attack && !truncate) {
            attack = true;
            shotSound.play(); // Toca o som do tiro
            yMissile = posY + nave.getHeight() / 2 - 12;
            xMissile = posX + nave.getWidth() / 2;
        }else if(truncate && !Gdx.input.isKeyPressed(Input.Keys.Z)){
            truncate = false;
        }
        if(attack){
            xMissile += 40 * Gdx.graphics.getDeltaTime();

            // Verifica se o míssil atingiu o limite da tela
            if(xMissile < Gdx.graphics.getWidth()){
                xMissile += 40;
            }else{
                attack = false;
            }
        }
        this.moveNave();
    }

    // Lida com o spawn dos inimigos
    private void spawnEnemies() {
        int baseSpawnIntervalMillis = 1000;
        int spawn = random.nextInt(800 - 0 + 1) + 0;
        int adjustedSpawnIntervalMillis = baseSpawnIntervalMillis - spawn;

        if (TimeUtils.timeSinceMillis(lastEnemyTime) > adjustedSpawnIntervalMillis) {
            Rectangle enemy = new Rectangle(
                Gdx.graphics.getWidth(),
                MathUtils.random(0, Gdx.graphics.getHeight() - tEnemy.getHeight()),
                tEnemy.getWidth(),
                tEnemy.getHeight()
            );
            enemies.add(enemy);
            lastEnemyTime = TimeUtils.millis();
        }
    }

    // Lida com o movimento dos inimigos
    private void moveEnemies(){
        if (TimeUtils.nanoTime() - lastEnemyTime > numEnemies) {
            this.spawnEnemies();
        }

        for (Iterator<Rectangle> iter = enemies.iterator(); iter.hasNext();) {
            Rectangle enemy = iter.next();
            enemy.x -= (200 + score) * Gdx.graphics.getDeltaTime();

            // Colisão com o míssil
            if (collide(enemy.x, enemy.y, enemy.width, enemy.height, xMissile, yMissile, missile.getWidth(), missile.getHeight()) && attack) {
                // Inimigo atingido, faça o que for necessário (pontuação, remoção do inimigo, etc.)
                score++;
                iter.remove();
                attack = false;
                damageSound.play(); // Toca o som de morte do inimigo

            // Colisão com a nave
            }else if(collide(enemy.x, enemy.y, enemy.width, enemy.height, posX, posY, nave.getWidth(), nave.getHeight()) && !gameover) {
                --power;
                damageSound.play(); // Toca o som de dano aos jogadores
                iter.remove();

            // Da dano ao chegar no fim do trajeto
            }else if(enemy.x + tEnemy.getWidth() < 75){
                --power;
                damageSound.play(); // Toca o som de morte do inimigo
                iter.remove();
            }

            if (power < 0) {
                gameover = true;
            }
        }
    }

    // Checagem das bordas
    private boolean collide(float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2){
        if( x1 + w1 > x2 && x1 < x2 + w2 && y1 + h1 > y2 && y1 < y2 + h2 ){
            return true;
        }
        return false;
    }
}

