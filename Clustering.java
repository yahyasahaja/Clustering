import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import java.lang.reflect.Field;

public class Clustering {
  //INIT
  public static class Feature {
    public double[] lat = new double[DATA_LENGTH]; //Lattitude
    public double[] lang = new double[DATA_LENGTH]; //langitude
    public double[] bright = new double[DATA_LENGTH]; //brightness
    public double[] conf = new double[DATA_LENGTH]; //confidence
  }

  public static final int DATA_LENGTH = 49;
  public static double treshold = 0;
  public static int totalCluster = 0;
  public static int[] clusters; //cluster for each data
  public static double[] featuresHolder;
  public static int index = 0;
  public static Feature feature = new Feature();
  public static double[][] distances;
  public static double totalDistances = 0;

  //RANDOM VALUE FOR EACH DATA
  public static void generateRandomClusterForEachData() {
    //ENSURE ALL CLUSTERS HAVE AT LEAST ONE FEATURE
    boolean[] isExist = new boolean[totalCluster];

    for (int i = 0; i < clusters.length; i++) {
      int rand = (int) (Math.random() * totalCluster);
      clusters[i] = rand;
      
      boolean isForced = false;
      if (i >= totalCluster - 1) {
        for (int j = 0; j < isExist.length; j++) {
          if (!isExist[j]) {
            clusters[i] = j;
            isExist[j] = true;
            isForced = true;
            break;
          } 
        }
      }
      if (!isForced) isExist[rand] = true;
    }

    System.out.println("Generated Random Clusters: " + Arrays.toString(clusters));
  }

  //INSERT DATA FROM FILE BASED ON IT'S FIELD
  public static void insertData(String field) {
    try (Stream<String> stream = Files.lines(Paths.get(field))) {
      index = 0;
      featuresHolder = new double[DATA_LENGTH];
      stream.forEachOrdered(line -> {
        featuresHolder[index++] = Double.valueOf(line);
      });
      
      feature.getClass().getField(field).set(feature, featuresHolder);
    } catch (Exception err) {
      System.err.println(err);
    }
  }

  public static void insertFeatureData() {
    insertData("lat");
    insertData("lang");
    insertData("bright");
    insertData("conf");
  }

  public static void showAllData() {
    System.out.printf("\nLangitude: %s\n", Arrays.toString(feature.lat));
    System.out.printf("\nLongitude: %s\n", Arrays.toString(feature.lang));
    System.out.printf("\nBrightness: %s\n", Arrays.toString(feature.bright));
    System.out.printf("\nConfidence: %s\n", Arrays.toString(feature.conf));
  }

  public static void calculate() {
    System.out.println("\n\n================ ITERATION " + ++index +" ================");
    distances = new double[totalCluster][DATA_LENGTH];
    double currentTotalDistances = 0;
    
    for (int i = 0; i < distances.length; i++) {
      double[] distancesEachCluster = distances[i];
      double c = 0;
      double cLat = 0;
      double cBright = 0;
      double cLang = 0;
      double cConf = 0;
      
      for (int j = 0; j < DATA_LENGTH; j++) {
        if (clusters[j] == i) {
          c++;
          cLat += feature.lat[j];
          cLang += feature.lang[j];
          cBright += feature.bright[j];
          cConf += feature.conf[j];
        }
      }

      double centroidLat = cLat / c;
      double centroidLang = cLang / c;
      double centroidBright = cBright / c;
      double centroidConf = cConf / c;
      // System.out.println("CENTROID " + centroidLat + " " + cLat + " " + c);

      for (int j = 0; j < DATA_LENGTH; j++) {
        distancesEachCluster[j] = Math.sqrt(
          Math.pow(feature.lat[j] - centroidLat, 2)
          + Math.pow(feature.lang[j] - centroidLang, 2)
          + Math.pow(feature.bright[j] - centroidBright, 2)
          + Math.pow(feature.conf[j] - centroidConf, 2)
        );
        // System.out.println(distancesEachCluster[j] + " " + Math.pow(feature.lat[j] - centroidLat, 2));
      }
    }
    // System.out.println(Arrays.toString(distances[0]));
    // System.out.println(Arrays.toString(distances[1]));

    //COMPARING FOR EACH DATA SET, SEARCH THE MINIMUM VALUE
    for (int i = 0; i < DATA_LENGTH; i++) {
      //BEGIN COMPARE
      int candidateCluster = Integer.MAX_VALUE;
      double candidateDistance = Integer.MAX_VALUE;
      for (int j = 0; j < totalCluster; j++) {
        if (distances[j][i] < candidateDistance) {
          candidateDistance = distances[j][i];
          candidateCluster = j;
        }
      }

      clusters[i] = candidateCluster;
      currentTotalDistances += candidateDistance;
    }  

    System.out.println("CLUSTERS " + Arrays.toString(clusters));

    System.out.println("Total Distances: " + currentTotalDistances);
    System.out.println("Old Total Distances: " + totalDistances);
    double f = Math.abs(totalDistances - currentTotalDistances);
    totalDistances = currentTotalDistances;

    //FINISHING
    if (f > treshold) {
      System.out.println("|Total Distances| > treshold");
      System.out.println(f + " > " + treshold);
      calculate();
    } else {
      System.out.println("|Total Distances| < treshold");
      System.out.println(f + " < " + treshold);
      System.out.println("FINISH");
      System.out.println(Arrays.toString(clusters));
    }
  }

  public static void main (String[] args) {
    Scanner in = new Scanner(System.in);
    int n = 0;
    
    System.out.println("============ CLUSTERING ============");
    insertFeatureData();

    do {
      System.out.println("\n\n============ MENU ============");
      System.out.println("1. Show Data");
      System.out.println("2. Start Clustering!");
      System.out.println("3. Exit :(");
      System.out.print("Pilih menu ke: ");
      n = in.nextInt();

      if (n == 1) 
        showAllData();
      else if (n == 2) {
        //BEGIN
        System.out.print("Masukkan treshold: ");
        treshold = in.nextDouble();
        System.out.print("Masukkan jumlah cluster: ");
        //instantiate new clusters and it's length
        totalCluster = in.nextInt();
        clusters = new int[DATA_LENGTH];
        generateRandomClusterForEachData();
        index = 0;
        calculate();

      } else System.out.println("GOOD BYE :(");
    } while (n >= 1 && n < 3);
  }
}