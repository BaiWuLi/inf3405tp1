import java.io.DataInputStream;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Client
{
    private static Socket socket;
    private static String serverAddress = null;
    private static int serverPort = 0;
    private static BufferedReader reader;
    private static DataOutputStream output;
    private static DataInputStream input;

    public static void main(String[] args) throws Exception
    {
        connectServer();

        identify();

        sendImage();

        waitForImage();

        socket.close();
        // close reader, output, input?
    }

    private static void connectServer() throws Exception
    {
        reader = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.print("Enter server address: ");
        serverAddress = reader.readLine();
        
        System.out.print("Enter port: ");
        serverPort = Integer.parseInt(reader.readLine()); // handle exception

        if (!isServerValid(serverAddress, serverPort))
        {
            System.out.println("Invalid server address or port, please try again");
            connectServer();
            return;
        }

        socket = new Socket(serverAddress, serverPort);
        System.out.format("Server launched on [%s:%d]\n", serverAddress, serverPort);

        output = new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());
    }

    private static void identify() throws Exception
    {
        System.out.print("Enter username: ");
        String username = reader.readLine();
        
        System.out.print("Enter password: ");
        String password = reader.readLine();

        output.writeUTF(username);
        output.writeUTF(password);

        String response = input.readUTF();
        System.out.println(response);

        if (response.equals("Authentication failed, please try again"))
        {
            identify();
        }
    }

    private static boolean isServerValid(String address, int port)
    {
        String addressPattern = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$"; // 0-255 followed by . or endl (x4)
        
        boolean isAddressValid = Pattern.compile(addressPattern).matcher(address).matches();
        boolean isPortValid = port >=5000 && port <= 5050;

        return isAddressValid && isPortValid;
    }

    private static void sendImage() throws Exception
    {
        System.out.print("Enter image name: ");
        String imageName = reader.readLine();

        FileInputStream image = new FileInputStream("./Client/" + imageName); // Handle exception
        byte[] buffer = new byte[1024];

        output.writeUTF(imageName);

        int bytesRead = 0;
        while ((bytesRead = image.read(buffer)) > 0)
        {
            output.write(buffer, 0, bytesRead);
        }

        System.out.println("Image successfully sent to server");
    }

    private static void waitForImage() throws Exception
    {
        String imageName = input.readUTF();
        String imageDest = "./Client/" + imageName;
        FileOutputStream image = new FileOutputStream(imageDest); // Handle exception

        byte[] buffer = new byte[1024];

        int bytesRead = 0;
        while ((bytesRead = input.read(buffer)) > 0)
        {
            image.write(buffer, 0, bytesRead);
        }

        System.out.println("Image successfully received from server, saved as " + imageDest);
    }
}