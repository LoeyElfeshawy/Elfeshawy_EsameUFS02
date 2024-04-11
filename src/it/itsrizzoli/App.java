package it.itsrizzoli;
import java.io.*;
import java.net.*;
import java.util.*;

public class App {
    private static List<Hotel> hotelList = Arrays.asList(
            new Hotel("Hotel Armani", "Italia", 104, true),
            new Hotel("Hotel Clinton", "UK", 175, true),
            new Hotel("Hotel Amedeo", "Marocco", 136, false)
    );

    public static void main(String[] args) {
        int portNumber = 2222;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Create a new thread to handle client requests
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                out.println("Comandi:\n" + "all --> lista di tutti gli hotel\n" + "sorted_by_name --> lista di tutti gli hotel ordinati per nome\n" + "with_spa --> lista degli hotel con la spa\n" );
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Received command from client: " + inputLine);
                    String response = processCommand(inputLine.trim());
                    out.println(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String processCommand(String command) {
            switch (command) {
                case "all":
                    return formatoTabella(listAllHotels());
                case "sorted_by_name":
                    return formatoTabella(listAllHotelsSortedByName());
                case "with_spa":
                    return formatoTabella(listHotelsWithSpa());
                default:
                    return "Invalid command";
            }
        }

        private String formatoTabella(String data) {
            StringBuilder formattedData = new StringBuilder();
            String[] lines = data.split("\n");
            String header = lines[0];
            int[] columnWidths = new int[header.split(", ").length];

            for (String line : lines) {
                String[] columns = line.split(", ");
                for (int i = 0; i < columns.length; i++) {
                    columnWidths[i] = Math.max(columnWidths[i], columns[i].length());
                }
            }
            
            for (String line : lines) {
                String[] columns = line.split(", ");
                for (int i = 0; i < columns.length; i++) {
                    formattedData.append(String.format("%-" + (columnWidths[i] + 2) + "s", columns[i]));
                }
                formattedData.append("\n");
            }
            return formattedData.toString();
        }

        private String listAllHotels() {
            StringBuilder response = new StringBuilder();
            response.append("Hotel, Country, Price, Spa\n");
            for (Hotel hotel : hotelList) {
                response.append(hotel).append("\n");
            }
            return response.toString();
        }

        private String listAllHotelsSortedByName() {
            List<Hotel> sortedList = new ArrayList<>(hotelList);
            sortedList.sort(Comparator.comparing(Hotel::getName));
            StringBuilder response = new StringBuilder();
            response.append("Hotel, Country, Price, Spa\n");
            for (Hotel hotel : sortedList) {
                response.append(hotel).append("\n");
            }
            return response.toString();
        }

        private String listHotelsWithSpa() {
            StringBuilder response = new StringBuilder();
            response.append("Hotel, Country, Price, Spa\n");
            for (Hotel hotel : hotelList) {
                if (hotel.getSpa()) {
                    response.append(hotel).append("\n");
                }
            }
            return response.toString();
        }
    }

    static class Hotel {
        private String name;
        private String country;
        private int price;
        private boolean spa;

        public Hotel(String name, String country, int price, boolean spa) {
            this.name = name;
            this.country = country;
            this.price = price;
            this.spa = spa;
        }

        public String getName() {
            return name;
        }

        public String getCountry() {
            return country;
        }

        public boolean getSpa() {
            return spa;
        }

        public int getPrice() {
            return price;
        }

        @Override
        public String toString() {
            return name + ", " + country + ", " + price + ", " + spa;
        }
    }
}
