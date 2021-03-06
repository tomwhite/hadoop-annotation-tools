#!/bin/bash

for f in $(find $@ -name package-info.java | xargs grep -l 'InterfaceAudience.\(LimitedPrivate\|Private\)'); do
  grep package $f | awk '{print $2}' | sed '$s/.$//'
done

find $@ -name *.java \
  | xargs apt -cp target/classes -nocompile \
      -factory InternalApiAnnotationProcessorFactory 2> /dev/null

cat extra-private-elements \
  | grep -v "^#" 