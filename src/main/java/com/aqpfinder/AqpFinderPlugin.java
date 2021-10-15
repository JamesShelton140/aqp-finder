package com.aqpfinder;

import com.google.inject.Provides;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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

	private final Map<Character, Integer> characterSizeMap = createMap();
	private static Map<Character, Integer> createMap()
	{
		Map<Character, Integer> result = new HashMap<>();
//			upper case
				result.put('A',6);
				result.put('B',5);
				result.put('C',5);
				result.put('D',5);
				result.put('E',4);
				result.put('F',4);
				result.put('G',6);
				result.put('H',5);
				result.put('I',1);
				result.put('J',5);
				result.put('K',5);
				result.put('L',4);
				result.put('M',7);
				result.put('N',6);
				result.put('O',6);
				result.put('P',5);
				result.put('Q',6);
				result.put('R',5);
				result.put('S',5);
				result.put('T',3);
				result.put('U',6);
				result.put('V',5);
				result.put('W',7);
				result.put('X',5);
				result.put('Y',5);
				result.put('Z',5);
//			lower case
				result.put('a',5);
				result.put('b',5);
				result.put('c',4);
				result.put('d',5);
				result.put('e',5);
				result.put('f',4);
				result.put('g',5);
				result.put('h',5);
				result.put('i',1);
				result.put('j',4);
				result.put('k',4);
				result.put('l',1);
				result.put('m',7);
				result.put('n',5);
				result.put('o',5);
				result.put('p',5);
				result.put('q',5);
				result.put('r',3);
				result.put('s',5);
				result.put('t',3);
				result.put('u',5);
				result.put('v',5);
				result.put('w',5);
				result.put('x',5);
				result.put('y',5);
				result.put('z',5);
//			numbers
				result.put('0',6);
				result.put('1',4);
				result.put('2',6);
				result.put('3',5);
				result.put('4',5);
				result.put('5',5);
				result.put('6',6);
				result.put('7',5);
				result.put('8',6);
				result.put('9',6);
//			symbols
				result.put(' ',1);
				result.put(':',1);
				result.put(';',2);
				result.put('"',3);
				result.put('@',11);
				result.put('!',1);
				result.put('.',1);
				result.put('\'',2);
				result.put(',',2);
				result.put('(',2);
				result.put(')',2);
				result.put('+',5);
				result.put('-',4);
				result.put('=',6);
				result.put('?',6);
				result.put('*',7);
				result.put('/',4);
				result.put('$',6);
				result.put('£',8);
				result.put('^',6);
				result.put('{',3);
				result.put('}',3);
				result.put('[',3);
				result.put(']',3);
				result.put('&',9);
				return Collections.unmodifiableMap(result);
	}

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
