package cn.com.yyt.mybluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

import cn.com.yyt.mybluetooth.Constants.Constants;
import cn.com.yyt.mybluetooth.utils.PreferencesUtils;

/**
 * Created by licheng on 27/4/16.
 */
public class WebViewDownloadActivity extends Activity {
    private WebView webview;
    private String cardDir = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        webview = (WebView) findViewById(R.id.webView);

        webview.loadUrl("http://jwc.zjnu.edu.cn/Content.aspx?newsid=2646");
        webview.getSettings().setJavaScriptEnabled(true);
        webview.requestFocus();
        // 设置web视图客户端
        webview.setWebViewClient(new MyWebViewClient());
        webview.setDownloadListener(new MyWebViewDownLoadListener());

        if(!PreferencesUtils.getBoolean(WebViewDownloadActivity.this,Constants.IS_FIRST_USE_APP)){ //第一次使用APP需要复制apk文件
            Log.i("WebViewDownloadActivity","第一次使用");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    copyFileToCard();
                }
            }).start();
            PreferencesUtils.putBoolean(WebViewDownloadActivity.this, Constants.IS_FIRST_USE_APP,true);
        }else {
            Log.i("WebViewDownloadActivity","第N次使用");
        }
    }

    class MyWebViewClient extends WebViewClient {
        // 如果页面中链接，如果希望点击链接继续在当前browser中响应，
        // 而不是新开Android的系统browser中响应该链接，必须覆盖 webview的WebViewClient对象。
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return super.shouldOverrideUrlLoading(view, url);
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        }

        public void onPageFinished(WebView view, String url) {
        }

        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
        }

    }

    class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Toast t = Toast.makeText(WebViewDownloadActivity.this, "需要SD卡。", Toast.LENGTH_LONG);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                return;
            }
            //检测文件是否存在
            String fileName = url.substring(url.lastIndexOf("/")+1);
            File directory = Environment.getExternalStorageDirectory();
            File file = new File(directory,fileName);
            if(file.exists()){
                Log.i("tag", "The file has already exists.");
                //编辑打印
                showEditPrintDialog(file);
            }else {
                DownloaderTask task = new DownloaderTask();
                task.execute(url);
            }
        }
    }

    private void showEditPrintDialog(final File file) {
        EditPrintDialog dialog = new EditPrintDialog(WebViewDownloadActivity.this,R.style.MyDialog);
        dialog.show();
        dialog.setListener(new EditPrintDialog.BtnListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.btnEdit:
                        openWPS(file);
                        break;
                    case R.id.btnPrint:
                        openPrinterShare(file);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    //调用PrinterShare
    private void openPrinterShare(File file) {
        //检测是否安装PrinterShare
        PackageInfo packinfo = null;
        try {
            packinfo = getPackageManager().getPackageInfo("com.dynamixsoftware.printershare",0);
        } catch (PackageManager.NameNotFoundException e) {
            packinfo = null;
            e.printStackTrace();
        }

        if(packinfo == null){ //提示安装PrinterShare
            new AlertDialog.Builder(WebViewDownloadActivity.this)
                    .setTitle("提示")
                    .setMessage("您还没有安装打印软件,是否立即安装?")
                    .setNegativeButton("是",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog,
                                        int which) {
                                    String filePath = cardDir + "zdcx/print.apk"; // 文件需有可读权限
                                    Intent intent = new Intent();
                                    intent.setAction(android.content.Intent.ACTION_VIEW);
                                    intent.setDataAndType(
                                            Uri.parse("file://"
                                                    + filePath),
                                            "application/vnd.android.package-archive");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            })
                    .setPositiveButton("否",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(
                                        DialogInterface arg0, int arg1) {

                                }
                            }).show();
        }else {
            Intent intent = new Intent();
            ComponentName comp = new ComponentName(
                    "com.dynamixsoftware.printershare",
                    "com.dynamixsoftware.printershare.ActivityPrintDocuments");
            intent.setComponent(comp);
            intent.setAction("android.intent.action.VIEW");
            intent.setType("application/msword");
            intent.setData(Uri.fromFile(file));
            startActivity(intent);
        }
    }

    //调用WPS
    private void openWPS(File file) {
        //检测是否安装WPS
        PackageInfo packinfo = null;
        try {
            packinfo = getPackageManager().getPackageInfo("cn.wps.moffice_eng",0);
        } catch (PackageManager.NameNotFoundException e) {
            packinfo = null;
            e.printStackTrace();
        }
        if(packinfo == null){
            new AlertDialog.Builder(WebViewDownloadActivity.this)
                    .setTitle("提示")
                    .setMessage("您还没有安装WPS编辑软件,是否立即安装?")
                    .setNegativeButton("是",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog,
                                        int which) {
                                    String filePath = cardDir + "zdcx/wps.apk"; // 文件需有可读权限
                                    Intent intent = new Intent();
                                    intent.setAction(android.content.Intent.ACTION_VIEW);
                                    intent.setDataAndType(
                                            Uri.parse("file://"
                                                    + filePath),
                                            "application/vnd.android.package-archive");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            })
                    .setPositiveButton("否",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(
                                        DialogInterface arg0, int arg1) {

                                }
                            }).show();
        }else {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setClassName("cn.wps.moffice_eng", "cn.wps.moffice.documentmanager.PreStartActivity");
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            startActivity(intent);
        }
    }


    class DownloaderTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
