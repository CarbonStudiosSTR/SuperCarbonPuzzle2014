package view.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import enums.Controls;
import textures.TextureHolder;
import view.buttons.BasicButton;
import view.buttons.SoundButton;

public class MainMenuView extends PanelView {

    private BasicButton playButton;
    private BasicButton quitButton;
    private SoundButton soundButton;
    private Image logo;
    private Controls control;

    public MainMenuView(OrthographicCamera camera, BitmapFont buttonFont) {
        super(camera, buttonFont);

        this.logo = new Image(new Texture("menus/logo.png"));
        logo.setSize(camera.viewportWidth * 9 / 10, camera.viewportWidth * 9 * (logo.getHeight() / logo.getWidth()) / 10);
        logo.setPosition(camera.viewportWidth * 1 / 20, camera.viewportHeight - (camera.viewportWidth * 7 / 10));

        playButton = new BasicButton(TextureHolder.buttonsTexture, "Play", camera.viewportWidth / 4, camera.viewportHeight - ((camera.viewportWidth / 5) * 5), buttonFont, camera);
        quitButton = new BasicButton(TextureHolder.buttonsTexture, "Quit", camera.viewportWidth / 4, camera.viewportHeight - ((camera.viewportWidth / 5) * 6), buttonFont, camera);
        playButton.setButtonWorld(1);
        quitButton.setButtonWorld(1);

        soundButton = new SoundButton(new Texture("menus/soundon.png"), new Texture("menus/soundoff.png"));
        soundButton.setSize(camera.viewportWidth / 9, camera.viewportWidth / 9);
        soundButton.setPosition(camera.viewportWidth - (2 * soundButton.getWidth()), camera.viewportHeight - ((camera.viewportWidth / 5) * 7));

        playButton.addListener(new ClickListener() {
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                control = Controls.PLAY;
                playButton.setDrawable(playButton.getTextureRegionDrawable());

            }
        });

        quitButton.addListener(new ClickListener() {
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);

                Gdx.app.exit();

            }
        });


    }

    public void prepareMainMenu(Stage stage, Image background) {

        stage.clear();
        control = Controls.NONE;
        stage.addActor(playButton);
        stage.addActor(quitButton);
        stage.addActor(soundButton);
        this.background = background;

    }

    public Controls drawMainMenu(SpriteBatch batch) {

        batch.begin();
        background.draw(batch, 1);
        logo.draw(batch, 1);
        playButton.draw(batch, 1, buttonFont);
        quitButton.draw(batch, 1, buttonFont);
        soundButton.draw(batch, 1);

        batch.end();
        return this.control;

    }

    public Controls getControl() {
        return control;
    }
}
