#!/bin/bash

set -o pipefail
set -e

OUTPUT_FILE="cljsbuild.out"

if lein do clean, cljsbuild once 2>&1 | tee $OUTPUT_FILE 
then
  ! grep WARNING $OUTPUT_FILE
else 
  exit $? 
fi

