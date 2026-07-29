package com.example.expensemanager.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.example.expensemanager.R;
import com.example.expensemanager.model.Category;
import com.example.expensemanager.model.Transaction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Exports transactions to a UTF-8 CSV file and opens the Android share sheet. */
public class CsvExporter {

    /** @return false if there was nothing to export or the write failed. */
    public static boolean export(Context context, List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return false;
        }
        try {
            File dir = new File(context.getExternalFilesDir(null), "exports");
            if (!dir.exists() && !dir.mkdirs()) {
                return false;
            }
            File file = new File(dir, "quan_ly_chi_tieu.csv");

            try (OutputStreamWriter writer =
                         new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                // Header row; file is written as UTF-8.
                writer.write("Ngay,Loai,Danh muc,So tien,Ghi chu\n");
                for (Transaction t : transactions) {
                    Category category = t.category();
                    String name = category.name;
                    String type = t.isIncome() ? "Thu" : "Chi";
                    String date = DateUtil.formatDate(t.date);
                    long amount = Math.round(t.amount);
                    String note = t.note == null ? "" : t.note;
                    writer.write(cell(date) + "," + cell(type) + "," + cell(name)
                            + "," + amount + "," + cell(note) + "\n");
                }
            }

            Uri uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", file);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/csv");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(share,
                    context.getString(R.string.export_share_title)));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** Quote a CSV cell if it contains a comma, quote or newline. */
    private static String cell(String value) {
        if (value == null) {
            return "";
        }
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v + "\"";
        }
        return v;
    }
}
