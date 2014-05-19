#!/bin/bash
###############################################
#   Run intensive benchmark tests from here   #
###############################################

declare -i lines=100;
declare -i thresholds=1;
threshold=0.7;
testData="./labels_en.nt"
testDataUrl="http://downloads.dbpedia.org/3.9/en/labels_en.nt.bz2"

./gradlew downloadTestData -P testData=${testData} -P testDataUrl=${testDataUrl};

while [ ${lines} -lt 1000000 ]
do
    while [ ${thresholds} -lt 30 ]
    do
	    ./gradlew clean;
	    ./gradlew test -P threshold=${threshold} -P lines=${lines} -P testData=${testData}
	    threshold=`echo "$threshold + 0.01" | bc`
	    thresholds=thresholds+1;
	done;
	lines=lines*10
done;