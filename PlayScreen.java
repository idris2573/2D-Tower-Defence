package com.idris.boxout.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.idris.boxout.BoxOut;
import com.idris.boxout.Scenes.Hud;
import com.idris.boxout.Scenes.SpriteLifeBar;
import com.idris.boxout.Scenes.WinScreen;
import com.idris.boxout.Sprites.Health.BoxLife;
import com.idris.boxout.Sprites.Player;
import com.idris.boxout.Backgrounds.B2WorldCreator;
import com.idris.boxout.Sprites.Pieces.Knight;
import com.idris.boxout.Sprites.Pieces.Pawn;
import com.idris.boxout.Scenes.Controller;
import com.idris.boxout.Tools.HelpText;
import com.idris.boxout.Tools.ScreenShake;
import com.idris.boxout.Tools.WorldContactListener;

import java.util.ArrayList;


public class PlayScreen implements Screen{ //implements screen class from badlogic

    //Reference to our Game, used to set Screens
    private BoxOut game;

    private boolean exp100;
    private int expPoints;
    private boolean expGain;

    private float oldSpaceBetween;
    public static float zoomAmount;
    private float zoomMultiplyer;
    private float positionMultiplyer;

    //basic playscreen variables
    public static OrthographicCamera gamecam; //creates new camera
    private Viewport gamePort; //creates a new viewport
    private Hud hud;
    private Controller controller;
    //private GameOver gameOver;
    private WinScreen winScreen;
    private ArrayList<SpriteLifeBar> spriteLifeBarList;
    private SpriteLifeBar spriteLifeBar;
    public static ScreenShake screenShake;

    //Box2d variables
    private World world;
    private Box2DDebugRenderer b2dr;//gaphical representation
    private B2WorldCreator creator;

    private HelpText printOut;

    //players
    private Player firstPlayer, secondPlayer;
    private BoxLife[] firstPlayerLife ;
    private BoxLife[] secondPlayerLife;

    public static ArrayList<Float> bodyXPosition;

    //sprites
    public static Pawn[] pawn;
    public static Knight[] enemyKnight;

    private ArrayList<Pawn> playerPawnsList;

    private int life;

    private ArrayList<Knight> playerKnightsList;

    private Pawn playerPawn;
    private Knight playerKnight;
    private Pawn testPlayerPawn;

    //test spawn enemy
    private ArrayList<Pawn> spawnPawnList;
    private Pawn spawnPawn;

    private int count;
    public static int getLevel;

    private int playerPawnCount = 0;
    private int playerKnightCount = 0;
    private int spawnPawnCount = 0;
    private int testPawnCount = 0;
    private int pawnCount = 0;
    private int enemyKnightCount = 0;

    public static int countPieces;
    public static int deadPieces;

    public static float mostLeft;
    public static float mostRight;


    private InputMultiplexer multiplexer;


