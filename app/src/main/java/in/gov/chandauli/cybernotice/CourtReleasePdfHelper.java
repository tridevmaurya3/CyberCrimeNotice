package in.gov.chandauli.cybernotice;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public final class CourtReleasePdfHelper {

    private static final int A4_WIDTH = 595;
    private static final int A4_HEIGHT = 842;
    private static final float LEFT_MARGIN = 54f;
    private static final float RIGHT_MARGIN = 54f;
    private static final int CONTENT_WIDTH = (int) (A4_WIDTH - LEFT_MARGIN - RIGHT_MARGIN);

    private CourtReleasePdfHelper() {}

    public static class TransactionData implements java.io.Serializable {
        public String appAccIfsc, fraudAccIfsc, utr, date, amount;
        public String fraudAccount, fraudIfsc, fraudBank, fraudBranch, fraudAddress;

        public TransactionData(String appAccIfsc, String fraudAccIfsc, String utr, String date, String amount) {
            this.appAccIfsc = appAccIfsc; this.fraudAccIfsc = fraudAccIfsc;
            this.utr = utr; this.date = date; this.amount = amount;
            this.fraudAccount = fraudAccIfsc;
            this.fraudIfsc = "";
            this.fraudBank = "";
            this.fraudBranch = "";
            this.fraudAddress = "";
        }
    }

    public static class CourtReleaseData implements java.io.Serializable {
        String letterYear, letterDate, nodalBank, appName, fatherName, address;
        String appAccount, appBank, appIfsc, appBranch, appBankAddress;
        String fraudDate, fraudAmount, ncrpNo, firNo, sections;
        String reportType, reportDate, holdAmount, releaseAmount;
        String noticeNumber;
        List<TransactionData> transactions;

        public CourtReleaseData(String letterYear, String letterDate, String nodalBank, String appName, String fatherName, String address, String appAccount, String appBank, String appIfsc, String fraudDate, String fraudAmount, String ncrpNo, String firNo, String sections, String reportType, String reportDate, String holdAmount, String releaseAmount, List<TransactionData> transactions) {
            this(letterYear, letterDate, nodalBank, appName, fatherName, address,
                    appAccount, appBank, appIfsc, "", "", fraudDate, fraudAmount,
                    ncrpNo, firNo, sections, reportType, reportDate, holdAmount,
                    releaseAmount, transactions);
        }

        public CourtReleaseData(String letterYear, String letterDate, String nodalBank, String appName, String fatherName, String address, String appAccount, String appBank, String appIfsc, String appBranch, String appBankAddress, String fraudDate, String fraudAmount, String ncrpNo, String firNo, String sections, String reportType, String reportDate, String holdAmount, String releaseAmount, List<TransactionData> transactions) {
            this.letterYear = safeText(letterYear); this.letterDate = safeText(letterDate); this.nodalBank = safeText(nodalBank);
            this.appName = safeText(appName); this.fatherName = safeText(fatherName); this.address = safeText(address);
            this.appAccount = safeText(appAccount); this.appBank = safeText(appBank); this.appIfsc = safeText(appIfsc);
            this.appBranch = safeText(appBranch); this.appBankAddress = safeText(appBankAddress);
            this.fraudDate = safeText(fraudDate); this.fraudAmount = safeText(fraudAmount); this.ncrpNo = safeText(ncrpNo);
            this.firNo = safeText(firNo); this.sections = safeText(sections); this.reportType = safeText(reportType);
            this.reportDate = safeText(reportDate); this.holdAmount = safeText(holdAmount); this.releaseAmount = safeText(releaseAmount);
            this.transactions = transactions == null ? new ArrayList<>() : new ArrayList<>(transactions);
            this.noticeNumber = "";
        }
    }

    // अब यह मेथड isHindi पैरामीटर भी लेता है
    public static JSONObject toJson(CourtReleaseData data) {
        JSONObject object = new JSONObject();

        if (data == null) {
            return object;
        }

        try {
            object.put("letter_year", safeText(data.letterYear));
            object.put("letter_date", safeText(data.letterDate));
            object.put("nodal_bank", safeText(data.nodalBank));
            object.put("applicant_name", safeText(data.appName));
            object.put("father_name", safeText(data.fatherName));
            object.put("address", safeText(data.address));
            object.put("applicant_account", safeText(data.appAccount));
            object.put("applicant_bank", safeText(data.appBank));
            object.put("applicant_ifsc", safeText(data.appIfsc));
            object.put("applicant_branch", safeText(data.appBranch));
            object.put("applicant_bank_address", safeText(data.appBankAddress));
            object.put("fraud_date", safeText(data.fraudDate));
            object.put("fraud_amount", safeText(data.fraudAmount));
            object.put("ncrp_number", safeText(data.ncrpNo));
            object.put("fir_number", safeText(data.firNo));
            object.put("sections", safeText(data.sections));
            object.put("report_type", safeText(data.reportType));
            object.put("report_date", safeText(data.reportDate));
            object.put("hold_amount", safeText(data.holdAmount));
            object.put("release_amount", safeText(data.releaseAmount));
            object.put("notice_number", safeText(data.noticeNumber));

            JSONArray transactions = new JSONArray();
            for (TransactionData transaction : data.transactions) {
                JSONObject item = new JSONObject();
                item.put("app_acc_ifsc", safeText(transaction.appAccIfsc));
                item.put("fraud_acc_ifsc", safeText(transaction.fraudAccIfsc));
                item.put("fraud_account", safeText(transaction.fraudAccount));
                item.put("fraud_ifsc", safeText(transaction.fraudIfsc));
                item.put("fraud_bank", safeText(transaction.fraudBank));
                item.put("fraud_branch", safeText(transaction.fraudBranch));
                item.put("fraud_address", safeText(transaction.fraudAddress));
                item.put("utr", safeText(transaction.utr));
                item.put("date", safeText(transaction.date));
                item.put("amount", safeText(transaction.amount));
                transactions.put(item);
            }
            object.put("transactions_json", transactions.toString());
        } catch (Exception ignored) {
        }

        return object;
    }

    public static CourtReleaseData fromJson(JSONObject object) {
        if (object == null) {
            return null;
        }

        List<TransactionData> transactions = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(object.optString("transactions_json", "[]"));
            for (int index = 0; index < array.length(); index++) {
                JSONObject item = array.getJSONObject(index);
                TransactionData transaction = new TransactionData(
                        item.optString("app_acc_ifsc", ""),
                        item.optString("fraud_acc_ifsc", ""),
                        item.optString("utr", ""),
                        item.optString("date", ""),
                        item.optString("amount", "")
                );
                transaction.fraudAccount = item.optString(
                        "fraud_account",
                        transaction.fraudAccIfsc
                );
                transaction.fraudIfsc = item.optString("fraud_ifsc", "");
                transaction.fraudBank = item.optString("fraud_bank", "");
                transaction.fraudBranch = item.optString("fraud_branch", "");
                transaction.fraudAddress = item.optString("fraud_address", "");
                transactions.add(transaction);
            }
        } catch (Exception ignored) {
        }

        CourtReleaseData data = new CourtReleaseData(
                object.optString("letter_year", ""),
                object.optString("letter_date", ""),
                object.optString("nodal_bank", ""),
                object.optString("applicant_name", ""),
                object.optString("father_name", ""),
                object.optString("address", ""),
                object.optString("applicant_account", ""),
                object.optString("applicant_bank", ""),
                object.optString("applicant_ifsc", ""),
                object.optString("applicant_branch", ""),
                object.optString("applicant_bank_address", ""),
                object.optString("fraud_date", ""),
                object.optString("fraud_amount", ""),
                object.optString("ncrp_number", ""),
                object.optString("fir_number", ""),
                object.optString("sections", ""),
                object.optString("report_type", ""),
                object.optString("report_date", ""),
                object.optString("hold_amount", ""),
                object.optString("release_amount", ""),
                transactions
        );
        data.noticeNumber = object.optString("notice_number", "");
        return data;
    }

    public static void printCourtRelease(
            Activity activity,
            String jobName,
            CourtReleaseData data,
            boolean isHindi
    ) {
        if (activity == null || data == null) {
            return;
        }

        PrintManager printManager = (PrintManager) activity.getSystemService(
                Context.PRINT_SERVICE
        );
        if (printManager == null) {
            return;
        }

        String safeJobName = safeText(jobName).replaceAll("[^a-zA-Z0-9_-]", "_");
        if (safeJobName.isEmpty()) {
            safeJobName = "Court_Release_Order";
        }

        printManager.print(
                safeJobName,
                new CourtReleasePrintAdapter(activity.getResources(), data, isHindi),
                new PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
        );
    }

    private static final class CourtReleasePrintAdapter extends PrintDocumentAdapter {

        private final Resources resources;
        private final CourtReleaseData data;
        private final boolean isHindi;

        CourtReleasePrintAdapter(Resources resources, CourtReleaseData data, boolean isHindi) {
            this.resources = resources;
            this.data = data;
            this.isHindi = isHindi;
        }

        @Override
        public void onLayout(
                PrintAttributes oldAttributes,
                PrintAttributes newAttributes,
                CancellationSignal cancellationSignal,
                LayoutResultCallback callback,
                Bundle extras
        ) {
            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }

            PrintDocumentInfo info = new PrintDocumentInfo.Builder(
                    "Court_Release_Order.pdf"
            )
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build();
            callback.onLayoutFinished(info, true);
        }

        @Override
        public void onWrite(
                PageRange[] pages,
                ParcelFileDescriptor destination,
                CancellationSignal cancellationSignal,
                WriteResultCallback callback
        ) {
            if (cancellationSignal.isCanceled()) {
                callback.onWriteCancelled();
                return;
            }

            PdfDocument document = new PdfDocument();
            FileOutputStream outputStream = null;
            try {
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                        A4_WIDTH,
                        A4_HEIGHT,
                        1
                ).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                drawCourtReleaseOrder(page.getCanvas(), resources, data, isHindi);
                document.finishPage(page);

                outputStream = new FileOutputStream(destination.getFileDescriptor());
                document.writeTo(outputStream);
                callback.onWriteFinished(new PageRange[]{new PageRange(0, 0)});
            } catch (Exception exception) {
                callback.onWriteFailed(exception.getMessage());
            } finally {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException ignored) {
                }
                document.close();
            }
        }
    }

    public static boolean generateSilentPdf(Context context, File outputFile, CourtReleaseData data, boolean isHindi) {
        PdfDocument pdfDocument = new PdfDocument();
        FileOutputStream outputStream = null;
        try {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            drawCourtReleaseOrder(page.getCanvas(), context.getResources(), data, isHindi);
            pdfDocument.finishPage(page);
            outputStream = new FileOutputStream(outputFile);
            pdfDocument.writeTo(outputStream);
            outputStream.flush();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try { if (outputStream != null) outputStream.close(); } catch (Exception ignored) {}
            pdfDocument.close();
        }
    }

    private static void drawCourtReleaseOrder(Canvas canvas, Resources resources, CourtReleaseData data, boolean isHindi) {
        canvas.drawColor(Color.WHITE);

        Bitmap logo = BitmapFactory.decodeResource(resources, R.drawable.up_police_logo);
        if (logo != null) {
            int logoSize = 64;
            int logoLeft = (A4_WIDTH - logoSize) / 2;
            canvas.drawBitmap(logo, null, new android.graphics.Rect(logoLeft, 40, logoLeft + logoSize, 40 + logoSize), null);
        }

        TextPaint boldPaint = createTextPaint(12f, Color.BLACK, true);
        TextPaint normalPaint = createTextPaint(12f, Color.BLACK, false);

        boldPaint.setTextAlign(Paint.Align.CENTER);
        boldPaint.setTextSize(14f);
        canvas.drawText(isHindi ? "कार्यालय थाना साइबर क्राइम, जनपद चन्दौली।" : "OFFICE OF CYBER CRIME POLICE STATION, DISTRICT CHANDAULI.", A4_WIDTH / 2f, 130f, boldPaint);

        boldPaint.setTextAlign(Paint.Align.LEFT);
        boldPaint.setTextSize(11.5f);

        String letterPrefix = isHindi ? "पत्र संख्या- थाना साइबर क्राइम (मा0 न्याया0 रिलीज आदेश)/" : "Letter No.- Cyber Crime Station (Hon'ble Court Release Order)/";
        canvas.drawText(letterPrefix + data.letterYear, LEFT_MARGIN, 160f, normalPaint);

        String datePrefix = isHindi ? "दिनांक: " : "Date: ";
        canvas.drawText(datePrefix + data.letterDate, A4_WIDTH - RIGHT_MARGIN - 100f, 160f, normalPaint);

        canvas.drawText(isHindi ? "सेवा में," : "To,", LEFT_MARGIN, 190f, normalPaint);
        canvas.drawText(isHindi ? "नोडल आफिसर," : "Nodal Officer,", LEFT_MARGIN, 210f, normalPaint);
        canvas.drawText(data.nodalBank, LEFT_MARGIN, 230f, boldPaint);
        canvas.drawText(isHindi ? "महोदय," : "Sir/Madam,", LEFT_MARGIN, 260f, normalPaint);

        String reportSection;
        if (data.reportType.contains("प्रचलित") || data.reportType.contains("Pending")) {
            reportSection = isHindi ? "जिसमें विवेचना वर्तमान में प्रचलित है।" : "where the investigation is currently pending.";
        } else {
            reportSection = isHindi ? "जिसमें विवेचना उपरान्त दिनांक- " + data.reportDate + " को मा0 न्यायालय " + data.reportType + " प्रेषित की जा चुकी है।"
                    : "where after investigation, the " + data.reportType + " has been submitted to the Hon'ble Court on " + data.reportDate + ".";
        }

        String bodyText = isHindi ?
                "अवगत कराना है कि आवेदक " + data.appName + " पुत्र " + data.fatherName + " निवासी " + data.address +
                " के साथ दिनांक " + data.fraudDate + " को साइबर फ्राड हुआ था जिसमे आवेदक के बैंक खाता सं०- " + data.appAccount +
                " बैंक नाम- " + data.appBank + " से " + data.fraudAmount + "/- साइबर फ्राड द्वारा कट गया था। आवेदक द्वारा आनलाइन www.cybercrime.gov.in पर साइबर कम्पलेन दर्ज कराया गया था जिसका कम्पलेन Ack. NO. " + data.ncrpNo +
                " है तथा आवेदक द्वारा थाना साइबर क्राइम चन्दौली पर मु0अ0सं0 " + data.firNo + " धारा " + data.sections + " का अभियोग पंजीकृत कराया गया था, " + reportSection +
                " आवेदक के Amount " + data.fraudAmount + "/- में से आनलाइन कम्पलेन से " + data.holdAmount + "/-रूपया होल्ड है। होल्ड धनराशि को वापस करने हेतु मा०न्यायालय का रिलीज आदेश संलग्नक है, विवरण निम्न है-"
                :
                "This is to inform you that a cyber fraud occurred with the applicant " + data.appName + " S/o " + data.fatherName + " R/o " + data.address +
                " on " + data.fraudDate + " where an amount of Rs. " + data.fraudAmount + "/- was fraudulently deducted from their Bank A/C No. " + data.appAccount +
                " (Bank: " + data.appBank + "). The applicant registered an online complaint on www.cybercrime.gov.in (Ack. NO. " + data.ncrpNo +
                ") and an FIR No. " + data.firNo + " U/S " + data.sections + " was registered at Cyber Crime Station Chandauli, " + reportSection +
                " Out of the total amount, Rs. " + data.holdAmount + "/- is on hold. To refund this hold amount, the Hon'ble Court Release Order is attached. Details are as follows:";

        StaticLayout bodyLayout = StaticLayout.Builder.obtain(bodyText, 0, bodyText.length(), normalPaint, CONTENT_WIDTH)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(1f, 1.15f).build();

        canvas.save();
        canvas.translate(LEFT_MARGIN + 30f, 280f);
        bodyLayout.draw(canvas);
        canvas.restore();

        float currentY = 280f + bodyLayout.getHeight() + 20f;
        drawTransactionTable(canvas, data.transactions, currentY, normalPaint, boldPaint);

        currentY += (data.transactions.size() * 20f) + 40f;

        String conclusionText = isHindi ?
                "अतः माननीय न्यायालय के आदेश के क्रम में होल्ड धनराशि " + data.releaseAmount + "/-रूपया को आवेदक " + data.appName +
                " उपरोक्त के बैंक खाता सं०- " + data.appAccount + " बैंक नाम- " + data.appBank + " IFSC Code " + data.appIfsc + " में वापस करने का कष्ट करें।"
                :
                "Therefore, in compliance with the Hon'ble Court's order, you are requested to refund the hold amount of Rs. " + data.releaseAmount + "/- to the applicant " + data.appName +
                "'s Bank A/C No. " + data.appAccount + " (Bank: " + data.appBank + ", IFSC: " + data.appIfsc + ").";

        StaticLayout conclusionLayout = StaticLayout.Builder.obtain(conclusionText, 0, conclusionText.length(), normalPaint, CONTENT_WIDTH)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(1f, 1.15f).build();

        canvas.save();
        canvas.translate(LEFT_MARGIN, currentY);
        conclusionLayout.draw(canvas);
        canvas.restore();

        currentY += conclusionLayout.getHeight() + 30f;

        canvas.drawText(isHindi ? "संलग्नक-" : "Enclosures-", LEFT_MARGIN, currentY, boldPaint);
        canvas.drawText(isHindi ? "1. मा०न्यायालय आदेश कापी" : "1. Hon'ble Court Order Copy", LEFT_MARGIN, currentY + 15f, normalPaint);
        canvas.drawText(isHindi ? "2. NCRP शिकायत की कापी" : "2. NCRP Complaint Copy", LEFT_MARGIN, currentY + 30f, normalPaint);

        String signature = isHindi ? "प्रभारी निरीक्षक,\nथाना साइबर क्राइम\nजनपद - चन्दौली।" : "Inspector In-Charge,\nCyber Crime Police Station\nDistrict - Chandauli.";
        StaticLayout sigLayout = StaticLayout.Builder.obtain(signature, 0, signature.length(), boldPaint, 200)
                .setAlignment(Layout.Alignment.ALIGN_OPPOSITE).setLineSpacing(1f, 1.15f).build();

        canvas.save();
        canvas.translate(A4_WIDTH - RIGHT_MARGIN - 200f, currentY);
        sigLayout.draw(canvas);
        canvas.restore();
    }

    private static void drawTransactionTable(Canvas canvas, List<TransactionData> transactions, float startY, TextPaint normalPaint, TextPaint boldPaint) {
        float y = startY;
        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(1f);
        linePaint.setStyle(Paint.Style.STROKE);

        float col1 = LEFT_MARGIN;
        float col2 = col1 + 130f;
        float col3 = col2 + 130f;
        float col4 = col3 + 90f;
        float col5 = col4 + 60f;
        float endX = col5 + 77f;

        canvas.drawText("Applicant A/C", col1 + 5, y + 15, boldPaint);
        canvas.drawText("Fraudster A/C", col2 + 5, y + 15, boldPaint);
        canvas.drawText("UTR No.", col3 + 5, y + 15, boldPaint);
        canvas.drawText("Date", col4 + 5, y + 15, boldPaint);
        canvas.drawText("Amount", col5 + 5, y + 15, boldPaint);
        y += 25f;

        canvas.drawLine(col1, y, endX, y, linePaint);

        normalPaint.setTextSize(10f);
        for (TransactionData tx : transactions) {
            y += 15f;
            canvas.drawText(tx.appAccIfsc, col1 + 5, y, normalPaint);
            canvas.drawText(tx.fraudAccIfsc, col2 + 5, y, normalPaint);
            canvas.drawText(tx.utr, col3 + 5, y, normalPaint);
            canvas.drawText(tx.date, col4 + 5, y, normalPaint);
            canvas.drawText(tx.amount, col5 + 5, y, normalPaint);
            y += 5f;
            canvas.drawLine(col1, y, endX, y, linePaint);
        }
    }

    private static TextPaint createTextPaint(float textSize, int color, boolean bold) {
        TextPaint paint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(textSize);
        paint.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        return paint;
    }

    private static String safeText(String value) {
        return value == null ? "" : value;
    }
}
