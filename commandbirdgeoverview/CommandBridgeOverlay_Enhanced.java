package net.runelite.client.plugins.microbot.commandbridge;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class CommandBridgeOverlay extends OverlayPanel {

    private final CommandBridgePlugin plugin;

    @Inject
    public CommandBridgeOverlay(CommandBridgePlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            CommandBridgeScript script = plugin.getScript();

            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Command Bridge v" + CommandBridgeScript.version)
                    .color(Color.CYAN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Status:")
                    .right(script.getLastStatus())
                    .rightColor(getStatusColor(script.getLastStatus()))
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Connection:")
                    .right(script.getConnectionStatus())
                    .rightColor(script.getConnectionStatus().equals("Connected") ? Color.GREEN : Color.RED)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Queue Size:")
                    .right(String.valueOf(script.getQueueSize()))
                    .rightColor(script.getQueueSize() > 0 ? Color.YELLOW : Color.WHITE)
                    .build());

            if (Microbot.getClient().getLocalPlayer() != null) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Player:")
                        .right(Microbot.getClient().getLocalPlayer().getName())
                        .rightColor(Color.WHITE)
                        .build());

                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Location:")
                        .right("(" + Microbot.getClient().getLocalPlayer().getWorldLocation().getX() + 
                               ", " + Microbot.getClient().getLocalPlayer().getWorldLocation().getY() + ")")
                        .rightColor(Color.LIGHT_GRAY)
                        .build());
            }

        } catch (Exception ex) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Error:")
                    .right("Overlay error")
                    .rightColor(Color.RED)
                    .build());
        }

        return super.render(graphics);
    }

    private Color getStatusColor(String status) {
        if (status == null) return Color.WHITE;

        String lowerStatus = status.toLowerCase();
        if (lowerStatus.contains("error") || lowerStatus.contains("failed")) {
            return Color.RED;
        } else if (lowerStatus.contains("executing") || lowerStatus.contains("walking") || lowerStatus.contains("banking")) {
            return Color.YELLOW;
        } else if (lowerStatus.contains("idle") || lowerStatus.contains("connected")) {
            return Color.GREEN;
        } else {
            return Color.WHITE;
        }
    }
}