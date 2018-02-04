package com.example.bairong.webviewtest;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;
    private final static String TAG = "MainActivity";
    private final static String ALBUM_PATH
            = Environment.getExternalStorageDirectory() + "/download_test/";
    private ProgressDialog mSaveDialog = null;
    private Bitmap mBitmap;
    private InputStream mInputStream;
    private String mFileName;
    private String mJsName = null;
    private String mSaveMessage;
    private WebView webView;
    private String url = "https://creditcards.hsbc.com.cn/portal/30021/landingPage?smt_cp=93292&smt_pl=2044402&smt_md=60";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.webview);
        webView.loadUrl(url);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                webView.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

                return super.shouldInterceptRequest(view, url);
            }
        });

    }

    /**
     * 读取js文件
     *
     * @throws IOException
     */

    public FileInputStream readJsFile(String fileName) throws IOException {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            return null;
        } else {
            File dirFile = new File(ALBUM_PATH);
            if (!dirFile.exists()) {
                Toast.makeText(MainActivity.this, "文件不存在！", Toast.LENGTH_LONG).show();
                return null;
            } else {
                FileInputStream fis = new FileInputStream(ALBUM_PATH + fileName);
                return fis;
            }

        }
    }

    /**
     * 读取文件
     *
     * @throws IOException
     */
    public Bitmap readFile(String fileName) throws IOException {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            return null;
        } else {
            File dirFile = new File(ALBUM_PATH);
            if (!dirFile.exists()) {
                Toast.makeText(MainActivity.this, "文件不存在！", Toast.LENGTH_LONG).show();
                return null;
            } else {
                Bitmap bmpDefaultPic = BitmapFactory.decodeFile(ALBUM_PATH + fileName, null);
                return bmpDefaultPic;
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    if (mJsName != null) {
                        saveJsFile(mInputStream, mJsName);
                    } else {
                        saveFile(mBitmap, mFileName);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Permission Denied
                Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Get image from newwork
     *
     * @param path The path of image
     * @return InputStream
     * @throws Exception
     */
    public InputStream getImageStream(String path) throws Exception {

        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        dataLen=conn.getContentLength();
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return conn.getInputStream();
        }
        return null;
    }

    /**
     * 保存文件
     *
     * @param bm
     * @param fileName
     * @throws IOException
     */
    public void saveFile(Bitmap bm, String fileName) throws IOException {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            File dirFile = new File(ALBUM_PATH);
            if (!dirFile.exists()) {
                dirFile.mkdir();
            }
            File myCaptureFile = new File(ALBUM_PATH + fileName);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
            bos.close();
        }
    }

    /**
     * 保存js文件
     *
     * @throws IOException
     */
    public void saveJsFile(InputStream inputStream, String jsName) throws IOException {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            File dirFile = new File(ALBUM_PATH);
            if (!dirFile.exists()) {
                dirFile.mkdir();
            }
            File myCaptureFile = new File(ALBUM_PATH + jsName);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));

//             int lenth= inputStream.
            byte b[] = new byte[1024];
            int nIdx=0;
            int readCount=0;//已经成功读取的字节个数
            while((readCount=inputStream.read(b))>0){

                bos.write(b,0,readCount);
            }
            inputStream.close();
            bos.flush();
            bos.close();
        }
    }

    /**
     * 查看文件是否存在
     *
     * @param dirPath 需要查询的文件目录
     * @param _type   查询类型，比如mp3什么的
     */
    public boolean judgeFile(String dirPath, String _type) {
        boolean tag = false;
        File f = new File(dirPath);
        if (!f.exists()) {//判断路径是否存在
            return false;
        }
        File[] files = f.listFiles();
        if (files == null) {//判断权限
            return false;
        }
        for (File _file : files) {//遍历目录
            if (_file.isFile() && _file.getName().equals(_type)) {
                tag = true;
                break;
            } else if (_file.isDirectory()) {//查询子目录
                judgeFile(_file.getAbsolutePath(), _type);
            } else {
                tag = false;
            }
        }
        return tag;
    }


}