    public PlayScreen(BoxOut game, int level){


        this.game = game;
        getLevel = level +1;    //level +1;

        expGain = true;
        expPoints = getLevel*100;

        bodyXPosition = new ArrayList<Float>();
        deadPieces = 0;

        firstPlayerLife = new BoxLife[game.playerStats.getPlayerBoxes()];
        secondPlayerLife  = new BoxLife[(getLevel / 3) + 1];

        //create cam used to follow mario through cam world
        gamecam = new OrthographicCamera();

        //create a FitViewport to maintain virtual aspect ratio despite screen size
        gamePort = new StretchViewport(BoxOut.V_WIDTH / BoxOut.PPM , BoxOut.V_HEIGHT / BoxOut.PPM, gamecam); // initializes the type of viewport and sets its width and height aswell as it to the camera

        //set gamecam position
        gamecam.position.set(gamePort.getWorldWidth()/2 , gamePort.getWorldHeight()/2,0);

        //shake screen
        screenShake = new ScreenShake();

        //create our Box2D world, setting no gravity in X, -10 gravity in Y, and allow bodies to sleep
        world = new World(new Vector2(0, -10), true);
        //allows for debug lines of our box2d world.
        b2dr = new Box2DDebugRenderer();

        creator = new B2WorldCreator(this); //this refers to the this method/constructor PlayScreen

        playerPawnsList  = new ArrayList<Pawn>();
        playerKnightsList  = new ArrayList<Knight>();
        spawnPawnList = new ArrayList<Pawn>();
        spriteLifeBarList = new ArrayList<SpriteLifeBar>();

        pawn = new Pawn[getLevel];
        enemyKnight = new Knight[getLevel/2];

        int displaceX = BoxOut.V_WIDTH ;
        int displaceY = 200;
        for (int i = 0; i < pawn.length; i++) {
            pawn[i] = new Pawn(this, displaceX, displaceY, 2, "pawn.jpg", 1000, 2, 500);
            displaceX-=10;
            displaceY+=10;

            spriteLifeBar = new SpriteLifeBar(game.batch, "pawn");
            spriteLifeBarList.add(spriteLifeBar);
            spriteLifeBarList.get(spriteLifeBarList.size()-1).healthBar(pawn[i].body.getPosition().x,pawn[i].body.getPosition().y,pawn[i].getHealth());
            pawnCount++;
        }

        for (int i = 0; i < enemyKnight.length; i++) {
            enemyKnight[i] = new Knight(this, displaceX, displaceY, 2, "button2.png", 3000, 2, 500);
            displaceX-=10;
            displaceY+=10;

            spriteLifeBar = new SpriteLifeBar(game.batch, "lol");
            spriteLifeBarList.add(spriteLifeBar);
            spriteLifeBarList.get(spriteLifeBarList.size()-1).healthBar(enemyKnight[i].body.getPosition().x, enemyKnight[i].body.getPosition().y, enemyKnight[i].getHealth());
            enemyKnightCount++;
        }

        testPlayerPawn = new Pawn(this, 0, 100, 1, "playerPawn.png", game.playerStats.getPawnHealth(), game.playerStats.getPawnSpeed(), game.playerStats.getPawnStrength());
        spriteLifeBar = new SpriteLifeBar(game.batch, "testPawn");
        spriteLifeBarList.add(spriteLifeBar);
        spriteLifeBarList.get(spriteLifeBarList.size()-1).healthBar(testPlayerPawn.body.getPosition().x,testPlayerPawn.body.getPosition().y,testPlayerPawn.getHealth());
        testPawnCount++;
        
        //life boxes for both players
        for(life = 0; life < firstPlayerLife.length; life++) {
            firstPlayerLife[life] = new BoxLife(this,1,20,game.playerStats.getPlayerHealth());
        }
        for(life = 0; life < secondPlayerLife.length; life++) {
            secondPlayerLife[life] = new BoxLife(this,2,(BoxOut.V_WIDTH -20),1000 + (getLevel*100));
        }


        int p1DropHeight = firstPlayerLife[0].boxHeight() + 50;
        int p2DropHeight = secondPlayerLife[0].boxHeight() + 50;

        //players
        firstPlayer = new Player(this,firstPlayerLife[0].boxWidth(),p1DropHeight,1);
        secondPlayer = new Player(this,secondPlayerLife[0].boxWidth(),p2DropHeight,2);

        //create our game HUD for scores/timers/level info
        hud = new Hud(game.batch);

        controller = new Controller(game.batch);

        world.setContactListener(new WorldContactListener());

        winScreen = new WinScreen(game.batch);

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(controller.stage);
        multiplexer.addProcessor((winScreen.stage));

        gamecam.zoom = 1.0f;
        //gamecam.position.y = BoxOut.V_HEIGHT;
        //Gdx.app.log("gamecam", String.valueOf(gamecam.position.x));
        //Gdx.app.log("Lifeposition", String.valueOf(spriteLifeBarList.get(1).viewport.getCamera().position.x));

    }

    public void update(float dt){
        //handle user input first
        handleInput(dt);
        getPieceCount();
        zoom();

        //takes 1 step in the physics simulation(60 times per second)
        world.step(1 / 60f, 6, 2);

        updatePlayerLife(dt);
        getHealthBars();
        updatePieces(dt);


        loseGame(dt);
        winGame(dt);

        getMidPosition(); //return


        for(int i = 0; i < spriteLifeBarList.size(); i++){
            spriteLifeBarList.get(i).update(dt);
        }


        try{hud.update(dt, playerPawnsList.get(0).helpString() + " | " + "lol ");}catch(Exception e){}
        controller.update(dt);
        gamecam.update();

    }

