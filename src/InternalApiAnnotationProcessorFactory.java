import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class InternalApiAnnotationProcessorFactory implements
    AnnotationProcessorFactory {
  
  private static final Collection<String> supportedAnnotations
    = Collections.unmodifiableCollection(Arrays.asList("*"));

  @Override
  public Collection<String> supportedOptions() {
    return Collections.emptySet();
  }

  @Override
  public Collection<String> supportedAnnotationTypes() {
    return supportedAnnotations;
  }

  @Override
  public AnnotationProcessor getProcessorFor(
      Set<AnnotationTypeDeclaration> atds, AnnotationProcessorEnvironment env) {
    return new InternalApiAnnotationProcessor(env);
  }

}
