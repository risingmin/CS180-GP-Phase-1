package client;

import database.*;
import java.util.*;

/**
 * Main class to run the Marketplace client with a command-line interface
 */
public class MarketplaceClientMain {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static MarketplaceClient client;
    private static Scanner scanner;

    public static void main(String[] args) {
        client = new MarketplaceClient();
        scanner = new Scanner(System.in);
        
        // Get server connection details
        String host = promptString("Enter server host (default: " + DEFAULT_HOST + "): ");
        if (host.trim().isEmpty()) {
            host = DEFAULT_HOST;
        }
        
        int port = DEFAULT_PORT;
        String portStr = promptString("Enter server port (default: " + DEFAULT_PORT + "): ");
        if (!portStr.trim().isEmpty()) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port. Using default: " + DEFAULT_PORT);
            }
        }
        
        // Connect to server
        System.out.println("Connecting to server at " + host + ":" + port + "...");
        if (!client.connect(host, port)) {
            System.out.println("Failed to connect to server. Exiting.");
            return;
        }
        System.out.println("Connected to server!");
        
        // Main client loop
        boolean running = true;
        while (running) {
            displayMenu();
            int choice = promptInt("Enter your choice: ");
            
            try {
                running = handleMenuChoice(choice);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        
        client.disconnect();
        scanner.close();
        System.out.println("Disconnected from server. Goodbye!");
    }
    
    private static void displayMenu() {
        System.out.println("\n===== MARKETPLACE CLIENT =====");
        System.out.println("Currently logged in: " + (client.getLoggedInUser() != null ? client.getLoggedInUser() : "Not logged in"));
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Add Item");
        System.out.println("4. Search Items");
        System.out.println("5. Buy Item");
        System.out.println("6. View My Items");
        System.out.println("7. Send Message");
        System.out.println("8. View Messages");
        System.out.println("9. View Transactions");
        System.out.println("10. Check Balance");
        System.out.println("11. Logout");
        System.out.println("12. Exit");
        System.out.println("============================");
    }
    
    private static boolean handleMenuChoice(int choice) {
        switch (choice) {
            case 1: // Register
                registerUser();
                break;
            case 2: // Login
                loginUser();
                break;
            case 3: // Add Item
                addItem();
                break;
            case 4: // Search Items
                searchItems();
                break;
            case 5: // Buy Item
                buyItem();
                break;
            case 6: // View My Items
                viewMyItems();
                break;
            case 7: // Send Message
                sendMessage();
                break;
            case 8: // View Messages
                viewMessages();
                break;
            case 9: // View Transactions
                viewTransactions();
                break;
            case 10: // Check Balance
                checkBalance();
                break;
            case 11: // Logout
                logout();
                break;
            case 12: // Exit
                return false;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
        return true;
    }
    
    private static void registerUser() {
        System.out.println("\n----- REGISTER -----");
        String username = promptString("Enter username: ");
        String password = promptString("Enter password: ");
        
        TransactionResult result = client.register(username, password);
        
        if (result.isSuccess()) {
            System.out.println("Registration successful!");
        } else {
            System.out.println("Registration failed: " + result.getMessage());
        }
    }
    
    private static void loginUser() {
        System.out.println("\n----- LOGIN -----");
        String username = promptString("Enter username: ");
        String password = promptString("Enter password: ");
        
        TransactionResult result = client.login(username, password);
        
        if (result.isSuccess()) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Login failed: " + result.getMessage());
        }
    }
    
    private static void addItem() {
        if (client.getLoggedInUser() == null) {
            System.out.println("You must be logged in to add an item.");
            return;
        }
        
        System.out.println("\n----- ADD ITEM -----");
        String title = promptString("Enter item title: ");
        String description = promptString("Enter item description: ");
        double price = promptDouble("Enter item price: ");
        
        TransactionResult result = client.addItem(title, description, price);
        
        if (result.isSuccess()) {
            System.out.println("Item added successfully! Item ID: " + result.getItemId());
        } else {
            System.out.println("Failed to add item: " + result.getMessage());
        }
    }
    
    private static void searchItems() {
        System.out.println("\n----- SEARCH ITEMS -----");
        String query = promptString("Enter search query: ");
        
        List<Item> items = client.searchItems(query);
        
        if (items.isEmpty()) {
            System.out.println("No items found matching your query.");
        } else {
            System.out.println("Found " + items.size() + " items:");
            for (Item item : items) {
                System.out.printf("ID: %d | Title: %s | Price: $%.2f | Seller: %s\n", 
                    item.getId(), item.getTitle(), item.getPrice(), item.getSeller());
            }
        }
    }
    
    private static void buyItem() {
        if (client.getLoggedInUser() == null) {
            System.out.println("You must be logged in to buy an item.");
            return;
        }
        
        System.out.println("\n----- BUY ITEM -----");
        int itemId = promptInt("Enter item ID: ");
        
        TransactionResult result = client.buyItem(itemId);
        
        if (result.isSuccess()) {
            System.out.println("Purchase successful!");
        } else {
            System.out.println("Purchase failed: " + result.getMessage());
        }
    }
    
    private static void viewMyItems() {
        if (client.getLoggedInUser() == null) {
            System.out.println("You must be logged in to view your items.");
            return;
        }
        
        System.out.println("\n----- MY ITEMS -----");
        List<Item> items = client.getUserItems();
        
        if (items.isEmpty()) {
            System.out.println("You haven't listed any items.");
        } else {
            System.out.println("Your items:");
            for (Item item : items) {
                System.out.printf("ID: %d | Title: %s | Price: $%.2f | Sold: %s\n", 
                    item.getId(), item.getTitle(), item.getPrice(), item.isSold() ? "Yes" : "No");
            }
        }
    }
    
    private static void sendMessage() {
        if (client.getLoggedInUser() == null) {
            System.out.println("You must be logged in to send messages.");
            return;
        }
        
        System.out.println("\n----- SEND MESSAGE -----");
        String recipient = promptString("Enter recipient username: ");
        int itemId = promptInt("Enter item ID (0 for general message): ");
        String content = promptString("Enter message: ");
        
        TransactionResult result = client.sendMessage(recipient, content, itemId);
        
        if (result.isSuccess()) {
            System.out.println("Message sent successfully!");
        } else {
            System.out.println("Failed to send message: " + result.getMessage());
        }
    }
    
    private static void viewMessages() {
        if (client.getLoggedInUser() == null) {
            System.out.println("You must be logged in to view messages.");
            return;
        }
        
        System.out.println("\n----- MESSAGES -----");
        List<Message> messages = client.getMessages();
        
        if (messages.isEmpty()) {
            System.out.println("You have no messages.");
        } else {
            System.out.println("Your messages:");
            for (Message message : messages) {
                System.out.printf("From: %s | To: %s | Item ID: %d\n", 
                    message.getSender(), message.getRecipient(), message.getItemId());
                System.out.println("Content: " + message.getContent());
                System.out.println("Time: " + message.getTimestamp());
                System.out.println("-------------------");
            }
        }
    }
    
    private static void viewTransactions() {
        if (client.getLoggedInUser() == null) {
            System.out.println("You must be logged in to view transactions.");
            return;
        }
        
        System.out.println("\n----- TRANSACTIONS -----");
        List<Transaction> transactions = client.getTransactions();
        
        if (transactions.isEmpty()) {
            System.out.println("You have no transactions.");
        } else {
            System.out.println("Your transactions:");
            for (Transaction transaction : transactions) {
                System.out.printf("Item ID: %d | Amount: $%.2f | Time: %s\n", 
                    transaction.getItemId(), transaction.getAmount(), transaction.getTimestamp());
                
                if (transaction.getBuyer().equals(client.getLoggedInUser())) {
                    System.out.println("Type: Purchase from " + transaction.getSeller());
                } else {
                    System.out.println("Type: Sale to " + transaction.getBuyer());
                }
                System.out.println("-------------------");
            }
        }
    }
    
    private static void checkBalance() {
        if (client.getLoggedInUser() == null) {
            System.out.println("You must be logged in to check your balance.");
            return;
        }
        
        double balance = client.getBalance();
        
        if (balance < 0) {
            System.out.println("Error retrieving balance.");
        } else {
            System.out.printf("Your current balance: $%.2f\n", balance);
        }
    }
    
    private static void logout() {
        if (client.getLoggedInUser() == null) {
            System.out.println("You are not logged in.");
            return;
        }
        
        TransactionResult result = client.logout();
        
        if (result.isSuccess()) {
            System.out.println("Logged out successfully!");
        } else {
            System.out.println("Logout failed: " + result.getMessage());
        }
    }
    
    // Helper methods for input handling
    private static String promptString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
    
    private static int promptInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }
    
    private static double promptDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }
}
