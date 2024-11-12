package ralf2oo2.carmod.gui;

import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;

public class CarSpawnerScreen extends Screen {
    private Screen parent;
    private TextFieldWidget carNameField;
    private ItemStack item;

    public CarSpawnerScreen(Screen parent, ItemStack item){
        this.parent = parent;
        this.item = item;
    }
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();
        this.drawCenteredTextWithShadow(this.textRenderer, "Car name", this.width / 2, this.height / 4 - 60 + 20, 16777215);
        this.carNameField.render();
        super.render(mouseX, mouseY, delta);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        this.carNameField.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button.active) {
            if (button.id == 1) {
                this.minecraft.setScreen(this.parent);
            } else if (button.id == 0) {
                item.getStationNbt().putString("carName", carNameField.getText());
                this.minecraft.setScreen(null);
            }

        }
    }

    @Override
    public void init() {
        TranslationStorage var1 = TranslationStorage.getInstance();
        Keyboard.enableRepeatEvents(true);
        this.buttons.clear();
        this.buttons.add(new ButtonWidget(0, this.width / 2 - 100, this.height / 4 + 96 + 12, "set"));
        this.buttons.add(new ButtonWidget(1, this.width / 2 - 100, this.height / 4 + 120 + 12, "cancel"));
        String var2 = item.getStationNbt().contains("carName") ? item.getStationNbt().getString("carName") : "";
        ((ButtonWidget)this.buttons.get(0)).active = var2.length() > 0;
        this.carNameField = new TextFieldWidget(this, this.textRenderer, this.width / 2 - 100, this.height / 4 - 10 + 50 + 18, 200, 20, var2);
        this.carNameField.focused = true;
        this.carNameField.setMaxLength(128);
    }

    @Override
    public void tick() {
        this.carNameField.tick();
    }

    @Override
    public void removed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyPressed(char character, int keyCode) {
        this.carNameField.keyPressed(character, keyCode);
        if (character == '\r') {
            this.buttonClicked((ButtonWidget)this.buttons.get(0));
        }

        ((ButtonWidget)this.buttons.get(0)).active = this.carNameField.getText().length() > 0;
    }
}
