package com.thetdgroup.parsers;

import org.json.JSONObject;

import comThetdgroupSchemaLmfRev16.LemmaDocument.Lemma;
import comThetdgroupSchemaLmfRev16.LexicalEntryDocument.LexicalEntry;

public class LexicalEntryObject
{
 private LemmaObject lemmaObject = new LemmaObject(); 
 private LexicalEntrySenses lexicalSenses = new LexicalEntrySenses();
  
 //
 public void parse(LexicalEntry lexicalEntry)
 {
 	// Parse Terminology Lemma
 	Lemma terminologyLemma = lexicalEntry.getLemma();
 	lemmaObject.parse(terminologyLemma);
 	
 	// Parse Terminology Senses
 	lexicalSenses.parse(lexicalEntry);
 }
 
 //
 public JSONObject toJSON() throws Exception
 {
		JSONObject jsonObject = new JSONObject();
  
  //
  // Build LexicalEntry
  jsonObject.put("terminology_lemma", lemmaObject.toJSON());
  jsonObject.put("terminology_senses", lexicalSenses.toJSON());
		
		//
		return jsonObject;
 }
}
