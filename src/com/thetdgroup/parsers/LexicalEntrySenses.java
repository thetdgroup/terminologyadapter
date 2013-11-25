package com.thetdgroup.parsers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import comThetdgroupSchemaLmfRev16.LexicalEntryDocument.LexicalEntry;
import comThetdgroupSchemaLmfRev16.SenseDocument.Sense;

public class LexicalEntrySenses
{
	private List<SenseObject> senseObjects = new ArrayList<SenseObject>();
	
	//
 public void parse(LexicalEntry lexicalEntry)
 {
 	//
 	// Parse Senses
 	Sense[] senseArray = lexicalEntry.getSenseArray();

		for(int iSenseIndex = 0; iSenseIndex < senseArray.length; iSenseIndex++)
		{
			Sense sense = senseArray[iSenseIndex];
			
			//
			SenseObject senseObject = new SenseObject();
			senseObject.parse(sense);
			
			senseObjects.add(senseObject);
		}
 }
 
 //
 public JSONArray toJSON() throws Exception
 {
 	JSONArray jsonSenseArray = new JSONArray();
 	
 	for(SenseObject senseObject : senseObjects)
 	{
 		jsonSenseArray.put(senseObject.toJSON());
 	}

 	//
 	return jsonSenseArray;
 }
}
