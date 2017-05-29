package in.gvc;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;


/**
 * Created by arpit on 12/5/17.
 */
public class FileUploader {



    public void uploadIt(byte[] bytes) throws URISyntaxException, IOException {

        MultipartEntity multiPartEntity = new MultipartEntity() ;
        ContentBody cd = new InputStreamBody(new ByteArrayInputStream(bytes), "my-file.txt");
        multiPartEntity.addPart("file",cd);

        URIBuilder uriBuilder = new URIBuilder("http://localhost:8080/upload?");
        uriBuilder.setParameter("language", "en");
        uriBuilder.setParameter("detectOrientation ", "true");

        URI uri = uriBuilder.build();
        HttpPost request = new HttpPost(uri);
        request.setEntity(multiPartEntity);
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();

        if (entity != null)
        {
            String str = EntityUtils.toString(entity);
            System.out.println(str);
        }
    }

}

