package com.thetdgroup.parsers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import comThetdgroupSchemaLmfRev16.EquivalentDocument.Equivalent;
import comThetdgroupSchemaLmfRev16.FeatDocument.Feat;
import comThetdgroupSchemaLmfRev16.SenseDocument.Sense;

//
public class SenseObject
{
	private List<FeatureObject> featureObjects = new ArrayList<FeatureObject>();
	private List<EquivalentObject> equilaventArray = new ArrayList<EquivalentObject>();
	
	//
 public void parse(Sense senseEntry)
 {
		//
		// Parse Features
		Feat[] featureArray = senseEntry.getFeatArray();
		
		for(int iFeatureIndex = 0; iFeatureIndex < featureArray.length; iFeatureIndex++)
		{
			featureObjects.add(new FeatureObject(featureArray[iFeatureIndex]));
		}
		
		//
		// Parse Equivalents
 	Equivalent[] equivalentArray = senseEntry.getEquivalentArray();
 	
		for(int iEquivalentIndex = 0; iEquivalentIndex < equivalentArray.length; iEquivalentIndex++)
		{
			EquivalentObject fu = new EquivalentObject();
			fu.parse(equivalentArray[iEquivalentIndex]);
			
			equilaventArray.add(fu);		
		}
 }
 
 //
 public JSONObject toJSON() throws Exception
 {
 	//
 	JSONArray jsonEquivalentArray = new JSONArray();
 	
 	for(EquivalentObject equivalentObject : equilaventArray)
 	{
 		jsonEquivalentArray.put(equivalentObject.toJSON());
 	}
 	
 	//
 	JSONArray jsonFeatures = new JSONArray();
 	
 	for(FeatureObject featureObject : featureObjects)
 	{
 		jsonFeatures.put(featureObject.toJSON());
 	}
 	
 	//
 	JSONObject jsonObject = new JSONObject();
 	jsonObject.put("terminology_sense_features", jsonFeatures);
 	jsonObject.put("terminology_sense_equivalents", jsonEquivalentArray);

 	//
 	return jsonObject;
 }
}
