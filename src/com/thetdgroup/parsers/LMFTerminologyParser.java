package com.thetdgroup.parsers;

import org.json.JSONObject;

import com.thetdgroup.LexiconBuilder;

import comThetdgroupSchemaLmfRev16.LexicalEntryDocument.LexicalEntry;
import comThetdgroupSchemaLmfRev16.LexicalResourceDocument.LexicalResource;
import comThetdgroupSchemaLmfRev16.LexiconDocument.Lexicon;

//
public final class LMFTerminologyParser
{
	private String terminologyOriginalAuthor = "";
	private String terminologyParentPath = "";
	private String terminologyName = "";
	private String terminologyStatus = "";
	private String terminologyDate = "";
	
	private String terminologySchema = "";
	private String terminologySchemaName = "";
	
	private LexicalEntryObject lexicalEntryObject = new LexicalEntryObject();
	
	//
	public void parse(JSONObject jsonInputTerminology) throws Exception
	{
		terminologyOriginalAuthor = jsonInputTerminology.getString("terminology_original_author");
		terminologyParentPath = jsonInputTerminology.getString("parent_node_path");
		terminologyName = jsonInputTerminology.getString("node_name");
		terminologyStatus = jsonInputTerminology.getString("terminology_status");
		terminologyDate = jsonInputTerminology.getString("node_created_date");
		
		if(jsonInputTerminology.has("terminology_supporting_schema_data"))
		{
		 terminologySchema = jsonInputTerminology.getString("terminology_supporting_schema_name");
		}
		
		if(jsonInputTerminology.has("terminology_supporting_schema_data"))
		{
	 	terminologySchemaName = jsonInputTerminology.getString("terminology_supporting_schema_data");
		}
		
		//
		// Convert input document to a Lexicon Object
		LexiconBuilder terminologyData = new LexiconBuilder();
		terminologyData.loadDocument(jsonInputTerminology.getString("content_data"));

		//
		LexicalResource lexicalResource = terminologyData.getLexicalResource();
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
	}
	
	public JSONObject toJSON() throws Exception
	{
		JSONObject jsonObject = new JSONObject();
		
		//
		jsonObject.put("terminology_original_author", terminologyOriginalAuthor);
		jsonObject.put("terminology_folder", terminologyParentPath);
		jsonObject.put("terminology_name", terminologyName);
		jsonObject.put("terminology_status", terminologyStatus);
		jsonObject.put("terminology_date", terminologyDate);
		jsonObject.put("terminology_schema_name", terminologySchemaName);
		jsonObject.put("lexical_entry", lexicalEntryObject.toJSON());
		
		//
		return jsonObject;
	}
}
