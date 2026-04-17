package com.example.backend.dataset;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class KagglePreprocessor {

    private static class CustomerRecord {
        LocalDate lastPurchaseDate = LocalDate.MIN;
        int frequency = 0;
        double totalMonetary = 0.0;
    }

    public static void processWalmartData(String rawCsvPath, String outputCsvPath) {
        Map<String, CustomerRecord> customers = new HashMap<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy"); 
        LocalDate maxDateInDataset = LocalDate.MIN;

        System.out.println("Reading raw Walmart dataset...");

        try (BufferedReader br = new BufferedReader(new FileReader(rawCsvPath))) {
            String line = br.readLine(); // Skip the header row
            
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                // Split by comma. Note: If Product_Name contains commas, this might break.
                // Assuming standard clean CSV for now.
                String[] cols = line.split(",");

                String customerId = cols[0];
                LocalDate date = LocalDate.parse(cols[6], formatter);
                double amount = Double.parseDouble(cols[7]);

                //track the absolute latest date in the whole dataset
                if (date.isAfter(maxDateInDataset)) {
                    maxDateInDataset = date;
                }

                // Update this customer's record
                customers.putIfAbsent(customerId, new CustomerRecord());
                CustomerRecord record = customers.get(customerId);
                
                record.frequency += 1;
                record.totalMonetary += amount;
                if (date.isAfter(record.lastPurchaseDate)) {
                    record.lastPurchaseDate = date;
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing dataset: " + e.getMessage());
            return;
        }

        System.out.println("Found " + customers.size() + " unique customers. Writing RFM data...");

        // Write the summarized data to a new CSV
        try (FileWriter writer = new FileWriter(outputCsvPath)) {
            // Write standard header so DatasetLoader skips it correctly
            writer.append("Recency,Frequency,Monetary\n");

            for (CustomerRecord record : customers.values()) {
                long recency = ChronoUnit.DAYS.between(record.lastPurchaseDate, maxDateInDataset);
                
                writer.append(String.valueOf(recency)).append(",")
                      .append(String.valueOf(record.frequency)).append(",")
                      .append(String.valueOf(record.totalMonetary)).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing output file: " + e.getMessage());
        }

        System.out.println("Success! Clean RFM dataset saved to: " + outputCsvPath);
    }
}