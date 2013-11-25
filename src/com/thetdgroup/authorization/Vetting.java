package com.thetdgroup.authorization;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//
public final class Vetting
{
	private HashMap<String, Boolean> groupMap = new HashMap<String, Boolean>(); 
	
 //
	@SuppressWarnings("unchecked")
	public void parse(final Element userElement) throws JDOMException
	{
		groupMap.clear();
		
		//
		//
		XPath xPath = XPath.newInstance("group");
		List<Element> groupList = xPath.selectNodes(userElement);
		
		//
		for(Element groupElement : groupList)
		{
			groupMap.put(groupElement.getAttributeValue("group_id"), Boolean.parseBoolean(groupElement.getAttributeValue("is_vetter")));
		}
	}
	
	//
	public JSONArray getGroups() throws JSONException
	{
		JSONArray jsonArray = new JSONArray();
		
		//
		Iterator<String> iterator = groupMap.keySet().iterator();
		
		while(iterator.hasNext())
		{
			String groupID = iterator.next();
			
			JSONObject jsonObject  = new JSONObject();
			jsonObject.put("group_id", groupID);
			jsonObject.put("is_vetter", groupMap.get(groupID));
			
			jsonArray.put(jsonObject);
		}
		
		//
		return jsonArray;
	}
	
	//
	public boolean isGroupVetter(String groupID) throws JSONException
	{
		if(groupMap.get(groupID) == null)
			return false;
		
		//
		return groupMap.get(groupID);
	}
}
