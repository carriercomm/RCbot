package com.recursivechaos.rcbot.plugins.stoopsnoop.query;

import java.sql.Timestamp;
import java.util.List;

import org.pircbotx.hooks.events.MessageEvent;

import com.recursivechaos.rcbot.bot.object.BotException;
import com.recursivechaos.rcbot.bot.object.MyPircBotX;
import com.recursivechaos.rcbot.plugins.stoopsnoop.objects.CustomQuery;
import com.recursivechaos.rcbot.plugins.stoopsnoop.objects.EventLog;

/**
 * QueryDAO provides a common interface for queries to the database.
 * 
 * @author Andrew Bell
 * 
 */
public interface QueryDAO {

	/**
	 * getWordCount will search the records for the total number of strings like
	 * the searchTerm on the particular channel.
	 * 
	 * @param searchTerm
	 *            String, word or phrase, to query records
	 * @param channel
	 *            String name of channel to search on (include the '#' char)
	 * @return integer of total word count
	 * @throws BotException 
	 */
	public int getWordCount(String searchTerm, String channel);
	
	/**
	 * getWordCount will search for records for the total number of strings like the searchTerm
	 * on the particular channel, given a start and end time
	 * @param searchTerm	String, word, or phrase, to query records
	 * @param channel		String name of the channel to seanch on (include the '#' char)
	 * @param start			java.sqlTimestamp start of period
	 * @param end			java.sqlTimestamp end of period
	 * @return				integer of the total word count
	 */
	public int getWordCount(String searchTerm, String channel, Timestamp start, Timestamp end);
	
	public String[][] getTopWords(int NumOfRecords, String channel, Timestamp start, Timestamp end);

	public void newGenericQuery(MessageEvent<MyPircBotX> event);

	public boolean verifyUserHistory(CustomQuery customQuery);

	public void executeQuery(CustomQuery myQueryConfig);

	public List<EventLog> getPreviousHourMessages(
			MessageEvent<MyPircBotX> event, int tHRESHOLD);
}
