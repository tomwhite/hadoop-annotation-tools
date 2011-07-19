#!/bin/bash

for f in $(find $@ -name package-info.java | xargs grep -l InterfaceAudience.Private); do
  grep package $f | awk '{print $2}' | sed '$s/.$//'
done

find $@ -name *.java \
  | xargs apt -cp classes -nocompile \
      -factory InternalApiAnnotationProcessorFactory 2> /dev/null

cat extra-private-elements \
  | grep -v "^#" 