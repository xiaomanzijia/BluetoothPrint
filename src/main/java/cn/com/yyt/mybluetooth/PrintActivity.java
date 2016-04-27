package cn.com.yyt.mybluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by licheng on 2/3/16.
 */
public class PrintActivity extends Activity{
    private String cardDir = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/";
    EditText txtCorpName;
    EditText txtCorpAddress;
    public PrintActivity() {
        // TODO Auto-generated constructor stub
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.print);

        Button btnPrint = (Button) findViewById(R.id.btnPrint);
        Button btnSclawbook = (Button) findViewById(R.id.btnSclawbook);
        txtCorpName = (EditText) findViewById(R.id.txtCorpName);
        txtCorpAddress=(EditText)findViewById(R.id.txtCorpAddress);
        if (isFirstRun()) {
            writeFlag();
            copyFileToCard();
            ShowHidenDialog("第一次运行程序!");
        } else {
            ShowHidenDialog("第N次运行程序!");
        }

        // 发送并打印文书
        btnPrint.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                PackageInfo packageInfo;
                try {
                    packageInfo = getPackageManager().getPackageInfo(
                            "com.dynamixsoftware.printershare", 0);
                } catch (PackageManager.NameNotFoundException e) {
                    packageInfo = null;
                    e.printStackTrace();
                }

                if (packageInfo == null) {
                    Log.e("Waring...", "not installed");

                } else {
                    Log.e("Waring...", "not installed");
                }

                if (packageInfo != null) {

                    Intent intent = new Intent();
                    ComponentName comp = new ComponentName(
                            "com.dynamixsoftware.printershare",
                            "com.dynamixsoftware.printershare.ActivityPrintPDF");
                    intent = new Intent();
                    intent.setComponent(comp);
                    intent.setAction("android.intent.action.VIEW");
                    intent.setType("application/pdf");
                    File file = new File(cardDir + "safety/printtest.pdf");
                    if (file.exists()) {
                        intent.setData(Uri.fromFile(file));
                        startActivity(intent);
                    } else {
                        ShowHidenDialog("Sorry, ");
                    }
                } else {
                    new AlertDialog.Builder(PrintActivity.this)
                            .setTitle("提示")
                            .setMessage(" ,是否立即安装?")
                            .setNegativeButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            String filePath = cardDir
                                                    + "safety "; // 文件需有可读权限
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
                            .setPositiveButton("No",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(
                                                DialogInterface arg0, int arg1) {
                                            // TODO Auto-generated method
                                            // stub

                                        }
                                    }).show();
                }
            }

        });

        // 生成执法文书
        btnSclawbook.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Document document = new Document(PageSize.A4);

                try {
                    PdfWriter wirte=PdfWriter.getInstance(document, new FileOutputStream(
                            cardDir + "safety/HelloWorld.pdf"));
                    document.open();
                    BaseFont titleBaseFont = BaseFont.createFont(
                            Environment.getExternalStorageDirectory()
                                    .getAbsolutePath()
                                    + "/Android/data/"
                                    + PrintActivity.this.getPackageName()
                                    + "/files/zhong.ttf", BaseFont.IDENTITY_H,
                            BaseFont.EMBEDDED);

                    Font titleFont1 = new Font(titleBaseFont, 18, Font.BOLD);
                    Font titleFont2 = new Font(titleBaseFont, 16, Font.BOLD);
                    Paragraph titleParagraph = new Paragraph("安全生产行政执法文书",
                            titleFont1);
                    titleParagraph.setAlignment(Element.ALIGN_CENTER);
                    document.add(titleParagraph);
                    PdfContentByte cb = wirte.getDirectContent();
                    cb.setLineWidth(1f);
                    cb.moveTo(90, 762);
                    cb.lineTo(545, 762);
                    cb.stroke();
                    cb.moveTo(90, 760);
                    cb.lineTo(545, 760);
                    cb.stroke();
                    titleParagraph = new Paragraph("现场检查记录",
                            titleFont2);
                    titleParagraph.setAlignment(Element.ALIGN_CENTER);
                    titleParagraph.setLeading(40f);
                    document.add(titleParagraph);

                    Font contentFont = new Font(titleBaseFont, 12, Font.NORMAL);
                    Paragraph corp_name = new Paragraph("被检查单位：", contentFont);
                    cb.setLineWidth(1f);
                    cb.moveTo(158, 685);
                    cb.lineTo(545, 685);
                    cb.stroke();
                    corp_name.setIndentationLeft(55f);
                    corp_name.add(txtCorpName.getText().toString().trim());
                    corp_name.setLeading(50f);
                    document.add(corp_name);

                    Paragraph corp_address = new Paragraph("地址：", contentFont);
                    cb.setLineWidth(1f);
                    cb.moveTo(122, 667);
                    cb.lineTo(545, 667);
                    cb.stroke();
                    corp_address.setIndentationLeft(55f);
                    corp_address.add(txtCorpAddress.getText().toString().trim());
                    document.add(corp_address);

                } catch (DocumentException de) {
                    System.err.println(de.getMessage());
                } catch (IOException ioe) {
                    System.err.println(ioe.getMessage());
                }
                document.close();
                ShowHidenDialog("恭喜哦,生成文书成功,点击打印文档试试看...");
            }
        });
    }

    private void copyFileToCard() {
        // 程序第一次安装时才复制文件
        byte[] arr = new byte[1024];
        InputStream is = null;
        FileOutputStream os = null;
        int readLen = 0;
        try {
            // 复制打印插件到SD卡中
            File dir = new File(cardDir + "safety");
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

            // 复制文书demo文件到SD卡中
            // is = getAssets().open("lawbook.pdf");
            // os = new FileOutputStream(dir + "/lawbook.pdf");
            // while ((readLen = is.read(arr)) != -1) {
            // os.write(arr, 0, readLen);
            // }
            // os.flush();
            // os.close();
            // is.close();

            // 复制字体文件到SD卡中
            is = getAssets().open("zhong.ttf");
            String APP_DIR_NAME = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/  ";
            File temp = new File(APP_DIR_NAME
                    + PrintActivity.this.getPackageName() + "/files/");
            if (!temp.exists()) {
                temp.mkdirs();
            }
            os = new FileOutputStream(temp + "/zhong.ttf");
            while ((readLen = is.read(arr)) != -1) {
                os.write(arr, 0, readLen);
            }
            os.flush();
            os.close();
            is.close();

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            Log.e("Bluetooth...", "未找到打印插件...");
        }
    }

    private boolean isFirstRun() {
        String flag;
        SharedPreferences obj = getSharedPreferences("isFirstRun",
                Context.MODE_PRIVATE);
        flag = obj.getString("isFirst", "");
        return !flag.equals("0") ? true : false;// 不为0则为第一次运行程序
    }

    private void writeFlag() {
        SharedPreferences obj = getSharedPreferences("isFirstRun",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = obj.edit();// 获取编辑器
        editor.putString("isFirst", "0");
        editor.commit();// 提交修改
    }

    /**
     * 自定义对话框
     */
    private void ShowDialog(String msg) {
        new AlertDialog.Builder(this).setTitle("提示").setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }
                }).show();
    }

    /**
     * 可消失的对话框
     */
    private void ShowHidenDialog(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
