package net.runelite.client.plugins.microbot.commandbridge;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ButtonComponent;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class CommandBridgeOverlay extends OverlayPanel {
    public final ButtonComponent myButton;
    private final CommandBridgePlugin plugin;
    private List<String> lastConnectedUsernames = new ArrayList<>();
    @Inject
    CommandBridgeOverlay(CommandBridgePlugin plugin)
    {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
        myButton = new ButtonComponent("Test");
        myButton.setPreferredSize(new Dimension(100, 30));
        myButton.setParentOverlay(this);
        myButton.setFont(FontManager.getRunescapeBoldFont());
        myButton.setOnClick(() -> Microbot.openPopUp("Microbot", String.format("S-1D:<br><br><col=ffffff>%s Popup</col>", "CommandBridge")));
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(250, 350));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Micro CommandBridge V1.0.0")
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Connected Clients:")
                    .build());

            for (String username : plugin.getLastConnectedUsernames()) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left(username)
                        .build());
            }

            panelComponent.getChildren().add(myButton);
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
