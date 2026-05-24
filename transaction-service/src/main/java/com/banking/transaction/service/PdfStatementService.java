package com.banking.transaction.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import com.banking.transaction.dto.TransactionDto;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class PdfStatementService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generateStatement(String accountNumber, String holderName,
                                    String fromDate, String toDate,
                                    List<TransactionDto> transactions) {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // Header background
                cs.setNonStrokingColor(new Color(10, 15, 30));
                cs.addRect(0, 750, 595, 92);
                cs.fill();

                // Bank name
                cs.setNonStrokingColor(Color.WHITE);
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 22);
                cs.newLineAtOffset(40, 810);
                cs.showText("ENTERPRISE BANK");
                cs.endText();

                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(40, 790);
                cs.showText("Account Statement | Confidential");
                cs.endText();

                // Account info section
                cs.setNonStrokingColor(new Color(245, 158, 11));
                cs.addRect(40, 700, 515, 1);
                cs.fill();

                cs.setNonStrokingColor(Color.BLACK);
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
                cs.newLineAtOffset(40, 685);
                cs.showText("Account Holder: " + holderName);
                cs.endText();

                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                cs.newLineAtOffset(40, 668);
                cs.showText("Account Number: " + maskAccountNumber(accountNumber));
                cs.endText();

                cs.beginText();
                cs.newLineAtOffset(40, 651);
                cs.showText("Statement Period: " + fromDate + " to " + toDate);
                cs.endText();

                // Table header
                float y = 620;
                cs.setNonStrokingColor(new Color(14, 165, 233));
                cs.addRect(40, y, 515, 22);
                cs.fill();

                cs.setNonStrokingColor(Color.WHITE);
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
                cs.newLineAtOffset(45, y + 7);
                cs.showText("DATE");
                cs.newLineAtOffset(80, 0);
                cs.showText("REFERENCE");
                cs.newLineAtOffset(130, 0);
                cs.showText("DESCRIPTION");
                cs.newLineAtOffset(140, 0);
                cs.showText("TYPE");
                cs.newLineAtOffset(60, 0);
                cs.showText("AMOUNT (INR)");
                cs.endText();

                // Rows
                y -= 5;
                boolean alternate = false;
                for (TransactionDto txn : transactions) {
                    y -= 20;
                    if (y < 60) break; // Avoid overflow (multi-page not implemented here)

                    if (alternate) {
                        cs.setNonStrokingColor(new Color(245, 245, 250));
                        cs.addRect(40, y - 4, 515, 20);
                        cs.fill();
                    }
                    alternate = !alternate;

                    boolean isCredit = txn.getToAccountNumber() != null &&
                                       txn.getToAccountNumber().equals(accountNumber);
                    Color amtColor = isCredit ? new Color(34, 197, 94) : new Color(239, 68, 68);

                    cs.setNonStrokingColor(Color.BLACK);
                    cs.beginText();
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
                    cs.newLineAtOffset(45, y + 3);
                    cs.showText(txn.getCreatedAt() != null ? txn.getCreatedAt().format(DATE_FMT) : "-");
                    cs.newLineAtOffset(80, 0);
                    cs.showText(txn.getReferenceNumber().substring(0, Math.min(14, txn.getReferenceNumber().length())));
                    cs.newLineAtOffset(130, 0);
                    String desc = txn.getDescription() != null ? txn.getDescription() : txn.getTransactionType().name();
                    cs.showText(desc.length() > 22 ? desc.substring(0, 22) : desc);
                    cs.newLineAtOffset(140, 0);
                    cs.showText(txn.getTransactionType().name());
                    cs.endText();

                    cs.setNonStrokingColor(amtColor);
                    cs.beginText();
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 8);
                    cs.newLineAtOffset(455, y + 3);
                    cs.showText((isCredit ? "+" : "-") + "₹" + String.format("%.2f", txn.getAmount()));
                    cs.endText();
                }

                // Footer
                cs.setNonStrokingColor(new Color(10, 15, 30));
                cs.addRect(0, 0, 595, 40);
                cs.fill();
                cs.setNonStrokingColor(Color.WHITE);
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
                cs.newLineAtOffset(40, 18);
                cs.showText("This is a computer-generated statement. Enterprise Bank | IFSC: BANK0001234 | support@enterprisebank.in");
                cs.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();

        } catch (IOException e) {
            log.error("PDF generation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate statement PDF", e);
        }
    }

    private String maskAccountNumber(String acc) {
        if (acc == null || acc.length() < 8) return acc;
        return "XXXX XXXX " + acc.substring(acc.length() - 4);
    }
}
