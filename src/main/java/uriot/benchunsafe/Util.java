package uriot.benchunsafe;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *  Fonctions utilitaires
 */

public final class Util {

  /** affiche une table */
  public static void listMatrix(int[][] matrix) {
    String ligne;
    for (int i = 0; i < matrix.length; i++) {
      ligne = i + " -- ";
      for (int j = 0; j < matrix[0].length; j++) {
        ligne = ligne + matrix[i][j] + " ";
      }
      System.out.println(ligne);
    }
  }
  
  /** sauve une table */
  public static void saveArray(String filename, int[][] output_veld) {
    try {
      final FileOutputStream fos = new FileOutputStream(filename);
      final GZIPOutputStream gzos = new GZIPOutputStream(fos);
      final ObjectOutputStream out = new ObjectOutputStream(gzos);
      out.writeObject(output_veld);
      out.flush();
      out.close();
    } catch (IOException e) {
      System.out.println(e);
    }
  } 

  /** charge une table */
  public static int[][] loadArray(String filename) {
    int[][] gelezen_veld=new int[0][0];
    try {
      final FileInputStream fis = new FileInputStream(filename);
      final GZIPInputStream gzis = new GZIPInputStream(fis);
      final ObjectInputStream in = new ObjectInputStream(gzis);
      gelezen_veld = (int[][]) in.readObject();
      in.close();
    } catch (Exception e) {
      System.out.println(e);
    }
    return gelezen_veld;
  }

  /** affiche une table unsafe*/
  public static void listUnsafeArray(UnsafeArray a) {
    listPartialUnsafeArray(a,0,a.rows);
  }
  public static void listPartialUnsafeArray(UnsafeArray a,int start,int end) {
    String ligne;
    for (int i = start; i < end; i++) {
      ligne = i + " -- ";
      for (int j = 0; j < a.cols; j++) {
        ligne = ligne + a.get(i,j) + " ";
      }
      System.out.println(ligne);
    }
  }

  /** affiche l'occupation mémoire */
  public static void memoryUsed() {
    final long safe=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
    final int unsafe=UnsafeArray.unsafeUsed;
    System.out.println("memory : "+safe/1024+" k safe + "+(unsafe/1024)+" k unsafe = "+(safe+unsafe)/(1024*1024)+" M");
  }
  
  private static HashMap chronos=new HashMap();
  
  /** démarre un chrono */
  public static void chronoStart(String name) {
    chronos.put(name,(Long)System.nanoTime());
  }
  
  /** arrete un chrono */
  public static void chronoStop(String name) {
    final float end=System.nanoTime();
    final float start=(Long)chronos.get(name);
    final float sec=(end-start)/1000000000;
    final float mil=(end-start)/1000000;
    System.out.printf("time %s : %.1f s %.3f ms \n",name,sec,mil);    
  }
  
}
