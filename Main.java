import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        // Read products
        Map<Integer, String> productMap = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("products.tsv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                productMap.put(id, name);
            }
        }

        // Read customers
        Map<String, String> customerMap = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("customers.tsv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                String id = parts[0];
                String name = parts[1];
                customerMap.put(id, name);
            }
        }

        // Read orders and process data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        Map<Integer, ProductData> productDataMap = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("orders.tsv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                int productId = Integer.parseInt(parts[3]);
                String customerId = parts[2];
                LocalDate date = LocalDate.parse(parts[1], formatter);

                ProductData productData = productDataMap.get(productId);
                if (productData == null) {
                    String productName = productMap.get(productId);
                    productData = new ProductData(productId, productName);
                    productDataMap.put(productId, productData);
                }
                productData.incrementPurchaseCount();

                CustomerData customerData = productData.getCustomerData(customerId);
                if (customerData == null) {
                    String customerName = customerMap.get(customerId);
                    customerData = new CustomerData(customerId, customerName);
                    productData.addCustomerData(customerData);
                }
                customerData.addPurchaseDate(date);
            }
        }

        // Sort product data by number of customers, descending
        List<ProductData> productData = productDataMap.values().stream()
                .sorted((a, b) -> b.getCustomerCount() - a.getCustomerCount())
                .collect(Collectors.toList());
        // Write output file
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("output.tsv"))) {
            for (ProductData product : productData) {
                int productId = product.getId();
                String productName = product.getName();
                int customerCount = product.getCustomerCount();
                int purchaseCount = product.getPurchaseCount();
                CustomerData minCustomer = product.getMinCustomer();
                String minCustomerId = minCustomer.getId();
                String minCustomerName = minCustomer.getName();
                LocalDate lastPurchaseDate = minCustomer.getLastPurchaseDate();
                long daysSinceLastPurchase = today.toEpochDay() - lastPurchaseDate.toEpochDay();

                String line = String.format("%d\t%s\t%d\t%d\t%s\t%d",
                        productId, productName, customerCount, purchaseCount,
                        minCustomerName, daysSinceLastPurchase);
                writer.write(line);
                writer.newLine();
            }
        }
    }
}

class ProductData {
    private final int id;
    private final String name;
    private int purchaseCount;
    private final Map<String, CustomerData> customerDataMap;

    public ProductData(int id, String name) {
        this.id = id;
        this.name = name;
        this.customerDataMap = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPurchaseCount() {
        return purchaseCount;
    }

    public void incrementPurchaseCount() {
        purchaseCount++;
    }

    public int getCustomerCount() {
        return customerDataMap.size();
    }

    public CustomerData getMinCustomer() {
        return customerDataMap.values().stream()
                .min((a, b) -> a.getId().compareTo(b.getId()))
                .orElse(null);
    }

    public CustomerData getCustomerData(String customerId) {
        return customerDataMap.get(customerId);
    }

    public void addCustomerData(CustomerData customerData) {
        customerDataMap.put(customerData.getId(), customerData);
    }
}

class CustomerData {
    private final String id;
    private final String name;
    private final List<LocalDate> purchaseDates;

    public CustomerData(String id, String name) {
        this.id = id;
        this.name = name;
        this.purchaseDates = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void addPurchaseDate(LocalDate date) {
        purchaseDates.add(date);
    }

    public LocalDate getLastPurchaseDate() {
        return purchaseDates.get(purchaseDates.size() - 1);
    }
}

