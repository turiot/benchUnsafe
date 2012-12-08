package uriot.benchunsafe;

import java.lang.reflect.Field;
import sun.misc.Unsafe;

/**
*  Fonctions utilitaires : unsafe
 */

public class UnsafeHelper {

  private static Unsafe unsafe=null;
  
  /** singleton not thread safe */
  public static Unsafe getUnsafe() {
    //Unsafe unsafe = (Unsafe) AccessController.doPrivileged(Unsafe.getUnsafe());
    if (unsafe==null) {
      try {
        final Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        unsafe = (Unsafe) field.get(null);
      }
      catch (NoSuchFieldException ex) { ex.printStackTrace(); System.exit(1); }
      catch (IllegalAccessException ex) { ex.printStackTrace(); System.exit(1); }
    }
    return unsafe;
  }

}
