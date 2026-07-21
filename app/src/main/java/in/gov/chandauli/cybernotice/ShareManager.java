package in.gov.chandauli.cybernotice;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;

public class ShareManager {

    public static void sharePdf(Context context, File pdfFile, String noticeNumber) {
        if (pdfFile == null || !pdfFile.exists()) {
            Toast.makeText(context, "PDF फ़ाइल नहीं मिली! पहले PDF बनाएँ।", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // सुरक्षित URI जनरेट करना
            Uri pdfUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    pdfFile
            );

            // शेयर इंटेंट बनाना
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, pdfUri);

            // ईमेल के लिए Subject
            intent.putExtra(Intent.EXTRA_SUBJECT, "Cyber Crime Notice: " + noticeNumber);

            // WhatsApp / Email की बॉडी में अपने-आप लिखा आने वाला मैसेज
            String emailBody = "महोदय/महोदया,\n\n" +
                    "कृपया संलग्न साइबर क्राइम नोटिस (" + noticeNumber + ") प्राप्त करें। " +
                    "यह नोटिस साइबर क्राइम पुलिस स्टेशन, चन्दौली द्वारा जारी किया गया है।\n\n" +
                    "धन्यवाद,\n" +
                    "साइबर क्राइम पुलिस स्टेशन\nचन्दौली, उत्तर प्रदेश";
            intent.putExtra(Intent.EXTRA_TEXT, emailBody);

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // शेयर मेनू खोलना
            context.startActivity(Intent.createChooser(intent, "नोटिस शेयर करें..."));

        } catch (Exception e) {
            Toast.makeText(context, "शेयर करने में त्रुटि: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}