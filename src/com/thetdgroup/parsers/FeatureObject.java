package com.thetdgroup.parsers;

import org.json.JSONObject;

import comThetdgroupSchemaLmfRev16.FeatDocument.Feat;

public class FeatureObject
{
 private String featureName = "";
 private String featureValue = "";
 
 //
 FeatureObject(String featureName, String featureValue)
 {
 	this.featureName = featureName;
 	this.featureValue = featureValue;
 }
 
 FeatureObject(Feat feature)
 {
 	this.featureName = feature.getAtt();
 	this.featureValue = feature.getVal();
 }
 
 //
	public String getFeatureName()
	{
		return featureName;
	}
	
	public void setFeatureName(String featureName)
	{
		this.featureName = featureName;
	}
	
	public String getFeatureValue()
	{
		return featureValue;
	}
	
	public void setFeatureValue(String featureValue)
	{
		this.featureValue = featureValue;
	}
 
	//
	public JSONObject toJSON() throws Exception
	{
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("feature_name", featureName);
		jsonObject.put("feature_value", featureValue);
		
		return jsonObject;
	}
 
}
