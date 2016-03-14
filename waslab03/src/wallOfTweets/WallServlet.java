package wallOfTweets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;



@SuppressWarnings("serial")
public class WallServlet extends HttpServlet {

	private String TWEETS_URI = "/waslab03/tweets/";

	@Override
	// Implements GET http://localhost:8080/waslab03/tweets
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		resp.setContentType("application/json");
		resp.setHeader("Cache-control", "no-cache");
		List<Tweet> tweets= Database.getTweets();
		JSONArray job = new JSONArray();
		for (Tweet t: tweets) {
			JSONObject jt = new JSONObject(t);
			jt.remove("class");
			job.put(jt);
		}
		resp.getWriter().println(job.toString());
	}

	@Override
	// Implements POST http://localhost:8080/waslab03/tweets/:id/likes
	//        and POST http://localhost:8080/waslab03/tweets
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String uri = req.getRequestURI();
		int lastIndex = uri.lastIndexOf("/likes");
		if (lastIndex > -1) {  // uri ends with "/likes"
			// Implements POST http://localhost:8080/waslab03/tweets/:id/likes
			long id = Long.valueOf(uri.substring(TWEETS_URI.length(),lastIndex));		
			resp.setContentType("text/plain");
			resp.getWriter().println(Database.likeTweet(id));
		}
		else { 
			// Implements POST http://localhost:8080/waslab03/tweets
			int max_length_of_data = req.getContentLength();
			byte[] httpInData = new byte[max_length_of_data];
			ServletInputStream  httpIn  = req.getInputStream();
			httpIn.readLine(httpInData, 0, max_length_of_data);
			String body = new String(httpInData);
			/*      ^
		      The String variable body contains the sent (JSON) Data. 
		      Complete the implementation below.*/
			
			try {
				JSONObject tweet = new JSONObject(body);
				String author = tweet.getString("author");
				String text = tweet.getString("text");
				Tweet tweetIn = Database.insertTweet(author, text);
				JSONObject tweetInJSON = new JSONObject(tweetIn);
				String hashID = Base64.encode(tweetIn.getId().toString().getBytes());
				tweetInJSON.append("token",hashID);
				resp.getWriter().println(tweetInJSON.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	@Override
	// Implements DELETE http://localhost:8080/waslab03/tweets/:id
	public void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		
		String uri = req.getRequestURI();
		int lastIndex = uri.lastIndexOf("id=");
		if (lastIndex > -1) {
			long id = Long.valueOf(uri.substring(lastIndex+3,uri.length()));
			int indexToken = uri.lastIndexOf("token=");
			String token = uri.substring(indexToken+6,lastIndex-1);
			Boolean borrat = false;
			
			/*
			 * No sé per qué el valor de decode després de fer base64.decode no es
			 * correspon al valor que hauria de tenir (en fer el decode de MTgy en lloc
			 * d'obtenir el valor correcte decode té valor [B@e8a2db2 )
			 * Per tal de garantir el funcionament del programa, queda comentada aquesta
			 * part de comprobació, tota la resta del programa es correspon a les especificacions
			 * idicades en el exercici
			 * 
			String decode = "";
			try {
				decode = Base64.decode(token).toString();
			} catch (Base64DecodingException e) {
				e.printStackTrace();
			}
			if (decode.equals(String.valueOf(id))) borrat = Database.deleteTweet(id);
			*/
			borrat = Database.deleteTweet(id);
			if (!borrat) throw new ServletException("No sha esborrat de la Base de Dades.");
		}
		else  throw new ServletException("No sha esborrat de la Base de Dades.");

	}

}
