package com.aqpfinder;

import com.google.inject.Provides;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MessageNode;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.Notifier;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Aqp Finder"
)
public class AqpFinderPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private AqpFinderConfig config;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private Notifier notifier;

	private final Map<Character, Integer> characterSizeMap = Map.ofEntries(
//			upper case
			Map.entry('A',6),
			Map.entry('B',5),
			Map.entry('C',5),
			Map.entry('D',5),
			Map.entry('E',4),
			Map.entry('F',4),
			Map.entry('G',6),
			Map.entry('H',5),
			Map.entry('I',1),
			Map.entry('J',5),
			Map.entry('K',5),
			Map.entry('L',4),
			Map.entry('M',7),
			Map.entry('N',6),
			Map.entry('O',6),
			Map.entry('P',5),
			Map.entry('Q',6),
			Map.entry('R',5),
			Map.entry('S',5),
			Map.entry('T',3),
			Map.entry('U',6),
			Map.entry('V',5),
			Map.entry('W',7),
			Map.entry('X',5),
			Map.entry('Y',5),
			Map.entry('Z',5),
//			lower case
			Map.entry('a',5),
			Map.entry('b',5),
			Map.entry('c',4),
			Map.entry('d',5),
			Map.entry('e',5),
			Map.entry('f',4),
			Map.entry('g',5),
			Map.entry('h',5),
			Map.entry('i',1),
			Map.entry('j',4),
			Map.entry('k',4),
			Map.entry('l',1),
			Map.entry('m',7),
			Map.entry('n',5),
			Map.entry('o',5),
			Map.entry('p',5),
			Map.entry('q',5),
			Map.entry('r',3),
			Map.entry('s',5),
			Map.entry('t',3),
			Map.entry('u',5),
			Map.entry('v',5),
			Map.entry('w',5),
			Map.entry('x',5),
			Map.entry('y',5),
			Map.entry('z',5),
//			numbers
			Map.entry('0',6),
			Map.entry('1',4),
			Map.entry('2',6),
			Map.entry('3',5),
			Map.entry('4',5),
			Map.entry('5',5),
			Map.entry('6',6),
			Map.entry('7',5),
			Map.entry('8',6),
			Map.entry('9',6),
//			symbols
			Map.entry(' ',1),
			Map.entry(':',1),
			Map.entry(';',2),
			Map.entry('"',3),
			Map.entry('@',11),
			Map.entry('!',1),
			Map.entry('.',1),
			Map.entry('\'',2),
			Map.entry(',',2),
			Map.entry('(',2),
			Map.entry(')',2),
			Map.entry('+',5),
			Map.entry('-',4),
			Map.entry('=',6),
			Map.entry('?',6),
			Map.entry('*',7),
			Map.entry('/',4),
			Map.entry('$',6),
			Map.entry('£',8),
			Map.entry('^',6),
			Map.entry('{',3),
			Map.entry('}',3),
			Map.entry('[',3),
			Map.entry(']',3),
			Map.entry('&',9)
			);

	@Override
	protected void startUp() throws Exception
	{
//		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
//		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		MessageNode messageNode = chatMessage.getMessageNode();
		String message = messageNode.getValue();
		boolean update = false;

		if(message.contains("q p"))
		{
			String[] messageSegments = message.split("q p");

			List<Integer> segmentLengths = Arrays.stream(messageSegments).map(chatMsg -> getChatLength(chatMsg) + 4).collect(Collectors.toList());
			segmentLengths.set(0, segmentLengths.get(0) + getChatLength(messageNode.getName()) - getChatLength(client.getLocalPlayer().getName()));
			for(int i = 1; i < segmentLengths.size(); i++)
			{
				segmentLengths.set(i, segmentLengths.get(i) + 4);
			}

			if(config.recommendCharacters())
			{
				message += "   " + segmentLengths.stream().map(this::charactersToW).collect(Collectors.toList());
			}
			else
			{
				message += "   " + segmentLengths;
			}

			messageNode.setValue(message);
			update = true;

			if(config.notifyOnQP())
			{
				notifier.notify("A q p opportunity!");
			}

		}



		if (update)
		{
			messageNode.setRuneLiteFormatMessage(messageNode.getValue());
			chatMessageManager.update(messageNode);
		}
	}

	private int getChatLength(String chatMsg)
	{
		if(chatMsg == null) {return 0;}

		try
		{
			return chatMsg.chars()
					.mapToObj(ch -> (char) ch)
					.map(key -> characterSizeMap.get(key) + 2)
					.reduce(0, (a, b) -> a + b);
		}
		catch (NullPointerException e)
		{
			return -5;
		}
	}

	private String charactersToW(int pixels)
	{
		String characters = "";

		if(pixels % 3 == 1)
		{
			pixels -= 4;
			characters += ", and ";
		}

		if(pixels % 3 == 2)
		{
			pixels -= 5;
			characters += "\" and ";
		}

		if(pixels < 0)
		{
			characters = "impossible";
		}
		else
		{
			characters += pixels/3 + " spaces";
		}

		return characters;
	}

	@Provides
	AqpFinderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AqpFinderConfig.class);
	}
}
