package com.nkjayanet.app;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private Process phpProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView webView = new WebView(this);
        setContentView(webView);

        // Extract PHP binary
        File filesDir = getFilesDir();
        File phpBinary = new File(filesDir, "php");
        extractAsset("php/php", phpBinary);
        phpBinary.setExecutable(true);

        // Extract mikhmonv3 folder
        File mikhmonDir = new File(filesDir, "mikhmonv3");
        if (!mikhmonDir.exists()) {
            mikhmonDir.mkdirs();
            copyAssetFolder("mikhmonv3", mikhmonDir);
        }

        // Start PHP server
        try {
            String cmd = phpBinary.getAbsolutePath() +
                    " -S 127.0.0.1:8080 -t " + mikhmonDir.getAbsolutePath();
            phpProcess = Runtime.getRuntime().exec(cmd);
            Toast.makeText(this, "PHP server started", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to start PHP server", Toast.LENGTH_LONG).show();
        }

        // Setup WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("http://127.0.0.1:8080/");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (phpProcess != null) {
            phpProcess.destroy();
        }
    }

    // Extract single file from assets
    private void extractAsset(String assetName, File outFile) {
        try (InputStream in = getAssets().open(assetName);
             FileOutputStream out = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Recursive copy folder from assets
    private void copyAssetFolder(String assetFolder, File dest) {
        try {
            String[] assets = getAssets().list(assetFolder);
            if (assets.length == 0) {
                // It's a file
                extractAsset(assetFolder, dest);
            } else {
                if (!dest.exists()) dest.mkdirs();
                for (String asset : assets) {
                    copyAssetFolder(assetFolder + "/" + asset,
                            new File(dest, asset));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
