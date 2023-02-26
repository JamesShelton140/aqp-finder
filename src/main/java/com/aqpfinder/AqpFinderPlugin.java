package com.aqpfinder;

import com.aqpfinder.config.RecommendationMode;
import com.google.inject.Provides;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.FriendsChatRank;
import net.runelite.api.MessageNode;
import net.runelite.api.VarClientStr;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.Notifier;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Aqp Finder"
)
public class AqpFinderPlugin extends Plugin implements KeyListener {
	@Inject
	private Client client;

	@Inject
	private AqpFinderConfig config;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private Notifier notifier;

	@Inject
	private KeyManager keyManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private AqpFinderOverlay overlay;

	private final String qp = "q p";
	private final int qpLength = 17;

	/**
	 * An immutable map of characters to size in pixels of that character in OSRS chatbox.
	 */
	private final Map<Character, Integer> characterSizeMap = createCharacterSizeMap();

	/**
	 * Creates an immutable map of characters to size in pixels of that character in OSRS chatbox.
	 * Sizes listed cover the visible character only. Each character is padded by 2 blank pixels when printed in chat.
	 *
	 * @return immutable map of (character, size) pairs.
	 */
	private static Map<Character, Integer> createCharacterSizeMap()
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
				result.put('#',11);
//				other
				result.put('\u00A0',1);//no-break space
				return Collections.unmodifiableMap(result);
	}

	/**
	 * An immutable list of characters that end a sentence in player chat.
	 */
	private final List<Character> endSentenceCharList = createEndSentenceCharList();

	/**
	 * Creates an immutable list of characters that end a sentence in player chat.
	 * A character is considered to end a sentence if an immediately following letter is formatted to upper case in chat.
	 *
	 * @return immutable list of end of sentence characters.
	 */
	private static List<Character> createEndSentenceCharList()
	{
		List<Character> result = new ArrayList<>();

		result.add('.');
		result.add('!');
		result.add('?');

		return Collections.unmodifiableList(result);
	}

	@Getter
	private boolean lastMessageIncludesQP = false;
	private boolean lastQPFromPM = false;
	private Integer[] lastMessageSegmentIndex = new Integer[0];
	private String chatBoxTypedText = "";
	private int chatBoxTypedTextLength = 0;
	@Getter
	private final List<String> overlayText = new ArrayList<>();
	@Getter
	private final List<Integer> overlayTextColour = new ArrayList<>();

	@Override
	protected void startUp() throws Exception
	{
		keyManager.registerKeyListener(this);
		if(config.showOverlay())
		{
			overlayManager.add(overlay);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		keyManager.unregisterKeyListener(this);
		if(config.showOverlay())
		{
			overlayManager.remove(overlay);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (event.getKey().equals("showOverlay"))
		{
			if (config.showOverlay())
			{
				overlayManager.add(overlay);
				if(lastMessageIncludesQP)
				{
					refreshChatBoxTypedText();
					updateOverlayText();
				}
			}
			else
			{
				overlayManager.remove(overlay);
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		MessageNode messageNode = chatMessage.getMessageNode();
		String message = messageNode.getValue();
		boolean update = false;
		if(message.contains(qp))
		{
			lastMessageIncludesQP = true;
			String originalMessage = message;
			message = message.substring(0, message.lastIndexOf(qp)+3); // Remove characters after last "q p"
			String[] messageSegments = message.split(qp); // Split

			List<Integer> segmentLengths = Arrays.stream(messageSegments).map(this::getChatLength).collect(Collectors.toList()); // can add 4 to move pixels to vertical of q

			// Get cumulative segment lengths
			Integer[] segmentIndex = new Integer[segmentLengths.size()];
			int total = -qpLength; // minus qpLength as offset

			if(messageNode.getType().equals(ChatMessageType.PRIVATECHAT)) // Private message from another player
			{
				// Align first segment with q vertical (+4) and add From/To offset (-15) if "q p" found in PM (same name used for both)
				segmentLengths.set(0, segmentLengths.get(0) + 4 - 15);
				lastQPFromPM = true;
			}
			else if(messageNode.getType().equals(ChatMessageType.PRIVATECHATOUT)) // Private message not sent from the local player
			{
				// Align first segment with q vertical
				segmentLengths.set(0, segmentLengths.get(0) + 4);
				lastQPFromPM = true;
			}
			else // Not private message
			{
				String sender = messageNode.getName();
				String localPlayer = client.getLocalPlayer().getName();

				int senderNameLength = getNameLength(sender);
				int localNameLength = getNameLength(localPlayer);

				// Account for Friends Chat icons
				FriendsChatRank rank;
				if(messageNode.getType().equals(ChatMessageType.FRIENDSCHAT))
				{
					senderNameLength += getFriendsChatRankIconSize(messageNode.getName());
					localNameLength += getFriendsChatRankIconSize(client.getLocalPlayer().getName());
				}

				// Align first segment with q vertical and add player name length offset
				segmentLengths.set(0, segmentLengths.get(0) + 4 + senderNameLength - localNameLength);

				lastQPFromPM = false;
			}

			// If local player has a chat icon then offset that (-13)
			if((client.getAccountType().isIronman() || client.getAccountType().isGroupIronman() || config.hasIcon()) && !messageNode.getType().equals(ChatMessageType.PRIVATECHATOUT))
			{
				segmentLengths.set(0, segmentLengths.get(0) - 13);
			}

			// Get cumulative segment lengths
			for(int i = 0; i < segmentIndex.length; i++)
			{
				total += segmentLengths.get(i) + qpLength;
				segmentIndex[i] = total;
			}

			// Save array of each q p index for dynamic overlay
			lastMessageSegmentIndex = Arrays.copyOf(segmentIndex, segmentIndex.length);

			// Align segments with vertical of the q
			for(int i = 1; i < segmentLengths.size(); i++)
			{
				segmentLengths.set(i, segmentLengths.get(i) + 8);
			}

			if(config.showCumulative())
			{
				segmentLengths = Arrays.asList(segmentIndex);
			}
			else
			{
				// If not using cumulative lengths then set any that are impossible to -1 and make first possible its index value
				for(int i = 0; i < segmentLengths.size() - 1; i++)
				{
					if(segmentIndex[i] < 0)
					{
						segmentLengths.set(i+1, segmentIndex[i+1]);
						segmentLengths.set(i, -1);
					}
				}
				if(segmentIndex[segmentIndex.length - 1] < 0) // final iteration of previous loop to avoid overflow
				{
					segmentLengths.set(segmentIndex.length - 1, -1);
				}
			}

			// Set inline recommendation style
			if(config.recommendationMode().equals(RecommendationMode.SPACES))
			{
				message = originalMessage + "   " + segmentLengths.stream().map(this::spacesToW).collect(Collectors.toList());
			}
			else
			{
				message = originalMessage + "   " + segmentLengths;
			}

			if (config.giveInlineHints())
			{
				messageNode.setValue(message);
			}
			update = true;
		}
		else if(lastMessageIncludesQP)
		{
			lastMessageIncludesQP = false;
			lastMessageSegmentIndex = null;
		}

		if(lastMessageIncludesQP && config.showOverlay())
		{
			refreshChatBoxTypedText();
			updateOverlayText();
		}

		if (update)
		{
			messageNode.setRuneLiteFormatMessage(messageNode.getValue());

			if(config.notifyOnQP())
			{
				notifier.notify("A qp opportunity!");
			}
		}
	}

	/**
	 * Returns the pixel width of the friends chat rank icon of the specified player.
	 *
	 * @param name the name of the player to check
	 * @return integer width of rank icon
	 */
	private int getFriendsChatRankIconSize(String name)
	{
		int length = 0;
		try {
			FriendsChatRank rank;
			rank = client.getFriendsChatManager()
					.findByName(name.replaceAll("<img=\\d+>", ""))
					.getRank();
			length = rank.equals(FriendsChatRank.UNRANKED) ? 0 : 11; // No icon if unranked, icon size = 11px
		}
		catch(NullPointerException e)
		{
			log.warn("Caught NullPointerException when trying to find \"{}\"", name.replaceAll("<img=\\d+>", ""));
		}

		return length;
	}

	/**
	 * Returns the pixel width of the specified string in the chatbox.
	 *
	 * @param chatMsg the String to measure
	 * @return the integer width of the specified String
	 */
	private int getChatLength(String chatMsg)
	{
		if(chatMsg == null) {return -5;}

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

	/**
	 * Returns the pixel width of a player name including chat icon if relevant.
	 *
	 * @param name the player name to measure
	 * @return the integer width of the specified name with chat icon
	 */
	private int getNameLength(String name)
	{
		if(name == null) {return -5;}

		return getChatLength(name.replaceAll("<img=\\d+>","@"));
	}

	/**
	 * Returns a string recommendation including the number of space characters to give a total width in pixels equal to the given integer.
	 *
	 * If the given integer width cannot be met with only spaces then another character is included.
	 * This character cannot be a letter as case is not guaranteed.
	 *
	 * @param pixels the integer pixel width to match
	 * @return a String that contains the number of spaces (and other characters) required to match the specified integer width, or "impossible" if pixels is too small
	 */
	private String spacesToW(int pixels)
	{
		String recommendation = "";

		if(pixels % 3 == 1)
		{
			pixels -= 4;
			recommendation += ", and ";
		}

		if(pixels % 3 == 2)
		{
			pixels -= 5;
			recommendation += "\" and ";
		}

		if(pixels < 0)
		{
			recommendation = "impossible";
		}
		else
		{
			recommendation += pixels/3 + " spaces";
		}

		return recommendation;
	}

	@Provides
	AqpFinderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AqpFinderConfig.class);
	}

	/**
	 * Formats the given String to match format of public chat messages.
	 *
	 * Format rules:
	 * <ul>
	 *  <li>If the first non-whitespace character of a sentence is a letter then it is forced upper case.</li>
	 *  <li>Any letter immediately proceeded by a letter is forced lower case.</li>
	 *  <li>A sentence is ended by the characters: . ! ?</li>
	 *</ul>
	 * @param chatText the string to be formatted as a public chat message
	 * @return the formatted string or an empty string if chatText is null
	 */
	private String formatChatText(String chatText)
	{
		if(chatText == null)
		{
			return "";
		}

		char[] chatTextArray = chatText.toCharArray();
		boolean inWord = false;
		boolean newSentence = true;

		for (int i = 0; i < chatTextArray.length; i++)
		{
			char ch = chatTextArray[i];

			if (Character.isLetter(ch))
			{
				if (inWord)
				{
					chatTextArray[i] = Character.toLowerCase(ch);
				}
				if (newSentence)
				{
					chatTextArray[i] = Character.toUpperCase(ch);
				}
				inWord = true;
			}
			else
			{
				inWord = false;
			}

			if(endSentenceCharList.contains(ch))
			{
				newSentence = true;
			}
			else if(!Character.isWhitespace(ch))
			{
				newSentence = false;
			}
		}

		return new String(chatTextArray);
	}

	/**
	 * Updates the stored string to be the current string typed into the input of the last "q p" message received.
	 */
	private void refreshChatBoxTypedText()
	{
		String newText = "";
		if(lastQPFromPM)
		{
			newText = client.getVarcStrValue(VarClientStr.INPUT_TEXT);
		}
		else
		{
			newText = client.getVarcStrValue(VarClientStr.CHATBOX_TYPED_TEXT);
		}

		// ignore chat box channel command strings
		int controlCharacters = 0;

		if(newText.matches("/.*"))
		{
			controlCharacters = 1;
		}

		if(newText.matches("//.*|/p.*|/P.*|/f.*|/F.*|/c.*|/C.*|/g.*|/G.*"))
		{
			controlCharacters = 2;
		}

		if(newText.matches("/gc.*|/GC.*|/Gc.*|/gC.*"))
		{
			controlCharacters = 3;
		}

		newText = newText.substring(controlCharacters);

		newText = formatChatText(newText);

		if (!Objects.equals(newText, chatBoxTypedText))
		{
			chatBoxTypedText = newText;
			chatBoxTypedTextLength = getChatLength(chatBoxTypedText);
		}
	}

	/**
	 * Creates recommendation lines and corresponding red colour value for all "q p"s in the most recent message containing at least one.
	 * Recommendations take the current chat input into account.
	 */
	private void updateOverlayText()
	{
		overlayText.clear();
		overlayTextColour.clear();
		for (Integer seg : lastMessageSegmentIndex) {

			int scaledPercentPixelsToW;
			if (seg < 0)
			{
				scaledPercentPixelsToW = 255;
			}
			else
			{
				scaledPercentPixelsToW = Math.min(Math.round( Math.abs(((seg - chatBoxTypedTextLength) * 255) / (float) seg)), 255);
			}

			String lineText = "error";

			if (seg - chatBoxTypedTextLength >= 3) {
				lineText = spacesToW(seg - chatBoxTypedTextLength);
			} else if (3 > seg - chatBoxTypedTextLength && seg - chatBoxTypedTextLength > 0) {
				lineText = "Too close.";
			} else if (seg == chatBoxTypedTextLength) {
//				lineText = "Target acquired.";
				lineText = "Hit W now!";
			} else if (seg - chatBoxTypedTextLength < 0) {
				lineText = "Too far.";
			}

			overlayText.add(lineText);
			overlayTextColour.add(scaledPercentPixelsToW);
		}
	}

	/**
	 * Refreshes the chatbox input and updates the recommendation overlay if it is enabled and last message includes a qp.
	 */
	private void checkChatBoxUpdateOverlay()
 	{
		if(lastMessageIncludesQP && config.showOverlay())
		{
			refreshChatBoxTypedText();
			updateOverlayText();
		}
 	}

	@Override
	public void keyTyped(KeyEvent e) {
		checkChatBoxUpdateOverlay();
	}

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {
		checkChatBoxUpdateOverlay();
	}
}
