package br.com.devedores.app;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import androidx.core.content.FileProvider;
import java.io.*;

public class DocumentManager {
    public static File clientFolder(Context c, String clientId, String folder){
        File root=new File(c.getFilesDir(),"clientes/"+clientId+"/"+safe(folder)); if(!root.exists())root.mkdirs(); return root;
    }
    public static String copyToClient(Context c, String clientId, String folder, Uri uri) throws Exception{
        ContentResolver cr=c.getContentResolver(); String name=displayName(cr,uri); String mime=cr.getType(uri); if(name==null||name.trim().isEmpty())name="documento";
        name=safe(name); File dir=clientFolder(c,clientId,folder); File out=new File(dir,System.currentTimeMillis()+"_"+name);
        InputStream in=cr.openInputStream(uri); if(in==null)throw new IOException("Arquivo indisponível"); FileOutputStream fos=new FileOutputStream(out);byte[] b=new byte[8192];int n;while((n=in.read(b))>0)fos.write(b,0,n);in.close();fos.close();return out.getAbsolutePath();
    }
    public static String displayName(ContentResolver cr, Uri uri){Cursor cur=null;try{cur=cr.query(uri,null,null,null,null);if(cur!=null&&cur.moveToFirst()){int i=cur.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(i>=0)return cur.getString(i);}}catch(Exception ignored){}finally{if(cur!=null)cur.close();}return null;}
    public static Uri uriForFile(Context c,String path){return FileProvider.getUriForFile(c,c.getPackageName()+".fileprovider",new File(path));}
    public static void delete(String path){if(path!=null)new File(path).delete();}
    public static String safe(String s){return s.replaceAll("[^a-zA-Z0-9._-]","_");}
}
