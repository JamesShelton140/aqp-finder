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
		keyName = "notifyOnQP",
		name = "Notify on q p",
		description = "Notifies when a possible q p is found. Why would you use this?"
	)
	default boolean notifyOnQP()
	{
		return false;
	}
}
