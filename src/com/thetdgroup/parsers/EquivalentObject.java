package com.thetdgroup.parsers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import comThetdgroupSchemaLmfRev16.EquivalentDocument.Equivalent;
import comThetdgroupSchemaLmfRev16.FeatDocument.Feat;
import comThetdgroupSchemaLmfRev16.TextRepresentationDocument.TextRepresentation;

public class EquivalentObject
{
	private List<FeatureObject> featureObjects = new ArrayList<FeatureObject>();

 //
 public void parse(Equivalent equivalent)
 {
		//
		// Parse Features
		Feat[] featureArray = equivalent.getFeatArray();
		
		for(int iFeatureIndex = 0; iFeatureIndex < featureArray.length; iFeatureIndex++)
		{
			featureObjects.add(new FeatureObject(featureArray[iFeatureIndex]));
		}
		
		//
		// Parse Equivalents
 	TextRepresentation[] textRepresentationArray = equivalent.getTextRepresentationArray();
 	
		for(int iTextRepresentationIndex = 0; iTextRepresentationIndex < textRepresentationArray.length; iTextRepresentationIndex++)
		{
			TextRepresentation textRepresentationObject = textRepresentationArray[iTextRepresentationIndex];
			textRepresentationObject.getFeatArray();
		}
 }
 
 //
 public JSONObject toJSON() throws Exception
 {
 	//
 	/*JSONArray jsonEquivalents = new JSONArray();
 	
 	for(EquivalentObject equivalentObject : equilaventArray)
 	{
 		jsonEquivalent.put(equivalentObject.toJSON());
 	}*/
 	
 	//
 	JSONArray jsonFeatures = new JSONArray();
 	
 	for(FeatureObject featureObject : featureObjects)
 	{
 		jsonFeatures.put(featureObject.toJSON());
 	}
 	
 	//
 	JSONObject jsonObject = new JSONObject();
 	jsonObject.put("terminology_equivalent_features", jsonFeatures);
 	//jsonObject.put("terminology_equivalent_text_representation", jsonEquivalents);

 	//
 	return jsonObject;
 }
}
