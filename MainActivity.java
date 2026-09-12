package your.package.name;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends Activity {

    private static final int PICK_FILE = 100;

    private TextView statusText;
    private Uri selectedFile;

    // Demo key.
    // Production version में user-created recovery key इस्तेमाल करेंगे.
    private static final String SECRET =
            "1234567890123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);

        Button selectButton = findViewById(R.id.selectButton);
        Button backupButton = findViewById(R.id.backupButton);
        Button restoreButton = findViewById(R.id.restoreButton);

        selectButton.setOnClickListener(v -> selectFile());

        backupButton.setOnClickListener(v -> createBackup());

        restoreButton.setOnClickListener(v -> restoreBackup());
    }

    private void selectFile() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(intent, PICK_FILE);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == PICK_FILE &&
                resultCode == RESULT_OK &&
                data != null) {

            selectedFile = data.getData();

            statusText.setText(
                    "Selected:\n" + selectedFile.toString()
            );
        }
    }

    private void createBackup() {

        if (selectedFile == null) {

            Toast.makeText(
                    this,
                    "पहले file select करो",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try {

            InputStream input =
                    getContentResolver()
                            .openInputStream(selectedFile);

            File backupFile =
                    new File(
                            getFilesDir(),
                            "backup.enc"
                    );

            FileOutputStream output =
                    new FileOutputStream(backupFile);

            Cipher cipher =
                    Cipher.getInstance("AES/ECB/PKCS5Padding");

            SecretKeySpec key =
                    new SecretKeySpec(
                            SECRET.getBytes("UTF-8"),
                            "AES"
                    );

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    key
            );

            byte[] buffer =
                    new byte[8192];

            int length;

            while ((length = input.read(buffer)) != -1) {

                byte[] encrypted =
                        cipher.update(
                                buffer,
                                0,
                                length
                        );

                if (encrypted != null) {
                    output.write(encrypted);
                }
            }

            byte[] finalData =
                    cipher.doFinal();

            if (finalData != null) {
                output.write(finalData);
            }

            input.close();
            output.close();

            Toast.makeText(
                    this,
                    "Encrypted backup created",
                    Toast.LENGTH_LONG
            ).show();

            statusText.setText(
                    "Backup ready:\n" +
                    backupFile.getAbsolutePath()
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Backup failed",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void restoreBackup() {

        File backupFile =
                new File(
                        getFilesDir(),
                        "backup.enc"
                );

        if (!backupFile.exists()) {

            Toast.makeText(
                    this,
                    "Backup नहीं मिला",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        try {

            File outputFile =
                    new File(
                            getExternalFilesDir(null),
                            "restored_file"
                    );

            java.io.FileInputStream input =
                    new java.io.FileInputStream(
                            backupFile
                    );

            FileOutputStream output =
                    new FileOutputStream(
                            outputFile
                    );

            Cipher cipher =
                    Cipher.getInstance("AES/ECB/PKCS5Padding");

            SecretKeySpec key =
                    new SecretKeySpec(
                            SECRET.getBytes("UTF-8"),
                            "AES"
                    );

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    key
            );

            byte[] buffer =
                    new byte[8192];

            int length;

            while ((length = input.read(buffer)) != -1) {

                byte[] decrypted =
                        cipher.update(
                                buffer,
                                0,
                                length
                        );

                if (decrypted != null) {
                    output.write(decrypted);
                }
            }

            byte[] finalData =
                    cipher.doFinal();

            if (finalData != null) {
                output.write(finalData);
            }

            input.close();
            output.close();

            Toast.makeText(
                    this,
                    "Backup restored",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Restore failed",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
