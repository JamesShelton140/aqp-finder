package com.aqpfinder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class AqpFinderOverlay extends OverlayPanel
{
    private final AqpFinderPlugin plugin;

    @Inject
    private AqpFinderOverlay(AqpFinderPlugin plugin)
    {
        this.plugin = plugin;
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPosition(OverlayPosition.BOTTOM_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.isLastMessageIncludesQP())
        {
            return null;
        }

        final FontMetrics fontMetrics = graphics.getFontMetrics();
        int panelWidth = Math.max(ComponentConstants.STANDARD_WIDTH, fontMetrics.stringWidth("A q p target sighted!") +
                ComponentConstants.STANDARD_BORDER + ComponentConstants.STANDARD_BORDER);

        panelComponent.setPreferredSize(new Dimension(panelWidth, 0));
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("A q p target sighted!")
                .build());

        List<String> overlayText = plugin.getOverlayText();
        List<Integer> overlayTextColour = plugin.getOverlayTextColour();

        for (int i = 0; i < overlayText.size(); i++)
        {
            String lineText = overlayText.get(i);
            Integer redColourValue = overlayTextColour.get(i);

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(lineText)
                    .leftColor(new Color(redColourValue, 255 - redColourValue, 0))
                    .build());
        }

        return super.render(graphics);
    }
}
