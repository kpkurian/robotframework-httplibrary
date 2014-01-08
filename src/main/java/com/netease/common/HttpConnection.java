package com.netease.common;

import org.apache.commons.lang.StringUtils;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywordOverload;
import org.robotframework.javalib.annotation.RobotKeywords;
import org.robotframework.javalib.annotation.ArgumentNames;

import java.util.Date;
import java.util.List;

@RobotKeywords
public class HttpConnection {
	
	private static final String CHARACTER_ENCODING = "UTF-8";
    private static int connectionTimeout = 10000;
    private static int readTimeout = 10000;
    private static Header[] initHeaders = {new BasicHeader("Content-Type", "application/x-www-form-urlencoded"),
                                             new BasicHeader("Accept-Charset", CHARACTER_ENCODING)};
    private static Header[] headers = initHeaders.clone();
    private static DefaultHttpClient client = createDefaultHttpClient();
    private static CookieStore cookieStore = HttpConnection.client.getCookieStore();

    @RobotKeyword("This keyword sets HTTP cookies\n\n"
            + "| Options  | Man. | Description |\n"
            + "| cookies  | Yes  | Cookies with format 'key=value' |\n\n"
            + "Examples:\n"
            + "| Set HTTP Cookies | name=NetEase |\n"
            + "| Set HTTP Cookies | name=NetEase | department=R&D |\n")
    @ArgumentNames({"*cookies"})
    public void setHttpCookies(String... cookies) {
        for(String cookie:cookies) {
            BasicClientCookie clientCookie = new BasicClientCookie("abc", "123");
            clientCookie.setDomain("www.baidu.com");
            clientCookie.setPath("/");
            HttpConnection.cookieStore.addCookie(clientCookie);
        }
            HttpConnection.client.setCookieStore(HttpConnection.cookieStore);
    }

    @RobotKeyword("This keyword resets HTTP cookies to empty\n\n"
            + "Examples:\n"
            + "| Reset HTTP Cookies |\n")
    @ArgumentNames({"*cookies"})
    public void resetHttpCookies() {
        HttpConnection.cookieStore.clear();
        HttpConnection.client.setCookieStore(HttpConnection.cookieStore);
    }

    @RobotKeyword("This keyword sets HTTP connection timeout, "
                + "and it will return the original connection timeout value\n\n"
                + "| Options  | Man. | Description |\n"
                + "| timeout  | Yes  | Timeout in ms |\n\n"
                + "Examples:\n"
                + "| Set HTTP Connection Timeout | 15000 |\n"
                + "| ${original_connection_timeout} | Set HTTP Connection Timeout | 15000 |\n"
                + "| Set HTTP Connection Timeout | ${original_connection_timeout} |\n")
    @ArgumentNames({"timeout"})
    public int setHttpConnectionTimeout(String timeout) throws Exception {
        int originalConnectionTimeout = HttpConnection.connectionTimeout;
        try {
            HttpConnection.connectionTimeout = Integer.parseInt(timeout);
        } catch (Exception e) {
              throw new Exception("Convert timeout " + timeout + "to integer failed");
        }
        return originalConnectionTimeout;
    }

    @RobotKeyword("This keyword sets HTTP read timeout, "
                + "and it will return the original read timeout value\n"
                + "| Options  | Man. | Description |\n"
                + "| timeout  | Yes  | Timeout in ms |\n\n"
                + "Examples:\n"
                + "| Set HTTP Read Timeout | 15000 |\n"
                + "| ${original_read_timeout} | Set HTTP Read Timeout | 15000 |\n"
                + "| Set HTTP Read Timeout | ${original_read_timeout} |\n")
    @ArgumentNames({"timeout"})
    public int setHttpReadTimeout(String timeout) throws Exception {
        int originalReadTimeout = HttpConnection.readTimeout;
        try {
            HttpConnection.readTimeout = Integer.parseInt(timeout);
        } catch (Exception e) {
            throw new Exception("Convert timeout " + timeout + "to integer failed");
        }
        return originalReadTimeout;
    }

    @RobotKeywordOverload
    @ArgumentNames({})
    public int setHttpReadTimeout() throws Exception {
        int originalReadTimeout = HttpConnection.readTimeout;
        setHttpReadTimeout("10000");

        return originalReadTimeout;
    }

