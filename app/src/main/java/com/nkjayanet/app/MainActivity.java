package com.nkjayanet.app;

import android.content.Context;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        // Extract PHP binary + Mikhmonv3 ke internal storage
        File appDir = getFilesDir();
        File phpDir = new File(appDir, "php");
        File wwwDir = new File(appDir, "mikhmonv3");

        copyAssetFolder("php", phpDir.getAbsolutePath());
        copyAssetFolder("mikhmonv3", wwwDir.getAbsolutePath());

        // kasih executable permission ke php binary
        File phpBin = new File(phpDir, "php");
        phpBin.setExecutable(true);

        // Start PHP built-in server
        try {
            String cmd = phpBin.getAbsolutePath() +
                    " -S 127.0.0.1:8080 -t " + wwwDir.getAbsolutePath();
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // buka Mikhmonv3 di WebView
        webView.loadUrl("http://127.0.0.1:8080/");
    }

    private void copyAssetFolder(String assetFolder, String destPath) {
        try {
            String[] files = getAssets().list(assetFolder);
            if (files == null) return;
            File dir = new File(destPath);
            if (!dir.exists()) dir.mkdirs();

            for (String file : files) {
                InputStream in = getAssets().open(assetFolder + "/" + file);
                File outFile = new File(destPath, file);
                FileOutputStream out = new FileOutputStream(outFile);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}