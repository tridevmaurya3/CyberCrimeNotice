package in.gov.chandauli.cybernotice;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PdfPrintHelper {

    private static final int A4_WIDTH = 595;
    private static final int A4_HEIGHT = 842;

    private static final int LEFT_MARGIN = 68;
    private static final int RIGHT_MARGIN = 68;
    private static final int TOP_MARGIN = 38;
    private static final int BOTTOM_MARGIN = 48;

    public static void printView(Activity activity, View noticeView, String jobName) {
        PrintManager printManager =
                (PrintManager) activity.getSystemService(Context.PRINT_SERVICE);

        if (printManager != null) {
            printManager.print(
                    jobName,
                    new NoticePrintAdapter(noticeView, jobName),
                    null
            );
        }
    }

    private static class NoticePrintAdapter extends PrintDocumentAdapter {

        private final View noticeView;
        private final String jobName;

        NoticePrintAdapter(View noticeView, String jobName) {
            this.noticeView = noticeView;
            this.jobName = jobName;
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

            PrintDocumentInfo info = new PrintDocumentInfo.Builder(jobName)
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

            if (!isFirstPageRequested(pages)) {
                callback.onWriteFinished(new PageRange[]{});
                return;
            }

            PdfDocument document = new PdfDocument();

            try {
                PdfDocument.PageInfo pageInfo =
                        new PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create();

                PdfDocument.Page page = document.startPage(pageInfo);

                drawA4Notice(page.getCanvas(), noticeView);

                document.finishPage(page);

                FileOutputStream outputStream =
                        new FileOutputStream(destination.getFileDescriptor());

                document.writeTo(outputStream);
                outputStream.flush();
                outputStream.close();

                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

            } catch (Exception exception) {
                callback.onWriteFailed(exception.getMessage());
            } finally {
                document.close();
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

    private static void drawA4Notice(Canvas canvas, View noticeView) {
        canvas.drawColor(Color.WHITE);

        List<String> allText = new ArrayList<>();
        collectText(noticeView, allText);

        Bitmap logoBitmap = findLogo(noticeView);

        int contentWidth = A4_WIDTH - LEFT_MARGIN - RIGHT_MARGIN;
        float currentY = TOP_MARGIN;

        if (logoBitmap != null) {
            int logoSize = 68;
            int logoLeft = (A4_WIDTH - logoSize) / 2;

            canvas.drawBitmap(
                    logoBitmap,
                    null,
                    new android.graphics.Rect(
                            logoLeft,
                            (int) currentY,
                            logoLeft + logoSize,
                            (int) currentY + logoSize
                    ),
                    null
            );

            currentY += logoSize + 10;
        }

        List<String> headerLines = new ArrayList<>();
        List<String> bodyLines = new ArrayList<>();
        List<String> officerLines = new ArrayList<>();

        int index = 0;

        while (index < allText.size() && isDraftText(allText.get(index))) {
            index++;
        }

        while (index < allText.size() && headerLines.size() < 4) {
            String value = cleanText(allText.get(index));

            if (!value.isEmpty() && !isButtonText(value)) {
                headerLines.add(value);
            }

            index++;
        }

        boolean officerStarted = false;

        while (index < allText.size()) {
            String value = cleanText(allText.get(index));
            index++;

            if (value.isEmpty() || isButtonText(value)) {
                continue;
            }

            if (isOfficerHeading(value)) {
                officerStarted = true;
            }

            if (officerStarted) {
                officerLines.add(value);
            } else {
                bodyLines.add(value);
            }
        }

        String headerText = joinLines(headerLines);
        String bodyText = joinLines(bodyLines);
        String officerText = joinLines(officerLines);

        if (bodyText.isEmpty()) {
            bodyText = "Notice data is not available.";
        }

        TextPaint headerPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        headerPaint.setColor(Color.rgb(0, 51, 128));
        headerPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        float headerSize = 15.5f;
        StaticLayout headerLayout;

        do {
            headerPaint.setTextSize(headerSize);

            headerLayout = createLayout(
                    headerText,
                    headerPaint,
                    contentWidth,
                    Layout.Alignment.ALIGN_CENTER
            );

            headerSize -= 0.5f;

        } while (headerLayout.getHeight() > 88 && headerSize >= 11.0f);

        // Header: page के मध्य में, सही left margin के साथ
        canvas.save();
        canvas.translate(LEFT_MARGIN, currentY);
        headerLayout.draw(canvas);
        canvas.restore();

        currentY += headerLayout.getHeight() + 16;

        StaticLayout officerLayout = null;
        int officerWidth = 235;
        float officerTop = A4_HEIGHT - BOTTOM_MARGIN;

        if (!officerText.isEmpty()) {
            TextPaint officerPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            officerPaint.setColor(Color.BLACK);
            officerPaint.setTextSize(10.0f);
            officerPaint.setTypeface(android.graphics.Typeface.DEFAULT);

            officerLayout = createLayout(
                    officerText,
                    officerPaint,
                    officerWidth,
                    Layout.Alignment.ALIGN_OPPOSITE
            );

            officerTop = A4_HEIGHT - BOTTOM_MARGIN - officerLayout.getHeight();

            if (officerTop < currentY + 100) {
                officerTop = currentY + 100;
            }
        }

        float availableBodyHeight = officerTop - currentY - 16;

        if (availableBodyHeight < 100) {
            availableBodyHeight = 100;
        }

        TextPaint bodyPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        bodyPaint.setColor(Color.BLACK);
        bodyPaint.setTypeface(android.graphics.Typeface.DEFAULT);

        float bodySize = 12.0f;
        StaticLayout bodyLayout;

        do {
            bodyPaint.setTextSize(bodySize);

            bodyLayout = createLayout(
                    bodyText,
                    bodyPaint,
                    contentWidth,
                    Layout.Alignment.ALIGN_NORMAL
            );

            bodySize -= 0.5f;

        } while (bodyLayout.getHeight() > availableBodyHeight && bodySize >= 7.5f);

        // Notice body: निश्चित बायाँ और दायाँ margin
        canvas.save();

        canvas.clipRect(
                LEFT_MARGIN,
                currentY,
                A4_WIDTH - RIGHT_MARGIN,
                officerTop - 8
        );

        canvas.translate(LEFT_MARGIN, currentY);

        float verticalScale = 1.0f;

        if (bodyLayout.getHeight() > availableBodyHeight) {
            verticalScale = availableBodyHeight / bodyLayout.getHeight();
        }

        canvas.scale(1.0f, verticalScale);
        bodyLayout.draw(canvas);
        canvas.restore();

        // Investigation Officer: नीचे दाईं तरफ
        if (officerLayout != null) {
            float officerLeft = A4_WIDTH - RIGHT_MARGIN - officerWidth;

            canvas.save();
            canvas.translate(officerLeft, officerTop);
            officerLayout.draw(canvas);
            canvas.restore();
        }
    }

    private static StaticLayout createLayout(
            String text,
            TextPaint paint,
            int width,
            Layout.Alignment alignment
    ) {
        return StaticLayout.Builder.obtain(
                        text,
                        0,
                        text.length(),
                        paint,
                        width
                )
                .setAlignment(alignment)
                .setIncludePad(true)
                .setLineSpacing(0, 1.03f)
                .build();
    }

    private static void collectText(View view, List<String> textList) {
        if (view instanceof TextView) {
            CharSequence text = ((TextView) view).getText();

            if (text != null) {
                String value = text.toString().trim();

                if (!value.isEmpty()) {
                    textList.add(value);
                }
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {
                collectText(group.getChildAt(i), textList);
            }
        }
    }

    private static Bitmap findLogo(View view) {
        if (view instanceof ImageView) {
            Drawable drawable = ((ImageView) view).getDrawable();

            if (drawable != null) {
                if (drawable instanceof BitmapDrawable) {
                    return ((BitmapDrawable) drawable).getBitmap();
                }

                int width = drawable.getIntrinsicWidth() > 0
                        ? drawable.getIntrinsicWidth() : 100;

                int height = drawable.getIntrinsicHeight() > 0
                        ? drawable.getIntrinsicHeight() : 100;

                Bitmap bitmap = Bitmap.createBitmap(
                        width,
                        height,
                        Bitmap.Config.ARGB_8888
                );

                Canvas bitmapCanvas = new Canvas(bitmap);

                drawable.setBounds(0, 0, bitmapCanvas.getWidth(), bitmapCanvas.getHeight());
                drawable.draw(bitmapCanvas);

                return bitmap;
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {
                Bitmap bitmap = findLogo(group.getChildAt(i));

                if (bitmap != null) {
                    return bitmap;
                }
            }
        }

        return null;
    }

    private static String joinLines(List<String> lines) {
        StringBuilder builder = new StringBuilder();

        for (String line : lines) {
            String cleanLine = cleanText(line);

            if (!cleanLine.isEmpty()) {
                if (builder.length() > 0) {
                    builder.append("\n");
                }

                builder.append(cleanLine);
            }
        }

        return builder.toString();
    }

    private static String cleanText(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\r", "")
                .replace("\n\n", "\n")
                .trim();
    }

    private static boolean isDraftText(String value) {
        String text = value.toLowerCase(Locale.ROOT);

        return text.contains("draft for review")
                || text.contains("draft")
                || text.contains("समीक्षा हेतु प्रारूप")
                || text.contains("प्रारूप");
    }

    private static boolean isButtonText(String value) {
        String text = value.toLowerCase(Locale.ROOT);

        return text.contains("print")
                || text.contains("save as pdf")
                || text.contains("edit form")
                || text.contains("save to register")
                || text.contains("प्रिंट")
                || text.contains("पीडीएफ")
                || text.contains("फॉर्म संपादित")
                || text.contains("रजिस्टर में");
    }

    private static boolean isOfficerHeading(String value) {
        String text = value.toLowerCase(Locale.ROOT);

        return text.contains("investigating officer")
                || text.contains("investigation officer")
                || text.contains("investing officer")
                || text.contains("enquiry officer")
                || text.contains("जांच अधिकारी")
                || text.contains("अन्वेषण अधिकारी");
    }
}