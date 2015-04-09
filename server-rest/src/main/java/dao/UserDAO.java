package dao;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.view.*;
import entity.User;
import util.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mds on 07/04/15.
 * Class ${CLASS}
 */
public class UserDAO extends AbstractDAO<User> {

    public JsonDocument findByLogin(String username, String password) {
        JsonDocument res = this.currentBucket.get(Constant.DATATYPE_USER);
        res.content().get("username");
        return res;
    }

    @Override
    protected User jsonDocumentToEntity(JsonDocument jsonDocument) {
        User u = new User();
        try {
            JsonObject content = jsonDocument.content();
            if (Constant.DATATYPE_USER.equals(((JsonObject)content.get("properties")).getString("datatype"))) {
                u.setId(Long.parseLong(jsonDocument.id()));
                u.setUsername(((JsonObject)content.get("properties")).getString("username"));
                u.setPassword(((JsonObject)content.get("properties")).getString("password"));
            } else {
                throw new IllegalArgumentException();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            u = null;
        }
        return u;
    }

    @Override
    protected JsonDocument entityToJsonDocument(User u) {
        JsonObject properties = JsonObject.create();
        properties.put("datatype", u.getDataType())
                .put("username", u.getUsername())
                .put("password", u.getPassword());

        JsonObject jsonUser = JsonObject.empty()
                .put("properties", properties);

        JsonDocument doc = JsonDocument.create("" + u.getId(), jsonUser);
        return doc;
    }

    public User getByUsername(String username)
    {
        List<User> res = new ArrayList<User>();
        DesignDocument designDoc = currentBucket.bucketManager().getDesignDocument("designDoc");
        createViewGetByUsername();
        ViewResult result = currentBucket.query(ViewQuery.from("designDoc", "login_view" + datatype));
        // Iterate through the returned ViewRows
        User resUser = null;
        for (ViewRow row : result) {
            resUser = jsonDocumentToEntity(row.document());
            if(username.equals(resUser.getUsername()))
            {
                break;
            }
        }
        return resUser;
    }

    private void createViewGetByUsername()
    {
        DesignDocument designDoc = currentBucket.bucketManager().getDesignDocument("designDoc");

        String viewName = "login_view";
        String view = "function(doc) {\n" +
                "  if(doc.properties.datatype == \""+ Constant.DATATYPE_USER + "\"){\t\n" +
                "       \temit([doc.properties.username, doc.properties.password], doc.properties.username);\n" +
                "  }\n" +
                "}";
        designDoc.views().add(DefaultView.create(viewName, view, ""));
        System.out.println("designDoc" + designDoc);
        currentBucket.bucketManager().upsertDesignDocument(designDoc);
    }
}
