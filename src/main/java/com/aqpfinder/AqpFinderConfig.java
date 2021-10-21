package com.aqpfinder;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("aqpfinder")
public interface AqpFinderConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "recommendCharacters",
		name = "Character recommendations",
		description = "Replaces pixel count with recommended characters before W. Prioritises spaces."
	)
	default boolean recommendCharacters()
	{
		return true;
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
}
