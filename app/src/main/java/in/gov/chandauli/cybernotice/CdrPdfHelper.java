package in.gov.chandauli.cybernotice;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CdrPdfHelper {

    private static final int POLICE_BLUE = Color.rgb(6, 46, 110);
    private static final int DARK_TEXT = Color.rgb(20, 20, 20);
    private static final int LIGHT_LINE = Color.rgb(215, 224, 235);

    private CdrPdfHelper() {
    }

    public static void printCdr(
            Activity activity,
            String jobName,
            String referenceAndDate,
            String recipient,
            String subject,
            String body,
            String officeSummary,
            String caseSummary,
            String identifierSummary,
            String requestedInformation,
            String period,
            String justification,
            String signature
    ) {
        if (activity == null) {
            return;
        }

        PrintManager printManager = (PrintManager) activity.getSystemService(
                Context.PRINT_SERVICE
        );

        if (printManager == null) {
            return;
        }

        String safeJobName = isEmpty(jobName)
                ? "CDR_Request"
                : jobName.replaceAll("[^a-zA-Z0-9_\\- ]", "_");

        printManager.print(
                safeJobName,
                new CdrPrintAdapter(
                        activity,
                        referenceAndDate,
                        recipient,
                        subject,
                        body,
                        officeSummary,
                        caseSummary,
                        identifierSummary,
                        requestedInformation,
                        period,
                        justification,
                        signature
                ),
                new PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
        );
    }

    // नया फंक्शन: डायरेक्ट शेयर करने के लिए बैकग्राउंड में PDF बनाना
    public static boolean generateSilentPdf(
            Context context,
            File outputFile,
            String referenceAndDate,
            String recipient,
            String subject,
            String body,
            String officeSummary,
            String caseSummary,
            String identifierSummary,
            String requestedInformation,
            String period,
            String justification,
            String signature
    ) {
        PrintedPdfDocument document = null;
        FileOutputStream outputStream = null;

        try {
            PrintAttributes printAttributes = new PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build();

            document = new PrintedPdfDocument(context, printAttributes);
            PrintedPdfDocument.Page page = document.startPage(0);

            drawCdrPage(
                    page.getCanvas(),
                    page.getInfo().getContentRect(),
                    context,
                    referenceAndDate,
                    recipient,
                    subject,
                    body,
                    officeSummary,
                    caseSummary,
                    identifierSummary,
                    requestedInformation,
                    period,
                    justification,
                    signature
            );

            document.finishPage(page);

            outputStream = new FileOutputStream(outputFile);
            document.writeTo(outputStream);
            outputStream.flush();

            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException ignored) {
            }
            if (document != null) {
                document.close();
            }
        }
    }

    private static class CdrPrintAdapter extends PrintDocumentAdapter {

        private final Activity activity;
        private final String referenceAndDate;
        private final String recipient;
        private final String subject;
        private final String body;
        private final String officeSummary;
        private final String caseSummary;
        private final String identifierSummary;
        private final String requestedInformation;
        private final String period;
        private final String justification;
        private final String signature;

        private PrintAttributes printAttributes;

        CdrPrintAdapter(
                Activity activity,
                String referenceAndDate,
                String recipient,
                String subject,
                String body,
                String officeSummary,
                String caseSummary,
                String identifierSummary,
                String requestedInformation,
                String period,
                String justification,
                String signature
        ) {
            this.activity = activity;
            this.referenceAndDate = safe(referenceAndDate);
            this.recipient = safe(recipient);
            this.subject = safe(subject);
            this.body = safe(body);
            this.officeSummary = safe(officeSummary);
            this.caseSummary = safe(caseSummary);
            this.identifierSummary = safe(identifierSummary);
            this.requestedInformation = safe(requestedInformation);
            this.period = safe(period);
            this.justification = safe(justification);
            this.signature = safe(signature);
        }

        @Override
        public void onLayout(
                PrintAttributes oldAttributes,
                PrintAttributes newAttributes,
                CancellationSignal cancellationSignal,
                LayoutResultCallback callback,
                Bundle extras
        ) {
            printAttributes = newAttributes;

            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }

            PrintDocumentInfo info = new PrintDocumentInfo.Builder(
                    "CDR_Request.pdf"
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
            if (!isPageRequested(pages, 0)) {
                callback.onWriteFinished(new PageRange[0]);
                return;
            }

            if (cancellationSignal.isCanceled()) {
                callback.onWriteCancelled();
                return;
            }

            PrintedPdfDocument document = null;
            FileOutputStream outputStream = null;

            try {
                document = new PrintedPdfDocument(activity, printAttributes);

                PrintedPdfDocument.Page page = document.startPage(0);

                drawCdrPage(
                        page.getCanvas(),
                        page.getInfo().getContentRect(),
                        activity,
                        referenceAndDate,
                        recipient,
                        subject,
                        body,
                        officeSummary,
                        caseSummary,
                        identifierSummary,
                        requestedInformation,
                        period,
                        justification,
                        signature
                );

                document.finishPage(page);

                outputStream = new FileOutputStream(
                        destination.getFileDescriptor()
                );

                document.writeTo(outputStream);

                callback.onWriteFinished(
                        new PageRange[]{new PageRange(0, 0)}
                );

            } catch (Exception exception) {
                callback.onWriteFailed(exception.getMessage());

            } finally {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException ignored) {
                }

                if (document != null) {
                    document.close();
                }
            }
        }
    }

    private static void drawCdrPage(
            Canvas canvas,
            android.graphics.Rect contentRect,
            Context context,
            String referenceAndDate,
            String recipient,
            String subject,
            String body,
            String officeSummary,
            String caseSummary,
            String identifierSummary,
            String requestedInformation,
            String period,
            String justification,
            String signature
    ) {
        canvas.drawColor(Color.WHITE);

        boolean isHindi = "hi".equals(LanguageManager.getLanguage(context));

        float pageLeft = contentRect.left;
        float pageTop = contentRect.top;
        float pageRight = contentRect.right;
        float pageBottom = contentRect.bottom;

        float horizontalMargin = Math.max(28f, (pageRight - pageLeft) * 0.045f);

        float left = pageLeft + horizontalMargin;
        float right = pageRight - horizontalMargin;
        float width = right - left;

        float headerTop = pageTop + 18f;
        float contentStartY = headerTop + 128f;
        float footerY = pageBottom - 18f;
        float availableContentHeight = footerY - contentStartY - 8f;

        drawHeader(
                canvas,
                context,
                left,
                right,
                headerTop,
                isHindi,
                referenceAndDate
        );

        PageContent pageContent = null;

        for (float fontSize = 10.8f; fontSize >= 8.2f; fontSize -= 0.3f) {
            pageContent = createPageContent(
                    (int) width,
                    fontSize,
                    isHindi,
                    recipient,
                    subject,
                    body,
                    officeSummary,
                    caseSummary,
                    identifierSummary,
                    requestedInformation,
                    period,
                    justification,
                    signature
            );

            float requiredHeight = pageContent.bodyHeight
                    + 22f
                    + pageContent.signatureLayout.getHeight();

            if (requiredHeight <= availableContentHeight) {
                break;
            }
        }

        if (pageContent == null) {
            return;
        }

        float requiredHeight = pageContent.bodyHeight
                + 22f
                + pageContent.signatureLayout.getHeight();

        float verticalScale = 1f;

        if (requiredHeight > availableContentHeight) {
            verticalScale = availableContentHeight / requiredHeight;
        }

        canvas.save();
        canvas.translate(left, contentStartY);
        canvas.scale(1f, verticalScale);

        float currentY = 0f;

        for (RenderedBlock block : pageContent.blocks) {
            drawLayout(canvas, block.layout, 0f, currentY);
            currentY += block.layout.getHeight() + block.gapAfter;
        }

        float signatureWidth = Math.min(width * 0.52f, 260f);
        float signatureX = width - signatureWidth;
        float signatureY = pageContent.bodyHeight + 22f;

        drawLayout(
                canvas,
                pageContent.signatureLayout,
                signatureX,
                signatureY
        );

        canvas.restore();

        TextPaint footerPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        footerPaint.setColor(Color.rgb(145, 106, 0));
        footerPaint.setTextSize(7.2f);
        footerPaint.setTypeface(
                android.graphics.Typeface.create(
                        android.graphics.Typeface.DEFAULT,
                        android.graphics.Typeface.BOLD
                )
        );

        String footer = isHindi
                ? "ड्राफ्ट — जारी करने से पूर्व सक्षम अधिकारी का अनुमोदन आवश्यक है।"
                : "DRAFT — Requires authorised approval before issue.";

        canvas.drawText(footer, left, footerY, footerPaint);
    }

    private static PageContent createPageContent(
            int width,
            float normalSize,
            boolean isHindi,
            String recipient,
            String subject,
            String body,
            String officeSummary,
            String caseSummary,
            String identifierSummary,
            String requestedInformation,
            String period,
            String justification,
            String signature
    ) {
        PageContent content = new PageContent();

        TextPaint normalPaint = createPaint(
                normalSize,
                DARK_TEXT,
                android.graphics.Typeface.NORMAL
        );

        TextPaint subjectPaint = createPaint(
                normalSize + 0.15f,
                DARK_TEXT,
                android.graphics.Typeface.BOLD
        );

        TextPaint sectionPaint = createPaint(
                normalSize + 0.55f,
                POLICE_BLUE,
                android.graphics.Typeface.BOLD
        );

        TextPaint signaturePaint = createPaint(
                normalSize,
                DARK_TEXT,
                android.graphics.Typeface.NORMAL
        );

        addBlock(content, recipient, normalPaint, width, 8f);
        addBlock(content, subject, subjectPaint, width, 10f);
        addBlock(content, body, normalPaint, width, 10f);

        addSection(
                content,
                isHindi ? "अनुरोधकर्ता कार्यालय" : "Requesting Office",
                officeSummary,
                sectionPaint,
                normalPaint,
                width
        );

        addSection(
                content,
                isHindi ? "प्रकरण विवरण" : "Case Particulars",
                caseSummary,
                sectionPaint,
                normalPaint,
                width
        );

        addSection(
                content,
                isHindi ? "लक्षित पहचान विवरण" : "Target Identifier",
                identifierSummary,
                sectionPaint,
                normalPaint,
                width
        );

        addSection(
                content,
                isHindi ? "मांगी गई सूचना" : "Information Requested",
                requestedInformation,
                sectionPaint,
                normalPaint,
                width
        );

        addSection(
                content,
                isHindi ? "मांगी गई अवधि" : "Period Requested",
                period,
                sectionPaint,
                normalPaint,
                width
        );

        addSection(
                content,
                isHindi ? "औचित्य" : "Justification",
                justification,
                sectionPaint,
                normalPaint,
                width
        );

        float signatureWidth = Math.min(width * 0.52f, 260f);

        content.signatureLayout = createLayout(
                signature,
                signaturePaint,
                (int) signatureWidth,
                Layout.Alignment.ALIGN_OPPOSITE
        );

        return content;
    }

    private static void addSection(
            PageContent content,
            String heading,
            String value,
            TextPaint sectionPaint,
            TextPaint normalPaint,
            int width
    ) {
        addBlock(content, heading, sectionPaint, width, 2f);
        addBlock(content, value, normalPaint, width, 7f);
    }

    private static void addBlock(
            PageContent content,
            String text,
            TextPaint paint,
            int width,
            float gapAfter
    ) {
        StaticLayout layout = createLayout(
                safe(text),
                paint,
                width,
                Layout.Alignment.ALIGN_NORMAL
        );

        content.blocks.add(new RenderedBlock(layout, gapAfter));
        content.bodyHeight += layout.getHeight() + gapAfter;
    }

    private static void drawHeader(
            Canvas canvas,
            Context context,
            float left,
            float right,
            float top,
            boolean isHindi,
            String referenceAndDate
    ) {
        float centerX = (left + right) / 2f;
        float width = right - left;

        Bitmap logo = BitmapFactory.decodeResource(
                context.getResources(),
                R.drawable.up_police_logo
        );

        if (logo != null) {
            float logoSize = Math.min(58f, width * 0.13f);

            RectF logoRect = new RectF(
                    centerX - (logoSize / 2f),
                    top,
                    centerX + (logoSize / 2f),
                    top + logoSize
            );

            Paint logoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            logoPaint.setFilterBitmap(true);
            canvas.drawBitmap(logo, null, logoRect, logoPaint);
        }

        TextPaint policePaint = createPaint(
                11.5f,
                POLICE_BLUE,
                android.graphics.Typeface.BOLD
        );

        TextPaint stationPaint = createPaint(
                8.6f,
                POLICE_BLUE,
                android.graphics.Typeface.BOLD
        );

        TextPaint titlePaint = createPaint(
                10.4f,
                POLICE_BLUE,
                android.graphics.Typeface.BOLD
        );

        String policeTitle = isHindi
                ? "उत्तर प्रदेश पुलिस"
                : "UTTAR PRADESH POLICE";

        OfficerProfileManager.OfficerProfile profile =
                OfficerProfileManager.getProfile(context);
        boolean hasSelectedLocation = !profile.getPoliceStation().isEmpty()
                && !profile.getDistrict().isEmpty();

        String fallbackStationName = isHindi
                ? "साइबर क्राइम पुलिस स्टेशन, चंदौली"
                : "Cyber Crime Police Station, Chandauli";

        String fallbackDistrictName = isHindi
                ? "जनपद: चंदौली, उत्तर प्रदेश"
                : "District: Chandauli, Uttar Pradesh";

        String stationName = hasSelectedLocation
                ? profile.getPoliceStation()
                : fallbackStationName;

        String districtName = hasSelectedLocation
                ? (isHindi ? "जनपद: " : "District: ")
                        + profile.getDistrict()
                        + (isHindi ? ", उत्तर प्रदेश" : ", Uttar Pradesh")
                : fallbackDistrictName;

        String documentTitle = isHindi
                ? "कॉल डिटेल रिकॉर्ड (सीडीआर) अनुरोध"
                : "CALL DETAIL RECORD (CDR) REQUEST";

        float logoBottom = top + 58f;

        drawCenteredText(
                canvas,
                policeTitle,
                centerX,
                logoBottom + 11f,
                policePaint
        );

        drawCenteredText(
                canvas,
                stationName,
                centerX,
                logoBottom + 22f,
                stationPaint
        );

        drawCenteredText(
                canvas,
                districtName,
                centerX,
                logoBottom + 32f,
                stationPaint
        );

        drawCenteredText(
                canvas,
                documentTitle,
                centerX,
                logoBottom + 46f,
                titlePaint
        );

        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(LIGHT_LINE);
        linePaint.setStrokeWidth(1.2f);

        canvas.drawLine(
                left,
                logoBottom + 53f,
                right,
                logoBottom + 53f,
                linePaint
        );

        TextPaint referencePaint = createPaint(
                7.5f,
                DARK_TEXT,
                android.graphics.Typeface.NORMAL
        );

        float referenceWidth = Math.min(width * 0.42f, 220f);

        StaticLayout referenceLayout = createLayout(
                referenceAndDate,
                referencePaint,
                (int) referenceWidth,
                Layout.Alignment.ALIGN_OPPOSITE
        );

        drawLayout(
                canvas,
                referenceLayout,
                right - referenceWidth,
                logoBottom + 59f
        );
    }

    private static TextPaint createPaint(
            float size,
            int color,
            int style
    ) {
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(size);
        paint.setTypeface(
                android.graphics.Typeface.create(
                        android.graphics.Typeface.DEFAULT,
                        style
                )
        );
        return paint;
    }

    private static StaticLayout createLayout(
            String text,
            TextPaint paint,
            int width,
            Layout.Alignment alignment
    ) {
        String safeText = safe(text);

        return StaticLayout.Builder.obtain(
                        safeText,
                        0,
                        safeText.length(),
                        paint,
                        Math.max(width, 1)
                )
                .setAlignment(alignment)
                .setIncludePad(false)
                .setLineSpacing(0f, 1.05f)
                .build();
    }

    private static void drawLayout(
            Canvas canvas,
            StaticLayout layout,
            float x,
            float y
    ) {
        canvas.save();
        canvas.translate(x, y);
        layout.draw(canvas);
        canvas.restore();
    }

    private static void drawCenteredText(
            Canvas canvas,
            String text,
            float centerX,
            float baselineY,
            TextPaint paint
    ) {
        float textWidth = paint.measureText(text);
        canvas.drawText(
                text,
                centerX - (textWidth / 2f),
                baselineY,
                paint
        );
    }

    private static boolean isPageRequested(
            PageRange[] pageRanges,
            int pageIndex
    ) {
        if (pageRanges == null || pageRanges.length == 0) {
            return false;
        }

        for (PageRange pageRange : pageRanges) {
            if (pageRange.getStart() <= pageIndex
                    && pageRange.getEnd() >= pageIndex) {
                return true;
            }
        }

        return false;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String safe(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }

        return value.trim();
    }

    private static class RenderedBlock {
        final StaticLayout layout;
        final float gapAfter;

        RenderedBlock(StaticLayout layout, float gapAfter) {
            this.layout = layout;
            this.gapAfter = gapAfter;
        }
    }

    private static class PageContent {
        final List<RenderedBlock> blocks = new ArrayList<>();
        float bodyHeight = 0f;
        StaticLayout signatureLayout;
    }
}
