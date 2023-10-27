package com.filenet.filenet.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.security.auth.Subject;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.filenet.api.admin.StorageArea;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.DocumentSet;
import com.filenet.api.collection.ObjectStoreSet;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.AutoUniqueName;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.ClassNames;
import com.filenet.api.constants.DefineSecurityParentage;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Connection;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.ReferentialContainmentRelationship;
// import com.filenet.api.core.Factory.StorageArea;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;
import com.filenet.filenet.component.P8Connector;
import com.filenet.filenet.model.ReqCreateDoc;

@Service
public class P8Service {
    private final P8Connector p8Connector;
    private static final String uri = "http://192.168.206.99:9080/wsi/FNCEWS40MTOM/";
    private static final String username = "adam";
    private static final String password = "P@ssw0rdnd5";

    
    public P8Service(P8Connector p8Connector){
        this.p8Connector = p8Connector;
    }

    public void doSomething() {
        // Memanggil fungsi newFunction dari P8Connector
        System.out.println("Ini hasilnya >>> " +p8Connector.tryConnection());
    }

    public String testConnectionP8(){
        try {
            String result = p8Connector.tryConnectionP8();
            return result;
        } catch (Exception e) {
            System.out.println(e);
            return "Error";
        }
    }

    public ArrayList<String> getDocumentList(){
        // Make connection.
        Connection conn = Factory.Connection.getConnection(uri);
        Subject subject = UserContext.createSubject(conn, username, password, null);
        UserContext.get().pushSubject(subject);

        try {
        // Get default domain.

            Domain domain = Factory.Domain.fetchInstance(conn, null, null);
            System.out.println("Domain: " + domain.get_Name());

            // Get object stores for domain.
            ObjectStoreSet osSet = domain.get_ObjectStores();
            ObjectStore store = null;
            
            @SuppressWarnings("rawtypes")
            Iterator osIter = osSet.iterator();

                while (osIter.hasNext()) {
                    store = (ObjectStore) osIter.next();
                }
            System.out.println("Object store: " + store.get_Name());

            ArrayList<String> listDoc = getDocumentList(store);
            
            return listDoc;

        } catch(Exception e) {
            
            e.printStackTrace();
            return null;

        } finally {
            UserContext.get().popSubject();
        }
    }

    @SuppressWarnings("rawtypes")
    private static ArrayList<String> getDocumentList(ObjectStore os){
        ArrayList<String> listDocument = new ArrayList<String>();
        try {

		    String sqlStr = "SELECT PathName FROM Folder Where FolderName = 'data1'";

		    SearchSQL searchSQL = new SearchSQL(sqlStr);
            SearchScope searchScope = new SearchScope(os);
            Boolean continuable = true;
            RepositoryRowSet rowSet = searchScope.fetchRows(searchSQL, null, null, continuable);

            PropertyFilter pf = new PropertyFilter();
            pf.addIncludeProperty(new FilterElement(1, null, null, PropertyNames.PATH_NAME, null));
            pf.addIncludeProperty(new FilterElement(2, null, null, PropertyNames.CONTAINED_DOCUMENTS, null));
            pf.addIncludeProperty(new FilterElement(2, null, null, "DocumentTitle", null));

            Folder admissionFolder = null;
            
            Iterator rowIter = rowSet.iterator();

            while (rowIter.hasNext()) {

			RepositoryRow rr = (RepositoryRow) rowIter.next();
			Properties properties = rr.getProperties();
			String folderPath = properties.getStringValue("PathName").toString();
			admissionFolder = Factory.Folder.fetchInstance(os, folderPath, pf);
            
            DocumentSet documentSet = admissionFolder.get_ContainedDocuments();
            
            Iterator documentIterator = documentSet.iterator();

                while (documentIterator.hasNext()) {
                    Document document = (Document) documentIterator.next();
                    String documentName = document.getProperties().getStringValue("DocumentTitle");
                    listDocument.add(documentName);
                }
		    }
		    return listDocument;
        } catch (Exception e) {
            e.printStackTrace();
            ArrayList<String> error = new ArrayList<>();
            error.add("INTERNAL SERVER ERROR");
            return error;
        }
    }

