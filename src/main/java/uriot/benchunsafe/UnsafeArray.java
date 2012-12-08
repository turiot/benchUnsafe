package uriot.benchunsafe;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import sun.misc.Unsafe;

/**
 *  unsafe array of int   
 */
public class UnsafeArray {

  public long base;
  public int size;
  public int sizeIncrement;
  public int rows;
  public int cols;
  public int maxSize;
  public int maxRows;
  public static final int COLSIZE=4;
  private Unsafe unsafe;
  
  public UnsafeArray(int size,int sizeIncrement) {
    this.size=size;
    this.sizeIncrement=sizeIncrement;
    unsafe=UnsafeHelper.getUnsafe();
    base=unsafe.allocateMemory(size);
    addUnsafeUsed(size);
  }
  
  public UnsafeArray(int size) {
    this(size,0);
  }
  
  public UnsafeArray(int rows,int cols,int rowsIncrement) {
    this.size=rows*cols*COLSIZE;
    this.sizeIncrement=rowsIncrement*cols*COLSIZE;
    this.rows=rows;
    this.cols=cols;
    unsafe=UnsafeHelper.getUnsafe();
    base=unsafe.allocateMemory(size);
    addUnsafeUsed(size);
  }
  
  public void free() {
    unsafe.freeMemory(base);
    removeUnsafeUsed(size);
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      free();
    } finally {
      super.finalize();
    }
  }
  
  public void expand() {
    size+=sizeIncrement;
    base=unsafe.reallocateMemory(base,size);
    addUnsafeUsed(sizeIncrement);
    rows+=sizeIncrement/(cols*COLSIZE);
  }

  /** faire une méthode add ? */
  public void setMaxRows(int nb) {
    maxRows=nb;
    maxSize=maxRows*cols*COLSIZE;
  }

  /** si rows+col */
  public void append(UnsafeArray source) {
    base=unsafe.reallocateMemory(base,size+source.maxSize);
    addUnsafeUsed(source.maxSize);
    unsafe.copyMemory(source.base,base+size,source.maxSize);
    size+=source.maxSize;
    rows+=source.maxRows;
  }
  
  public void setMemory(byte filler) {
    unsafe.setMemory(base,size,filler);
  }

  /** si rows+col */
  public void put(int row,int col,int val) {
    unsafe.putInt(base+row*cols*COLSIZE+col*COLSIZE,val);
  }

  /** si rows+col */
  public int get(int row,int col) {
    return unsafe.getInt(base+row*cols*COLSIZE+col*COLSIZE);
  }
  
  public byte getByte(int offset) {
    return unsafe.getByte(base+offset);
  }
  
  public void putByte(int offset,byte b) {
    unsafe.putByte(base+offset,b);
  }
  
  /** si rows+col */
  public void qsort(int crit,int crit2) {
    sort(crit,crit2,0,rows-1);
  }
  private void sort(int crit,int crit2,int low,int high) {
    int i=low, j=high;
    final int pivot=get((low+high)/2,crit);
    int pivot2=-1; if (crit2>0) pivot2=get((low+high)/2,crit2);
    while (i <= j) {
      boolean compi=false;
      if (get(i,crit) < pivot) compi=true;
      if ((get(i,crit) == pivot) && (pivot2!=-1) && (get(i,crit2) < pivot2)) compi=true;
      while (compi) {
        i++;
        compi=false;
        if (get(i,crit) < pivot) compi=true;
        if ((get(i,crit) == pivot) && (pivot2!=-1) && (get(i,crit2) < pivot2)) compi=true;
      }
      boolean compj=false;
      if (get(j,crit) > pivot) compj=true;
      if ((get(j,crit) == pivot) && (pivot2!=-1) && (get(j,crit2) > pivot2)) compj=true;
      while (compj) {
        j--;
        compj=false;
        if (get(j,crit) > pivot) compj=true;
        if ((get(j,crit) == pivot) && (pivot2!=-1) && (get(j,crit2) > pivot2)) compj=true;
      }
      if (i<=j) {
        swap(i,j);
        i++;
        j--;
      }
    }
    if (low < j)
      sort(crit,crit2,low,j);
    if (i < high)
      sort(crit,crit2,i,high);
     
  }
  private void swap(int i,int j)
  {
    for (int n=0;n<cols;n++) {
      final int temp=get(i,n);
      put(i,n,get(j,n));
      put(j,n,temp);
    }
    /* pas optimal
    final long temp=UnsafeHelper.getUnsafe().allocateMemory(cols*COLSIZE);
    UnsafeHelper.getUnsafe().copyMemory(base+i*cols*COLSIZE,temp,cols*COLSIZE);
    UnsafeHelper.getUnsafe().copyMemory(base+j*cols*COLSIZE,base+i*cols*COLSIZE,cols*COLSIZE);
    UnsafeHelper.getUnsafe().copyMemory(temp,base+j*cols*COLSIZE,cols*COLSIZE);
    UnsafeHelper.getUnsafe().freeMemory(temp);
    */
  }
 
  public void save(String filePath,boolean fastwrite) {
    FastBufferedOutputStream fast = null;
    BufferedOutputStream     slow = null;
    try {
      final FileOutputStream fos=new FileOutputStream(filePath);
      if (fastwrite) {
        fast = new FastBufferedOutputStream(fos,8192);
      }
      else {
        slow = new BufferedOutputStream(fos);
      }
      for(int i=0;i<4;i++) {
        final byte octet=(byte)(size>>>(i*8));
        if (fastwrite) {
          fast.write(octet);
        }
        else {
          slow.write(octet);
        }
      }
      for (int i=0;i<size;i++) {
        final byte octet=getByte(i);
        if (fastwrite) {
          fast.write(octet);
        }
        else {
          slow.write(octet);
        }
      }
    } catch (FileNotFoundException ex) {
      ex.printStackTrace();
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      try {
        if (fast != null) {
          fast.flush();
          fast.close();
          fast=null;
        }
        if (slow != null) {
          slow.flush();
          slow.close();
          slow=null;
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }  

  public void load(String filePath,boolean fast) {
    InputStream bufferedInput=null;
    try {
      if (fast)
        bufferedInput=new FastBufferedInputStream(new FileInputStream(filePath));
      else
        bufferedInput=new BufferedInputStream(new FileInputStream(filePath));
      int sz=0;
      byte[] buffer = new byte[4];
      int bytesRead=bufferedInput.read(buffer);
      if (bytesRead != -1) {
        for(int i=3;i>-1;i--){      
          sz <<= 8;  
          sz+=(int)buffer[i] & 0xFF;
        }  
      }
      removeUnsafeUsed(size);
      size=sz;
      base=unsafe.reallocateMemory(base,size);
      addUnsafeUsed(size);
      rows=sz/(cols*COLSIZE); // stocker dims
      int i=0;
      buffer = new byte[1000000];
      bytesRead=0;
      while ((bytesRead = bufferedInput.read(buffer)) != -1) {
        for(int buf=0;buf<bytesRead;++buf) {
          putByte(i,buffer[buf]);
          i++;
        }
      }
    } catch (FileNotFoundException ex) {
      ex.printStackTrace();
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      try {
        if (bufferedInput != null) {
          bufferedInput.close();
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }  
  
  public static int unsafeUsed=0;
  
  public static synchronized void addUnsafeUsed(int memory) {
    unsafeUsed+=memory;
  }
  public static synchronized void removeUnsafeUsed(int memory) {
    unsafeUsed-=memory;
  }
  
}
