import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.io.File;
import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.FileWriter;

public class ClientHandler extends Thread
{
    private Socket socket;
    private int clientNumber; 
    private DataInputStream input;
    private DataOutputStream output;

    public ClientHandler(Socket socket, int clientNumber)
    {
        this.socket = socket;
        this.clientNumber = clientNumber;
        System.out.println("New connection with client#" + clientNumber + " at" + socket);
    }

    public void run()
    {
        try
        {
            this.authenticateClient();

            this.processImage();
        }
        catch (IOException e)
        {
            System.out.println("Error handling client# " + clientNumber + ": " + e);
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                System.out.println("Couldn't close a socket, what's going on?");
            }
            System.out.println("Connection with client# " + clientNumber + " closed");
        }
    }

    private void authenticateClient() throws IOException
    {
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());

        String username = input.readUTF();
        String password = input.readUTF();

        String[] userInfo = findUserInfo(username, password);

        if (userInfo == null)
        {
            output.writeUTF("Username does not exist, new account created - " + username + " client #" + clientNumber);
            createNewAccount(username, password);
        }
        else if (!password.equals(userInfo[1]))
        {    
            output.writeUTF("Error in password input"); // translate in french?
            authenticateClient();

            return;
        }
        else
        {
            output.writeUTF("Authentication successful, " + username + " client #" + clientNumber);
        }
    }

    private static String[] findUserInfo(String username, String password) throws IOException
    {
        BufferedReader txtReader = new BufferedReader(new FileReader("./Server/accounts.txt"));
        String rawInfo = txtReader.readLine();

        while (rawInfo != null)
        {
            String[] userInfo = rawInfo.split(",");
            if (userInfo[0].equals(username))
            {
                return userInfo;
            }
            rawInfo = txtReader.readLine();
        }

        return null; // close txtReader?
    }

    private static void createNewAccount(String username, String password) throws IOException
    {
        FileWriter txtWriter = new FileWriter("./Server/accounts.txt", true);
        txtWriter.write("\n" + username + "," + password);
        txtWriter.close();
    }

    private void processImage() throws IOException
    {
        String fileName = input.readUTF();

        byte[] buffer = new byte[1024]; // send 1KB of image info at a time

        FileOutputStream fileWriter = new FileOutputStream("./Server/" + fileName);

        int bytesRead = 0;
        while ((bytesRead = input.read(buffer)) > 0)
        {
            fileWriter.write(buffer, 0, bytesRead);
            if (input.available() == 0) // avoid blocking
            {
                break;
            }
        }

        System.out.println("File received from client #" + clientNumber); // TODO: [Nom d’utilisateur - Adresse IP : Port client - Date et Heure (min, sec)] 
        fileWriter.close();

        BufferedImage rawImage = ImageIO.read(new File("./Server/" + fileName));
        BufferedImage processedImage = Sobel.process(rawImage);

        ImageIO.write(processedImage, "jpg", new File("./Server/" + "processed_" + fileName));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(processedImage, "jpg", byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        output.writeUTF("processed_" + fileName);
        output.write(imageBytes);
        // flush all streams ?
    }
}