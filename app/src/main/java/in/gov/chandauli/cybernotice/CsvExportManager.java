package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CsvExportManager {

    public static void exportAndShareReport(Context context) {
        List<NoticeRecord> notices = NoticeStore.getAllNotices(context);

        if (notices.isEmpty()) {
            Toast.makeText(context, "एक्सपोर्ट करने के लिए कोई नोटिस उपलब्ध नहीं है।", Toast.LENGTH_SHORT).show();
            return;
        }

        File cacheDir = context.getExternalCacheDir();
        if (cacheDir != null && !cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        String dateStr = new SimpleDateFormat("dd_MMM_yyyy_HHmm", Locale.getDefault()).format(new Date());
        File csvFile = new File(cacheDir, "Audit_Report_" + dateStr + ".csv");

        try {
            FileWriter writer = new FileWriter(csvFile);

            // Excel में हिंदी/स्पेशल कैरेक्टर सही से दिखने के लिए BOM (Byte Order Mark) जोड़ना
            writer.append('\ufeff');

            // हेडर (Columns)
            writer.append("Date,Notice Number,Type,Primary Info,Secondary Info,Status\n");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            // सारा डेटा CSV फॉर्मेट में लिखना
            for (NoticeRecord record : notices) {
                writer.append(escapeCsv(sdf.format(new Date(record.getCreatedAt())))).append(",");
                writer.append(escapeCsv(record.getNoticeNumber())).append(",");
                writer.append(escapeCsv(getReadableType(record.getNoticeType()))).append(",");
                writer.append(escapeCsv(record.getPrimaryValue())).append(",");
                writer.append(escapeCsv(record.getSecondaryValue())).append(",");
                writer.append(escapeCsv(record.getStatus())).append("\n");
            }

            writer.flush();
            writer.close();

            shareCsv(context, csvFile);

        } catch (Exception e) {
            Toast.makeText(context, "रिपोर्ट बनाने में त्रुटि: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static String getReadableType(String type) {
        if ("SECTION_94".equals(type)) return "Sec 94 BNSS";
        if ("SECTION_35".equals(type)) return "Sec 35(3) BNSS";
        if ("CDR".equals(type)) return "CDR Proforma";
        if ("COURT_RELEASE".equals(type)) return "Court Release Order";
        return type != null ? type : "Unknown";
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private static void shareCsv(Context context, File csvFile) {
        try {
            Uri fileUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    csvFile
            );

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Cyber Crime Notice - Audit Report");
            intent.putExtra(Intent.EXTRA_TEXT, "महोदय,\n\nकृपया साइबर क्राइम नोटिस सिस्टम की संलग्न ऑडिट रिपोर्ट (CSV) प्राप्त करें। इसे Microsoft Excel या Google Sheets में खोला जा सकता है।");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(intent, "रिपोर्ट शेयर करें..."));
        } catch (Exception e) {
            Toast.makeText(context, "शेयर करने में त्रुटि। FileProvider चेक करें।", Toast.LENGTH_SHORT).show();
        }
    }
}
