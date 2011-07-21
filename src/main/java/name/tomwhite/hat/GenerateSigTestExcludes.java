package name.tomwhite.hat;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.scannotation.AnnotationDB;

public class GenerateSigTestExcludes {
  
  public static void main(String[] args) throws Exception {
    
    String classpath = null;
    String annotations =
      "org.apache.hadoop.classification.InterfaceAudience$Private," +
      "org.apache.hadoop.classification.InterfaceAudience$LimitedPrivate," +
      "org.apache.hadoop.classification.InterfaceStability$Unstable";
    
    if (args.length == 1) {
      classpath = args[0];
    } else if (args.length == 2) {
      classpath = args[0];
      annotations = args[1];
    } else {
      System.err.printf("Usage: java %s <classpath> [<annotations>] \n",
          GenerateSigTestExcludes.class.getName());
      System.err.println("\t<annotations> is an optional list of comma-separated annotation classnames");
      System.err.println("\t\tdefault: " + annotations);
      System.exit(1);
    }
    
    String[] jars = classpath.split(System.getProperty("path.separator"));
    URL[] urls = new URL[jars.length];
    for (int i = 0; i < jars.length; i++) {
      urls[i] = new File(jars[i]).toURI().toURL();
    }
    
    AnnotationDB db = new AnnotationDB();
    db.setScanFieldAnnotations(false);
    db.setScanMethodAnnotations(false);
    db.setScanParameterAnnotations(false);
    db.scanArchives(urls);
    
    Map<String, Set<String>> annotationIndex = db.getAnnotationIndex();
    Set<String> classes = new HashSet<String>();
    for (String annotation : annotations.split(",")) {
      Set<String> annotatedClasses = annotationIndex.get(annotation);
      if (annotatedClasses != null) {
        classes.addAll(annotatedClasses);
      }
    }
    
    SortedSet<String> allClasses = new TreeSet<String>();
    allClasses.addAll(db.getClassIndex().keySet());
    
    // Classes in private packages are private by implication
    // We can't just exclude the package, since subpackages will also be excluded
    Set<String> packageClasses = packageClasses(classes, allClasses);
    
    // Nested classes of private classes are private by implication
    Set<String> nestedClasses = nestedClasses(classes, allClasses);
    
    SortedSet<String> sortedClasses = new TreeSet<String>(classes);
    sortedClasses.addAll(packageClasses);
    sortedClasses.addAll(nestedClasses);
    for (String classname : sortedClasses) {
      if (!classname.endsWith(".package-info")) {
        System.out.println(classname);
      }
    }
    
  }
  
  private static Set<String> packageClasses(Collection<String> classes, SortedSet<String> allClasses) {
    Set<String> packageClasses = new HashSet<String>();
    Collection<String> packages = Collections2.filter(allClasses, new Predicate<String>() {
      @Override public boolean apply(String input) {
        return input.endsWith(".package-info");
      }
    });
    for (String packageName : packages) {
      String prefix = packageName.substring(0, packageName.lastIndexOf('.') + 1);
      for (String c : subSetStartingWith(allClasses, prefix)) {
        // Assume classes start with an upper case character
        if (Character.isUpperCase(c.charAt(prefix.length()))) {
          packageClasses.add(c);
        }
      }
    }
    return packageClasses;
  }
  
  private static Set<String> nestedClasses(Collection<String> classes, SortedSet<String> allClasses) {
    Set<String> nestedClasses = new HashSet<String>();
    for (String classname : classes) {
      nestedClasses.addAll(subSetStartingWith(allClasses, classname + '$'));
    }
    return nestedClasses;
  }
  
  private static SortedSet<String> subSetStartingWith(SortedSet<String> set, String prefix) {
    char last = prefix.charAt(prefix.length() - 1);
    return set.subSet(prefix, prefix.substring(0, prefix.length() - 1) + (last+1));
  }

}
