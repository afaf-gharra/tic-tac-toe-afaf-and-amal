package il.cshaifasweng.OCSFMediatorExample.server;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{

	private static GameServer server;
    public static void main( String[] args ) throws IOException
    {
        server = new GameServer(3000);
        server.listen();
    }
}