    @Override
    public void render(float delta) {
        //separate our update logic from render
        update(delta);

        //Clear the game screen with Black
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);



            playersAlive();



            try { if (firstPlayer.isPlayerDead()) {
                game.batch.setProjectionMatrix(winScreen.stage.getCamera().combined);
                winScreen.stage.draw(); }} catch (Exception e) {}

            try { if (secondPlayer.isPlayerDead()) {
                game.batch.setProjectionMatrix(winScreen.stage.getCamera().combined);
                winScreen.stage.draw(); }} catch (Exception e) {}


        //screenshake
        screenShake.update(delta, gamecam);
        game.batch.setProjectionMatrix(gamecam.combined);
        gamecam.update();

    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width,height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        //dispose of all our opened resources
        world.dispose();
        b2dr.dispose();
        hud.dispose();
        controller.dispose();
        for(int i = 0; i < spriteLifeBarList.size(); i++) {
            spriteLifeBarList.get(i).dispose();
        }
        try {
            winScreen.dispose();
        }catch(Exception e){}

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(multiplexer);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////

    public World getWorld(){
        return world;
    }

    public void drawPieces(){
        //draw firstPlayer
        firstPlayer.draw(game.batch);
        secondPlayer.draw(game.batch);

        for (int i = 0; i < playerKnightsList.size(); i++) {
            playerKnightsList.get(i).draw(game.batch);
        }

        //draws test Pawn
        for (int i = 0; i < pawn.length; i++) {
            pawn[i].draw(game.batch);
        }

        for (int i = 0; i < enemyKnight.length; i++) {
            enemyKnight[i].draw(game.batch);
        }

        try {
            testPlayerPawn.draw(game.batch);
        } catch (Exception e) {
        }

        for (life = 0; life < firstPlayerLife.length; life++) {
            firstPlayerLife[life].draw(game.batch);
        }
        for (life = 0; life < secondPlayerLife.length; life++) {
            secondPlayerLife[life].draw(game.batch);
        }



        for (int i = 0; i < playerPawnsList.size(); i++) {
            playerPawnsList.get(i).draw(game.batch);
        }


        for (int i = 0; i < spawnPawnList.size(); i++) {
            spawnPawnList.get(i).draw(game.batch);
        }
    }

    public void playersAlive(){

        //if(!firstPlayer.isPlayerDead() && !secondPlayer.isPlayerDead()) { //return

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(gamecam.combined);

        //this is where you draw objects into the game
        game.batch.begin();

        //draw background
        creator.draw(game.batch);

        drawPieces();

        game.batch.end(); //drawing objects ends here

        //Set our batch to now draw what the Hud camera sees.
        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();
        //controller hud
        game.batch.setProjectionMatrix(controller.stage.getCamera().combined);
        controller.stage.draw();

        //render our Box2dDebugLines
        b2dr.render(world, gamecam.combined);

        for (int i = 0; i < spriteLifeBarList.size(); i++) {
            try { game.batch.setProjectionMatrix(spriteLifeBarList.get(i).stage.getCamera().combined);
                spriteLifeBarList.get(i).stage.draw(); } catch (Exception e) {}}

        //} //return
    }

    public void winGame(float dt){
        //winning
        try{
            if(secondPlayer.isPlayerDead()) {

                BoxOut.missionCompleted[getLevel] = true;
                BoxOut.missionUnlocked[getLevel] = true;
                gainExperience();

                lvlUp();
                winScreen.win = true;
                winScreen.update(dt);
                multiplexer.removeProcessor(controller.stage);
            }
        }catch(Exception e){}
    }

    public void loseGame(float dt){
        try{
            if(firstPlayer.isPlayerDead()) {
                winScreen.update(dt);
                winScreen.win = true;
                multiplexer.removeProcessor(controller.stage);
            }
        }catch(Exception e){}
    }

