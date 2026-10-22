package com.macoshapps.capitalflow;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.JavascriptInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import java.io.OutputStream;

public class MainActivity extends Activity {
 private WebView web; private String pendingJson; private static final int EXPORT=101,IMPORT=102;
 @Override public void onCreate(Bundle b){super.onCreate(b);web=new WebView(this);setContentView(web);web.setWebViewClient(new WebViewClient());web.setWebChromeClient(new WebChromeClient());WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(true);web.addJavascriptInterface(new Bridge(),"AndroidBackup");web.loadUrl("file:///android_asset/index.html");}
 public class Bridge { @JavascriptInterface public void exportJson(String json){pendingJson=json;runOnUiThread(()->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/json");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"CapitalFlow-backup.json");startActivityForResult(i,EXPORT);});} @JavascriptInterface public void importJson(){runOnUiThread(()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/json");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,IMPORT);});} }
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null)return;Uri uri=data.getData();try{if(request==EXPORT){try(OutputStream out=getContentResolver().openOutputStream(uri)){out.write(pendingJson.getBytes("UTF-8"));}}else if(request==IMPORT){byte[] bytes;try(java.io.InputStream in=getContentResolver().openInputStream(uri);java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()){byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);bytes=out.toByteArray();}String encoded=Base64.encodeToString(bytes,Base64.NO_WRAP);web.evaluateJavascript("window.receiveAndroidBackup(atob('"+encoded+"'))",null);}}catch(Exception ex){runOnUiThread(()->android.widget.Toast.makeText(this,"Błąd pliku JSON",android.widget.Toast.LENGTH_LONG).show());}}
 @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
}
