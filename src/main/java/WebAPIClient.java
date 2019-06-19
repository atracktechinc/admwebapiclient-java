import java.io.*;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WebAPIClient {

    public static void main( String[] args ) {
        final String HttpMethod = PropReader.HttpMethod;
        final String BaseURL = PropReader.BaseURL;
        String AbsolutePath = PropReader.AbsolutePath;
        final String APPId = PropReader.APPId;
        final String SecretKey = PropReader.SecretKey;
        final String AuthenticationScheme = PropReader.AuthenticationScheme;

        try {
            System.out.println( "===================================================================================" );
            System.out.println( "= HTTP Method : " + HttpMethod );
            System.out.println( "= Base URL : " + BaseURL );
            System.out.println( "= Absolute Path : " + AbsolutePath );
            System.out.println( "= APP Id : " + APPId );
            System.out.println( "= Secret Key : " + SecretKey );
            System.out.println( "===================================================================================" );

            WebAPIClient apiClient = new WebAPIClient();

            apiClient.validateAPICall( HttpMethod, AbsolutePath );

            //String APIKey = null;
            String RequestURL = null;
            String queryParam = null;
            String BodyContent = null;
            String APIKey = null;

            AbsolutePath = apiClient.GenPathParam( HttpMethod, AbsolutePath );
            RequestURL = BaseURL + AbsolutePath;

            queryParam = apiClient.GenQueryParam( HttpMethod, AbsolutePath );
            if ( !apiClient.StringIsNullOrEmpty( queryParam ) ) {
                System.out.println();
                RequestURL += "?" + queryParam;
            }

            System.out.println( "Request URL:" );
            System.out.println( RequestURL );
            System.out.println();

            BodyContent = apiClient.GenBodyContent( HttpMethod, AbsolutePath );

            if ( BodyContent != null ) {
                System.out.println();
                System.out.println( "Body Content:" );
                System.out.println( BodyContent );
                System.out.println();
            }

            RequestURL = URLEncoder.encode( RequestURL, "UTF-8" ).toLowerCase();
            APIKey = apiClient.GenAPIKey( APPId, HttpMethod, RequestURL, SecretKey );
            APIKey = AuthenticationScheme + " " + APIKey;
            System.out.println( "API Key:" );
            System.out.println( APIKey );
            System.out.println();

            System.out.println( "-- Press Enter to continue... --" );
            System.out.println();
            System.in.read();

            /////////////////////////////////////////////

            System.out.println( "Calling the Web API..." );
            System.out.println();

            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = null;

            if ( HttpMethod.toLowerCase().equals( "post" ) ) {
                HttpPost request = new HttpPost( BaseURL + AbsolutePath );
                request.setHeader( HttpHeaders.AUTHORIZATION, APIKey );

                request.setHeader( HttpHeaders.CONTENT_TYPE, "application/json" );
                StringEntity content = new StringEntity( BodyContent, "UTF-8" );
                request.setEntity( content );

                response = client.execute( request );
            } else if ( HttpMethod.toLowerCase().equals( "get" ) ) {
                if ( queryParam != null ) {
                    AbsolutePath += "?" + queryParam;
                }

                HttpGet request = new HttpGet( BaseURL + AbsolutePath );
                request.setHeader( HttpHeaders.AUTHORIZATION, APIKey );
                response = client.execute( request );
            } else if ( HttpMethod.toLowerCase().equals( "put" ) ) {
                HttpPut request = new HttpPut( BaseURL + AbsolutePath );
                request.setHeader( HttpHeaders.AUTHORIZATION, APIKey );

                request.setHeader( HttpHeaders.CONTENT_TYPE, "application/json" );
                StringEntity content = new StringEntity( BodyContent, "UTF-8" );
                request.setEntity( content );

                response = client.execute( request );
            } else if ( HttpMethod.toLowerCase().equals( "delete" ) ) {
                HttpDelete request = new HttpDelete( BaseURL + AbsolutePath );
                request.setHeader( HttpHeaders.AUTHORIZATION, APIKey );
                response = client.execute( request );
            }

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader( response.getEntity().getContent() ) );

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append( line );
            }

            int responseStatusCode = response.getStatusLine().getStatusCode();
            String responseReasonPhrase = response.getStatusLine().getReasonPhrase();
            String responseContent = result.toString();
            System.out.println( "Response: " );
            System.out.println( responseContent );
            System.out.println();

            if ( responseStatusCode == 200 ) {
                System.out.printf( "HTTP Status: %d, Reason: %s. ", responseStatusCode, responseReasonPhrase );
            } else {
                System.out.printf( "Failed to call the API. HTTP Status: %d, Reason: %s", responseStatusCode, responseReasonPhrase );
            }

            System.out.println();
            System.out.println( "-- Press Enter to exit --" );
            System.in.read();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private String GenBodyContent( String HttpMethod, String AbsolutePath ) throws Exception {
        if ( Arrays.asList( new String[] {"post", "put"} ).contains( HttpMethod.toLowerCase() ) ) {
            // POST /api/devices/fwassignments
            if ( AbsolutePath.startsWith( "/api/devices/fwassignments" ) ) {
                System.out.println( "--- Input Required Body Parameter ---" );

                String fw = readLine( "Firmware:" );
                while (StringIsNullOrEmpty( fw )) {
                    fw = readLine( "Firmware:" );
                }

                String IMEI = readLine( "IMEI: (separated by COMMA ',')" );
                while (StringIsNullOrEmpty( IMEI )) {
                    IMEI = readLine( "IMEI: (separated by COMMA ',')" );
                }

                JSONArray IMEIs = new JSONArray();
                for ( String i : IMEI.split( "," ) ) {
                    IMEIs.add( i );
                }

                JSONObject json = new JSONObject();
                json.put( "Firmware", fw );
                json.put( "IMEIs", IMEIs );

                return json.toJSONString();
            }

            // POST /api/devices/configassignments
            if ( AbsolutePath.startsWith( "/api/devices/configassignments" ) ) {
                System.out.println( "--- Input Required Body Parameter ---" );

                String config = readLine( "Configuration:" );
                while (StringIsNullOrEmpty( config )) {
                    config = readLine( "Configuration:" );
                }

                String IMEI = readLine( "IMEI: (separated by COMMA ',')" );
                while (StringIsNullOrEmpty( IMEI )) {
                    IMEI = readLine( "IMEI: (separated by COMMA ',')" );
                }

                JSONArray IMEIs = new JSONArray();
                for ( String i : IMEI.split( "," ) ) {
                    IMEIs.add( i );
                }

                JSONObject json = new JSONObject();
                json.put( "Configuration", config );
                json.put( "IMEIs", IMEIs );

                return json.toJSONString();
            }

            // POST /api/devices/groupassignments
            if ( AbsolutePath.startsWith( "/api/devices/groupassignments" ) ) {
                System.out.println( "--- Input Required Body Parameter ---" );

                String group = readLine( "Group Name:" );
                while (StringIsNullOrEmpty( group )) {
                    group = readLine( "Group Name:" );
                }

                String IMEI = readLine( "IMEI: (separated by COMMA ',')" );
                while (StringIsNullOrEmpty( IMEI )) {
                    IMEI = readLine( "IMEI: (separated by COMMA ',')" );
                }

                JSONArray IMEIs = new JSONArray();
                for ( String i : IMEI.split( "," ) ) {
                    IMEIs.add( i );
                }

                JSONObject json = new JSONObject();
                json.put( "Group", group );
                json.put( "IMEIs", IMEIs );

                return json.toJSONString();
            }

            // POST /api/devices/companyassignments
            if ( AbsolutePath.startsWith( "/api/devices/companyassignments" ) ) {
                System.out.println( "--- Input Required Body Parameter ---" );

                String company = readLine( "Company Alias Name:" );
                while (StringIsNullOrEmpty( company )) {
                    company = readLine( "Company Alias Name:" );
                }

                String group = readLine( "Group Name:" );
                while (StringIsNullOrEmpty( company )) {
                    group = readLine( "Group Name:" );
                }

                String IMEI = readLine( "IMEI: (separated by COMMA ',')" );
                while (StringIsNullOrEmpty( IMEI )) {
                    IMEI = readLine( "IMEI: (separated by COMMA ',')" );
                }

                JSONArray IMEIs = new JSONArray();
                for ( String i : IMEI.split( "," ) ) {
                    IMEIs.add( i );
                }

                JSONObject json = new JSONObject();
                json.put( "CompanyAliasName", company );
                json.put( "GroupName", group );
                json.put( "IMEIs", IMEIs );

                return json.toJSONString();
            }

            // POST /api/groups
            if ( AbsolutePath.startsWith( "/api/groups" ) ) {
                System.out.println( "--- Input Required Body Parameter ---" );

                String group = readLine( "Group Name:" );
                while (StringIsNullOrEmpty( group )) {
                    group = readLine( "Group Name:" );
                }

                String remark = readLine( "Remark:" );
                while (StringIsNullOrEmpty( remark )) {
                    remark = readLine( "Remark:" );
                }

                JSONObject json = new JSONObject();
                json.put( "Name", group );
                json.put( "Remark", remark );

                return json.toJSONString();
            }
        }

        return null;
    }

    private String GenPathParam( String HttpMethod, String AbsolutePath ) throws Exception {
        String pathParam = null;
        String pathParamName = null;
        // Refer to http://hant.ask.helplib.com/c++/post_1615063
        Pattern p = Pattern.compile( "(?<=\\{)[^\\}]*(?=\\})" );

        if ( Arrays.asList( new String[] {"get", "put", "delete"} ).contains( HttpMethod.toLowerCase() ) ) {
            Matcher matcher = p.matcher( AbsolutePath );
            if ( matcher.find() ) {
                pathParamName = matcher.group();

                System.out.println( "--- Input Required Path Parameter Value ---" );

                pathParam = readLine( pathParamName + ":" );
                while (StringIsNullOrEmpty( pathParam )) {
                    pathParam = readLine( pathParamName + ":" );
                }

                pathParam = URLEncoder.encode( pathParam, "UTF-8" );
                // Append path parameter value
                return AbsolutePath.replace( "{", "" ).replace( "}", "" ).replace( pathParamName, pathParam );
            }
        }

        return AbsolutePath;
    }

    private String GenQueryParam( String HttpMethod, String AbsolutePath ) throws Exception {
        String queryParam = "";

        if ( HttpMethod.toLowerCase().equals( "get" ) ) {
            if ( AbsolutePath.equals( "/api/devices" ) ) {
                System.out.println( "--- Input Query Parameters ---" );

                String companyName = readLine( "Company Alias Name:" );
                String IMEI = readLine( "IMEI: (separated by comman ',')" );
                String groupName = readLine( "Group Name:" );
                String fwVer = readLine( "Firmware Version:" );
                String configVer = readLine( "Config Version:" );
                String configStatus = readLine( "Config Status: (0=Created, 1=Pending, 3=Completed, 4=Failed, -1=All) " );
                String fwStatus = readLine( "Firmware Status: (0=Created, 1=Pending, 3=Completed, 4=Failed, -1=All)" );
                String online = readLine( "Is Online: (0=Online, 1=Timeout, -1=All) " );
                String use = readLine( "Use Status: (0=New, 1=In Use, 3=Suspended, 5=Recalled, -1=All) " );
                String model = readLine( "Model Name:" );

                if ( !StringIsNullOrEmpty( companyName ) ) {
                    queryParam = "conditions.companyName=" + URLEncoder.encode( companyName, "UTF-8" );
                }
                if ( !StringIsNullOrEmpty( IMEI ) ) {
                    queryParam += (!StringIsNullOrEmpty( queryParam ) ? "&" : "") + "conditions.iMEI=" + URLEncoder.encode( IMEI, "UTF-8" );
                }
                if ( !StringIsNullOrEmpty( groupName ) ) {
                    queryParam += (!StringIsNullOrEmpty( queryParam ) ? "&" : "") + "conditions.groupName=" + URLEncoder.encode( groupName, "UTF-8" );
                }
                if ( !StringIsNullOrEmpty( fwVer ) ) {
                    queryParam += (!StringIsNullOrEmpty( queryParam ) ? "&" : "") + "conditions.firmwareVersion=" + URLEncoder.encode( fwVer, "UTF-8" );
                }
                if ( !StringIsNullOrEmpty( configVer ) ) {
                    queryParam += (!StringIsNullOrEmpty( queryParam ) ? "&" : "") + "conditions.configVersion=" + URLEncoder.encode( configVer, "UTF-8" );
                }
                if ( !StringIsNullOrEmpty( configStatus ) ) {
                    queryParam += (!StringIsNullOrEmpty( queryParam ) ? "&" : "") + "conditions.configStatus=" + URLEncoder.encode( configStatus, "UTF-8" );
                }
                if ( !StringIsNullOrEmpty( fwStatus ) ) {
                    queryParam += (!StringIsNullOrEmpty( queryParam ) ? "&" : "") + "conditions.firmwareStatus=" + URLEncoder.encode( fwStatus, "UTF-8" );
                }
                if ( !StringIsNullOrEmpty( online ) ) {
                    queryParam += (!StringIsNullOrEmpty( queryParam ) ? "&" : "") + "conditions.isOnline=" + URLEncoder.encode( online, "UTF-8" );
                }
                if ( !StringIsNullOrEmpty( use ) ) {
                    queryParam += (!StringIsNullOrEmpty( queryParam ) ? "&" : "") + "conditions.useStatus=" + URLEncoder.encode( use, "UTF-8" );
                }
                if ( !StringIsNullOrEmpty( model ) ) {
                    queryParam += (!StringIsNullOrEmpty( queryParam ) ? "&" : "") + "conditions.modelName=" + URLEncoder.encode( model, "UTF-8" );
                }
            }
        }

        return queryParam;
    }

    private String GenAPIKey( String APPId, String HttpMethod, String RequestURL, String SecretKey ) throws Exception {
        //Calculate UNIX time
        long unixTime = Instant.now().getEpochSecond();
        String RequestTimeStamp = String.valueOf( unixTime );

        System.out.println( "RequestTimeStamp: " + RequestTimeStamp );
        System.out.println();

        //create random nonce for each request
        String Nonce = UUID.randomUUID().toString();

        System.out.println( "Nonce: " + Nonce );
        System.out.println();

        //Creating the raw signature String
        String RawData = String.format( "%s%s%s%s%s",
                APPId, HttpMethod, RequestURL, RequestTimeStamp, Nonce );
        byte[] SecretKeyBytes = SecretKey.getBytes( "UTF-8" );
        byte[] RawDataBytes = RawData.getBytes( "UTF-8" );

        Mac sha256_HMAC = Mac.getInstance( "HmacSHA256" );
        SecretKeySpec secret_key = new SecretKeySpec( SecretKeyBytes, "HmacSHA256" );
        sha256_HMAC.init( secret_key );

        byte[] signatureBytes = sha256_HMAC.doFinal( RawDataBytes );

        String signature = Base64.encodeBase64String( signatureBytes );

        return String.format( "%s:%s:%s:%s", APPId, signature, Nonce, RequestTimeStamp );
    }

    private String readLine( String question ) throws Exception {
        BufferedReader buf = new BufferedReader( new InputStreamReader( System.in ) );
        String c;
        String value = "";

        System.out.println( question );
        while ((c = buf.readLine()) != null) {
            return c;
        }
        return value;
    }

    private boolean StringIsNullOrEmpty( String value ) {
        return (value == null) || value.equals( "" );
    }

    private void validateAPICall(String HttpMethod, String AbsolutePath) throws Exception {
        Exception ex = new Exception("Invalid API call. Please verify HTTP-Method and Absolute-Path.");

        if (HttpMethod.toLowerCase().equals( "get" )) {
            if (!Arrays.asList( new String[]{
                    "/api/devices",
                    "/api/devices/{IMEI}",
                    "/api/groups",
                    "/api/groups/{id}"} ).contains( AbsolutePath )) {
                throw ex;
            }
        }

        if (HttpMethod.toLowerCase().equals( "post" )) {
            if (!Arrays.asList( new String[]{
                    "/api/devices/fwassignments",
                    "/api/devices/configassignments",
                    "/api/devices/groupassignments",
                    "/api/devices/companyassignments",
                    "/api/groups"} ).contains( AbsolutePath )) {
                throw ex;
            }
        }

        if (HttpMethod.toLowerCase().equals( "delete" )) {
            if (!Arrays.asList( new String[]{
                    "/api/groups/{id}"} ).contains( AbsolutePath )) {
                throw ex;
            }
        }
    }
}