    public String deleteDocument(String Docid){
        // Make connection.
        Connection conn = Factory.Connection.getConnection(uri);
        Subject subject = UserContext.createSubject(conn, username, password, null);
        UserContext.get().pushSubject(subject);

        try {
        // Get default domain.

            Domain domain = Factory.Domain.fetchInstance(conn, null, null);
            System.out.println("Domain: " + domain.get_Name());

            // Get object stores for domain.
            ObjectStoreSet osSet = domain.get_ObjectStores();
            ObjectStore store = null;
            
            @SuppressWarnings("rawtypes")
            Iterator osIter = osSet.iterator();

                while (osIter.hasNext()) {
                    store = (ObjectStore) osIter.next();
                }
            System.out.println("Object store: " + store.get_Name());

            String resultDelete = deleteDocument(store, "{" + Docid + "}");

            return resultDelete;

        } catch(Exception e) {
            
            e.printStackTrace();
            return "INTERNAL SERVER ERROR";

        } finally {
            UserContext.get().popSubject();
        }
    }

    private static String deleteDocument(ObjectStore os, String DocId){
        try {

            Document doc = Factory.Document.getInstance(os, ClassNames.DOCUMENT, new Id(DocId) );
            doc.delete();
            doc.save(RefreshMode.NO_REFRESH);

            return "Succes";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }

    public String updateDocument(String DocId, String newName){
        // Make connection.
        Connection conn = Factory.Connection.getConnection(uri);
        Subject subject = UserContext.createSubject(conn, username, password, null);
        UserContext.get().pushSubject(subject);

        try {
        // Get default domain.

            Domain domain = Factory.Domain.fetchInstance(conn, null, null);
            System.out.println("Domain: " + domain.get_Name());

            // Get object stores for domain.
            ObjectStoreSet osSet = domain.get_ObjectStores();
            ObjectStore store = null;
            
            @SuppressWarnings("rawtypes")
            Iterator osIter = osSet.iterator();

                while (osIter.hasNext()) {
                    store = (ObjectStore) osIter.next();
                }
            System.out.println("Object store: " + store.get_Name());

            String resultUpdate = updateDocument(store, "{"+ DocId +"}", newName);

            return resultUpdate;
            
        } catch(Exception e) {
            
            e.printStackTrace();
            return "INTERNAL SERVER ERROR";

        } finally {
            UserContext.get().popSubject();
        }
    }
    @SuppressWarnings("rawtypes")
    private static String updateDocument(ObjectStore os, String DocID, String newName) throws Exception {
        try {

            // Get document and populate property cache.
            PropertyFilter pf = new PropertyFilter();
            pf.addIncludeProperty(new FilterElement(null, null, null, "DocumentTitle", null));
            pf.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.MIME_TYPE, null));
            Document doc = Factory.Document.fetchInstance(os, new Id(DocID),pf );
            // doc.set_MimeType("text/plain");
            String jenisFile = doc.get_MimeType();
            System.out.println("jenis file >>>>> " +jenisFile);

            // Return document properties.
            com.filenet.api.property.Properties props = doc.getProperties();


            // Iterate the set and print property values. 
            Iterator iter = props.iterator();
            System.out.println("Property" +"\t" + "Value");
            System.out.println("------------------------");
            while (iter.hasNext() )
            {
                Property prop = (Property)iter.next();
                if (prop.getPropertyName().equals("DocumentTitle") )
                    System.out.println(prop.getPropertyName() + "\t" + prop.getStringValue() );
                else if (prop.getPropertyName().equals(PropertyNames.MIME_TYPE) )
                    System.out.println(prop.getPropertyName() + "\t" + prop.getStringValue() );
            }

            // Change property value.
            props.putValue("DocumentTitle", newName);

            // Save and update property cache.
            doc.save(RefreshMode.REFRESH);
            
            return "Succes";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }

    public String createDocument(ReqCreateDoc dataDoc){
        // Make connection.
        Connection conn = Factory.Connection.getConnection(uri);
        Subject subject = UserContext.createSubject(conn, username, password, null);
        UserContext.get().pushSubject(subject);

        try {
        // Get default domain.

            Domain domain = Factory.Domain.fetchInstance(conn, null, null);
            System.out.println("Domain: " + domain.get_Name());

            // Get object stores for domain.
            ObjectStoreSet osSet = domain.get_ObjectStores();
            ObjectStore store = null;
            
            @SuppressWarnings("rawtypes")
            Iterator osIter = osSet.iterator();

                while (osIter.hasNext()) {
                    store = (ObjectStore) osIter.next();
                }
            System.out.println("Object store: " + store.get_Name());

            String resultCreate = createDocument(store, dataDoc);

            return resultCreate;
            
        } catch(Exception e) {
            
            e.printStackTrace();
            return "INTERNAL SERVER ERROR";

        } finally {
            UserContext.get().popSubject();
        }
    }

