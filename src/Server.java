import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Server
{
    private static ServerSocket Listener;
    private static String serverAddress = null;
    private static int serverPort = 0;

    public static void main(String[] args) throws Exception
    {
        enterServerInfo();

        initServerSocket();

        run();
    }

    private static void enterServerInfo() throws Exception
    {
        BufferedReader Reader = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.print("Enter server address: ");
        serverAddress = Reader.readLine();
        
        System.out.print("Enter port: ");
        serverPort = Integer.parseInt(Reader.readLine());

        if (!isServerValid(serverAddress, serverPort))
        {
            System.out.println("Invalid server address or port");
            enterServerInfo();
        }
    }

    private static boolean isServerValid(String address, int port)
    {
        String addressPattern = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$"; // 0-255 followed by . or endl (x4)
        
        boolean isAddressValid = Pattern.compile(addressPattern).matcher(address).matches();
        boolean isPortValid = port >=5000 && port <= 5050;

        return isAddressValid && isPortValid;
    }

    private static void initServerSocket() throws Exception
    {
        InetAddress serverIP = InetAddress.getByName(serverAddress);

        Listener = new ServerSocket();
        Listener.setReuseAddress(true);
        Listener.bind(new InetSocketAddress(serverIP, serverPort));

        System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
    }

    private static void run() throws Exception
    {
        int clientNumber = 0;

        try
        {
            while (true)
            {
                new ClientHandler(Listener.accept(), clientNumber++).start();
            }
        }
        finally
        {
            Listener.close();
        }
    }
}