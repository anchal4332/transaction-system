package com.example.transactionsystem.controller;

import com.example.transactionsystem.service.TransactionService;
import com.example.transactionsystem.entity.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.springframework.data.domain.Page;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    @Autowired
    private TransactionService service;

    @PostMapping("/upload-excel")
    public String uploadExcel(@RequestParam("file") MultipartFile file) {
        return service.processExcel(file);
    }

    @GetMapping("/test")
    public String test() {
        return "Transaction System API is working 🚀";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return "File is empty";
        }

        return service.processCSV(file);
    }

    @GetMapping("/search")
    public List<Transaction> searchTransactions(
            @RequestParam(required = false) String txnId,
            @RequestParam(required = false) String rrn,
            @RequestParam(required = false) String payerVpa,
            @RequestParam(required = false) String payeeVpa
    ) {
        return service.searchTransactions(txnId, rrn, payerVpa, payeeVpa);
    }

    @GetMapping("/transactions")
    public List<Transaction> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "txnId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return service.getTransactions(page, size, sortBy, direction);
    }

    @GetMapping("/download")
    public void downloadCSV(HttpServletResponse response) {

        try {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=transactions.csv");

            List<Transaction> transactions = service.getAllTransactions();

            PrintWriter writer = response.getWriter();

            // header
            writer.println("txnId,rrn,payerVpa,payeeVpa,remitterDetails,transactionDate");

            for (Transaction t : transactions) {
                writer.println(
                        t.getTxnId() + "," +
                                t.getRrn() + "," +
                                t.getPayerVpa() + "," +
                                t.getPayeeVpa() + "," +
                                t.getRemitterDetails() + "," +
                                t.getTransactionDate()
                );
            }

            writer.flush();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/download-zip")
    public void downloadZip(HttpServletResponse response) {

        try {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=transactions.zip");

            ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());

            // Creates CSV file inside ZIP
            ZipEntry zipEntry = new ZipEntry("transactions.csv");
            zipOut.putNextEntry(zipEntry);

            PrintWriter writer = new PrintWriter(zipOut);

            // CSV header
            writer.println("txnId,rrn,payerVpa,payeeVpa,remitterDetails,transactionDate");

            // fetch data
            List<Transaction> list = service.getAllTransactions();

            for (Transaction t : list) {
                writer.println(
                        t.getTxnId() + "," +
                                t.getRrn() + "," +
                                t.getPayerVpa() + "," +
                                t.getPayeeVpa() + "," +
                                t.getRemitterDetails() + "," +
                                t.getTransactionDate()
                );
            }

            writer.flush();
            zipOut.closeEntry();
            zipOut.finish();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
