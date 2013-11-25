package com.thetdgroup;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.thetdgroup.authorization.AuthorizationParser;
import com.thetdgroup.builders.LMFTerminologyBuilder;
import com.thetdgroup.AdapterConstants;
import com.thetdgroup.ServiceConstants;
import com.thetdgroup.parsers.LMFTerminologyParser;
import com.thetdgroup.parsers.LMFTerminologyPublisher;
import com.thetdgroup.util.xml.XMLUtil;

//
public final class TerminologyAdapter extends BaseTerminologyAdapter
{
	private static XMLReader terminologyValidator = null;
	private static String terminologyMimetype = "";
	
	private static String terminologySchemaName = ""; 
	private static String terminologySchemaData = ""; 
	private static String terminologyIndexingSchemaName = ""; 
	
	private static String authorizationFileName = "";
	
	private FuzeInCommunication fuzeInCommunication = new FuzeInCommunication();

	//
	public void initialize(final JSONObject configurationObject) throws Exception
	{
		if(configurationObject.has("adapter_configuration_file") == false)
		{
			throw new Exception("The adapter_configuration_file parameter was not found");
		}
		
		//
		if(configurationObject.has("fuzein_connection_info"))
		{
			JSONObject jsonCommParams = configurationObject.getJSONObject("fuzein_connection_info");
			
			fuzeInCommunication.setFuzeInConnection(jsonCommParams.getString("service_url"), 
																																											jsonCommParams.getInt("service_socket_timeout"), 
																																											jsonCommParams.getInt("service_connection_timeout"), 
																																											jsonCommParams.getInt("service_connection_retry"));
		}
		
		//
		parseAdapterConfiguration(configurationObject.getString("adapter_configuration_file"));
	}
	
	//
	public void destroy()
	{
	 if(fuzeInCommunication != null)
	 {
	 	fuzeInCommunication.releaseConnection();
	 }
	}
	
	//
	public JSONObject saveTerminology(final String identificationKey, final JSONObject jsonData) throws Exception
	{
		//
		// Validate that all required params are present
		//
		if(jsonData.has("submittal_type") == false)
		{
			throw new Exception("The 'submittal_type' parameter is required.");
		}
		
		if(jsonData.has("content_management_folder") == false)
		{
			throw new Exception("The 'content_management_folder' parameter is required.");
		}
		
		if(jsonData.has("user_information") == false)
		{
			throw new Exception("The 'user_information' object is required.");
		}
  		
		if(jsonData.has("terminology_data") == false)
		{
			throw new Exception("The 'terminology_data' object is required.");
		}
  
		//
		String terminologyDocumentName = "";
		String terminologyDocumentData = "";
		
		//
		// Process Document based on submittal type
		//
		String submittalType = jsonData.getString("submittal_type");
		
		if(submittalType.equals("file"))
		{
			if(jsonData.has("terminology_name") == false || jsonData.getString("terminology_name").length() == 0)
			{
				throw new Exception("A Terminology name is required.");
			}
			
			//
			// Read file
			InputStream inputStream = null;
			BOMInputStream bomInputStream = null;
			
			try
			{
				inputStream = new FileInputStream(jsonData.getString("document_data"));
		 	bomInputStream = new BOMInputStream(inputStream, false);
		 	
		 	//
				String xmlString = IOUtils.toString(bomInputStream, "UTF-8");
				
				if(xmlString.length() == 0)
				{
					xmlString = jsonData.getString("document_data");
				}
				
				//
				// Validate against the LMF schema
			 Document terminologyDocument = saxBuilder.build(bomInputStream);
				validateTerminology(XMLUtil.compactDocumentString(terminologyDocument));
				
				// 
				submittalType = "string";
				terminologyDocumentName = jsonData.getString("terminology_name");
				terminologyDocumentData = xmlString;
			}
			catch(Exception exception)
			{
				throw new Exception(exception);
			}
			finally
			{
				if(bomInputStream != null)
				{
					bomInputStream.close();
				}
				
				if(inputStream != null)
				{
					inputStream.close();
				}
			}
		}
		else if(submittalType.equals("xml"))
		{
			if(jsonData.has("terminology_name") == false || jsonData.getString("terminology_name").length() == 0)
			{
				throw new Exception("A Terminology name is required.");
			}
			
			//
			InputStream inputStream = null;
			BOMInputStream bomInputStream = null;
			
			try
			{
				inputStream = new FileInputStream(jsonData.getString("terminology_data"));
		 	bomInputStream = new BOMInputStream(inputStream, false);
				
		 	String xmlString = IOUtils.toString(bomInputStream, "UTF-8");
		 	
				if(xmlString.length() == 0)
				{
					xmlString = jsonData.getString("terminology_data");
				}

				//
				// Validate against the LMF schema
			 Document terminologyDocument = saxBuilder.build(bomInputStream);
				validateTerminology(XMLUtil.compactDocumentString(terminologyDocument));
				
				// 
				submittalType = "string";
				terminologyDocumentName = jsonData.getString("terminology_name");
				terminologyDocumentData = xmlString;
			}
			catch(Exception exception)
			{
				throw new Exception(exception);
			}
			finally
			{
				if(bomInputStream != null)
				{
					bomInputStream.close();
				}
				
				if(inputStream != null)
				{
					inputStream.close();
				}
			}
		}
		else if(submittalType.equals("json"))
		{
			//
			// Reconstruct the terminology given a JSONObject
			JSONObject jsonLexiconDocument = LMFTerminologyBuilder.createDocument(jsonData.getJSONObject("terminology_data"), true);

			//
			submittalType = "string";
			terminologyDocumentName = jsonLexiconDocument.getString("suggested_document_name");
			terminologyDocumentData = jsonLexiconDocument.getString("lexicon_document_data");
		}
		else if(submittalType.equals("stream"))
		{

		}
		else
		{
			throw new Exception("The 'submittal_type' parameter is invalid. Only 'file', 'xml', json' or 'stream' are supported.");
		}
		
		//
		// Extract User Information
		JSONObject jsonUserInformation = jsonData.getJSONObject("user_information");
		
		//
		// Add Terminology Metadata
		JSONObject jsonMetadataObject = new JSONObject();
	 jsonMetadataObject.put(TerminologyMetadata.TERMINOLOGY_ORIGINAL_AUTHOR, jsonUserInformation.getString("user_name")); 
		jsonMetadataObject.put(TerminologyStatus.TERMINOLOGY_STATUS, TerminologyStatus.status.NEW_TERMINOLOGY);
	 jsonMetadataObject.put(TerminologyMetadata.TERMINOLOGY_SUPPORTING_SCHEMA_NAME, terminologySchemaName); 
	 jsonMetadataObject.put(TerminologyMetadata.TERMINOLOGY_SUPPORTING_INDEXING_SCHEMA_NAME, terminologyIndexingSchemaName); 
		
		//
		// Add Schema Attachment
		JSONObject jsonAttachmentsObject = new JSONObject();
		//jsonAttachmentsObject(TerminologyMetadata.TERMINOLOGY_SUPPORTING_SCHEMA_NAME, terminologySchemaName);
		//jsonAttachmentsObject(TerminologyMetadata.TERMINOLOGY_SUPPORTING_SCHEMA_DATA, terminologyDescriptor.getString("terminology_schema_name"));
		//jsonAttachmentsObject(TerminologyMetadata.TERMINOLOGY_SUPPORTING_INDEXING_SCHEMA_NAME, terminologyIndexingSchemaName);
		//jsonAttachmentsObject(TerminologyMetadata.TERMINOLOGY_SUPPORTING_INDEXING_SCHEMA_DATA, terminologyDescriptor.getString("terminology_schema_name"));
		
		//
		// Build CM Object
		JSONObject jsonFileObject = new JSONObject();
		jsonFileObject.put("parent_folder", jsonData.getString("content_management_folder"));
		jsonFileObject.put("node_name", terminologyDocumentName);		
		jsonFileObject.put("submittal_type", submittalType);
		jsonFileObject.put("data", terminologyDocumentData);
		jsonFileObject.put("fuzein_mimetype", terminologyMimetype);
		jsonFileObject.put("document_metadata", jsonMetadataObject);
		jsonFileObject.put("document_attachments", jsonAttachmentsObject);

		//
		// Submit to the Content Management Service
		JSONObject jsonMessageObject = new JSONObject();
		jsonMessageObject.put("service_meta", "fuzein_cm_service");
		jsonMessageObject.put("service_action", "add_content");
		jsonMessageObject.put("service_api_data", jsonFileObject);
		
		String serviceResponse = fuzeInCommunication.callService(identificationKey, jsonMessageObject);
		JSONObject jsonResponse = processServiceResponse(serviceResponse);
		
		//
		// Process response
		JSONObject jsonTerminologyObject = new JSONObject();

		if(jsonResponse.getString(AdapterConstants.ADAPTER_STATUS).equals(AdapterConstants.status.SUCCESS.toString()))
		{
			JSONObject jsonDataObject = jsonResponse.getJSONArray(AdapterConstants.ADAPTER_DATA).getJSONObject(0);
			
			//
			jsonTerminologyObject.put("terminology_status", jsonDataObject.getString("terminology_status"));
			jsonTerminologyObject.put("terminology_original_author", jsonDataObject.getString("terminology_original_author"));
			jsonTerminologyObject.put("terminology_date", jsonDataObject.getString("node_created_date"));
			jsonTerminologyObject.put("terminology_name", jsonDataObject.getString("node_name"));
			jsonTerminologyObject.put("terminology_folder", jsonDataObject.getString("parent_node_path"));
			//jsonTerminologyObject.put("terminology_schema_name", jsonDataObject.getString("terminology_original_author"));
		}
		
		//
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.SUCCESS);
		jsonObject.put(AdapterConstants.ADAPTER_DATA, jsonTerminologyObject);

