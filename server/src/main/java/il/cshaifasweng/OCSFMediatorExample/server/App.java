package il.cshaifasweng.OCSFMediatorExample.server;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{

	private static XOServer server;
    public static void main( String[] args ) throws IOException
    {
        server = new XOServer(3000);
        server.listen();
    }
}
