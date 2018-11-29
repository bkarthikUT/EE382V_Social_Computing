#!/bin/bash

dir=`dirname $0`;

java -cp $dir/bin SRP $dir/testcases/large_1000_A.txt $dir/testcases/large_1000_B.txt;


