#!/bin/bash

declare -i cores=1;
declare -i maxCores=8;
threshold=0.99;
lines=100000;
testData="./labels_en.nt";
testDataUrl="http://downloads.dbpedia.org/3.9/en/labels_en.nt.bz2";

gradle downloadTestData -P testData=${testData} -P testDataUrl=${testDataUrl};

while [ ${cores} -le ${maxCores} ]
do
        gradle cleanTest;
        echo "> benchmarking for $cores cores...";
	    gradle test -P threshold=${threshold} -P lines=${lines} -P testData=${testData} -P cores=${cores};
	    chmod -R 777 ./benchmarks;
	    cores=`echo "$cores * 2" | bc`;
done;