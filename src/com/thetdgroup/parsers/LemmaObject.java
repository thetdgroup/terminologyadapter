package com.thetdgroup.parsers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import comThetdgroupSchemaLmfRev16.FeatDocument.Feat;
import comThetdgroupSchemaLmfRev16.FormRepresentationDocument.FormRepresentation;
import comThetdgroupSchemaLmfRev16.LemmaDocument.Lemma;

//
public class LemmaObject
{
 private List<FormRepresentationObject> formRepresentationObjects = new ArrayList<FormRepresentationObject>();
 private List<Feat> featureObjects = new ArrayList<Feat>();
	
	//
 public void parse(Lemma lemmaEntry)
 {
 	FormRepresentation[] formRepresentations = lemmaEntry.getFormRepresentationArray();

		for(int iFormIndex = 0; iFormIndex < formRepresentations.length; iFormIndex++)
		{
			FormRepresentation formRepresentation = formRepresentations[iFormIndex];
			
			//
			FormRepresentationObject formObject = new FormRepresentationObject();
			formObject.parse(formRepresentation);
			
			formRepresentationObjects.add(formObject);
		}
 }
 
 //
 public JSONObject toJSON() throws Exception
 {
		JSONObject jsonObject = new JSONObject();
		
		//
		// Get all Terminology Senses
		JSONArray jsonFormRepresentationArray = new JSONArray();
		
  for(FormRepresentationObject formRepresentation : formRepresentationObjects)
  {
  	jsonFormRepresentationArray.put(formRepresentation.toJSON());
  }
   
  //
  // Build
  jsonObject.put("form_representation", jsonFormRepresentationArray);
  
		//
		return jsonObject;
 }
}