    public void lvlUp(){
        if(exp100) {
            game.playerStats.playerLevel += 1;

            game.playerStats.pawnHealth += 200;
            game.playerStats.pawnSpeed += 20;



            //Gdx.app.log("LVL", String.valueOf(game.playerStats.getPlayerLevel()));
            exp100 = false;
            winScreen.lvlUp = true;
        }
    }

    public int gainExperience(){

        if(expGain) {
            game.exp.addExperiencePoints(expPoints);
            //Gdx.app.log("Exp", String.valueOf(game.exp.getExperiencePoints()));
            game.gainedExp = game.exp.getExperiencePoints();
            expGain = false;
        }

        if(game.exp.getExperiencePoints() >= game.exp.expLevel[game.playerStats.getPlayerLevel()]){
            //Gdx.app.log("Exp","Leveled Up");
            double remainingExp = game.exp.getExperiencePoints() - game.exp.expLevel[game.playerStats.getPlayerLevel()];
            game.exp.experiencePoints = 0 + remainingExp;

            exp100 = true;

        }

        return expPoints;
    }

    public void getHealthBars(){

        float healthBarHeight = 61 / BoxOut.PPM;

        for(int i = 0; i < spriteLifeBarList.size(); i++){
            if(spriteLifeBarList.get(i).getPiece.equals("pawn")) {
                spriteLifeBarList.get(i).healthBar(pawn[i].body.getPosition().x, pawn[i].body.getPosition().y, pawn[i].getHealth());

            }

            if(spriteLifeBarList.get(i).getPiece.equals("enemyKnight")) {
                spriteLifeBarList.get(i).healthBar(enemyKnight[i].body.getPosition().x, enemyKnight[i].body.getPosition().y, enemyKnight[i].getHealth());

            }


            if(spriteLifeBarList.get(i).getPiece.equals("testPawn")) {
                spriteLifeBarList.get(i).healthBar(testPlayerPawn.body.getPosition().x, testPlayerPawn.body.getPosition().y, testPlayerPawn.getHealth());
            }

            for(int j = 0; j < playerPawnsList.size(); j++){
                if(spriteLifeBarList.get(i).getPiece.equals("playerPawn" + j)) {
                    spriteLifeBarList.get(i).healthBar(playerPawnsList.get(j).body.getPosition().x, playerPawnsList.get(j).body.getPosition().y, playerPawnsList.get(j).getHealth());

                }
            }

            for(int j = 0; j < playerKnightsList.size(); j++){
                if(spriteLifeBarList.get(i).getPiece.equals("playerKnight" + j)) {
                    spriteLifeBarList.get(i).healthBar(playerKnightsList.get(j).body.getPosition().x,healthBarHeight, playerKnightsList.get(j).getHealth());
               //playerKnightsList.get(j).body.getPosition().y -0.04f
                }
            }

            for(int j = 0; j < spawnPawnList.size(); j++){
                if(spriteLifeBarList.get(i).getPiece.equals("spawnPawn" + j)) {
                    spriteLifeBarList.get(i).healthBar(spawnPawnList.get(j).body.getPosition().x, spawnPawnList.get(j).body.getPosition().y, spawnPawnList.get(j).getHealth());
                }
            }


        }
    }

