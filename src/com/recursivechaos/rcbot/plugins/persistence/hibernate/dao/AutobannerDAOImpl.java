package com.recursivechaos.rcbot.plugins.persistence.hibernate.dao;
/**
 * AutobannerDAOImpl provides all the necessary functions to check for spammers, as well
 * as ban spammers.
 * 
 * @author Andrew Bell www.recursivechaos.com
 */
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Criteria;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;
import org.pircbotx.hooks.events.MessageEvent;

import com.recursivechaos.rcbot.bot.object.MyPircBotX;
import com.recursivechaos.rcbot.plugins.stoopsnoop.autobanner.AutobannerDAO;
import com.recursivechaos.rcbot.plugins.stoopsnoop.objects.EventLog;
import com.recursivechaos.rcbot.plugins.stoopsnoop.objects.NickFilterGroup;
import com.recursivechaos.rcbot.plugins.stoopsnoop.query.QueryBO;

public class AutobannerDAOImpl extends DAO implements AutobannerDAO{

	@Override
	public boolean isFloodSpam(MessageEvent<MyPircBotX> event) {
		Boolean isSpam = false;
		int THRESHOLD = 2;// The message in question is already logged, so always include it
		int count = 0;
		// if the message is only one word, proceed to call hibernate
		if(!event.getMessage().contains(" ")){
			QueryDAOImpl myQuery = new QueryDAOImpl();
			List<EventLog> prevMsgs = myQuery.getPreviousMessages(event,THRESHOLD);
			for(int i = 0; i< THRESHOLD; i++){
				if(prevMsgs.get(i).getMessage().toLowerCase().equals(event.getMessage().toLowerCase())){
					count++;
				}
			}
		}
		if(count>=THRESHOLD){
			isSpam = true;
		}
		return isSpam;
	}

	@Override
	public boolean isBulkSpam(MessageEvent<MyPircBotX> event) {
		Boolean isSpam = false;
		int THRESHOLD = 5;
		int count = 0;
		// Get word map by passing it a list (of one) events
		EventLog eventLog = new EventLog(event);
		List<EventLog> eventList = new ArrayList<EventLog>();
		eventList.add(eventLog);
		HashMap<String, Integer> results = QueryBO.getWordMap(eventList);
		// Initial threshold check, before calling a hibernate query
		Iterator<Entry<String, Integer>> it = results.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it.next();
            int val = (Integer)entry.getValue();
            if (val>count){
            	count = val;
            }
        }
        // If above threshold, reset count, remove ignored words, and then rechecks threshold
        if(count>=THRESHOLD){
        	count = 0;
        	Iterator<Entry<String, Integer>> it2 = results.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it2.next();
                int val = (Integer)entry.getValue();
                if (val>count){
                	count = val;
                }
            }
            if(count>=THRESHOLD){
            	isSpam = true;
            }
        }
		return isSpam;
	}

	@Override
	public void banUser(MessageEvent<MyPircBotX> event, String note, int hours) {
		QueryBO helper = new QueryBO();
		Timestamp start = helper.getNow(event);
		Timestamp end = helper.getHoursFromNow(event,hours);
		// create ban object
		NickFilterGroup ban = new NickFilterGroup();
		ban.setNick(event.getUser().getNick());
		ban.setChannel(event.getChannel().getName());
		// search for any existing bans
		try{
			Criteria c = getSession().createCriteria(NickFilterGroup.class);
			c.add( Example.create(ban));
			c.add(Restrictions.lt("end",end));
			List results = c.list();
			// If ban in effect, don't add another
			if(results.isEmpty()){
				// set rest of ban
				ban.setNickFilterName("Spammer");
				ban.setEnd(end);
				ban.setStart(start);
				ban.setNote(note);
				// commit
				begin();
				getSession().save(ban);
				commit();
			}
		}catch(Exception e){
			
		}finally{
			close();
		}

	}

}