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
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.io.File;
import java.io.FileOutputStream;

public final class Section94PdfHelper {

    private static final int A4_WIDTH = 595;
    private static final int A4_HEIGHT = 842;

    private static final float LEFT_MARGIN = 54f;
    private static final float RIGHT_MARGIN = 54f;
    private static final int CONTENT_WIDTH =
            (int) (A4_WIDTH - LEFT_MARGIN - RIGHT_MARGIN);

    private Section94PdfHelper() {
    }

    public static void printSection94(
            Activity activity,
            String jobName,
            String noticeReference,
            String recipient,
            String subject,
            String body,
            String requirements,
            String instruction,
            String signature
    ) {
        PrintManager printManager =
                (PrintManager) activity.getSystemService(Context.PRINT_SERVICE);

        if (printManager == null) {
            return;
        }

        NoticeData noticeData = new NoticeData(
                noticeReference,
                recipient,
                subject,
                body,
                requirements,
                instruction,
                signature
        );

        printManager.print(
                jobName,
                new Section94PrintAdapter(activity, noticeData, jobName),
                null
        );
    }

    // नया फंक्शन: बैकग्राउंड में (बिना डायलॉग खोले) PDF बनाने के लिए
    public static boolean generateSilentPdf(
            Context context,
            File outputFile,
            String noticeReference,
            String recipient,
            String subject,
            String body,
            String requirements,
            String instruction,
            String signature
    ) {
        NoticeData noticeData = new NoticeData(
                noticeReference,
                recipient,
                subject,
                body,
                requirements,
                instruction,
                signature
        );

        PdfDocument pdfDocument = new PdfDocument();
        FileOutputStream outputStream = null;

        try {
            PdfDocument.PageInfo pageInfo =
                    new PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            boolean isHindi = "hi".equals(LanguageManager.getLanguage(context));

            drawSection94Notice(
                    page.getCanvas(),
                    context.getResources(),
                    noticeData,
                    isHindi
            );

            pdfDocument.finishPage(page);

            outputStream = new FileOutputStream(outputFile);
            pdfDocument.writeTo(outputStream);
            outputStream.flush();

            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (outputStream != null) outputStream.close();
            } catch (Exception ignored) {
            }
            pdfDocument.close();
        }
    }

    private static class NoticeData {

        private final String noticeReference;
        private final String recipient;
        private final String subject;
        private final String body;
        private final String requirements;
        private final String instruction;
        private final String signature;

        NoticeData(
                String noticeReference,
                String recipient,
                String subject,
                String body,
                String requirements,
                String instruction,
                String signature
        ) {
            this.noticeReference = safeText(noticeReference);
            this.recipient = safeText(recipient);
            this.subject = safeText(subject);
            this.body = safeText(body);
            this.requirements = safeText(requirements);
            this.instruction = safeText(instruction);
            this.signature = normaliseSignature(signature);
        }
    }

    private static class Section94PrintAdapter extends PrintDocumentAdapter {

        private final Activity activity;
        private final NoticeData noticeData;
        private final String jobName;

        Section94PrintAdapter(
                Activity activity,
                NoticeData noticeData,
                String jobName
        ) {
            this.activity = activity;
            this.noticeData = noticeData;
            this.jobName = jobName;
        }

        @Override
        public void onLayout(
                android.print.PrintAttributes oldAttributes,
                android.print.PrintAttributes newAttributes,
                CancellationSignal cancellationSignal,
                LayoutResultCallback callback,
                Bundle extras
        ) {
            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }

            PrintDocumentInfo documentInfo =
                    new PrintDocumentInfo.Builder(jobName)
                            .setContentType(
                                    PrintDocumentInfo.CONTENT_TYPE_DOCUMENT
                            )
                            .setPageCount(1)
                            .build();

            callback.onLayoutFinished(documentInfo, true);
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

            if (!isFirstPageRequested(pages)) {
                callback.onWriteFinished(new PageRange[]{});
                return;
            }

            PdfDocument pdfDocument = new PdfDocument();
            FileOutputStream outputStream = null;

            try {
                PdfDocument.PageInfo pageInfo =
                        new PdfDocument.PageInfo.Builder(
                                A4_WIDTH,
                                A4_HEIGHT,
                                1
                        ).create();

                PdfDocument.Page page = pdfDocument.startPage(pageInfo);

                boolean isHindi = "hi".equals(
                        LanguageManager.getLanguage(activity)
                );

                drawSection94Notice(
                        page.getCanvas(),
                        activity.getResources(),
                        noticeData,
                        isHindi
                );

                pdfDocument.finishPage(page);

                outputStream = new FileOutputStream(
                        destination.getFileDescriptor()
                );

                pdfDocument.writeTo(outputStream);
                outputStream.flush();

                callback.onWriteFinished(
                        new PageRange[]{PageRange.ALL_PAGES}
                );

            } catch (Exception exception) {
                callback.onWriteFailed(
                        "Unable to create Section 94 PDF."
                );
            } finally {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (Exception ignored) {
                }

                pdfDocument.close();
            }
        }
    }

    private static boolean isFirstPageRequested(PageRange[] pages) {
        if (pages == null || pages.length == 0) {
            return true;
        }

        for (PageRange pageRange : pages) {
            if (pageRange != null
                    && pageRange.getStart() <= 0
                    && pageRange.getEnd() >= 0) {
                return true;
            }
        }

        return false;
    }

    private static void drawSection94Notice(
            Canvas canvas,
            Resources resources,
            NoticeData data,
            boolean isHindi
    ) {
        canvas.drawColor(Color.WHITE);

        Bitmap logo = BitmapFactory.decodeResource(
                resources,
                R.drawable.up_police_logo
        );

        if (logo != null) {
            int logoSize = 54;
            int logoLeft = (A4_WIDTH - logoSize) / 2;

            canvas.drawBitmap(
                    logo,
                    null,
                    new android.graphics.Rect(
                            logoLeft,
                            24,
                            logoLeft + logoSize,
                            24 + logoSize
                    ),
                    null
            );
        }

        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.rgb(6, 46, 110));
        titlePaint.setTextAlign(Paint.Align.CENTER);

        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextSize(14f);

        canvas.drawText(
                isHindi ? "उत्तर प्रदेश पुलिस" : "UTTAR PRADESH POLICE",
                A4_WIDTH / 2f,
                92f,
                titlePaint
        );

        titlePaint.setTextSize(10.5f);

        canvas.drawText(
                isHindi
                        ? "साइबर क्राइम पुलिस स्टेशन, चन्दौली"
                        : "Cyber Crime Police Station, Chandauli",
                A4_WIDTH / 2f,
                108f,
                titlePaint
        );

        titlePaint.setTypeface(Typeface.DEFAULT);
        titlePaint.setTextSize(9.5f);

        canvas.drawText(
                isHindi
                        ? "जनपद: चन्दौली, उत्तर प्रदेश"
                        : "District: Chandauli, Uttar Pradesh",
                A4_WIDTH / 2f,
                122f,
                titlePaint
        );

        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextSize(13f);

        canvas.drawText(
                isHindi
                        ? "धारा 94 बीएनएसएस, 2023 के अंतर्गत नोटिस"
                        : "NOTICE U/S 94 BNSS, 2023",
                A4_WIDTH / 2f,
                143f,
                titlePaint
        );

        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.rgb(160, 181, 208));
        linePaint.setStrokeWidth(1f);

        canvas.drawLine(
                LEFT_MARGIN,
                153f,
                A4_WIDTH - RIGHT_MARGIN,
                153f,
                linePaint
        );

        TextPaint referencePaint = createTextPaint(
                11.5f,
                Color.rgb(35, 42, 50),
                true
        );

        StaticLayout referenceLayout = createLayout(
                data.noticeReference,
                referencePaint,
                CONTENT_WIDTH,
                Layout.Alignment.ALIGN_OPPOSITE
        );

        drawLayout(
                canvas,
                referenceLayout,
                LEFT_MARGIN,
                160f
        );

        float contentStartY = 160f
                + referenceLayout.getHeight()
                + 13f;

        TextPaint signaturePaint = createTextPaint(
                11.5f,
                Color.rgb(35, 42, 50),
                true
        );

        String signatureText = data.signature;

        if (signatureText.isEmpty()) {
            signatureText = isHindi
                    ? "जांच अधिकारी\nसाइबर क्राइम पुलिस स्टेशन, चन्दौली"
                    : "Investigating Officer\nCyber Crime Police Station, Chandauli";
        }

        int signatureWidth = 235;

        StaticLayout signatureLayout = createLayout(
                signatureText,
                signaturePaint,
                signatureWidth,
                Layout.Alignment.ALIGN_OPPOSITE
        );

        float footerY = A4_HEIGHT - 28f;

        float maxSignatureTop = footerY
                - signatureLayout.getHeight()
                - 19f;

        float minimumGap = 16f;
        float preferredGap = 30f;

        float availableContentHeight = maxSignatureTop
                - contentStartY
                - minimumGap;

        float fontSize = 12.5f;
        PageLayouts pageLayouts;

        do {
            pageLayouts = createPageLayouts(
                    data,
                    isHindi,
                    fontSize
            );

            if (pageLayouts.totalHeight <= availableContentHeight) {
                break;
            }

            fontSize -= 0.4f;

        } while (fontSize >= 9.5f);

        float verticalScale = 1f;

        if (pageLayouts.totalHeight > availableContentHeight) {
            verticalScale = availableContentHeight
                    / pageLayouts.totalHeight;
        }

        float renderedContentHeight =
                pageLayouts.totalHeight * verticalScale;

        float signatureTop = contentStartY
                + renderedContentHeight
                + preferredGap;

        if (signatureTop > maxSignatureTop) {
            signatureTop = maxSignatureTop;
        }

        canvas.save();

        canvas.clipRect(
                LEFT_MARGIN,
                contentStartY,
                A4_WIDTH - RIGHT_MARGIN,
                signatureTop - minimumGap
        );

        canvas.translate(LEFT_MARGIN, contentStartY);
        canvas.scale(1f, verticalScale);

        drawPageLayouts(canvas, pageLayouts);

        canvas.restore();

        float signatureLeft = A4_WIDTH
                - RIGHT_MARGIN
                - signatureWidth;

        drawLayout(
                canvas,
                signatureLayout,
                signatureLeft,
                signatureTop
        );

        Paint draftPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        draftPaint.setColor(Color.rgb(122, 86, 0));
        draftPaint.setTextSize(8.5f);
        draftPaint.setTypeface(Typeface.DEFAULT_BOLD);

        String draftText = isHindi
                ? "प्रारूप — जारी करने से पूर्व सक्षम अनुमोदन आवश्यक है।"
                : "DRAFT — Requires authorised approval before issue.";

        canvas.drawText(
                draftText,
                LEFT_MARGIN,
                footerY,
                draftPaint
        );
    }

    private static PageLayouts createPageLayouts(
            NoticeData data,
            boolean isHindi,
            float fontSize
    ) {
        TextPaint normalPaint = createTextPaint(
                fontSize,
                Color.rgb(35, 42, 50),
                false
        );

        TextPaint subjectPaint = createTextPaint(
                fontSize,
                Color.rgb(6, 46, 110),
                true
        );

        TextPaint headingPaint = createTextPaint(
                fontSize + 0.6f,
                Color.rgb(6, 46, 110),
                true
        );

        StaticLayout recipientLayout = createLayout(
                data.recipient,
                normalPaint,
                CONTENT_WIDTH,
                Layout.Alignment.ALIGN_NORMAL
        );

        StaticLayout subjectLayout = createLayout(
                data.subject,
                subjectPaint,
                CONTENT_WIDTH,
                Layout.Alignment.ALIGN_NORMAL
        );

        StaticLayout bodyLayout = createLayout(
                data.body,
                normalPaint,
                CONTENT_WIDTH,
                Layout.Alignment.ALIGN_NORMAL
        );

        String requirementHeading = isHindi
                ? "मांगी गई सूचना / दस्तावेज"
                : "Information / Documents Requested";

        StaticLayout requirementHeadingLayout = createLayout(
                requirementHeading,
                headingPaint,
                CONTENT_WIDTH,
                Layout.Alignment.ALIGN_NORMAL
        );

        StaticLayout requirementsLayout = createLayout(
                data.requirements,
                normalPaint,
                CONTENT_WIDTH,
                Layout.Alignment.ALIGN_NORMAL
        );

        StaticLayout instructionLayout = createLayout(
                data.instruction,
                normalPaint,
                CONTENT_WIDTH,
                Layout.Alignment.ALIGN_NORMAL
        );

        return new PageLayouts(
                recipientLayout,
                subjectLayout,
                bodyLayout,
                requirementHeadingLayout,
                requirementsLayout,
                instructionLayout
        );
    }

    private static class PageLayouts {

        private final StaticLayout recipientLayout;
        private final StaticLayout subjectLayout;
        private final StaticLayout bodyLayout;
        private final StaticLayout requirementHeadingLayout;
        private final StaticLayout requirementsLayout;
        private final StaticLayout instructionLayout;
        private final float totalHeight;

        PageLayouts(
                StaticLayout recipientLayout,
                StaticLayout subjectLayout,
                StaticLayout bodyLayout,
                StaticLayout requirementHeadingLayout,
                StaticLayout requirementsLayout,
                StaticLayout instructionLayout
        ) {
            this.recipientLayout = recipientLayout;
            this.subjectLayout = subjectLayout;
            this.bodyLayout = bodyLayout;
            this.requirementHeadingLayout = requirementHeadingLayout;
            this.requirementsLayout = requirementsLayout;
            this.instructionLayout = instructionLayout;

            totalHeight = recipientLayout.getHeight()
                    + 12f
                    + subjectLayout.getHeight()
                    + 14f
                    + bodyLayout.getHeight()
                    + 17f
                    + requirementHeadingLayout.getHeight()
                    + 8f
                    + requirementsLayout.getHeight()
                    + 16f
                    + instructionLayout.getHeight();
        }
    }

    private static void drawPageLayouts(
            Canvas canvas,
            PageLayouts layouts
    ) {
        float y = 0f;

        drawLayout(canvas, layouts.recipientLayout, 0f, y);
        y += layouts.recipientLayout.getHeight() + 12f;

        drawLayout(canvas, layouts.subjectLayout, 0f, y);
        y += layouts.subjectLayout.getHeight() + 14f;

        drawLayout(canvas, layouts.bodyLayout, 0f, y);
        y += layouts.bodyLayout.getHeight() + 17f;

        drawLayout(canvas, layouts.requirementHeadingLayout, 0f, y);
        y += layouts.requirementHeadingLayout.getHeight() + 8f;

        drawLayout(canvas, layouts.requirementsLayout, 0f, y);
        y += layouts.requirementsLayout.getHeight() + 16f;

        drawLayout(canvas, layouts.instructionLayout, 0f, y);
    }

    private static TextPaint createTextPaint(
            float textSize,
            int color,
            boolean bold
    ) {
        TextPaint paint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(textSize);
        paint.setTypeface(
                bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT
        );

        return paint;
    }

    private static StaticLayout createLayout(
            String text,
            TextPaint paint,
            int width,
            Layout.Alignment alignment
    ) {
        String safeValue = safeText(text);

        return StaticLayout.Builder.obtain(
                        safeValue,
                        0,
                        safeValue.length(),
                        paint,
                        width
                )
                .setAlignment(alignment)
                .setIncludePad(true)
                .setLineSpacing(1f, 1.03f)
                .build();
    }

    private static void drawLayout(
            Canvas canvas,
            StaticLayout layout,
            float left,
            float top
    ) {
        canvas.save();
        canvas.translate(left, top);
        layout.draw(canvas);
        canvas.restore();
    }

    private static String normaliseSignature(String value) {
        return safeText(value)
                .replace("Investing Officer", "Investigating Officer");
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}