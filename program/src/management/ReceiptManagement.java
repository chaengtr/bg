package management;

import model.Receipt;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ReceiptManagement {
    private String receiptPath = "D:/";
    private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat df2 = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd ", new Locale("th"));

    public ReceiptManagement() { }

    public void receipt(Receipt receipt) {
        try {
            PDDocument doc = new PDDocument();
            PDPage page = new PDPage();
            doc.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(doc, page);

            PDFont font = PDType0Font
                    .load(doc, this.getClass()
                    .getResourceAsStream("/file/THSarabunNew.ttf"));
            PDFont fontBold = PDType0Font
                    .load(doc, this.getClass()
                    .getResourceAsStream("/file/THSarabunNew Bold.ttf"));
            float fontSize = 20;
            float fontSizeBold = 40;
            float leading = 20;

            float yCordinate = page.getCropBox().getUpperRightY() - 50;
            float startX = page.getCropBox().getLowerLeftX() + 30;
            float endX = page.getCropBox().getUpperRightX() - 300;

            contentStream.beginText();
            contentStream.newLineAtOffset(startX, yCordinate);
            contentStream.newLineAtOffset(65, 0);
            contentStream.setFont(fontBold, fontSizeBold);
            contentStream.showText("ใบเสร็จรับเงิน");
            contentStream.newLineAtOffset(-65, 0);
            yCordinate -= 10;

            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText("เลขที่ใบเสร็จ");
            contentStream.newLineAtOffset(150, 0);
            contentStream.showText(receipt.getId());
            contentStream.newLineAtOffset(-150, 0);
            yCordinate -= leading;

            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText("รหัสการจองเกม");
            contentStream.newLineAtOffset(150, 0);
            contentStream.showText(receipt.getBookingCode());
            contentStream.newLineAtOffset(-150, 0);
            yCordinate -= leading;

            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText("เวลาเริ่มเกม");
            contentStream.newLineAtOffset(150, 0);
            contentStream.showText(df.format(receipt.getStart()));
            contentStream.newLineAtOffset(-150, 0);
            yCordinate -= leading;

            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText("เวลาจบเกม");
            contentStream.newLineAtOffset(150, 0);
            contentStream.showText(df.format(receipt.getEnd()));
            contentStream.newLineAtOffset(-150, 0);
            yCordinate -= leading;

            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText("วันที่ออกใบเสร็จ");
            contentStream.newLineAtOffset(150, 0);
            contentStream.showText(df2.format(receipt.getDate()));
            contentStream.newLineAtOffset(-150, 0);
            yCordinate -= leading;

            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText("ผู้ใช้");
            contentStream.newLineAtOffset(150, 0);
            contentStream.showText(receipt.getUser());
            contentStream.newLineAtOffset(-150, 0);
            yCordinate -= leading;
            contentStream.endText();

            // Create line
            contentStream.moveTo(startX, yCordinate);
            contentStream.lineTo(endX, yCordinate);
            contentStream.stroke();
            yCordinate -= leading;

            /*---------------------------------------------------------------*/
            contentStream.beginText();
            contentStream.newLineAtOffset(startX, yCordinate);
            contentStream.setFont(fontBold, fontSize);
            contentStream.showText("รายการ");
            contentStream.newLineAtOffset(160, 0);
            contentStream.showText("จำนวน");
            contentStream.newLineAtOffset(70, 0);
            contentStream.showText("ราคา");
            contentStream.newLineAtOffset(-230, 0);
            yCordinate -= leading;
            contentStream.endText();

            contentStream.moveTo(startX, yCordinate);
            contentStream.lineTo(endX, yCordinate);
            contentStream.stroke();
            yCordinate -= leading;

            /*---------------------------------------------------------------*/
            contentStream.beginText();
            contentStream.newLineAtOffset(startX, yCordinate);
            contentStream.setFont(font, fontSize);
            contentStream.showText(receipt.getGame());
            contentStream.newLineAtOffset(165, 0);
            contentStream.showText(String.valueOf(receipt.getHour()));
            contentStream.newLineAtOffset(65, 0);
            contentStream.showText(String.format("%.2f", receipt.getPrice()));
            contentStream.newLineAtOffset(-230, 0);
            yCordinate -= leading;

            String pro = receipt.getPromotion();
            if (pro != "") {
                contentStream.newLineAtOffset(0, -leading);
                contentStream.showText(receipt.getPromotion());
                yCordinate -= leading;
            }
            contentStream.endText();

            contentStream.moveTo(startX, yCordinate);
            contentStream.lineTo(endX, yCordinate);
            contentStream.stroke();
            yCordinate -= leading;

            /*---------------------------------------------------------------*/
            contentStream.beginText();
            contentStream.newLineAtOffset(startX, yCordinate);
            contentStream.setFont(fontBold, fontSize);
            contentStream.showText("ยอดรวม");
            contentStream.newLineAtOffset(230, 0);
            contentStream.showText(String.format("%.2f", receipt.getPrice()));
            contentStream.newLineAtOffset(-230, 0);
            yCordinate -= leading;

            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText("ส่วนลด");
            contentStream.newLineAtOffset(230, 0);
            contentStream.showText(String.format("%.2f", receipt.getDiscount()));
            contentStream.newLineAtOffset(-230, 0);
            yCordinate -= leading;

            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText("ยอดรวมสุทธิ");
            contentStream.newLineAtOffset(230, 0);
            contentStream.showText(String.format("%.2f", receipt.getAmount()));
            contentStream.newLineAtOffset(-230, 0);
            yCordinate -= leading;

            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText("ได้รับคะแนน");
            contentStream.newLineAtOffset(230, 0);
            contentStream.showText(String.valueOf(receipt.getPoint()));
            contentStream.newLineAtOffset(-230, 0);
            yCordinate -= leading;
            contentStream.endText();

            contentStream.moveTo(startX, yCordinate);
            contentStream.lineTo(endX, yCordinate);
            contentStream.stroke();
            contentStream.close();

            String path = receiptPath + receipt.getId() + ".pdf";
            doc.save(path);
            doc.close();
            System.out.println("ReceiptManagement - PDF create done");
        } catch (IOException e) {
            System.err.println("ReceiptManagement - error");
        }
    }
}
