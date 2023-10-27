package com.filenet.filenet.component;

import java.util.ArrayList;
import java.util.Iterator;

import javax.security.auth.Subject;

import com.filenet.api.collection.DocumentSet;
import com.filenet.api.collection.ObjectStoreSet;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.Document;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.Properties;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@Configuration
@ConfigurationProperties(prefix = "filenet")
@PropertySource("classpath:application.properties")
public class P8Connector {
    private static P8Connector instance = null;

    private static final String uri = "http://192.168.206.99:9080/wsi/FNCEWS40MTOM/";
    private static final String username = "adam";
    private static final String password = "P@ssw0rdnd5";

    // @Value("${filenet.url-filenet}")
    // private static String filenetUrl;

    // @Value("${filenet.username}")
    // private static String filenetUser;

    // @Value("${filenet.password}")
    // private static String filenetPassword;

    public static P8Connector getInstance(){
        if(instance == null){
            instance = new P8Connector();
        }
        return instance;
    }

    public String tryConnection(){
        return "Succes connect";
    }

    public String tryConnectionP8(){
            
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

            getDocumentName("","", store);            
            // deleteDocument(store, "{14FEF303-95CB-CCDA-8524-8B3DD5800000}");
            // updateDocument(store, "{14FEF303-95CB-CCDA-8524-8B3DD5800000}");
            // createDocument();
            
            return "Connection to Content Platform Engine successful";

        } catch(Exception e) {
            
            e.printStackTrace();
            return "INTERNAL SERVER ERROR";
        } finally {
            
            UserContext.get().popSubject();

        }
    }


    @SuppressWarnings("rawtypes")
	public ArrayList<String> getDocumentName(String admissionNo, String completedPath,
			ObjectStore os) {
		ArrayList<String> listDocument = new ArrayList<String>();

		String sqlStr = "SELECT PathName FROM Folder Where FolderName = 'data1'";

		System.out.println(sqlStr);

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
                System.out.println("Document Name: " + documentName);
                listDocument.add(documentName);
            }

		}
        
		return listDocument;
	}
}
