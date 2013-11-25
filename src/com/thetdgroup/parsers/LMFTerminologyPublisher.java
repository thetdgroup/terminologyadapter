package com.thetdgroup.parsers;

import org.json.JSONArray;
import org.json.JSONObject;

import com.thetdgroup.LexiconBuilder;

import comThetdgroupSchemaLmfRev16.FeatDocument.Feat;
import comThetdgroupSchemaLmfRev16.LexicalEntryDocument.LexicalEntry;
import comThetdgroupSchemaLmfRev16.LexicalResourceDocument.LexicalResource;
import comThetdgroupSchemaLmfRev16.LexiconDocument.Lexicon;

public final class LMFTerminologyPublisher
{
	//
	public static String publish(JSONObject jsonInputTerminology, JSONArray jsonCollections) throws Exception
	{
		LexicalEntryObject lexicalEntryObject = new LexicalEntryObject();
		
		//
		// Convert input document to a Lexicon Object
		LexiconBuilder terminologyData = new LexiconBuilder();
		terminologyData.loadDocument(jsonInputTerminology.getString("content_data"));

		//
		LexicalResource lexicalResource = terminologyData.getLexicalResource();

		//
		// Add Collection names
 	for(int iCollectionIndex = 0; iCollectionIndex < jsonCollections.length(); iCollectionIndex++)
 	{
 		JSONObject jsonCollectionObject = jsonCollections.getJSONObject(iCollectionIndex);
 		
 		//
 		Feat feature = lexicalResource.addNewFeat();
 		feature.setAtt("dictionary");
 		feature.setVal(jsonCollectionObject.getString("dictionary_name"));
 	}
		
		//
		// Add Lexical Entries (Lemma and Senses)
		Lexicon[] resourceLexicon = lexicalResource.getLexiconArray();
		
		for(int iLexiconIndex = 0; iLexiconIndex < resourceLexicon.length; iLexiconIndex++)
		{
			Lexicon lexicon = resourceLexicon[iLexiconIndex];
			LexicalEntry[] lexicalEntries = lexicon.getLexicalEntryArray();
			
			//
			for(int iLexicalIndex = 0; iLexicalIndex < lexicalEntries.length; iLexicalIndex++)
			{
				LexicalEntry lexicalEntry = lexicalEntries[iLexicalIndex];
				lexicalEntryObject.parse(lexicalEntry);
			}
		}
		
		//
		return terminologyData.saveDocument();
	}
}
