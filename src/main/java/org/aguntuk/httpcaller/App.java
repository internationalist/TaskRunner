package org.aguntuk.httpcaller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws ClientProtocolException, IOException
    {
    	String url = "https://na.api.pvp.net/api/lol/na/v2.5/league/by-summoner/51027602?api_key=RGAPI-b36642fc-a582-48ed-a1de-f2dbda9c3768";
    	HttpGet request = new HttpGet(url);
    	//request.addHeader("User-Agent", USER_AGENT);
    	HttpResponse response = HttpContainer.instance.getClient().execute(request);
    	System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

    	BufferedReader rd = new BufferedReader(
    			new InputStreamReader(response.getEntity().getContent()));

    	StringBuffer result = new StringBuffer();
    	String line = "";
    	while ((line = rd.readLine()) != null) {
    		result.append(line);
    	}
    	System.out.println(result);
    	Header[] headers = response.getAllHeaders();
    	for (Header header : headers) {
    		System.out.println("Key : " + header.getName()
    		      + " ,Value : " + header.getValue());
    	}
    }
}
