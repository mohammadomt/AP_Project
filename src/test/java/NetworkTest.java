import network.Client;
import network.GameHost;

import java.util.Scanner;

public class NetworkTest
{
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();
        if (s.equals("server"))
        {
            GameHost gameHost = new GameHost(8888);
            Thread thread = new Thread(gameHost);
            thread.start();
            String s1 = scanner.nextLine();

        }
        else if (s.equals("client"))
        {
            Client client = new Client(Long.toString(System.currentTimeMillis()), 8888, "localhost");
            Thread thread = new Thread(client);
            thread.start();
            while (true)
            {
                String s1 = scanner.nextLine();
                client.sendMessage(s1);
            }
        }
    }
}
