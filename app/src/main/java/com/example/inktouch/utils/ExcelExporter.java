package com.example.inktouch.utils;

import android.content.Context;
import android.os.Environment;

import com.example.inktouch.models.Order;
import com.example.inktouch.models.OrderItem;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExcelExporter {

    private static final String FOLDER_NAME = "InkTouch";
    private static final String FILE_PREFIX = "Laporan_Transaksi_";

    public static File exportOrdersToExcel(Context context, List<Order> orders, Map<String, List<OrderItem>> orderItemsMap) throws IOException {
        // Create workbook
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Laporan Transaksi");

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Create data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // Create currency style
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(dataStyle);
        DataFormat format = workbook.createDataFormat();
        currencyStyle.setDataFormat(format.getFormat("Rp #,##0"));

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"No", "Nomor Order", "Tanggal", "Customer", "Items", "Subtotal", "Tunai", "Kembalian"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Fill data
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        int rowNum = 1;
        double totalRevenue = 0;

        for (Order order : orders) {
            Row row = sheet.createRow(rowNum++);

            // No
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(rowNum - 1);
            cell0.setCellStyle(dataStyle);

            // Order Number
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(order.getOrderNumber());
            cell1.setCellStyle(dataStyle);

            // Date
            Cell cell2 = row.createCell(2);
            try {
                Date date = inputFormat.parse(order.getCreatedAt());
                cell2.setCellValue(dateFormat.format(date));
            } catch (Exception e) {
                cell2.setCellValue(order.getCreatedAt());
            }
            cell2.setCellStyle(dataStyle);

            // Customer
            Cell cell3 = row.createCell(3);
            String customerName = order.getCustomerName();
            cell3.setCellValue(customerName != null && !customerName.isEmpty() ? customerName : "Guest");
            cell3.setCellStyle(dataStyle);

            // Items
            Cell cell4 = row.createCell(4);
            List<OrderItem> items = orderItemsMap.get(order.getId());
            if (items != null && !items.isEmpty()) {
                StringBuilder itemsText = new StringBuilder();
                for (int i = 0; i < items.size(); i++) {
                    OrderItem item = items.get(i);
                    if (item.getProduct() != null) {
                        itemsText.append(item.getProduct().getName());
                    } else {
                        itemsText.append("Product");
                    }
                    itemsText.append(" x").append(item.getQty());
                    if (i < items.size() - 1) {
                        itemsText.append(", ");
                    }
                }
                cell4.setCellValue(itemsText.toString());
            } else {
                cell4.setCellValue("-");
            }
            cell4.setCellStyle(dataStyle);

            // Subtotal
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(order.getSubtotal());
            cell5.setCellStyle(currencyStyle);

            // Cash
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(order.getCash());
            cell6.setCellStyle(currencyStyle);

            // Change
            Cell cell7 = row.createCell(7);
            cell7.setCellValue(order.getChange());
            cell7.setCellStyle(currencyStyle);

            totalRevenue += order.getSubtotal();
        }

        // Add summary row
        rowNum++;
        Row summaryRow = sheet.createRow(rowNum);
        Cell summaryLabelCell = summaryRow.createCell(4);
        summaryLabelCell.setCellValue("Total Pendapatan:");
        summaryLabelCell.setCellStyle(headerStyle);

        Cell summaryValueCell = summaryRow.createCell(5);
        summaryValueCell.setCellValue(totalRevenue);
        summaryValueCell.setCellStyle(currencyStyle);

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Save file
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), FOLDER_NAME);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = FILE_PREFIX + timestamp + ".xlsx";
        File file = new File(directory, fileName);

        FileOutputStream outputStream = new FileOutputStream(file);
        workbook.write(outputStream);
        outputStream.close();
        workbook.close();

        return file;
    }

    public static String generateWhatsAppMessage(List<Order> orders, Map<String, List<OrderItem>> orderItemsMap) {
        StringBuilder message = new StringBuilder();
        message.append("*LAPORAN TRANSAKSI INKTOUCH*\n");
        message.append("================================\n\n");

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        double totalRevenue = 0;
        int orderCount = 0;

        for (Order order : orders) {
            orderCount++;
            message.append("*Order #").append(orderCount).append("*\n");
            message.append("No. Order: ").append(order.getOrderNumber()).append("\n");

            // Date
            try {
                Date date = inputFormat.parse(order.getCreatedAt());
                message.append("Tanggal: ").append(dateFormat.format(date)).append("\n");
            } catch (Exception e) {
                message.append("Tanggal: ").append(order.getCreatedAt()).append("\n");
            }

            // Customer
            String customerName = order.getCustomerName();
            message.append("Customer: ").append(customerName != null && !customerName.isEmpty() ? customerName : "Guest").append("\n");

            // Items
            List<OrderItem> items = orderItemsMap.get(order.getId());
            if (items != null && !items.isEmpty()) {
                message.append("Items:\n");
                for (OrderItem item : items) {
                    message.append("  - ");
                    if (item.getProduct() != null) {
                        message.append(item.getProduct().getName());
                    } else {
                        message.append("Product");
                    }
                    message.append(" x").append(item.getQty());
                    message.append(" @ ").append(currencyFormatter.format(item.getPrice()));
                    message.append("\n");
                }
            }

            message.append("Subtotal: ").append(currencyFormatter.format(order.getSubtotal())).append("\n");
            message.append("--------------------------------\n\n");

            totalRevenue += order.getSubtotal();
        }

        message.append("*RINGKASAN*\n");
        message.append("Total Transaksi: ").append(orderCount).append("\n");
        message.append("Total Pendapatan: ").append(currencyFormatter.format(totalRevenue)).append("\n");
        message.append("\n_Laporan dibuat oleh InkTouch POS_");

        return message.toString();
    }
}
