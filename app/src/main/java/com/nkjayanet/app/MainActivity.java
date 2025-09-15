package com.nkjayanet.app;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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

        // Extract php-cgi binary
        File binDir = new File(getFilesDir(), "php");
        if (!binDir.exists()) binDir.mkdirs();
        File phpCgi = new File(binDir, "php-cgi");
        if (!phpCgi.exists()) {
            copyAsset("php/php-cgi", phpCgi);
            phpCgi.setExecutable(true);
        }

        // Extract mikhmonv3
        File wwwDir = new File(getFilesDir(), "mikhmonv3");
        if (!wwwDir.exists()) {
            copyAssetFolder("mikhmonv3", wwwDir.getAbsolutePath());
        }

        // Start HTTP server via php-cgi
        try {
            phpProcess = new ProcessBuilder(
                    phpCgi.getAbsolutePath(),
                    "-b", "127.0.0.1:8080",
                    "-c", getFilesDir().getAbsolutePath()
            )
            .redirectErrorStream(true)
            .start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("http://127.0.0.1:8080/mikhmonv3/");
    }

    private void copyAsset(String assetPath, File outFile) {
        try (InputStream in = getAssets().open(assetPath);
             FileOutputStream out = new FileOutputStream(outFile)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyAssetFolder(String assetFolder, String destPath) {
        try {
            String[] list = getAssets().list(assetFolder);
            File dir = new File(destPath);
            if (!dir.exists()) dir.mkdirs();
            for (String name : list) {
                String assetFilePath = assetFolder + "/" + name;
                String outFilePath = destPath + "/" + name;
                String[] sub = getAssets().list(assetFilePath);
                if (sub != null && sub.length > 0) {
                    copyAssetFolder(assetFilePath, outFilePath);
                } else {
                    copyAsset(assetFilePath, new File(outFilePath));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (phpProcess != null) {
            phpProcess.destroy();
        }
    }
}