    private static String createDocument(ObjectStore os, ReqCreateDoc dataDoc){
        try {

            String storageId  = "{9D6E75B7-91C5-4232-852E-6BAF333B18A0}";
            String FolderId = "{3A064AA6-B437-CD65-8716-837E8C000000}";
            String nameFile = dataDoc.nama_document;

            // Create a document instance.
            Document doc = Factory.Document.createInstance(os, ClassNames.DOCUMENT);

            // Set document properties.
            doc.getProperties().putValue("DocumentTitle", nameFile);
            doc.set_MimeType("text/plain");

            StorageArea sa = Factory.StorageArea.getInstance(os, new Id(storageId) );

            doc.set_StorageArea(sa);
            doc.save(RefreshMode.NO_REFRESH );

            // Check in the document.
            doc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
            doc.save(RefreshMode.NO_REFRESH);

            // File the document.
            Folder folder = Factory.Folder.getInstance(os, ClassNames.FOLDER,
                    new Id(FolderId) );
            ReferentialContainmentRelationship rcr = folder.file(doc,
                    AutoUniqueName.AUTO_UNIQUE, "New Document via Java API",
                    DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
            rcr.save(RefreshMode.NO_REFRESH);

            return "Success";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }

    public String uploadDocument(JsonNode data, MultipartFile file){
        // Make connection.
        Connection conn = Factory.Connection.getConnection(uri);
        Subject subject = UserContext.createSubject(conn, username, password, null);
        UserContext.get().pushSubject(subject);

        try {
        // Get default domain.

            Domain domain = Factory.Domain.fetchInstance(conn, null, null);
            System.out.println("Domain: " + domain.get_Name());

            // Get object stores for domain.
            ObjectStoreSet osSet = domain.get_ObjectStores();
            ObjectStore store = null;
            
            @SuppressWarnings("rawtypes")
            Iterator osIter = osSet.iterator();

                while (osIter.hasNext()) {
                    store = (ObjectStore) osIter.next();
                }
            System.out.println("Object store: " + store.get_Name());

            String resultCreate = uploadDocument(store, data, file);

            return resultCreate;
            
        } catch(Exception e) {
            
            e.printStackTrace();
            return "INTERNAL SERVER ERROR";

        } finally {
            UserContext.get().popSubject();
        }
    }

    @SuppressWarnings("rawtypes")
    private static String uploadDocument(ObjectStore os, JsonNode data, MultipartFile file){
        try {

            String storageId  = "{9D6E75B7-91C5-4232-852E-6BAF333B18A0}";
            String FolderId = "{3A064AA6-B437-CD65-8716-837E8C000000}";
            String nameFile = data.get("name").asText();

            // Create a document instance.
            Document doc = Factory.Document.createInstance(os, ClassNames.DOCUMENT);

            // Tambahan
            ContentElementList ctl = Factory.ContentElement.createList();
            ContentTransfer ct = Factory.ContentTransfer.createInstance();
            
            File fileTemp = File.createTempFile("temp", null);

            try(FileOutputStream fos = new FileOutputStream(fileTemp)){
                fos.write(file.getBytes());
            }

            ct.setCaptureSource(new java.io.FileInputStream(fileTemp));
            ctl.add(ct);
            doc.set_ContentElements(ctl);


            // Set document properties.
            doc.getProperties().putValue("DocumentTitle", nameFile);
            doc.set_MimeType(file.getContentType());

            // StorageArea sa = Factory.StorageArea.getInstance(os, new Id(storageId) );

            // doc.set_StorageArea(sa);
            // doc.save(RefreshMode.NO_REFRESH );

            // Check in the document.
            doc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
            doc.save(RefreshMode.NO_REFRESH);

            // File the document.
            Folder folder = Factory.Folder.getInstance(os, ClassNames.FOLDER,
                    new Id(FolderId) );
            ReferentialContainmentRelationship rcr = folder.file(doc,
                    AutoUniqueName.AUTO_UNIQUE, "New Document via Java API",
                    DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
            rcr.save(RefreshMode.NO_REFRESH);

            return "Success";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }


    protected static PropertyFilter initPropertyFilter() {
		PropertyFilter pf = new PropertyFilter();
		pf.addIncludeProperty(new FilterElement(1, null, null, PropertyNames.CLASS_DESCRIPTION, null));
		pf.addIncludeProperty(new FilterElement(1, null, null, PropertyNames.ID, null));
		pf.addIncludeProperty(new FilterElement(1, null, null, PropertyNames.NAME, null));
		pf.addIncludeProperty(new FilterElement(1, null, null, PropertyNames.CONTAINED_DOCUMENTS, null));
		pf.addIncludeProperty(new FilterElement(1, null, null, PropertyNames.CONTAINMENT_NAME, null));
		pf.addIncludeProperty(new FilterElement(1, null, null, PropertyNames.CONTAINERS, null));
		pf.addIncludeProperty(new FilterElement(1, null, null, PropertyNames.CONTAINEES, null));
		pf.addIncludeProperty(new FilterElement(1, null, null, PropertyNames.CONTENT_ELEMENTS, null));
		pf.addIncludeProperty(new FilterElement(1, null, null, PropertyNames.RETRIEVAL_NAME, null));
		pf.addIncludeProperty(new FilterElement(1, null, null, PropertyNames.IS_CURRENT_VERSION, null));
		pf.addIncludeProperty(new FilterElement(1, null, null, PropertyNames.IS_RESERVED, null));
		pf.addIncludeProperty(new FilterElement(1, null, null, PropertyNames.CURRENT_VERSION, null));
		pf.addIncludeProperty(new FilterElement(1, null, null, PropertyNames.MIME_TYPE, null));
		pf.addIncludeProperty(new FilterElement(1, null, null, PropertyNames.TAIL, null));
		pf.addIncludeProperty(new FilterElement(1, null, null, PropertyNames.MAJOR_VERSION_NUMBER, null));
		pf.addIncludeProperty(new FilterElement(1, null, null, PropertyNames.FOLDERS_FILED_IN, null));
		pf.addIncludeProperty(new FilterElement(1, null, null, PropertyNames.FOLDER_NAME, null));

		pf.addIncludeProperty(new FilterElement(1, null, null, "HospitalUnit", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "FolderProcess", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "InvoiceNo", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "AdmissionNo", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "PayerID", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "PayerName", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "DocumentType", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "MrNo", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "LOB", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "ListAdmissionNo", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "DocumentTitle", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "InvoiceType", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "PayerGroup", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "OriginalFilename", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "IDCard", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "QueueId", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "PatientName", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "IsUpdateVersion", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "IsDuplicateDoc", null));

		pf.addIncludeProperty(new FilterElement(1, null, null, "UnitIDAX", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "CustomerAccountCodeAX", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "DocTrxDate", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "DocumentType", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "RetryCount", null));
		pf.addIncludeProperty(new FilterElement(1, null, null, "UUID", null));
		return pf;
	}


    private static void store(MultipartFile fileupload) throws FileNotFoundException, IOException { 
     	  Connection conn = Factory.Connection.getConnection(uri);
          UserContext.get().pushSubject(UserContext.createSubject(conn, username, password, null));
             
          Domain domain = Factory.Domain.fetchInstance(conn, null, null);
          ObjectStore os = Factory.ObjectStore.fetchInstance(domain, objectStoreName, null);
           
        // Get document and populate property cache.
           PropertyFilter pf = new PropertyFilter();
           pf.addIncludeProperty(new FilterElement(null, null, null, "DocumentTitle", null));
           Document doc = Factory.Document.createInstance(os, ClassNames.DOCUMENT);
	       ContentElementList contentElementList = Factory.ContentElement.createList();
	       ContentTransfer contentTransfer = Factory.ContentTransfer.createInstance();
	       File file = File.createTempFile("temp", null);
	       try (FileOutputStream fos = new FileOutputStream(file)) {
	           fos.write(fileupload.getBytes());
	       }
	       contentTransfer.setCaptureSource(new java.io.FileInputStream(file));
	       contentElementList.add(contentTransfer);
	       doc.set_ContentElements(contentElementList); 
	       
	          // Set document properties.
	          doc.getProperties().putValue("DocumentTitle", fileupload.getOriginalFilename());	          
	          doc.set_MimeType(fileupload.getContentType());
	          //doc.accessContentStream(0);
	          
	          doc.save(RefreshMode.NO_REFRESH );
	          
	          // Check in the document.
	          doc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
	          doc.save(RefreshMode.NO_REFRESH);

	          // File the document.
	          Folder folder = Factory.Folder.getInstance(os, ClassNames.FOLDER,
	                  new Id("{009E468B-0000-C210-8686-461FF52CAA72}") );
	          ReferentialContainmentRelationship rcr = folder.file(doc,
	                  AutoUniqueName.AUTO_UNIQUE, "New Document via Java API",
	                  DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
	          rcr.save(RefreshMode.NO_REFRESH);
      }


}
