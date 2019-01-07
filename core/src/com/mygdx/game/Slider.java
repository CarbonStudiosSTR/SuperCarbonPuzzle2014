package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import enums.Controls;
import enums.GameStates;
import help.utils.Constants;
import map.Map;
import mapSystem.MapsInfo;
import player.Player;
import sound.ClickSound;
import sound.SlideSound;
import textures.TextureHolder;
import view.MapView;
import view.menus.*;

import java.util.ArrayList;

import static enums.GameStates.*;

public class Slider extends ApplicationAdapter {

    final String VERT =
            "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
                    "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" +
                    "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +

                    "uniform mat4 u_projTrans;\n" +
                    " \n" +
                    "varying vec4 vColor;\n" +
                    "varying vec2 vTexCoord;\n" +

                    "void main() {\n" +
                    "       vColor = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" +
                    "       vTexCoord = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +
                    "       gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
                    "}";
    final String FRAG =
            //GL ES specific stuff
            "#ifdef GL_ES\n" //
                    + "#define LOWP lowp\n" //
                    + "precision mediump float;\n" //
                    + "#else\n" //
                    + "#define LOWP \n" //
                    + "#endif\n" + //
                    "varying LOWP vec4 vColor;\n" +
                    "varying vec2 vTexCoord;\n" +
                    "uniform sampler2D u_texture;\n" +
                    "uniform float grayscale;\n" +
                    "void main() {\n" +
                    "       vec4 texColor = texture2D(u_texture, vTexCoord);\n" +
                    "       \n" +
                    "       float gray = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));\n" +
                    "       texColor.rgb = mix(vec3(gray), texColor.rgb, grayscale);\n" +
                    "       \n" +
                    "       gl_FragColor = texColor * vColor;\n" +
                    "}";
    ArrayList<Image> backgrounds;
    private IActivityRequestHandler myRequestHandler;
    private ShaderProgram shader;
    private OrthographicCamera camera;
    private Stage mainStage;
    private SpriteBatch mainBatch;
    private Player player;
    private MapsInfo mapsInfo;
    private int selectedLevel;
    private int selectedWorld;
    private GameStates gameState;
    private Map map;
    private MapView mapView;
    private WorldSelectionView worldSelectionView;
    private MainMenuView mainMenuView;
    private LevelSelectionView levelSelectionView;
    private AfterLevelView afterLevelView;
    private BitmapFont buttonFont;
    private boolean worldSelectWasMade;
    private boolean levelSelectWasMade;
    private boolean mapViewWasMade;
    private int splashState;
    private int adCounter;

    public Slider(IActivityRequestHandler myRequestHandler) {
        this.myRequestHandler = myRequestHandler;
    }

    @Override
    public void create() {


        mainBatch = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gameState = BEFORE_GAME;
        splashState = 0;
    }