//          Log.i("tag", "url="+url);
            String fileName = url.substring(url.lastIndexOf("/")+1);
            fileName = URLDecoder.decode(fileName);
            Log.i("tag", "fileName="+fileName);

            File directory = Environment.getExternalStorageDirectory();
            File file = new File(directory,fileName);
            if(file.exists()){
                Log.i("tag", "The file has already exists.");
                return fileName;
            }
            try {
                HttpClient client = new DefaultHttpClient();
//                client.getParams().setIntParameter("http.socket.timeout",3000);//设置超时
                HttpGet get = new HttpGet(url);
                HttpResponse response = client.execute(get);
                if(HttpStatus.SC_OK == response.getStatusLine().getStatusCode()){
                    HttpEntity entity = response.getEntity();
                    InputStream input = entity.getContent();

                    writeToSDCard(fileName,input);

                    input.close();
//                  entity.consumeContent();
                    return fileName;
                }else{
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result == null){
                Toast t=Toast.makeText(WebViewDownloadActivity.this, "连接错误！请稍后再试！", Toast.LENGTH_LONG);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                return;
            }

            Toast t = Toast.makeText(WebViewDownloadActivity.this, "已保存到SD卡。", Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            File directory = Environment.getExternalStorageDirectory();
            File file = new File(directory,result);
            Log.i("tag", "Path="+file.getAbsolutePath());

            //编辑打印
            showEditPrintDialog(file);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    //根据文件类型获取相应启用软件
    private Intent getFileIntent(File file){
//       Uri uri = Uri.parse("http://m.ql18.com.cn/hpf10/1.pdf");
        Uri uri = Uri.fromFile(file);
        String type = getMIMEType(file);
        Log.i("tag", "type="+type);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, type);
        return intent;
    }

    //将下载信息写入文件
    private void writeToSDCard(String fileName,InputStream input){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File directory = Environment.getExternalStorageDirectory();
            File file = new File(directory,fileName);
//          if(file.exists()){
//              Log.i("tag", "The file has already exists.");
//              return;
//          }
            try {
                FileOutputStream fos = new FileOutputStream(file);
                byte[] b = new byte[2048];
                int j = 0;
                while ((j = input.read(b)) != -1) {
                    fos.write(b, 0, j);
                }
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else{
            Log.i("tag", "NO SDCard.");
        }
    }

    //获取文件格式类型
    private String getMIMEType(File f) {
        String type = "";
        String fName = f.getName();
      /* 取得扩展名 */
        String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();

      /* 依扩展名的类型决定MimeType */
        if (end.equals("pdf")) {
            type = "application/pdf";//
        } else if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") ||
                end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
            type = "audio/*";
        } else if (end.equals("3gp") || end.equals("mp4")) {
            type = "video/*";
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") ||
                end.equals("jpeg") || end.equals("bmp")) {
            type = "image/*";
        } else if (end.equals("apk")) {
        /* android.permission.INSTALL_PACKAGES */
            type = "application/vnd.android.package-archive";
        } else if (end.equals("pptx") || end.equals("ppt")) {
            type = "application/vnd.ms-powerpoint";
        } else if (end.equals("docx") || end.equals("doc")) {
            type = "application/vnd.ms-word";
        } else if (end.equals("xlsx") || end.equals("xls")) {
            type = "application/vnd.ms-excel";
        } else {
//        /*如果无法直接打开，就跳出软件列表给用户选择 */
            type = "*/*";
        }
        return type;
    }

    //复制apk文件到sd卡
    private void copyFileToCard() {
        // 程序第一次安装时才复制文件
        byte[] arr = new byte[1024];
        InputStream is = null;
        FileOutputStream os = null;
        int readLen = 0;
        try {
            // 复制打印插件到SD卡中
            File dir = new File(cardDir+"zdcx");
            if (!dir.exists())
                dir.mkdir();

            is = getAssets().open("print.apk");
            os = new FileOutputStream(dir + "/print.apk");
            while ((readLen = is.read(arr)) != -1) {
                os.write(arr, 0, readLen);
            }
            os.flush();
            os.close();
            is.close();

            //复制WPS到SD卡中
            is = getAssets().open("wps.apk");
            os = new FileOutputStream(dir + "/wps.apk");
            while ((readLen = is.read(arr)) != -1){
                os.write(arr, 0, readLen);
            }
            os.flush();
            os.close();
            is.close();

        } catch (IOException e1) {
            Log.e("Bluetooth...", "未找到打印插件...");
        }
    }


}
