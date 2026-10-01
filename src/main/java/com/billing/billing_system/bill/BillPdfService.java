
package com.billing.billing_system.bill;

import com.billing.billing_system.shop.ShopProfile;
import com.billing.billing_system.shop.ShopProfileService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class BillPdfService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final BillService billService;
    private final ShopProfileService shopService;

    public BillPdfService(BillService billService, ShopProfileService shopService) {
        this.billService = billService;
        this.shopService = shopService;
    }

    public byte[] generate(Long billId) {
        Bill bill = billService.getById(billId);
        ShopProfile shop = shopService.get();

        Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A5, 30, 30, 30, 30);

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Shop details at the top
            centered(doc, shop.getShopName(), title);
            centered(doc, shop.getAddress(), normal);
            if (hasText(shop.getPhone())) {
                centered(doc, "Phone: " + shop.getPhone(), normal);
            }
            if (hasText(shop.getGstin())) {
                centered(doc, "GSTIN: " + shop.getGstin(), normal);
            }
            doc.add(new Paragraph(" "));

            // Bill and customer details
            doc.add(new Paragraph("Bill No: " + bill.getBillNumber()
                    + "    Date: " + bill.getCreatedAt().format(DATE_FORMAT), normal));
            doc.add(new Paragraph("Payment: " + bill.getPaymentMode(), normal));
            if (hasText(bill.getCustomerName())) {
                doc.add(new Paragraph("Customer: " + bill.getCustomerName(), normal));
            }
            if (hasText(bill.getCustomerPhone())) {
                doc.add(new Paragraph("Customer Phone: " + bill.getCustomerPhone(), normal));
            }
            if (hasText(bill.getCustomerGstin())) {
                doc.add(new Paragraph("Customer GSTIN: " + bill.getCustomerGstin(), normal));
            }

            // Items table
            PdfPTable table = new PdfPTable(new float[] {4f, 1.2f, 1.8f, 1.4f, 2.2f});
            table.setWidthPercentage(100);
            table.setSpacingBefore(8f);
            table.setHeaderRows(1);

            for (String header : new String[] {"Item", "Qty", "Price", "GST %", "Amount"}) {
                table.addCell(cell(header, bold, Element.ALIGN_CENTER));
            }
            for (BillItem item : bill.getItems()) {
                table.addCell(cell(item.getProductName(), normal, Element.ALIGN_LEFT));
                table.addCell(cell(String.valueOf(item.getQuantity()), normal, Element.ALIGN_CENTER));
                table.addCell(cell(item.getUnitPrice().toPlainString(), normal, Element.ALIGN_RIGHT));
                table.addCell(cell(item.getGstPercent().toPlainString(), normal, Element.ALIGN_RIGHT));
                table.addCell(cell(item.getLineTotal().toPlainString(), normal, Element.ALIGN_RIGHT));
            }
            doc.add(table);

            // Totals
            doc.add(new Paragraph(" "));
            right(doc, "Subtotal: Rs. " + bill.getSubtotal().toPlainString(), normal);
            right(doc, "GST: Rs. " + bill.getGstTotal().toPlainString(), normal);
            right(doc, "TOTAL: Rs. " + bill.getTotalAmount().toPlainString(), bold);

            // Footer
            doc.add(new Paragraph(" "));
            String footer = hasText(shop.getFooterText())
                    ? shop.getFooterText() : "Thank you, visit again";
            centered(doc, footer, normal);

            doc.close();
            return out.toByteArray();

        } catch (DocumentException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not create the PDF");
        }
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private void centered(Document doc, String text, Font font) throws DocumentException {
        if (!hasText(text)) {
            return;
        }
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);
    }

    private void right(Document doc, String text, Font font) throws DocumentException {
        Paragraph p = new Paragraph(text, font);
        p.setAlignment(Element.ALIGN_RIGHT);
        doc.add(p);
    }

    private PdfPCell cell(String text, Font font, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setHorizontalAlignment(align);
        c.setPadding(4f);
        return c;
    }
}