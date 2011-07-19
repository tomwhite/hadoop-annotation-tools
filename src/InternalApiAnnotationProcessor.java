import static com.sun.mirror.util.DeclarationVisitors.NO_OP;
import static com.sun.mirror.util.DeclarationVisitors.getDeclarationScanner;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.SimpleDeclarationVisitor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class InternalApiAnnotationProcessor implements AnnotationProcessor {
  
  private static final String METHODS = "methods";
  
  private static final List<String> annotations = Arrays.asList(
    "org.apache.hadoop.classification.InterfaceAudience.Private",
    "org.apache.hadoop.classification.InterfaceAudience.LimitedPrivate",
    "org.apache.hadoop.classification.InterfaceStability.Unstable"
  );

  private final AnnotationProcessorEnvironment env;
  public InternalApiAnnotationProcessor(AnnotationProcessorEnvironment env) {
    this.env = env;
  }
  
  @Override
  public void process() {
    DeclarationVisitor visitor;
    if (env.getOptions().containsKey("-Aformat=" + METHODS)) {
      visitor = new MethodVisitor();
    } else {
      visitor = new TypeVisitor();
    }
    for (TypeDeclaration typeDecl : env.getSpecifiedTypeDeclarations()) {
      typeDecl.accept(getDeclarationScanner(visitor, NO_OP));
    }
  }
  

  private static class TypeVisitor extends SimpleDeclarationVisitor {
    public void visitTypeDeclaration(TypeDeclaration d) {
      process(d);
    }
    private void process(TypeDeclaration d) {
      Collection<AnnotationMirror> mirrors = d.getAnnotationMirrors();
      for (AnnotationMirror mirror : mirrors) {
        if (annotations.contains(getAnnotationName(mirror))) {
          System.out.println(d.getQualifiedName());
          printNestedTypes(d, d.getQualifiedName());
          return;
        }
      }
    }
  }
  
  private static void printNestedTypes(TypeDeclaration d, String className) {
    for (TypeDeclaration nested : d.getNestedTypes()) {
      String nestedClassName = className + "$" + nested.getSimpleName();
      System.out.println(nestedClassName);
      printNestedTypes(nested, nestedClassName);
    }
  }
  
  private static class MethodVisitor extends SimpleDeclarationVisitor {
    public void visitMethodDeclaration(MethodDeclaration d) {
      Collection<AnnotationMirror> mirrors = d.getAnnotationMirrors();
      for (AnnotationMirror mirror : mirrors) {
        if (annotations.contains(getAnnotationName(mirror))) {
          System.out.println("Method " +
              d.getDeclaringType().getQualifiedName() + "#" +
              d.getSimpleName());
          return;
        }
      }
    }
  }
  
  private static String getAnnotationName(AnnotationMirror mirror) {
    // Pull out the class name
    return mirror.toString().replaceFirst("^@([^\\(]*).*$", "$1");
  }

}