    @RobotKeyword("This keyword sets HTTP headers\n"
                + "The headers can be given with 'key=value' format and can be multiple"
                + "| Options  | Man. | Description |\n"
                + "| headers  | Yes  | HTTP headers |\n\n"
                + "Examples:\n"
                + "| Set HTTP Headers | a=1 |\n"
                + "| Set HTTP Headers | a=1 | b=2 |"
                + "| ${original_http_headers} | Set HTTP Headers | c=3 |\n"
                + "| Set HTTP Headers | ${original_http_headers} |\n")
    @ArgumentNames({"*headers"})
    public Header[] setHttpHeaders(String... headers) {
        Header[] originalHttpHeaders = HttpConnection.headers;
        HttpConnection.headers = new Header[headers.length];
        int index = 0;
        for(String header:headers) {
            try {
                String[] headerNameValuePair = header.split("=");
                HttpConnection.headers[index] = new BasicHeader(headerNameValuePair[0], headerNameValuePair[1]);
                index ++;
            } catch (Exception e) {
                System.out.println("*WARN* HTTP header " + header + " is invalid");
                System.out.println("*WARN* Exception is " + e.toString());
            }
        }

        return originalHttpHeaders;
    }
    /*
    @RobotKeywordOverload
    @ArgumentNames({"headers"})
    public Header[] setHttpHeaders(Header[] headers) {
        Header[] originalHttpHeaders = HttpConnection.headers;
        HttpConnection.headers = headers;

        return originalHttpHeaders;
    } */

    @RobotKeyword("This keyword resets HTTP headers to default value\n"
                + "The default HTTP header is \"Content-Type\" = \"application/x-www-form-urlencoded\" and "
                + "\"Accept-Charset\" =  " + CHARACTER_ENCODING + "\n\n"
                + "Examples:\n"
                + "| Reset HTTP Headers |\n")
    @ArgumentNames({})
    public static void resetHttpHeaders() {
        HttpConnection.headers = HttpConnection.initHeaders.clone();
    }
	
	@RobotKeyword("This keyword sends HTTP message via POST method\n\n"
			    + "It returns HttpResponseResult object and following attributes can be directly accessed\n"
			    + "- statusCode: HTTP response code\n"
                + "- headers: HTTP response headers, it is an array\n"
			    + "- rawBody: HTTP response body\n"
			    + "- jsonBody: HTTP response body, but with JSON format\n"
			    + "| Options  | Man. | Description |\n"
			    + "| url      | Yes  | URL |\n"
			    + "| data     | Yes  | Message body |\n\n"
			    + "Examples:\n"
			    + "| POST | http://1.2.3.4:5678 | ${EMPTY} |\n"
			    + "| POST | http://1.2.3.4:5678 | name=yixin&id=123 |\n"
			    + "| ${resp} | POST | http://1.2.3.4:5678 | {\"message\":\"test\"} |\n"
			    + "| Should Be Equal As Strings | ${resp.statusCode} | 200 |\n"
			    + "| Should Be Equal As Strings | ${resp.jsonBody[\"code\"] | 1 |")
	@ArgumentNames({"uri", "data"})
	public static HttpResponseResult post(String uri, String data) throws Exception {
        //DefaultHttpClient httpClient = createDefaultHttpClient();

		StringEntity stringEntity = new StringEntity(data, CHARACTER_ENCODING);

		HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeaders(HttpConnection.headers);
		httpPost.setEntity(stringEntity);
		
		System.out.println("*INFO* Request: POST " + uri + " " + data);
        System.out.println("*INFO* Request Headers: " + StringUtils.join(headers, " | "));
		
		try {
		    HttpResponse httpResponse = HttpConnection.client.execute(httpPost);
		    return new HttpResponseResult(httpResponse);
		}
		finally {
			httpPost.releaseConnection();
		}
	}
	
	@RobotKeyword("This keyword sends HTTP message via GET method\n\n"
			    + "It returns HttpResponseResult object and following attributes can be directly accessed\n"
			    + "- statusCode: HTTP response code\n"
                + "- headers: HTTP response headers, it is an array\n"
			    + "- rawBody: HTTP response body\n"
			    + "- jsonBody: HTTP response body, but with JSON format\n"
			    + "| Options  | Man. | Description |\n"
			    + "| url      | Yes  | URL |\n\n"
			    + "Examples:\n"
			    + "| Get | http://1.2.3.4:5678 |\n"
			    + "| ${resp} | Get | http://1.2.3.4:5678?name=yixin&id=1 |\n"
			    + "| Should Be Equal As Strings | ${resp.statusCode} | 200 |\n"
			    + "| Should Be Equal As Strings | ${resp.jsonBody[\"code\"] | 1 |")
	@ArgumentNames({"uri"})
	public static HttpResponseResult get(String uri) throws Exception {
        //DefaultHttpClient httpClient = createDefaultHttpClient();

		HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeaders(HttpConnection.headers);
		
	    System.out.println("*INFO* Request: GET " + uri);
        System.out.println("*INFO* Request Headers: " + StringUtils.join(headers, " | "));

		try {
			HttpResponse httpResponse = HttpConnection.client.execute(httpGet);
			return new HttpResponseResult(httpResponse);
		}
		finally {
			httpGet.releaseConnection();
		}
	}

    private static DefaultHttpClient createDefaultHttpClient() {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, HttpConnection.connectionTimeout);
        HttpConnectionParams.setSoTimeout(httpParams, HttpConnection.readTimeout);

        return new DefaultHttpClient(httpParams);
    }
}