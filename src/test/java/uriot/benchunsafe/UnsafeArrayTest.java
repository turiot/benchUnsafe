package uriot.benchunsafe;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UnsafeArrayTest extends TestCase {

  public UnsafeArrayTest(String testName) {
    super(testName);
  }

  public static Test suite() {
    return new TestSuite(UnsafeArrayTest.class);
  }

  public void testApp() {
    final int DIM = 10000;
    final int RUN = 1000*1000*100;
    Random randomGenerator = new Random();
  
    Util.chronoStart("test_unsafe");
    Util.chronoStart("alloc_unsafe");
    UnsafeArray a = new UnsafeArray(DIM,DIM,0);
    Util.chronoStop("alloc_unsafe");
    
    Util.chronoStart("put_unsafe");
    for (int i=0;i<RUN;i++) {
      a.put(randomGenerator.nextInt(DIM), randomGenerator.nextInt(DIM), randomGenerator.nextInt(100));
    }
    Util.chronoStop("put_unsafe");
    
    Util.chronoStart("get_unsafe");
    int x=0;
    for (int i=0;i<RUN;i++) {
      x+=a.get(randomGenerator.nextInt(DIM), randomGenerator.nextInt(DIM));
    }
    Util.chronoStop("get_unsafe");
    
    Util.chronoStop("test_unsafe");
    Util.memoryUsed();
    System.out.println(a.size);
    a.free();
    
    Util.chronoStart("test_safe");
    Util.chronoStart("alloc_safe");
    int[][] b = new int[DIM][DIM];
    Util.chronoStop("alloc_safe");
    
    Util.chronoStart("put_safe");
    for (int i=0;i<RUN;i++) {
      b[randomGenerator.nextInt(DIM)][randomGenerator.nextInt(DIM)]=randomGenerator.nextInt(100);
    }
    Util.chronoStop("put_safe");
    
    Util.chronoStart("get_safe");
    int y=0;
    for (int i=0;i<RUN;i++) {
      y+=b[randomGenerator.nextInt(DIM)][randomGenerator.nextInt(DIM)];
    }
    Util.chronoStop("get_safe");
    
    Util.chronoStop("test_safe");
    Util.memoryUsed();
    
  }
  
}
