package com.aqpfinder;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import com.aqpfinder.config.RecommendationMode;

@ConfigGroup("aqpfinder")
public interface AqpFinderConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "recommendationMode",
		name = "Recommendations",
		description = "Configures the recommendations to be displayed in pixels or spaces (default)."
	)
	default RecommendationMode recommendationMode()
	{
		return RecommendationMode.SPACES;
	}

	@ConfigItem(
			position = 1,
			keyName = "showCumulative",
			name = "Cumulative lengths",
			description = "Distance shown to each \"q p\" is from message start."
	)
	default boolean showCumulative()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "notifyOnQP",
		name = "Notify on q p",
		description = "Notifies when a possible q p is found. Why would you use this?"
	)
	default boolean notifyOnQP()
	{
		return false;
	}

	@ConfigItem(
			position = 3,
			keyName = "hasIcon",
			name = "Local name icon",
			description = "Tick if your character name has an icon in chat (iron, pmod etc.)."
	)
	default boolean hasIcon()
	{
		return false;
	}

	@ConfigItem(
			position = 4,
			keyName = "showOverlay",
			name = "Use overlay",
			description = "Enables a dynamic overlay that displays recommendations when a q p is present."
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
			position = 5,
			keyName = "giveInlineHints",
			name = "Inline hints",
			description = "Hints are printed inline with the message containing \"q p\"."
	)
	default boolean giveInlineHints()
	{
		return false;
	}
}
