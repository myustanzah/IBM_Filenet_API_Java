package com.filenet.filenet.service;

import java.util.ArrayList;
import java.util.Iterator;

import javax.security.auth.Subject;

import org.springframework.stereotype.Service;

import com.filenet.api.admin.StorageArea;
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
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.ReferentialContainmentRelationship;
// import com.filenet.api.core.Factory.StorageArea;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.Properties;
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
            // TODO: handle exception
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
            // TODO: handle exception
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
            // TODO: handle exception
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

    private static String updateDocument(ObjectStore os, String DocID, String newName){
        try {

            // Get document and populate property cache.
            PropertyFilter pf = new PropertyFilter();
            pf.addIncludeProperty(new FilterElement(null, null, null, "DocumentTitle", null));
            Document doc = Factory.Document.fetchInstance(os, new Id(DocID),pf );

            // Return document properties.
            com.filenet.api.property.Properties props = doc.getProperties();

            // Change property value.
            props.putValue("DocumentTitle", newName);
            doc.set_MimeType("text/plain");

            // Save and update property cache.
            doc.save(RefreshMode.REFRESH );
            
            return "Succes";
        } catch (Exception e) {
            // TODO: handle exception
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
            // TODO: handle exception
            e.printStackTrace();
            return "Error";
        }
    }

    
}
