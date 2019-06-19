import java.util.ResourceBundle;

public class PropReader {
    static ResourceBundle rb = ResourceBundle.getBundle( "API_Info" );
    public static String HttpMethod = rb.getString( "HttpMethod" );
    public static String BaseURL = rb.getString( "BaseURL" );
    public static String AbsolutePath = rb.getString( "AbsolutePath" );
    public static String APPId = rb.getString( "APPId" );
    public static String SecretKey = rb.getString( "SecretKey" );
    public static String AuthenticationScheme = rb.getString( "AuthenticationScheme" );
}
