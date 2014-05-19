#!/bin/bash
###############################################
#   Run intensive benchmark tests from here   #
###############################################


declare -i thresholds=1;
declare -i lines=1000;
declare -i times=30;
step=0.01;
thresholdStart=0.7;
testData="./labels_en.nt"
testDataUrl="http://downloads.dbpedia.org/3.9/en/labels_en.nt.bz2"

if [ $# -ge 4 ]
then
    declare -i lines=${1};
    declare -i times=${3};
    step=${4};
    thresholdStart=${2};
fi

./gradlew downloadTestData -P testData=${testData} -P testDataUrl=${testDataUrl};

while [ ${lines} -lt 1000000 ]
do
    thresholds=1
    threshold=${thresholdStart}
    while [ ${thresholds} -lt ${times} ]
    do
	    ./gradlew clean;
	    ./gradlew test -P threshold=${threshold} -P lines=${lines} -P testData=${testData}
	    threshold=`echo "$threshold + $step" | bc`
	    thresholds=thresholds+1;
	done;
	lines=`echo "$lines * 10" | bc`
done;