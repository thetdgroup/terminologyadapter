package com.thetdgroup.authorization;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class AuthorizationParser 
{
	private static HashMap<String, Vetting> authorizationMap = new HashMap<String, Vetting>();
	
	//
 @SuppressWarnings("unchecked")
	public static void parse(final SAXBuilder saxBuilder, final String authorizationFile) throws JDOMException, IOException
 {
 	authorizationMap.clear();
 	
		//
		// Parse Configuration
		Document permissionDocument = saxBuilder.build(authorizationFile);
		
		//
		// Get Authorization schema
		XPath xPath = XPath.newInstance("terminology_authorization/user");
		List<Element> userList = xPath.selectNodes(permissionDocument);
		
		//
		for(Element userElement : userList)
		{
			Vetting vettingPriviledge = new Vetting();
			vettingPriviledge.parse(userElement);
			
			authorizationMap.put(userElement.getAttributeValue("user_id"), vettingPriviledge);
		}
 }
 
 //
 public static JSONObject getGroups(String userID) throws JSONException
 {
 	JSONArray jsonArray = authorizationMap.get(userID).getGroups();
 	
 	//
 	JSONObject jsonObject = new JSONObject();
 	jsonObject.put("user_id", userID);
 	jsonObject.put("groups", jsonArray);
 	
 	return jsonObject;
 }
 
 //
 public static JSONArray getAvailableVetters() throws JSONException
 {
 	JSONArray jsonArray = new JSONArray();
 	
 	//
 	Iterator<String> iterator = authorizationMap.keySet().iterator();
 	
		while(iterator.hasNext())
		{
			String userID = iterator.next();
			
			JSONObject jsonObject = new JSONObject();
 	 jsonObject.put("user_id", userID);
			
 	 jsonArray.put(jsonObject);
		}
 	
 	//
 	return jsonArray;
 }
 
 //
 public static JSONArray getAvailableVettersForGroup(String groupID) throws JSONException
 {
 	JSONArray jsonArray = new JSONArray();
 	
 	//
 	Iterator<String> iterator = authorizationMap.keySet().iterator();
 	
		while(iterator.hasNext())
		{
			String userID = iterator.next();
			
			//
			Vetting vetting = authorizationMap.get(userID);
			
			if(vetting.isGroupVetter(groupID))
			{
				JSONObject jsonObject = new JSONObject();
	 	 jsonObject.put("user_id", userID);

	 	 //
	 	 jsonArray.put(jsonObject);
			}
		}
 	
 	//
 	return jsonArray;
 }
}
