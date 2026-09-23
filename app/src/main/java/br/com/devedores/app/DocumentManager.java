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
        File root=new File(c.getFilesDir(),"clientes/"+clientId+"/"+safe(folder));
        if(!root.exists()) root.mkdirs();
        return root;
    }

    public static String copyToClient(Context c, String clientId, String folder, Uri uri) throws Exception{
        ContentResolver cr=c.getContentResolver();
        String name=displayName(cr,uri);
        if(name==null||name.trim().isEmpty()) name="documento";
        String mime=cr.getType(uri);
        return copyStreamToClient(c,clientId,folder,cr.openInputStream(uri),name,mime);
    }

    public static String copyFileToClient(Context c, String clientId, String folder, File source, String name, String mime) throws Exception{
        if(source==null || !source.exists()) throw new FileNotFoundException("Arquivo de origem não encontrado");
        return copyStreamToClient(c,clientId,folder,new FileInputStream(source),name,mime);
    }

    private static String copyStreamToClient(Context c,String clientId,String folder,InputStream in,String name,String mime) throws Exception{
        if(in==null) throw new IOException("Arquivo indisponível");
        name=safe(name==null||name.trim().isEmpty()?"documento":name);
        File dir=clientFolder(c,clientId,folder);
        String prefix=System.currentTimeMillis()+"_";
        File out=new File(dir,prefix+name);
        try(InputStream input=in; FileOutputStream fos=new FileOutputStream(out)){
            byte[] b=new byte[16*1024]; int n;
            while((n=input.read(b))>0) fos.write(b,0,n);
        }
        return out.getAbsolutePath();
    }

    public static String displayName(ContentResolver cr, Uri uri){
        Cursor cur=null;
        try{
            cur=cr.query(uri,null,null,null,null);
            if(cur!=null&&cur.moveToFirst()){
                int i=cur.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if(i>=0) return cur.getString(i);
            }
        }catch(Exception ignored){}finally{if(cur!=null)cur.close();}
        return null;
    }

    public static Uri uriForFile(Context c,String path){
        return FileProvider.getUriForFile(c,c.getPackageName()+".fileprovider",new File(path));
    }

    public static String moveInsideClient(Context c,String clientId,String oldPath,String newFolder,String name) throws Exception{
        if(oldPath==null||oldPath.isEmpty()) throw new IOException("Arquivo inválido");
        File source=new File(oldPath);
        String path=copyFileToClient(c,clientId,newFolder,source,name,null);
        delete(oldPath);
        return path;
    }

    public static void delete(String path){if(path!=null&&!path.isEmpty())new File(path).delete();}

    public static String safe(String s){return s.replaceAll("[^a-zA-Z0-9._-]","_");}
}
