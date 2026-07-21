package in.gov.chandauli.cybernotice;

import android.app.Activity;
import android.content.Context;

import org.json.JSONObject;

import java.io.File;

public class RegisteredNoticePdfHelper {

    private RegisteredNoticePdfHelper() {
    }

    // पुराना प्रिंट/सेव फंक्शन (Android PrintManager वाला)
    public static boolean printRegisteredNotice(
            Activity activity,
            NoticeRecord noticeRecord
    ) {
        if (activity == null
                || noticeRecord == null
                || !noticeRecord.hasDocumentSnapshot()) {
            return false;
        }

        try {
            JSONObject snapshot = new JSONObject(
                    noticeRecord.getDocumentSnapshot()
            );

            String jobName = createJobName(
                    noticeRecord.getNoticeNumber()
            );

            if ("SECTION_94".equals(
                    noticeRecord.getNoticeType()
            )) {
                Section94PdfHelper.printSection94(
                        activity,
                        jobName,
                        getValue(snapshot, "notice_date"),
                        getValue(snapshot, "to"),
                        getValue(snapshot, "subject"),
                        getValue(snapshot, "body"),
                        getValue(snapshot, "requirements"),
                        getValue(snapshot, "instruction"),
                        getValue(snapshot, "signature")
                );

                return true;
            }

            if ("SECTION_35".equals(
                    noticeRecord.getNoticeType()
            )) {
                Section35PdfHelper.printSection35(
                        activity,
                        jobName,
                        getValue(snapshot, "notice_date"),
                        getValue(snapshot, "to"),
                        getValue(snapshot, "subject"),
                        getValue(snapshot, "body"),
                        getValue(snapshot, "case_reference"),
                        getValue(
                                snapshot,
                                "transaction_details"
                        ),
                        getValue(
                                snapshot,
                                "appearance_direction"
                        ),
                        getValue(snapshot, "signature")
                );

                return true;
            }

            if ("CDR".equals(noticeRecord.getNoticeType())) {
                CdrPdfHelper.printCdr(
                        activity,
                        jobName,
                        getValue(snapshot, "notice_date"),
                        getValue(snapshot, "to"),
                        getValue(snapshot, "subject"),
                        getValue(snapshot, "body"),
                        getValue(snapshot, "office_summary"),
                        getValue(snapshot, "case_summary"),
                        getValue(
                                snapshot,
                                "identifier_summary"
                        ),
                        getValue(
                                snapshot,
                                "requested_information"
                        ),
                        getValue(snapshot, "period"),
                        getValue(snapshot, "justification"),
                        getValue(snapshot, "signature")
                );

                return true;
            }

            if ("COURT_RELEASE".equals(noticeRecord.getNoticeType())) {
                CourtReleasePdfHelper.CourtReleaseData data =
                        CourtReleasePdfHelper.fromJson(
                                snapshot.optJSONObject("court_release_data")
                        );

                if (data == null) {
                    return false;
                }

                data.noticeNumber = noticeRecord.getNoticeNumber();
                CourtReleasePdfHelper.printCourtRelease(
                        activity,
                        jobName,
                        data,
                        "hi".equals(LanguageManager.getLanguage(activity))
                );
                return true;
            }

            return false;

        } catch (Exception ignored) {
            return false;
        }
    }

    // नया फंक्शन: डायरेक्ट शेयर करने के लिए बैकग्राउंड में PDF बनाना (तीनों के लिए)
    public static boolean generateSilentPdfForShare(
            Context context,
            NoticeRecord noticeRecord,
            File outputFile
    ) {
        if (context == null || noticeRecord == null || !noticeRecord.hasDocumentSnapshot() || outputFile == null) {
            return false;
        }

        try {
            JSONObject snapshot = new JSONObject(noticeRecord.getDocumentSnapshot());
            String type = noticeRecord.getNoticeType();

            if ("SECTION_94".equals(type)) {
                return Section94PdfHelper.generateSilentPdf(
                        context,
                        outputFile,
                        getValue(snapshot, "notice_date"),
                        getValue(snapshot, "to"),
                        getValue(snapshot, "subject"),
                        getValue(snapshot, "body"),
                        getValue(snapshot, "requirements"),
                        getValue(snapshot, "instruction"),
                        getValue(snapshot, "signature")
                );
            }

            if ("SECTION_35".equals(type)) {
                return Section35PdfHelper.generateSilentPdf(
                        context,
                        outputFile,
                        getValue(snapshot, "notice_date"),
                        getValue(snapshot, "to"),
                        getValue(snapshot, "subject"),
                        getValue(snapshot, "body"),
                        getValue(snapshot, "case_reference"),
                        getValue(snapshot, "transaction_details"),
                        getValue(snapshot, "appearance_direction"),
                        getValue(snapshot, "signature")
                );
            }

            if ("CDR".equals(type)) {
                return CdrPdfHelper.generateSilentPdf(
                        context,
                        outputFile,
                        getValue(snapshot, "notice_date"),
                        getValue(snapshot, "to"),
                        getValue(snapshot, "subject"),
                        getValue(snapshot, "body"),
                        getValue(snapshot, "office_summary"),
                        getValue(snapshot, "case_summary"),
                        getValue(snapshot, "identifier_summary"),
                        getValue(snapshot, "requested_information"),
                        getValue(snapshot, "period"),
                        getValue(snapshot, "justification"),
                        getValue(snapshot, "signature")
                );
            }

            if ("COURT_RELEASE".equals(type)) {
                CourtReleasePdfHelper.CourtReleaseData data =
                        CourtReleasePdfHelper.fromJson(
                                snapshot.optJSONObject("court_release_data")
                        );

                if (data == null) {
                    return false;
                }

                data.noticeNumber = noticeRecord.getNoticeNumber();
                return CourtReleasePdfHelper.generateSilentPdf(
                        context,
                        outputFile,
                        data,
                        "hi".equals(LanguageManager.getLanguage(context))
                );
            }

            return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String getValue(
            JSONObject snapshot,
            String key
    ) {
        return snapshot.optString(key, "");
    }

    private static String createJobName(String noticeNumber) {
        String safeNoticeNumber = noticeNumber == null
                ? "Notice"
                : noticeNumber.replaceAll(
                "[^a-zA-Z0-9_\\-]",
                "_"
        );

        return "Registered_" + safeNoticeNumber;
    }
}