		return jsonObject;
	}

	//
	public JSONObject updateTerminology(final String identificationKey, final JSONObject jsonData) throws Exception
	{
		//
		// Validate that all required params are present
		//
		if(jsonData.has("submittal_type") == false)
		{
			throw new Exception("The 'submittal_type' parameter is required.");
		}
		
		if(jsonData.has("content_management_folder") == false)
		{
			throw new Exception("The 'content_management_folder' parameter is required.");
		}
		
		if(jsonData.has("user_information") == false)
		{
			throw new Exception("The 'user_information' object is required.");
		}
  		
		if(jsonData.has("terminology_name") == false)
		{
			throw new Exception("The 'terminology_name' parameter is required.");
		}
		
		if(jsonData.has("terminology_data") == false)
		{
			throw new Exception("The 'terminology_data' parameter is required.");
		}
  
		//
		String terminologyDocumentData = "";
		
		//
		// Process Document based on submittal type
		//
		String submittalType = jsonData.getString("submittal_type");
		
		if(submittalType.equals("file"))
		{
			//
			// Read file
			File file = new File(jsonData.getString("terminology_data"));
			Document terminologyDocument = saxBuilder.build(file);
			
			//
			// Validate against the LMF schema
			validateTerminology(XMLUtil.compactDocumentString(terminologyDocument));
			
			// 
			submittalType = "string";
			terminologyDocumentData = XMLUtil.prettyDocumentString(terminologyDocument);
		}
		else if(submittalType.equals("xml"))
		{
			//
			// Read XML String
			ByteArrayInputStream byteArrayInputStream = null;
			Document terminologyDocument = null;
			
			try
			{
				String xmlString = jsonData.getString("terminology_data");
			 byteArrayInputStream = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
			 terminologyDocument = saxBuilder.build(byteArrayInputStream);
				
				//
				// Validate against the LMF schema
				validateTerminology(XMLUtil.compactDocumentString(terminologyDocument));
				
				// 
				submittalType = "string";
				terminologyDocumentData = xmlString;
			}
			catch(Exception exception)
			{
				throw new Exception(exception);
			}
			finally
			{
			 byteArrayInputStream.close();
			}
		}
		else if(submittalType.equals("json"))
		{
			//
			// Reconstruct the terminology given a JSONObject
			JSONObject jsonLexiconDocument = LMFTerminologyBuilder.createDocument(jsonData.getJSONObject("terminology_data"), false);
			
			//
			submittalType = "string";
			terminologyDocumentData = jsonLexiconDocument.getString("lexicon_document_data");
		}
		else if(submittalType.equals("stream"))
		{

		}
		else
		{
			throw new Exception("The 'submittal_type' parameter is invalid. Only 'file', 'xml', json' or 'stream' are supported.");
		}
		
		//
		// Extract User Information
		JSONObject jsonUserInformation = jsonData.getJSONObject("user_information");
		
		//
		// Add Terminology Metadata
		JSONObject jsonMetadataObject = new JSONObject();
	 jsonMetadataObject.put(TerminologyMetadata.TERMINOLOGY_MODIFICATION_AUTHOR, jsonUserInformation.getString("user_name")); 
		jsonMetadataObject.put(TerminologyStatus.TERMINOLOGY_STATUS, TerminologyStatus.status.UPDATED_TERMINOLOGY);
	 
		//
		// Add Schema Attachment
		JSONObject jsonAttachmentsObject = new JSONObject();
		//jsonAttachmentsObject(TerminologyMetadata.TERMINOLOGY_SUPPORTING_SCHEMA_NAME, terminologyDescriptor.getString("terminology_schema_name"));
		//jsonAttachmentsObject(TerminologyMetadata.TERMINOLOGY_SUPPORTING_SCHEMA, terminologyDescriptor.getString("terminology_schema_name"));
		
		//
		// Build CM Object
		JSONObject jsonFileObject = new JSONObject();
		jsonFileObject.put("node_path", jsonData.getString("content_management_folder") + "/" + jsonData.getString("terminology_name"));
		jsonFileObject.put("submittal_type", submittalType);
		jsonFileObject.put("data", terminologyDocumentData);
		jsonFileObject.put("fuzein_mimetype", terminologyMimetype);
		jsonFileObject.put("document_metadata", jsonMetadataObject);
		jsonFileObject.put("document_attachments", jsonAttachmentsObject);
		
		//
		// Submit to the Content Management Service
		JSONObject jsonMessageObject = new JSONObject();
		jsonMessageObject.put("service_meta", "fuzein_cm_service");
		jsonMessageObject.put("service_action", "update_content");
		jsonMessageObject.put("service_api_data", jsonFileObject);
		
		String serviceResponse = fuzeInCommunication.callService(identificationKey, jsonMessageObject);
		JSONObject jsonResponse = processServiceResponse(serviceResponse);

		//
		// Process response
		JSONObject jsonTerminologyObject = new JSONObject();
		
		if(jsonResponse.getString(AdapterConstants.ADAPTER_STATUS).equals(AdapterConstants.status.SUCCESS.toString()))
		{
			JSONObject jsonDataObject = jsonResponse.getJSONArray(AdapterConstants.ADAPTER_DATA).getJSONObject(0);

			//
			jsonTerminologyObject.put("terminology_status", jsonDataObject.getString("terminology_status"));
			jsonTerminologyObject.put("terminology_original_author", jsonDataObject.getString("terminology_original_author"));
			jsonTerminologyObject.put("terminology_modification_author", jsonDataObject.getString("terminology_modification_author"));
			jsonTerminologyObject.put("terminology_date", jsonDataObject.getString("node_created_date"));
			jsonTerminologyObject.put("terminology_name", jsonDataObject.getString("node_name"));
			jsonTerminologyObject.put("terminology_folder", jsonDataObject.getString("parent_node_path"));
			//jsonTerminologyObject.put("terminology_schema_name", jsonDataObject.getString("terminology_original_author"));
		}
		
		//
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.SUCCESS);
		jsonObject.put(AdapterConstants.ADAPTER_DATA, jsonTerminologyObject);

		return jsonObject;
	}
	
	//
	public JSONObject deleteTerminology(final String identificationKey, final JSONObject jsonData) throws Exception
	{
		JSONObject jsonObject = new JSONObject();
		
		//
		// Validate that all required params are present
		if(jsonData.has("content_management_folder") == false)
		{
			throw new Exception("The 'content_management_folder' parameter is required.");
		}
		
		if(jsonData.has("terminology_name") == false)
		{
			throw new Exception("The 'terminology_name' parameter is required.");
		}
		
		//
		// Submit to the Content Management Service
		JSONObject jsonCMObject = new JSONObject();
		jsonCMObject.put("node_path", jsonData.getString("content_management_folder") + "/" + jsonData.getString("terminology_name"));

		JSONObject jsonMessageObject = new JSONObject();
		jsonMessageObject.put("service_meta", "fuzein_cm_service");
		jsonMessageObject.put("service_action", "delete_content");
		jsonMessageObject.put("service_api_data", jsonCMObject);
		
		String serviceResponse = fuzeInCommunication.callService(identificationKey, jsonMessageObject);
		JSONObject jsonResponse = processServiceResponse(serviceResponse);
		
		//
		// Process response
		JSONObject jsonTerminologyObject = new JSONObject();

		if(jsonResponse.getString(AdapterConstants.ADAPTER_STATUS).equals(AdapterConstants.status.SUCCESS.toString()))
		{
			jsonTerminologyObject.put("terminology_name", jsonData.getString("terminology_name"));
			jsonTerminologyObject.put("terminology_folder", jsonData.getString("content_management_folder"));
		}
		
		//
		jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.SUCCESS);
		jsonObject.put(AdapterConstants.ADAPTER_DATA, jsonTerminologyObject);

		return jsonObject;
	}
	
	//
	public JSONObject getTerminologies(final String identificationKey, final JSONObject jsonData) throws Exception
	{
		//
		// Validate that all required params are present
		if(jsonData.has("user_information") == false)
		{
			throw new Exception("The 'user_information' object is required.");
		}
		
		//
		// Set Properties to Search against
		JSONArray jsonProperties = new JSONArray();
		
		JSONObject jsonPropObject = new JSONObject();
		jsonPropObject.put(JackRabbitPropertyOperator.PROPERTY_OPERATOR, JackRabbitPropertyOperator.operator.AND);		
		jsonPropObject.put(TerminologyStatus.TERMINOLOGY_STATUS, TerminologyStatus.status.NEW_TERMINOLOGY);
		jsonProperties.put(jsonPropObject);
		
		jsonPropObject = new JSONObject();
		jsonPropObject.put(JackRabbitPropertyOperator.PROPERTY_OPERATOR, JackRabbitPropertyOperator.operator.OR);
		jsonPropObject.put(TerminologyStatus.TERMINOLOGY_STATUS, TerminologyStatus.status.UPDATED_TERMINOLOGY);
		jsonProperties.put(jsonPropObject);
		
		jsonPropObject = new JSONObject();
		jsonPropObject.put(JackRabbitPropertyOperator.PROPERTY_OPERATOR, JackRabbitPropertyOperator.operator.OR);
		jsonPropObject.put(TerminologyStatus.TERMINOLOGY_STATUS, TerminologyStatus.status.REJECTED_TERMINOLOGY);
		jsonProperties.put(jsonPropObject);		
		
		jsonPropObject = new JSONObject();
		jsonPropObject.put(JackRabbitPropertyOperator.PROPERTY_OPERATOR, JackRabbitPropertyOperator.operator.OR);
		jsonPropObject.put(TerminologyStatus.TERMINOLOGY_STATUS, TerminologyStatus.status.SUBMITTED_TERMINOLOGY);
		jsonProperties.put(jsonPropObject);		
		
		jsonPropObject = new JSONObject();
		jsonPropObject.put(JackRabbitPropertyOperator.PROPERTY_OPERATOR, JackRabbitPropertyOperator.operator.OR);
		jsonPropObject.put(TerminologyStatus.TERMINOLOGY_STATUS, TerminologyStatus.status.APPROVED_TERMINOLOGY);
		jsonProperties.put(jsonPropObject);
		
		jsonPropObject = new JSONObject();
		jsonPropObject.put(JackRabbitPropertyOperator.PROPERTY_OPERATOR, JackRabbitPropertyOperator.operator.AND);
		jsonPropObject.put("jcr:mimeType", terminologyMimetype);
		jsonProperties.put(jsonPropObject);
		
		//
		// Build CM Object
		JSONObject jsonSearchObject = new JSONObject();
		jsonSearchObject.put("node_path", "/" + jsonData.getJSONObject("user_information").getString("user_id"));
		jsonSearchObject.put("node_properties", jsonProperties);
		
		//
		// Submit to the Content Management Service
		JSONObject jsonMessageObject = new JSONObject();
		jsonMessageObject.put("service_meta", "fuzein_cm_service");
		jsonMessageObject.put("service_action", "search_properties");
		jsonMessageObject.put("service_api_data", jsonSearchObject);
		
		String serviceResponse = fuzeInCommunication.callService(identificationKey, jsonMessageObject);
		JSONObject jsonResponse = processServiceResponse(serviceResponse);

		//
		// Process response
		JSONArray jsonTerminologiesObject = new JSONArray();
		
		if(jsonResponse.getString(AdapterConstants.ADAPTER_STATUS).equals(AdapterConstants.status.SUCCESS.toString()))
		{
			JSONArray jsonDataArray = jsonResponse.getJSONArray(AdapterConstants.ADAPTER_DATA);
			
			for(int iIndex = 0; iIndex < jsonDataArray.length(); iIndex++)
			{
				JSONObject jsonDataObject = jsonDataArray.getJSONObject(iIndex);
				
				JSONObject jsonTermObject = new JSONObject();
				jsonTermObject.put("terminology_status", jsonDataObject.getString("terminology_status"));
				jsonTermObject.put("terminology_original_author", jsonDataObject.getString("terminology_original_author"));
				jsonTermObject.put("terminology_date", jsonDataObject.getString("node_created_date"));
				jsonTermObject.put("terminology_name", jsonDataObject.getString("node_name"));
				jsonTermObject.put("terminology_folder", jsonDataObject.getString("parent_node_path"));
				
				jsonTerminologiesObject.put(jsonTermObject);
			}
		}
		
		//
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.SUCCESS);
		jsonObject.put(AdapterConstants.ADAPTER_DATA, jsonTerminologiesObject);

		return jsonObject;
	}
	
	public JSONObject getTerminology(final String identificationKey, final JSONObject jsonData) throws Exception
	{
		//
		// Validate that all required params are present
		if(jsonData.has("content_management_folder") == false)
		{
			throw new Exception("The 'content_management_folder' parameter is required.");
		}
		
		if(jsonData.has("user_information") == false)
		{
			throw new Exception("The 'user_information' object is required.");
		}
  		
		if(jsonData.has("terminology_name") == false)
		{
			throw new Exception("The 'terminology_name' object is required.");
		}
		
		//
		// Build CM Object
		JSONObject jsonFileObject = new JSONObject();
		jsonFileObject.put("node_path", jsonData.getString("content_management_folder") + "/" + jsonData.getString("terminology_name"));
		
		//
		// Submit to the Content Management Service
		JSONObject jsonMessageObject = new JSONObject();
		jsonMessageObject.put("service_meta", "fuzein_cm_service");
		jsonMessageObject.put("service_action", "get_content");
		jsonMessageObject.put("service_api_data", jsonFileObject);
		
		//
		String serviceResponse = fuzeInCommunication.callService(identificationKey, jsonMessageObject);
		JSONObject jsonResponse = processServiceResponse(serviceResponse);

		//
		// Process response
		JSONObject jsonTerminologyObject = new JSONObject();
		
		if(jsonResponse.getString(AdapterConstants.ADAPTER_STATUS).equals(AdapterConstants.status.SUCCESS.toString()))
		{
 		JSONObject jsonDataObject = jsonResponse.getJSONObject(AdapterConstants.ADAPTER_DATA);
			
	 	LMFTerminologyParser lmfTerminologyParser = new LMFTerminologyParser();
	 	lmfTerminologyParser.parse(jsonDataObject);
	 	jsonTerminologyObject = lmfTerminologyParser.toJSON();
		}
 	
		//
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.SUCCESS);
		jsonObject.put(AdapterConstants.ADAPTER_DATA, jsonTerminologyObject);
		
		return jsonObject;
	}
	
	public JSONObject getTerminologyProperties(final String identificationKey, final JSONObject jsonData) throws Exception
	{
		JSONObject jsonObject = new JSONObject();
	
		//
		// Validate that all required params are present
		if(jsonData.has("content_management_folder") == false)
		{
			throw new Exception("The 'content_management_folder' parameter is required.");
		}
		
		if(jsonData.has("terminology_name") == false)
		{
			throw new Exception("The 'terminology_name' parameter is required.");
		}
		
		//
		// Build CM Object
		JSONObject jsonFileObject = new JSONObject();
		jsonFileObject.put("node_path", jsonData.getString("content_management_folder") + "/" + jsonData.getString("terminology_name"));
		
		//
		// Submit to the Content Management Service
		JSONObject jsonMessageObject = new JSONObject();
		jsonMessageObject.put("service_meta", "fuzein_cm_service");
		jsonMessageObject.put("service_action", "get_content_properties");
		jsonMessageObject.put("service_api_data", jsonFileObject);
		
		String serviceResponse = fuzeInCommunication.callService(identificationKey, jsonMessageObject);
  JSONObject jsonResponse = new JSONObject(serviceResponse);
  
  //
  // Process response
  if(jsonResponse.has(ServiceConstants.SERVICE_STATUS) && jsonResponse.getString(AdapterConstants.ADAPTER_DATA).equals(AdapterConstants.status.SUCCESS))
  {
 		jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.SUCCESS);
 		jsonObject.put(AdapterConstants.ADAPTER_DATA, jsonResponse.getJSONObject(AdapterConstants.ADAPTER_DATA));
  }
  else
  {
  	jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.SUCCESS);
  }

  //
		return jsonObject;
	}
		
 //
	public JSONObject submitTerminology(final String identificationKey, final JSONObject jsonData) throws Exception
	{
		//
		// Validate that all required params are present
		if(jsonData.has("content_management_folder") == false)
		{
			throw new Exception("The 'content_management_folder' parameter is required.");
		}
		
		if(jsonData.has("user_information") == false)
		{
			throw new Exception("The 'user_information' object is required.");
		}
  		
		if(jsonData.has("terminology_name") == false)
		{
			throw new Exception("The 'terminology_name' object is required.");
		}
		
		if(jsonData.has("group_id") == false)
		{
			throw new Exception("The 'group_id' object is required.");
		}
		
		//
		// Extract User Information
		JSONObject jsonUserInformation = jsonData.getJSONObject("user_information");
		
		//
		// Create references (if required)
		String referenceCMDirectory = jsonData.getString("content_management_folder");
		String referenceDocument = jsonData.getString("terminology_name");
		
		createReference(identificationKey, jsonUserInformation, jsonData.getString("group_id"), referenceCMDirectory, referenceDocument);
		
		//
		// Update the Document status in the CM
		JSONObject jsonMetadataObject = new JSONObject();
		jsonMetadataObject.put(TerminologyStatus.TERMINOLOGY_STATUS, TerminologyStatus.status.SUBMITTED_TERMINOLOGY);

		//
		// Build CM Object
		JSONObject jsonFileObject = new JSONObject();
		jsonFileObject.put("node_path", referenceCMDirectory + "/" + referenceDocument);
		jsonFileObject.put("document_metadata", jsonMetadataObject);
		
		//
		// Submit to the Content Management Service
		JSONObject jsonMessageObject = new JSONObject();
		jsonMessageObject.put("service_meta", "fuzein_cm_service");
		jsonMessageObject.put("service_action", "update_metadata");
		jsonMessageObject.put("service_api_data", jsonFileObject);
		
		//
		String serviceResponse = fuzeInCommunication.callService(identificationKey, jsonMessageObject);
		JSONObject jsonServiceResponse = processServiceResponse(serviceResponse);

		//
		// Process response
		JSONObject jsonTerminologyObject = new JSONObject();

		if(jsonServiceResponse.getString(AdapterConstants.ADAPTER_STATUS).equals(AdapterConstants.status.SUCCESS.toString()))
		{
			JSONObject jsonDataObject = jsonServiceResponse.getJSONArray(AdapterConstants.ADAPTER_DATA).getJSONObject(0);
			
			//
			jsonTerminologyObject.put("terminology_status", jsonDataObject.getString("terminology_status"));
			jsonTerminologyObject.put("terminology_original_author", jsonDataObject.getString("terminology_original_author"));
			jsonTerminologyObject.put("terminology_date", jsonDataObject.getString("node_created_date"));
			jsonTerminologyObject.put("terminology_name", jsonDataObject.getString("node_name"));
			jsonTerminologyObject.put("terminology_folder", jsonDataObject.getString("parent_node_path"));
		}
		
		//
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.SUCCESS);
		jsonObject.put(AdapterConstants.ADAPTER_DATA, jsonTerminologyObject);

		return jsonObject;	
	}

	//
	public JSONObject rejectTerminology(final String identificationKey, final JSONObject jsonData) throws Exception
	{
		// Validate that all required params are present
		if(jsonData.has("content_management_folder") == false)
		{
			throw new Exception("The 'content_management_folder' parameter is required.");
		}
		
		if(jsonData.has("user_information") == false)
		{
			throw new Exception("The 'user_information' object is required.");
		}
  		
		if(jsonData.has("terminology_name") == false)
		{
			throw new Exception("The 'terminology_name' object is required.");
		}
		
		//
		// Extract User Information
		JSONObject jsonUserInformation = jsonData.getJSONObject("user_information");
		
		//
		// Update the Document Metadata
		JSONObject jsonMetadataObject = new JSONObject();
		jsonMetadataObject.put(TerminologyStatus.TERMINOLOGY_STATUS, TerminologyStatus.status.REJECTED_TERMINOLOGY);
	 jsonMetadataObject.put(TerminologyMetadata.TERMINOLOGY_VETTING_AUTHOR, jsonUserInformation.getString("user_name")); 

		//
		// Build CM Object
		JSONObject jsonFileObject = new JSONObject();
		jsonFileObject.put("node_path", jsonData.getString("content_management_folder") + "/" + jsonData.getString("terminology_name"));
		jsonFileObject.put("document_metadata", jsonMetadataObject);
		
		//
		// Submit to the Content Management Service
		JSONObject jsonMessageObject = new JSONObject();
		jsonMessageObject.put("service_meta", "fuzein_cm_service");
		jsonMessageObject.put("service_action", "update_metadata");
		jsonMessageObject.put("service_api_data", jsonFileObject);
		
		//
		String serviceResponse = fuzeInCommunication.callService(identificationKey, jsonMessageObject);
		JSONObject jsonServiceResponse = processServiceResponse(serviceResponse);
		
		//
		// Process response
		JSONObject jsonTerminologyObject = new JSONObject();

		if(jsonServiceResponse.getString(AdapterConstants.ADAPTER_STATUS).equals(AdapterConstants.status.SUCCESS.toString()))
		{
			JSONObject jsonDataObject = jsonServiceResponse.getJSONArray(AdapterConstants.ADAPTER_DATA).getJSONObject(0);

			//
			// After the Terminology has been REJECTED, remove all references that might be associated to a vetter
			boolean isTerminologyOwner = false;
			String terminologyOriginalAuthor = jsonDataObject.getString("terminology_original_author");
			
			if(jsonUserInformation.getString("user_name").equals(terminologyOriginalAuthor))
			{
				isTerminologyOwner = true;
			}
			else
			{
				isTerminologyOwner = false;

				//
				// Remove any references 
				removeReference(identificationKey, jsonData.getString("content_management_folder") + "/" + jsonData.getString("terminology_name"));
			}

			//
			//
			jsonTerminologyObject.put("remove_terminology", !isTerminologyOwner);
			jsonTerminologyObject.put("terminology_status", jsonDataObject.getString("terminology_status"));
			jsonTerminologyObject.put("terminology_original_author", jsonDataObject.getString("terminology_original_author"));
			jsonTerminologyObject.put("terminology_date", jsonDataObject.getString("node_created_date"));
			jsonTerminologyObject.put("terminology_name", jsonDataObject.getString("node_name"));
			jsonTerminologyObject.put("terminology_folder", jsonDataObject.getString("parent_node_path"));
		}
		
		//
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.SUCCESS);
		jsonObject.put(AdapterConstants.ADAPTER_DATA, jsonTerminologyObject);

		return jsonObject;
	}
		
	//
	public JSONObject publishTerminology(final String identificationKey, final JSONObject jsonData) throws Exception
	{
		//
		// Validate that all required params are present
		if(jsonData.has("submittal_type") == false)
		{
			throw new Exception("The 'submittal_type' parameter is required.");
		}
		
		if(jsonData.has("content_management_folder") == false)
		{
			throw new Exception("The 'content_management_folder' parameter is required.");
		}
		
		if(jsonData.has("user_information") == false)
		{
			throw new Exception("The 'user_information' object is required.");
		}
  		
		if(jsonData.has("terminology_name") == false)
		{
			throw new Exception("The 'terminology_name' object is required.");
		}
		
		if(jsonData.has("collection") == false)
		{
			throw new Exception("The 'collection' object is required.");
		}
		
		if(jsonData.getJSONArray("collection").length() == 0)
		{
			throw new Exception("The 'collection object' is empty");
		}
		
		//
		// Get the Document from the Content Management Service
		JSONObject jsonFileObject = new JSONObject();
		jsonFileObject.put("node_path", jsonData.getString("content_management_folder") + "/" + jsonData.getString("terminology_name"));
		
		//
		// Submit to the Content Management Service
		JSONObject jsonMessageObject = new JSONObject();
		jsonMessageObject.put("service_meta", "fuzein_cm_service");
		jsonMessageObject.put("service_action", "get_content");
		jsonMessageObject.put("service_api_data", jsonFileObject);
		
		//
		String serviceResponse = fuzeInCommunication.callService(identificationKey, jsonMessageObject);
		JSONObject jsonServiceResponse = processServiceResponse(serviceResponse);
		
		//
		// Check that CM Service status is OK
		if(jsonServiceResponse.getString(AdapterConstants.ADAPTER_STATUS).equals(AdapterConstants.status.FAILED.toString()))
		{
			throw new Exception("The Terminology Document could not be retrieved.");
		}
		
		//
		// Process Document based on submittal type
		String submittalType = jsonData.getString("submittal_type");
	
		//
		// Validate against schema
		JSONObject jsonDataObject = null;
		String terminologyDocument = null;
		
		if(submittalType.equals("json"))
		{
			jsonDataObject = jsonServiceResponse.getJSONObject(AdapterConstants.ADAPTER_DATA);

			//
			// Read XML String
			ByteArrayInputStream byteArrayInputStream = null;
			
			try
			{
		 	// Generate a publishable Terminology Document
				String xmlString = LMFTerminologyPublisher.publish(jsonDataObject, jsonData.getJSONArray("collection"));
				
			 byteArrayInputStream = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
			 Document document = saxBuilder.build(byteArrayInputStream);
				
				//
				// Validate against the LMF schema
			 terminologyDocument = XMLUtil.compactDocumentString(document);
				validateTerminology(terminologyDocument);
			}
			catch(Exception exception)
			{
				throw new Exception(exception);
			}
			finally
			{
			 byteArrayInputStream.close();
			}
		}
		
		//
		// Submit the Terminology Document over to the Datawarehouse for Storage and Indexing
		//
		JSONArray jsonResponseArray = new JSONArray();
		JSONArray jsonCollectionArray = jsonData.getJSONArray("collection");
		
		for(int iIndex = 0; iIndex < jsonCollectionArray.length(); iIndex++)
		{
			JSONObject jsonCollectionObject = jsonCollectionArray.getJSONObject(iIndex);
			
			//
			// Build DW object
			JSONObject dataObject = new JSONObject();
			dataObject.put("data_schema_name", jsonDataObject.getString("terminology_supporting_schema_name"));
			dataObject.put("indexing_schema_name", jsonDataObject.getString("terminology_supporting_indexing_schema_name"));
			dataObject.put("dictionary_name", jsonCollectionObject.getString("dictionary_name"));
			dataObject.put("document_name", jsonData.getString("terminology_name"));
			dataObject.put("document_content", terminologyDocument);
			
			jsonMessageObject = new JSONObject();
			jsonMessageObject.put("service_meta", "fuzein_datawarehouse_service");
			jsonMessageObject.put("service_action", "add_document");
			jsonMessageObject.put("service_api_data", dataObject);
   			
			//
			serviceResponse = fuzeInCommunication.callService(identificationKey, jsonMessageObject);		
			jsonServiceResponse = processServiceResponse(serviceResponse);

			if(jsonServiceResponse.getString(AdapterConstants.ADAPTER_STATUS).equals(AdapterConstants.status.SUCCESS.toString()))
			{
				// Extract User Information
				JSONObject jsonUserInformation = jsonData.getJSONObject("user_information");
				
				//
				// Update Terminology Metadata
				JSONObject jsonMetadataObject = new JSONObject();
			 jsonMetadataObject.put(TerminologyMetadata.TERMINOLOGY_VETTING_AUTHOR, jsonUserInformation.getString("user_name")); 
				jsonMetadataObject.put(TerminologyStatus.TERMINOLOGY_STATUS, TerminologyStatus.status.APPROVED_TERMINOLOGY);

				// Build CM Object
				jsonFileObject = new JSONObject();
				jsonFileObject.put("node_path", jsonData.getString("content_management_folder") + "/" + jsonData.getString("terminology_name"));
				jsonFileObject.put("document_metadata", jsonMetadataObject);
				
				//
				// Submit to the Content Management Service
				jsonMessageObject = new JSONObject();
				jsonMessageObject.put("service_meta", "fuzein_cm_service");
				jsonMessageObject.put("service_action", "update_metadata");
				jsonMessageObject.put("service_api_data", jsonFileObject);
				
				serviceResponse = fuzeInCommunication.callService(identificationKey, jsonMessageObject);
				jsonServiceResponse = processServiceResponse(serviceResponse);
				
				//
				// Process response
				JSONObject jsonTerminologyObject = new JSONObject();

				if(jsonServiceResponse.getString(AdapterConstants.ADAPTER_STATUS).equals(AdapterConstants.status.SUCCESS.toString()))
				{
					JSONObject jsonResponseObject = jsonServiceResponse.getJSONArray(AdapterConstants.ADAPTER_DATA).getJSONObject(0);

					//
					// After the Terminology has been REJECTED, remove all references that might be associated to a vetter
					boolean isTerminologyOwner = false;
					String terminologyOriginalAuthor = jsonResponseObject.getString("terminology_original_author");
					
					if(jsonUserInformation.getString("user_name").equals(terminologyOriginalAuthor))
					{
						isTerminologyOwner = true;
					}
					else
					{
						isTerminologyOwner = false;

						//
						// Remove any references from the CM
						removeReference(identificationKey, jsonData.getString("content_management_folder") + "/" + jsonData.getString("terminology_name"));
					}

					//
					//
					jsonTerminologyObject.put("remove_terminology", !isTerminologyOwner);
					jsonTerminologyObject.put("terminology_status", jsonResponseObject.getString("terminology_status"));
					jsonTerminologyObject.put("terminology_original_author", jsonResponseObject.getString("terminology_original_author"));
					jsonTerminologyObject.put("terminology_date", jsonResponseObject.getString("node_created_date"));
					jsonTerminologyObject.put("terminology_name", jsonResponseObject.getString("node_name"));
					jsonTerminologyObject.put("terminology_folder", jsonResponseObject.getString("parent_node_path"));
					
					jsonResponseArray.put(jsonTerminologyObject);
				}
			}
			else
			{
				System.out.println(" =========> " + jsonServiceResponse);
			}
		}
		
		//
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.SUCCESS);
		jsonObject.put(AdapterConstants.ADAPTER_DATA, jsonResponseArray);
		
		return jsonObject;
	}
	
	//
	public JSONObject searchLocalTerminology(final String identificationKey, final JSONObject jsonData) throws Exception
	{
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.UNSUPPORTED);
		return jsonObject;
	}
	
	//
	public JSONObject getUserSubmittalGroups(final String identificationKey, final JSONObject jsonData) throws Exception
	{
		// Validate that all required params are present
		if(jsonData.has("user_information") == false)
		{
			throw new Exception("The 'user_information' parameter is required.");
		}		
		
		// Get user Terminology Authorization.. what groups user is allowed to Submit too
		AuthorizationParser.parse(saxBuilder, authorizationFileName);
		JSONObject jsonGroupObject = AuthorizationParser.getGroups(jsonData.getJSONObject("user_information").getString("user_id"));
		
		//
		JSONObject jsonObject = new JSONObject();
 	jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.SUCCESS);
 	jsonObject.put(AdapterConstants.ADAPTER_DATA, jsonGroupObject);
 	
		return jsonObject;
	}
	
	//
	public JSONObject getUserVettingGroups(final String identificationKey, final JSONObject jsonData) throws Exception
	{
		// Validate that all required params are present
		if(jsonData.has("user_information") == false)
		{
			throw new Exception("The 'user_information' parameter is required.");
		}		
		
		//
	//	JSONObject jsonGroupObject = authorizationParser(jsonData.getJSONObject("user_information").getString("user_id"));
		
		//
		JSONObject jsonObject = new JSONObject();
 	jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.UNSUPPORTED);
 	//jsonObject.put(AdapterConstants.ADAPTER_DATA, jsonGroupObject);
 	
		return jsonObject;
	}

	//
	private void parseAdapterConfiguration(String adapterConfigurationFile) throws Exception
	{
		//
		// Parse Configuration
		Document configurationDocument = saxBuilder.build(adapterConfigurationFile);
		
		//
		// Get Validation schema
		XPath xPath = XPath.newInstance("terminology_configuration/validation_schema_file");
		Element validatorElement = (Element) xPath.selectSingleNode(configurationDocument);
		
		//
		// Set Terminology Validation
	 File schemaFile = new File(validatorElement.getText());
	 
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);

		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		factory.setSchema(schemaFactory.newSchema(new Source[] {new StreamSource(schemaFile)}));

		//
		SAXParser parser = factory.newSAXParser();
		terminologyValidator = parser.getXMLReader();
		
		//
		// Get Associated Schema Data
		terminologySchemaData = FileUtils.readFileToString(schemaFile, "UTF-8");

		//
		// Get Terminology Mimetype
		xPath = XPath.newInstance("terminology_configuration/terminology_mimetype");
		Element mimetypeElement = (Element) xPath.selectSingleNode(configurationDocument);
		terminologyMimetype = mimetypeElement.getText();
		
		//
		// Get Terminology Schema Name
		xPath = XPath.newInstance("terminology_configuration/terminology_schema_name");
		Element schemaElement = (Element) xPath.selectSingleNode(configurationDocument);
		terminologySchemaName = schemaElement.getText();
		
		//
		// Get Terminology Indexing Schema Name
		xPath = XPath.newInstance("terminology_configuration/terminology_indexing_schema_name");
		schemaElement = (Element) xPath.selectSingleNode(configurationDocument);
		terminologyIndexingSchemaName = schemaElement.getText();
		
		//
		// Get Terminology Authorization
		xPath = XPath.newInstance("terminology_configuration/terminology_authorization");
		schemaElement = (Element) xPath.selectSingleNode(configurationDocument);
		authorizationFileName = schemaElement.getText();
	}
	
	//
	private boolean validateTerminology(String terminologyDocument) throws Exception
	{
		ByteArrayInputStream objBAInputStream = new java.io.ByteArrayInputStream(terminologyDocument.getBytes("UTF-8"));
		terminologyValidator.parse(new InputSource(objBAInputStream));
		objBAInputStream.close();
		
		//
		return true;
	}
	
	private long getDictionary(String identificationKey, String dictionaryName) throws Exception
	{
		JSONObject jsonDictionaryObject = new JSONObject();
		jsonDictionaryObject.put("dictionary_name", dictionaryName);
		
		JSONObject jsonMessageObject = new JSONObject();
		jsonMessageObject.put("service_meta", "fuzein_datawarehouse_service");
		jsonMessageObject.put("service_action", "get_dictionary");
		jsonMessageObject.put("service_api_data", jsonDictionaryObject);
		
		String serviceResponse = fuzeInCommunication.callService(identificationKey, jsonMessageObject);		
		JSONObject jsonResponseObject = new JSONObject(serviceResponse);
		
		if(jsonResponseObject.has("service_status") && jsonResponseObject.getString("service_status").equals("SUCCESS"))
		{
			return jsonResponseObject.getJSONArray("dictionary_listing").getJSONObject(0).getLong("dw_dictionary_id");
 	}	
		
		return 0L;
	}
	
	private long getSchema(String identificationKey, String schemaName) throws Exception
	{
		JSONObject jsonDictionaryObject = new JSONObject();
		jsonDictionaryObject.put("schema_name", schemaName);
		
		JSONObject jsonMessageObject = new JSONObject();
		jsonMessageObject.put("service_meta", "fuzein_datawarehouse_service");
		jsonMessageObject.put("service_action", "get_schema");
		jsonMessageObject.put("service_api_data", jsonDictionaryObject);
		
		String serviceResponse = fuzeInCommunication.callService(identificationKey, jsonMessageObject);		
		JSONObject jsonResponseObject = new JSONObject(serviceResponse);
		
		if(jsonResponseObject.has("service_status") && jsonResponseObject.getString("service_status").equals("SUCCESS"))
		{
			return jsonResponseObject.getJSONArray("schema_listing").getJSONObject(0).getLong("dw_schema_id");
 	}	
		
		return 0L;
	}
	
	private boolean createReference(final String identificationKey, 
																																	final JSONObject jsonUserInformation, 
																																	final String groupID, 
																																	final String referencedCMDirectory,
																																	final String referencedDocument) throws Exception
	{
		//
		// Extract The Vetter Information from the passed Group
		AuthorizationParser.parse(saxBuilder, authorizationFileName);
		JSONArray jsonGroupVetters = AuthorizationParser.getAvailableVettersForGroup(groupID);
		
		//
		// For each vetter found in the Submitted Group ID
		//
		for(int iIndex = 0; iIndex < jsonGroupVetters.length(); iIndex++)
		{
			JSONObject jsonVetterObject = jsonGroupVetters.getJSONObject(iIndex);
			
			//
			// Add a reference ONLY AND ONLY IF the Submitter is NOT the same as the Vetter
			if(jsonUserInformation.getString("user_id").equals(jsonVetterObject.getString("user_id")) == false)
			{
				//
				// HACK HACK HACK
				//
				String vettingUserRepository = "/" + jsonVetterObject.getString("user_id") + "/Desktop/My Documents/Submitted Terminologies";
				
				//
				// Get the user's repository information from the ContentManagement Service
				//
				/*JSONObject serviceAPIData = new JSONObject();
				serviceAPIData.put("user_id", jsonObject.getString("user_id"));
				
				JSONObject jsonServiceObject = new JSONObject();
				jsonServiceObject.put("service_meta", "fuzein_cm_service");
				jsonServiceObject.put("service_action", "get_user_repository_info");
				jsonServiceObject.put("service_api_data", serviceAPIData);
				
				String serviceResponse = fuzeInCommunication.callService(identificationKey, jsonServiceObject);
				JSONObject jsonResponse = processServiceResponse(serviceResponse);*/
				
				//
				// Extract User Information
			 // TDB TDB TDB 
	
				
				//
				// Add Terminology Metadata
				JSONObject jsonMetadataObject = new JSONObject();
			 jsonMetadataObject.put(TerminologyMetadata.TERMINOLOGY_ORIGINAL_AUTHOR, jsonUserInformation.getString("user_name")); 
				jsonMetadataObject.put(TerminologyStatus.TERMINOLOGY_STATUS, TerminologyStatus.status.SUBMITTED_TERMINOLOGY);
				
				//
				// Build CM Object
				JSONObject jsonFileObject = new JSONObject();
				jsonFileObject.put("parent_folder", vettingUserRepository);
				jsonFileObject.put("node_name", referencedDocument);		
				jsonFileObject.put("referenced_user_id", jsonVetterObject.getString("user_id"));
				jsonFileObject.put("reference_document", referencedCMDirectory + "/" + referencedDocument);
				jsonFileObject.put("reference_metadata", jsonMetadataObject);
				jsonFileObject.put("fuzein_mimetype", terminologyMimetype);
	
				//
				// Submit the REFERENCED Document to the Content Management Service
				JSONObject jsonMessageObject = new JSONObject();
				jsonMessageObject.put("service_meta", "fuzein_cm_service");
				jsonMessageObject.put("service_action", "add_reference");
				jsonMessageObject.put("service_api_data", jsonFileObject);
				
				String serviceResponse = fuzeInCommunication.callService(identificationKey, jsonMessageObject);
				JSONObject jsonResponse = processServiceResponse(serviceResponse); 
			}
		}
		
		//
		return true;
	}
	
	private boolean removeReference(final String identificationKey, 
//																																	final JSONObject jsonUserInformation, 
																																	final String referencedDocument) throws Exception
	{
		//
		// Extract The Vetter Information from the passed Group
		AuthorizationParser.parse(saxBuilder, authorizationFileName);
		JSONArray jsonVetterArray = AuthorizationParser.getAvailableVetters();
		
		//
		// For each vetter found in the Group ID
		//
		for(int iIndex = 0; iIndex < jsonVetterArray.length(); iIndex++)
		{
			JSONObject jsonVetterObject = jsonVetterArray.getJSONObject(iIndex);
			
			//
			// HACK HACK HACK
			//
			String vettingUserRepository = "/" + jsonVetterObject.getString("user_id") + "/Desktop/My Documents/Submitted Terminologies";
			
			//
			// Get the user's repository information from the ContentManagement Service
			//
			/*JSONObject serviceAPIData = new JSONObject();
			serviceAPIData.put("user_id", jsonObject.getString("user_id"));
			
			JSONObject jsonServiceObject = new JSONObject();
			jsonServiceObject.put("service_meta", "fuzein_cm_service");
			jsonServiceObject.put("service_action", "get_user_repository_info");
			jsonServiceObject.put("service_api_data", serviceAPIData);
			
			String serviceResponse = fuzeInCommunication.callService(identificationKey, jsonServiceObject);
			JSONObject jsonResponse = processServiceResponse(serviceResponse);*/
			
			//
			// Extract User Information
			// TDB TDB TDB 
			
			
			//
			// Add Terminology Metadata
			JSONArray jsonProperties = new JSONArray();
			
			JSONObject jsonPropObject = new JSONObject();
			jsonPropObject.put(TerminologyStatus.TERMINOLOGY_STATUS, TerminologyStatus.status.SUBMITTED_TERMINOLOGY);
			
			jsonPropObject = new JSONObject();
			jsonPropObject.put(JackRabbitPropertyOperator.PROPERTY_OPERATOR, JackRabbitPropertyOperator.operator.AND);
			jsonPropObject.put("jcr:mimeType", terminologyMimetype);
			jsonProperties.put(jsonPropObject);
			
			//
			// Build CM Object
			JSONObject jsonFileObject = new JSONObject();
			jsonFileObject.put("node_path", vettingUserRepository);		
			jsonFileObject.put("referenced_document", referencedDocument);
			jsonFileObject.put("node_properties", jsonProperties);
			
			//
			// Submit to the Content Management Service
			JSONObject jsonMessageObject = new JSONObject();
			jsonMessageObject.put("service_meta", "fuzein_cm_service");
			jsonMessageObject.put("service_action", "delete_reference");
			jsonMessageObject.put("service_api_data", jsonFileObject);
			
			String serviceResponse = fuzeInCommunication.callService(identificationKey, jsonMessageObject);
			JSONObject jsonResponse = processServiceResponse(serviceResponse); 
			System.out.println(jsonResponse);
		}
		
		//
		return true;
 }	
}