    public void updatePieces(float dt){
        bodyXPosition.clear();
        for (int i = 0; i < pawn.length; i++) {
            pawn[i].update(dt); //updating the Pawn
            if(pawn[i].setToDestroy){ deadPieces++; pawn[i].setToDestroy = false;}
            if(!pawn[i].isDestroyed){bodyXPosition.add(pawn[i].body.getPosition().x);}
        }

        for (int i = 0; i < enemyKnight.length; i++) {
            enemyKnight[i].update(dt); //updating the Pawn
            if(enemyKnight[i].setToDestroy){ deadPieces++; enemyKnight[i].setToDestroy = false;}
            if(!enemyKnight[i].isDestroyed){bodyXPosition.add(enemyKnight[i].body.getPosition().x);}
        }

        for(int i = 0; i < spawnPawnList.size(); i++){
            spawnPawnList.get(i).update(dt);
            if(spawnPawnList.get(i).setToDestroy){ deadPieces++; spawnPawnList.get(i).setToDestroy = false;}
            if(!spawnPawnList.get(i).isDestroyed){bodyXPosition.add(spawnPawnList.get(i).body.getPosition().x);}
        }



        for(int i = 0; i < playerPawnsList.size(); i++){
            playerPawnsList.get(i).update(dt);
            if(playerPawnsList.get(i).setToDestroy){ deadPieces++; playerPawnsList.get(i).setToDestroy = false;}
            if(!playerPawnsList.get(i).isDestroyed){bodyXPosition.add(playerPawnsList.get(i).body.getPosition().x);}
        }

        for(int i = 0; i < playerKnightsList.size(); i++){
            playerKnightsList.get(i).update(dt);
            if( playerKnightsList.get(i).setToDestroy){ deadPieces++; playerKnightsList.get(i).setToDestroy = false;}
            if(!playerKnightsList.get(i).isDestroyed){bodyXPosition.add(playerKnightsList.get(i).body.getPosition().x);}
        }

        try{testPlayerPawn.update(dt);}catch(Exception e){}
        if(testPlayerPawn.setToDestroy){ deadPieces++; testPlayerPawn.setToDestroy = false;}
        if( !testPlayerPawn.isDestroyed){bodyXPosition.add(testPlayerPawn.body.getPosition().x);}
    }

    public void handleInput(float dt){
        //control our firstPlayer using immediate impulses


        if(controller.isPlayerPawnPressed()) {

            playerPawn = new Pawn(this, 100, 100, 1, "playerPawn.png", game.playerStats.getPawnHealth(), game.playerStats.getPawnSpeed(), game.playerStats.getPawnStrength());
            playerPawnsList.add(playerPawn);

            spriteLifeBar = new SpriteLifeBar(game.batch, "playerPawn" + String.valueOf(playerPawnCount)); //Gdx.app.log("PlayerPawn",  "playerPawn" + String.valueOf(playerPawnCount));
            spriteLifeBarList.add(spriteLifeBar);
            spriteLifeBarList.get(spriteLifeBarList.size()-1).healthBar(playerPawn.body.getPosition().x,playerPawn.body.getPosition().y,playerPawn.getHealth());
            playerPawnCount++;

            try{Thread.sleep(100);}catch(Exception e){}
        }

        if(controller.isPlayerKnightPressed()) {
            playerKnight = new Knight(this, 100, 100, 1, "knight.png", game.playerStats.getKnightHealth(), game.playerStats.getKnightSpeed(), game.playerStats.getKnightStrength());
            playerKnightsList.add(playerKnight);

            spriteLifeBar = new SpriteLifeBar(game.batch, "playerKnight" + String.valueOf(playerKnightCount));
            spriteLifeBarList.add(spriteLifeBar);
            spriteLifeBarList.get(spriteLifeBarList.size()-1).healthBar(playerKnight.body.getPosition().x,playerKnight.body.getPosition().y,playerKnight.getHealth());
            playerKnightCount++;

            try{Thread.sleep(100);}catch(Exception e){}
        }

        if(controller.isPawnPressed()) {
            spawnPawn = new Pawn(this, BoxOut.V_WIDTH -100, 100,2, "pawn.jpg", 1000, 2, 500);
            spawnPawnList.add(spawnPawn);

            spriteLifeBar = new SpriteLifeBar(game.batch, "spawnPawn" + String.valueOf(spawnPawnCount));
            spriteLifeBarList.add(spriteLifeBar);
            spriteLifeBarList.get(spriteLifeBarList.size()-1).healthBar(spawnPawn.body.getPosition().x,spawnPawn.body.getPosition().y,spawnPawn.getHealth());
            spawnPawnCount++;

            try{Thread.sleep(100);}catch(Exception e){}
        }

        if(controller.isRetryPressed()) {
            game.setScreen(new PlayScreen(game,getLevel -1));

        }

        if(controller.isBackPressed()) {
            game.setScreen(new LevelSelect(game));
        }

        //winscreen
        if(winScreen.isRetryButtonPressed()) {
            game.setScreen(new PlayScreen(game,getLevel -1));
            winScreen.dispose();
        }

        if(winScreen.isLevelSelectPressed()) {
            game.setScreen(new LevelSelect(game));
        }

        if(winScreen.isHomeButtonPressed()) {
            game.setScreen(new MenuScreen(game));
        }

    }

