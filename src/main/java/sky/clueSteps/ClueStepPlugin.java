package sky.clueSteps;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PluginDescriptor(
	name = "Clue Steps",
	description = "Show number of steps done on each clue",
	tags = {"inventory", "clues", "overlay"}
)
@Slf4j
public class ClueStepPlugin extends Plugin
{
	private static final Pattern BEGINNER_PATTERN = Pattern.compile("You have completed (\\d+) steps? on this beginner clue scroll\\.");
	private static final Pattern EASY_PATTERN = Pattern.compile("You have completed (\\d+) steps? on this easy clue scroll\\.");
	private static final Pattern MEDIUM_PATTERN = Pattern.compile("You have completed (\\d+) steps? on this medium clue scroll\\.");
	private static final Pattern HARD_PATTERN = Pattern.compile("You have completed (\\d+) steps? on this hard clue scroll\\.");
	private static final Pattern ELITE_PATTERN = Pattern.compile("You have completed (\\d+) steps? on this elite clue scroll\\.");
	private static final Pattern MASTER_PATTERN = Pattern.compile("You have completed (\\d+) steps? on this master clue scroll\\.");

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ClueStepOverlay overlay;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Provides
	ClueStepConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClueStepConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() == ChatMessageType.GAMEMESSAGE || event.getType() == ChatMessageType.SPAM)
		{
			String message = Text.removeTags(event.getMessage());
			Matcher beginnerMatcher = BEGINNER_PATTERN.matcher(message);
			Matcher easyMatcher = EASY_PATTERN.matcher(message);
			Matcher mediumMatcher = MEDIUM_PATTERN.matcher(message);
			Matcher hardMatcher = HARD_PATTERN.matcher(message);
			Matcher eliteMatcher = ELITE_PATTERN.matcher(message);
			Matcher masterMatcher = MASTER_PATTERN.matcher(message);

			if (beginnerMatcher.find())
			{
				updateBeginner(Integer.parseInt(beginnerMatcher.group(1)));
			}
			else if (easyMatcher.find())
			{
				updateEasy(Integer.parseInt(easyMatcher.group(1)));
			}
			else if (mediumMatcher.find())
			{
				updateMedium(Integer.parseInt(mediumMatcher.group(1)));
			}
			else if (hardMatcher.find())
			{
				updateHard(Integer.parseInt(hardMatcher.group(1)));
			}
			else if (eliteMatcher.find())
			{
				updateElite(Integer.parseInt(eliteMatcher.group(1)));
			}
			else if (masterMatcher.find())
			{
				updateMaster(Integer.parseInt(masterMatcher.group(1)));
			}
		}
	}

	private void updateBeginner(final int value)
	{
		setClueSteps(ClueStepConfig.KEY_BEGINNER, value);
	}

	private void updateEasy(final int value)
	{
		setClueSteps(ClueStepConfig.KEY_EASY, value);
	}

	private void updateEasy(final int value, final int ID)
	{
		setClueSteps(ClueStepConfig.KEY_EASY, value);
		setClueSteps(ClueStepConfig.ID_EASY, ID);
	}

	private void updateMedium(final int value)
	{
		setClueSteps(ClueStepConfig.KEY_MEDIUM, value);
	}

	private void updateMedium(final int value, final int ID)
	{
		setClueSteps(ClueStepConfig.KEY_MEDIUM, value);
		setClueSteps(ClueStepConfig.ID_MEDIUM, ID);
	}

	private void updateHard(final int value)
	{
		setClueSteps(ClueStepConfig.KEY_HARD, value);
	}

	private void updateHard(final int value, final int ID)
	{
		setClueSteps(ClueStepConfig.KEY_HARD, value);
		setClueSteps(ClueStepConfig.ID_HARD, ID);
	}

	private void updateElite(final int value)
	{
		setClueSteps(ClueStepConfig.KEY_ELITE, value);
	}

	private void updateElite(final int value, final int ID)
	{
		setClueSteps(ClueStepConfig.KEY_ELITE, value);
		setClueSteps(ClueStepConfig.ID_ELITE, ID);
	}

	private void updateMaster(final int value)
	{
		setClueSteps(ClueStepConfig.KEY_MASTER, value);
	}

	int getClueSteps(String key)
	{
		Integer i = configManager.getRSProfileConfiguration(ClueStepConfig.GROUP, key, Integer.class);
		return i == null ? -1 : i;
	}

	private void setClueSteps(String key, int value)
	{
		configManager.setRSProfileConfiguration(ClueStepConfig.GROUP, key, value);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event) {
		if (event.getGroupId() == WidgetID.DIALOG_SPRITE_GROUP_ID) {
			clientThread.invokeLater(() -> {
				Widget sprite = client.getWidget(WidgetInfo.DIALOG_SPRITE_SPRITE);
				int clueID = sprite.getItemId();

				switch (clueID){
					case ItemID.REWARD_CASKET_BEGINNER:
						updateBeginner(0);
						return;
					case ItemID.REWARD_CASKET_EASY:
						updateEasy(-1, 0);
						return;
					case ItemID.REWARD_CASKET_MEDIUM:
						updateMedium(-1, 0);
						return;
					case ItemID.REWARD_CASKET_HARD:
						updateHard(-1, 0);
						return;
					case ItemID.REWARD_CASKET_ELITE:
						updateElite(-1, 0);
						return;
					case ItemID.REWARD_CASKET_MASTER:
						updateMaster(0);
						return;
					default:
						break;
				}

				// Beginner and master clues all have the same ID, so can't look at an Id change to determine new clue
				ClueWithConfig clueType = ClueWithConfig.findItem(clueID);
				if (clueType == null) { return ;}

				int steps = getClueSteps(clueType.getConfigKey()) + 1;

				switch (clueType.getType()){
					case BEGINNER_CLUE:
						updateBeginner(steps);
						return;
					case MASTER_CLUE:
						updateMaster(steps);
						return;
					default:
						break;
				}
			});
		}
	}

	@Subscribe
	public void onItemContainerChanged(final ItemContainerChanged event) {
		if (event.getContainerId() != InventoryID.INVENTORY.getId()) return;

		Item[] invItems = event.getItemContainer().getItems();

		for (Item item : invItems) {
			ClueWithConfig clue = ClueWithConfig.findItem(item.getId());
			if (clue == null) continue;

			int clueID = item.getId();
			int steps = getClueSteps(clue.getConfigKey()) + 1;

			switch (clue.getType()){
				case EASY_CLUE:
					if (getClueSteps(ClueStepConfig.ID_EASY) != clueID) {
						updateEasy(steps, clueID);
					}
					break;
				case MEDIUM_CLUE:
					if (getClueSteps(ClueStepConfig.ID_MEDIUM) != clueID) {
						updateMedium(steps, clueID);
					}
					break;
				case HARD_CLUE:
					if (getClueSteps(ClueStepConfig.ID_HARD) != clueID) {
						updateHard(steps, clueID);
					}
					break;
				case ELITE_CLUE:
					if (getClueSteps(ClueStepConfig.ID_ELITE) != clueID) {
						updateElite(steps, clueID);
					}
					break;
				default:
					break;
			}
		}
	}
}
