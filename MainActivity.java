package cl.crm.clientes2;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import android.util.Base64;

public class MainActivity extends Activity {
    private WebView web;
    private android.content.SharedPreferences prefs;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("crm", MODE_PRIVATE);
        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient());
        web.addJavascriptInterface(new Bridge(this), "Android");
        web.loadUrl("file:///android_asset/index.html");
    }

    public class Bridge {
        private final Context ctx;
        Bridge(Context c){ctx=c;}

        @JavascriptInterface public String loadBackup(){
            return prefs.getString("backup", "{}");
        }

        @JavascriptInterface public void saveBackup(String json){
            prefs.edit().putString("backup", json).apply();
        }

        @JavascriptInterface public void copy(String text){
            ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("CRM", text));
            Toast.makeText(ctx,"Copiado ✔",Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface public void saveFile(String name,String mime,String text){
            try{
                if(android.os.Build.VERSION.SDK_INT>=29){
                    android.content.ContentValues v=new android.content.ContentValues();
                    v.put(MediaStore.Downloads.DISPLAY_NAME,name);
                    v.put(MediaStore.Downloads.MIME_TYPE,mime);
                    v.put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS);
                    Uri uri=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,v);
                    if(uri!=null){try(OutputStream os=getContentResolver().openOutputStream(uri)){os.write(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));}}
                }else{
                    File dir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if(!dir.exists())dir.mkdirs();
                    try(FileOutputStream fos=new FileOutputStream(new File(dir,name))){fos.write(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
                }
                Toast.makeText(ctx,"Guardado en Descargas ✔",Toast.LENGTH_SHORT).show();
            }catch(Exception e){Toast.makeText(ctx,"No se pudo guardar",Toast.LENGTH_SHORT).show();}
        }

        @JavascriptInterface public void shareImage(String dataUrl,String message,String phone){
            try{
                String b64=dataUrl.substring(dataUrl.indexOf(',')+1);
                byte[] bytes=Base64.decode(b64, Base64.DEFAULT);
                File f=new File(getCacheDir(),"promo.jpg");
                try(FileOutputStream fos=new FileOutputStream(f)){fos.write(bytes);}
                Uri uri=androidx.core.content.FileProvider.getUriForFile(ctx,"cl.crm.clientes2.fileprovider",f);
                Intent i=new Intent(Intent.ACTION_SEND);
                i.setType("image/jpeg");
                i.putExtra(Intent.EXTRA_TEXT,message);
                i.putExtra(Intent.EXTRA_STREAM,uri);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(i,"Compartir imagen"));
            }catch(Exception e){Toast.makeText(ctx,"No se pudo compartir",Toast.LENGTH_SHORT).show();}
        }

        @JavascriptInterface public void ocr(String dataUrl){
            Toast.makeText(ctx,"OCR no incluido en esta versión IA",Toast.LENGTH_SHORT).show();
        }
    }

    @Override public void onBackPressed(){
        if(web!=null && web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
