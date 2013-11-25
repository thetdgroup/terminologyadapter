package com.thetdgroup.parsers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import comThetdgroupSchemaLmfRev16.FeatDocument.Feat;
import comThetdgroupSchemaLmfRev16.FormRepresentationDocument.FormRepresentation;

public class FormRepresentationObject
{
	private List<FeatureObject> terminologyFormFeatures = new ArrayList<FeatureObject>();
	
	//
 public void parse(FormRepresentation formRepresentation)
 {
 	Feat[] formFeatures = formRepresentation.getFeatArray();
 	
		for(int iFeatureIndex = 0; iFeatureIndex < formFeatures.length; iFeatureIndex++)
		{
			terminologyFormFeatures.add(new FeatureObject(formFeatures[iFeatureIndex]));
		}
 }
 
 //
 public JSONArray toJSON() throws Exception
 {
 	JSONArray jsonArray = new JSONArray();
		
		// Get all Features
  for(FeatureObject featureObject : terminologyFormFeatures)
  {
  	jsonArray.put(featureObject.toJSON());
  }
		
		//
		return jsonArray;
 }
}
