#!/bin/bash

dir=`dirname $0`;

java -cp $dir/bin SRP $dir/testcases/large_100_A.txt $dir/testcases/large_100_B.txt $dir/testcases/large_100_C.txt;


