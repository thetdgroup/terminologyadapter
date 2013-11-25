package com.thetdgroup.builders;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.thetdgroup.LexiconBuilder;

import comThetdgroupSchemaLmfRev16.EquivalentDocument.Equivalent;
import comThetdgroupSchemaLmfRev16.FormRepresentationDocument.FormRepresentation;
import comThetdgroupSchemaLmfRev16.LemmaDocument.Lemma;
import comThetdgroupSchemaLmfRev16.LexicalEntryDocument.LexicalEntry;
import comThetdgroupSchemaLmfRev16.LexiconDocument.Lexicon;
import comThetdgroupSchemaLmfRev16.SenseDocument.Sense;

//
public final class LMFTerminologyBuilder
{
	//
	public static JSONObject createDocument(JSONObject jsonObject, boolean suggestDocumentName) throws Exception
	{
		if(jsonObject.has("Lemma") == false)
		{
			throw new Exception("A Lemma is required. Please correct and try again.");
		}
		
  //
  // Get Lemma and its form representation
  //
  JSONObject jsonLemmaObject = jsonObject.getJSONObject("Lemma");
  
  if(jsonLemmaObject.has("formRepresentation") == false)
  {
  	throw new Exception("A Lemma FormRepresentation is required. Please correct and try again.");
  }
  
  //
  JSONArray jsonFormRepresentation = jsonLemmaObject.getJSONArray("formRepresentation");
  
  if(jsonFormRepresentation.length() == 0)
  {
  	throw new Exception("A Lemma FormRepresentation is required. Please correct and try again.");
  }
  
		//
		//  Basic set of test are good to go. Create Document
		//
  String suggestedDocumentName = "";
  
  LexiconBuilder lexiconBuilder = new LexiconBuilder();
	 lexiconBuilder.createLexicalDocument("languageCoding", "ISO 639-3");
	 
  Lexicon lexicon = lexiconBuilder.addLexiconEntry();

  //
  // Get Lemma Form Representation Array
  //
  LexicalEntry lexEntry = lexiconBuilder.addLexicalEntry(lexicon);
  Lemma lemma = lexiconBuilder.addLemma(lexEntry);
  
  FormRepresentation formRepresentation = lexiconBuilder.addFormRepresentation(lemma);
  
  for(int iIndex = 0; iIndex < jsonFormRepresentation.length(); iIndex++)
  {
		 //
			// Get FormRepresentation Features
  	JSONObject jsonFeature = jsonFormRepresentation.getJSONObject(iIndex);
   lexiconBuilder.addFormRepresentationFeature(formRepresentation, jsonFeature.getString("feature_name"), jsonFeature.getString("feature_value"));
   
   //
   // Generate a suggested document (if specified)
   if(suggestDocumentName == true)
   {
   	if(jsonFeature.getString("feature_name").equals("writtenForm"))
   	{
   		String writtenForm = jsonFeature.getString("feature_value");
   		
   		if(writtenForm.length() == 0)
   		{
   			throw new Exception("The writtenForm value cannot be empty. Please correct and try again.");
   		}
   		else
   		{
   		 suggestedDocumentName = StringUtils.trim(writtenForm);
   		}
   	}
   }
  }
  
  //
  // Get All Senses
  //
  JSONArray jsonSenseArray = jsonObject.getJSONArray("Senses");

  for(int iSenseIndex = 0; iSenseIndex < jsonSenseArray.length(); iSenseIndex++)
  {
		 JSONObject jsonEquivalentObject = jsonSenseArray.getJSONObject(iSenseIndex).getJSONObject("Equivalent");

		 Sense sense = lexiconBuilder.addSense(lexEntry);
		 Equivalent equivalent = lexiconBuilder.addSenseEquivalent(sense);

		 //
			// Get Equivalent Features
		 JSONArray featureArray = jsonEquivalentObject.getJSONArray("features");
		 
		 for(int iFeatureIndex = 0; iFeatureIndex < featureArray.length(); iFeatureIndex++)
		 {
		 	JSONObject jsonFeature = featureArray.getJSONObject(iFeatureIndex);
		 	lexiconBuilder.addSenseEquivalentFeature(equivalent, jsonFeature.getString("feature_name"), jsonFeature.getString("feature_value"));
		 }
  }
  
  //
  JSONObject jsonReplyObject = new JSONObject();
  jsonReplyObject.put("lexicon_document_data", lexiconBuilder.saveDocument());
  
  if(suggestDocumentName == true)
  {
  	jsonReplyObject.put("suggested_document_name", suggestedDocumentName);
  }

  
  //
  return jsonReplyObject;
	}
}