    @Override
    public void render() {

        switch (gameState) {

            case BEFORE_GAME: {

                SplashScreen splashScreen = new SplashScreen();

                //-----------------------------------------------------------
                if (splashState == 0) {
                    splashScreen.draw(mainBatch, camera, splashState);
                    ClickSound.loadSound();
                    SlideSound.loadSound();
                    splashScreen.draw(mainBatch, camera, splashState);
                    TextureHolder.loadTextures();
                    worldSelectWasMade = false;
                    levelSelectWasMade = false;
                    mapViewWasMade = false;
                    adCounter = 0;
                    Constants.cheatMode = false;
                    Constants.cheatActivation = 0;
                    ShaderProgram.pedantic = false;
                }

                //-----------------------------------------------------------------------------
                if (splashState == 1) {
                    splashScreen.draw(mainBatch, camera, splashState);
                    shader = new ShaderProgram(VERT, FRAG);
                }
                //---------------------------------------------------------------------------
                if (splashState == 2) {

                    if (!shader.isCompiled()) {
                        System.err.println(shader.getLog());
                        System.exit(0);
                    }
                    if (shader.getLog().length() != 0)
                        System.out.println(shader.getLog());

                    splashScreen.draw(mainBatch, camera, splashState);

                    mainBatch.setShader(shader);
                    shader.begin();
                    shader.setUniformf("grayscale", 1f);
                    shader.end();

                    //keyboardController = new KeyboardController();
                    myRequestHandler.showAds(false);
                    map = new Map(1, 1);
                    Constants.spritesMovingSpeed = (int) (Constants.spritesSpeedFactor * camera.viewportWidth);
                    player = new Player();
                    mapsInfo = new MapsInfo();
                    selectedWorld = 1;
                    backgrounds = new ArrayList<Image>();
                    for (int i = 0; i < Constants.howManyWorlds; i++) {
                        backgrounds.add(new Image(new Texture("menus/background" + (i + 1) + ".png")));
                        backgrounds.get(i).setSize(camera.viewportWidth, camera.viewportHeight);
                    }
                }
                //--------------------------------------------------------------------
                if (splashState == 3) {
                    splashScreen.draw(mainBatch, camera, splashState);
                    FileHandle fontFile = Gdx.files.internal("menufont.ttf");
                    FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
                    FreeTypeFontGenerator.FreeTypeFontParameter freeTypeFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                    freeTypeFontParameter.size = (int) (camera.viewportWidth / 8);
                    buttonFont = generator.generateFont((int) (camera.viewportWidth / 8));
                    buttonFont.setColor(0, 0, 0, 1);
                    generator.dispose();

                    afterLevelView = new AfterLevelView(camera, buttonFont, mapsInfo, player);
                    mainMenuView = new MainMenuView(camera, buttonFont);
                    mainStage = new Stage();
                    mapView = new MapView(camera);

                    mainMenuView.prepareMainMenu(mainStage, backgrounds.get(selectedWorld - 1));
                    Gdx.input.setInputProcessor(mainStage);
                    gameState = MENU;
                }
                splashState++;
                break;
            }

            case MENU: {
                Controls control = mainMenuView.drawMainMenu(mainBatch);
                if (control == Controls.PLAY) {
                    if (!worldSelectWasMade) {
                        worldSelectionView = new WorldSelectionView(camera, buttonFont, mapsInfo, player);
                        worldSelectWasMade = true;
                    }

                    worldSelectionView.prepareWorldSelectionView(mainStage, player, mapsInfo, backgrounds.get(0), camera.viewportWidth);
                    gameState = WORLD_SELECT;
                }
                break;
            }

            case WORLD_SELECT: {

                int nowSelectedWorld = worldSelectionView.drawWorldSelection(mainBatch, camera, shader, mainStage);
                if (nowSelectedWorld > 0) {

                    if (!levelSelectWasMade) {
                        levelSelectionView = new LevelSelectionView(camera, player, buttonFont, mapsInfo);
                        levelSelectWasMade = true;
                    }

                    selectedWorld = nowSelectedWorld;
                    levelSelectionView.prepareLevelSelection(selectedWorld, mainStage, player, mapsInfo, backgrounds.get(selectedWorld - 1), camera.viewportWidth);
                    gameState = LEVEL_SELECT;

                } else if (nowSelectedWorld == -1) {
                    mainMenuView.prepareMainMenu(mainStage, backgrounds.get(0));
                    gameState = MENU;
                }

                if ((Gdx.input.isKeyPressed(Input.Keys.BACK))) {
                    while (Gdx.input.isKeyPressed(Input.Keys.BACK)) {

                    }
                    mainMenuView.prepareMainMenu(mainStage, backgrounds.get(0));
                    gameState = MENU;
                }
                break;
            }


            case LEVEL_SELECT: {
                int nowSelectedLevel = levelSelectionView.drawLevelSelection(camera, shader, mainBatch, mainStage);
                if (nowSelectedLevel > 0) {
                    selectedLevel = nowSelectedLevel;
                    map.loadMap(selectedWorld, selectedLevel);
                    mapView.prepareMapUI(camera, map, mainStage);
                    mapView.prepareMap(camera, map);
                    gameState = LEVEL;
                } else if (nowSelectedLevel == -1) {
                    worldSelectionView.prepareWorldSelectionView(mainStage, player, mapsInfo, backgrounds.get(0), camera.viewportWidth);
                    gameState = WORLD_SELECT;
                }

                if ((Gdx.input.isKeyPressed(Input.Keys.BACK))) {
                    while (Gdx.input.isKeyPressed(Input.Keys.BACK)) {

                    }
                    worldSelectionView.prepareWorldSelectionView(mainStage, player, mapsInfo, backgrounds.get(0), camera.viewportWidth);
                    gameState = WORLD_SELECT;
                }

                break;
            }


            case LEVEL: {

                myRequestHandler.showAds(true);

                if (map.checkForFinish()) {

                    player.update(map);
                    Gdx.input.setInputProcessor(mainStage);
                    afterLevelView.prepareAfterLevelView(mainStage, map, player, mapsInfo, backgrounds.get(selectedWorld - 1), camera);
                    gameState = AFTER_LEVEL;
                    player.savePlayer();
                    myRequestHandler.showAds(false);
                }

                Controls control = mapView.getControl();

                if (control == Controls.NONE) {
                    mapView.drawMap(map, camera, mainBatch);
                } else {
                    if (control == Controls.RESET) {
                        map.loadMap(selectedWorld, selectedLevel);
                        mapView.prepareMap(camera, map);
                        adCounter++;
                        checkIntAdd();

                    } else if (control == Controls.MENU) {
                        Gdx.input.setInputProcessor(mainStage);
                        levelSelectionView.prepareLevelSelection(selectedWorld, mainStage, player, mapsInfo, backgrounds.get(selectedWorld - 1), camera.viewportWidth);
                        myRequestHandler.showAds(false);
                        gameState = LEVEL_SELECT;
                        adCounter = adCounter + 5;
                        checkIntAdd();
                    } else {
                        map.makeMove(control);
                        mapView.checkForPortalMoves(map);
                        mapView.prepareAnimation(map);
                        gameState = LEVEL_ANIMATION;
                    }
                }

                if ((Gdx.input.isKeyPressed(Input.Keys.BACK))) {
                    while (Gdx.input.isKeyPressed(Input.Keys.BACK)) {

                    }
                    Gdx.input.setInputProcessor(mainStage);
                    levelSelectionView.prepareLevelSelection(selectedWorld, mainStage, player, mapsInfo, backgrounds.get(selectedWorld - 1), camera.viewportWidth);
                    myRequestHandler.showAds(false);
                    gameState = LEVEL_SELECT;

                    adCounter = adCounter + 5;
                    checkIntAdd();
                }
                break;
            }

            case LEVEL_ANIMATION: {
                if (mapView.drawAnimation(map, camera, mainBatch)) {
                    mapView.afterAnimation(camera);
                    gameState = LEVEL;
                }
                break;
            }

            case AFTER_LEVEL: {

                Controls controls = afterLevelView.drawAfterLevel(camera, shader, mainBatch, mainStage);
                if (controls == Controls.NEXT) {
                    selectedLevel++;
                    map.loadMap(selectedWorld, selectedLevel);
                    if (!mapViewWasMade) {
                        mapView = new MapView(camera);
                        mapViewWasMade = true;
                    }
                    mapView.prepareMapUI(camera, map, mainStage);
                    mapView.prepareMap(camera, map);
                    gameState = LEVEL;
                    adCounter = adCounter + 10;
                    checkIntAdd();
                } else if (controls == Controls.RESET) {
                    map.loadMap(selectedWorld, selectedLevel);
                    mapView.prepareMapUI(camera, map, mainStage);
                    mapView.prepareMap(camera, map);
                    gameState = LEVEL;
                    adCounter = adCounter + 5;
                    checkIntAdd();
                } else if (controls == Controls.MENU) {
                    levelSelectionView.prepareLevelSelection(selectedWorld, mainStage, player, mapsInfo, backgrounds.get(selectedWorld - 1), camera.viewportWidth);
                    gameState = LEVEL_SELECT;

                    adCounter = adCounter + 5;
                    checkIntAdd();
                }

                if ((Gdx.input.isKeyPressed(Input.Keys.BACK))) {
                    while (Gdx.input.isKeyPressed(Input.Keys.BACK)) {

                    }
                    levelSelectionView.prepareLevelSelection(selectedWorld, mainStage, player, mapsInfo, backgrounds.get(selectedWorld - 1), camera.viewportWidth);
                    gameState = LEVEL_SELECT;


                    adCounter = adCounter + 5;
                    checkIntAdd();
                }
                break;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.BACK)) {
            // Do something
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Constants.spritesMovingSpeed = (int) (Constants.spritesSpeedFactor * camera.viewportWidth);


        switch (gameState) {
            case MENU: {
                //    menuView.prepareMainMenu(camera);
                break;
            }
            case LEVEL_SELECT: {


                break;
            }
            case LEVEL: {
                mapView.prepareMap(camera, map);
                mapView.prepareMapUI(camera, map, mainStage);
                break;
            }
            case ALERT: {


                gameState = LEVEL_SELECT;
                break;
            }
        }
    }

    @Override
    public void pause() {

    }

    public void checkIntAdd() {
        if (adCounter >= Constants.addFrequency) {
            myRequestHandler.showIntAd();
            adCounter = 0;
        }
    }

}
