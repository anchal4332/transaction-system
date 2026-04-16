package com.example.transactionsystem.service;

import com.example.transactionsystem.entity.Transaction;
import com.example.transactionsystem.repository.TransactionRepository;
import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository repository;

    // =========================
    // PAGINATION AND SORTING
    // =========================
    public List<Transaction> getTransactions(int page, int size, String sortBy, String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findAll(pageable).getContent();
    }

    // =========================
    // CSV UPLOAD WITH DUPLICATES
    // =========================
    public String processCSV(MultipartFile file) {

        int inserted = 0;
        int duplicates = 0;

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {

            String[] line;
            List<Transaction> batch = new ArrayList<>();

            reader.readNext(); // skip header

            while ((line = reader.readNext()) != null) {

                if (line.length < 5) continue;

                String txnId = line[0];
                String rrn = line[1];

                // check duplicate
                boolean exists = repository.findByTxnIdAndRrn(txnId, rrn).isPresent();

                if (!exists) {

                    Transaction txn = new Transaction();
                    txn.setTxnId(txnId);
                    txn.setRrn(rrn);
                    txn.setPayerVpa(line[2]);
                    txn.setPayeeVpa(line[3]);
                    txn.setRemitterDetails(line[4]);
                    txn.setTransactionDate(LocalDateTime.now());

                    batch.add(txn);
                    inserted++;

                } else {
                    duplicates++;
                    System.out.println("Duplicate found (CSV): " + txnId + "-" + rrn);
                }

                if (batch.size() == 1000) {
                    repository.saveAll(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                repository.saveAll(batch);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }

        return "CSV Completed → Inserted: " + inserted + ", Duplicates: " + duplicates;
    }

    // =========================
    // SEARCH API
    // =========================
    public List<Transaction> searchTransactions(String txnId, String rrn, String payerVpa, String payeeVpa) {

        if (txnId != null) {
            return repository.findByTxnId(txnId);
        }

        if (rrn != null) {
            return repository.findByRrn(rrn);
        }

        if (payerVpa != null) {
            return repository.findByPayerVpa(payerVpa);
        }

        if (payeeVpa != null) {
            return repository.findByPayeeVpa(payeeVpa);
        }

        return repository.findAll();
    }

    // =========================
    // GET ALL
    // =========================
    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

    // =========================
    // EXCEL UPLOAD WITH DUPLICATES
    // =========================
    public String processExcel(MultipartFile file) {

        int inserted = 0;
        int duplicates = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            List<Transaction> batch = new ArrayList<>();

            for (Row row : sheet) {

                if (row.getRowNum() == 0) continue; // skip header

                String txnId = row.getCell(0).getStringCellValue();
                String rrn = row.getCell(1).getStringCellValue();

                // check duplicate
                boolean exists = repository.findByTxnIdAndRrn(txnId, rrn).isPresent();

                if (!exists) {

                    Transaction txn = new Transaction();
                    txn.setTxnId(txnId);
                    txn.setRrn(rrn);
                    txn.setPayerVpa(row.getCell(2).getStringCellValue());
                    txn.setPayeeVpa(row.getCell(3).getStringCellValue());
                    txn.setRemitterDetails(row.getCell(4).getStringCellValue());
                    txn.setTransactionDate(LocalDateTime.now());

                    batch.add(txn);
                    inserted++;

                } else {
                    duplicates++;
                    System.out.println("Duplicate found (EXCEL): " + txnId + "-" + rrn);
                }

                if (batch.size() == 1000) {
                    repository.saveAll(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                repository.saveAll(batch);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }

        return "Excel Completed → Inserted: " + inserted + ", Duplicates: " + duplicates;
    }
}