    public void updatePlayerLife(float dt){
        firstPlayer.update(dt);
        secondPlayer.update(dt);

        for(life = 0; life < firstPlayerLife.length; life++) {
            firstPlayerLife[life].update(dt);
        }

        for(life = 0; life < secondPlayerLife.length; life++) {
            secondPlayerLife[life].update(dt);
        }
    }

    public int getPieceCount(){
        countPieces = playerPawnCount + playerKnightCount + spawnPawnCount + testPawnCount + pawnCount + enemyKnightCount - deadPieces;
        return  countPieces;
    }

    public void getMidPosition(){
        mostLeft = BoxOut.V_WIDTH / BoxOut.PPM;
        mostRight = 0;

        for (int i = 0; i < bodyXPosition.size(); i++){
            if(bodyXPosition.get(i) < mostLeft) {
                mostLeft = bodyXPosition.get(i);
            }
            if(bodyXPosition.get(i) > mostRight) {
                mostRight = bodyXPosition.get(i);
            }
        }
            float midPoint = (mostLeft + mostRight)/2;


        if (gamecam.position.x < midPoint){
            gamecam.position.x += 0.01 +positionMultiplyer;

        }
        if (gamecam.position.x > midPoint){
            gamecam.position.x -= 0.01 +positionMultiplyer;

        }

        //Gdx.app.log("positionMultiplyer", String.valueOf(positionMultiplyer));
//        Gdx.app.log("position x", String.valueOf(gamecam.position.x));
//        Gdx.app.log("midPont", String.valueOf(midPoint));

    }

    public void zoom(){
        float spaceBetween = mostRight - mostLeft;
        zoomAmount = 0.004f *zoomMultiplyer;


//        if(spaceBetween == oldSpaceBetween){
//            zoomMultiplyer = 0.001f ;
//            Gdx.app.log("Reset", String.valueOf(zoomMultiplyer) );
//        }

        if (spaceBetween < oldSpaceBetween && spaceBetween > 20 / BoxOut.PPM){

            gamecam.zoom -= zoomAmount;
            gamecam.position.y -= zoomAmount;
            zoomMultiplyer += 0.1f;
        }

        if (spaceBetween > oldSpaceBetween && spaceBetween > 65 / BoxOut.PPM){

            gamecam.zoom += zoomAmount;
            gamecam.position.y += zoomAmount;
            zoomMultiplyer += 0.1f;
        }


//------------------------------------------------------------

        if(gamecam.zoom <= 0.6f){
            gamecam.zoom = 0.6f;
            zoomMultiplyer = 0.001f ;
            //Gdx.app.log("Reset", String.valueOf(zoomMultiplyer) );
        }

        if (gamecam.position.y <= 75/BoxOut.PPM ){
            gamecam.position.y = 75/BoxOut.PPM;
            zoomMultiplyer = 0.001f ;
            //Gdx.app.log("Reset", String.valueOf(zoomMultiplyer) );
        }

        if (gamecam.zoom >= 1.0f || spaceBetween > 250 / BoxOut.PPM) {
            gamecam.zoom = 1.0f;
            zoomMultiplyer = 0.001f ;
            //Gdx.app.log("Reset", String.valueOf(zoomMultiplyer) );
        }

        if (gamecam.position.y >= 1.04f || spaceBetween > 250 / BoxOut.PPM){
            gamecam.position.y = 1.04f;
            zoomMultiplyer = 0.001f ;
            //Gdx.app.log("Reset", String.valueOf(zoomMultiplyer) );
        }






        //Gdx.app.log("Space between", String.valueOf(spaceBetween) );
        //Gdx.app.log("Old Space", String.valueOf(oldSpaceBetween) );
        oldSpaceBetween = spaceBetween;

       // Gdx.app.log("Zoom Multipyer", String.valueOf(zoomMultiplyer) );

    }

}
