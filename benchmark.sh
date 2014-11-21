#!/bin/bash
###############################################
#   Run intensive benchmark tests from here   #
###############################################


declare -i thresholds=0;
declare -i lines=100;
declare -i maxlines=101;
declare -i times=20;
declare -i cores=1;
step=0.01;
thresholdStart=0.8;
testData="./labels_en.nt"
testDataUrl="http://downloads.dbpedia.org/3.9/en/labels_en.nt.bz2"

if [ $# -eq 1 ]
then
    if [ ${1} == "--info" ]
        then
            printf "Usage: \t ./benchmark.sh \$lines \$maxlines \$thresholdStart \$steps \$times \$cores \n";
            exit;
        fi
fi

if [ $# -ge 6 ]
then
    declare -i lines=${1};
    declare -i maxlines=${2};
    thresholdStart=${3};
    step=${4};
    declare -i times=${5};
    declare -i cores=${6};
fi

gradle downloadTestData -P testData=${testData} -P testDataUrl=${testDataUrl};

while [ ${lines} -lt ${maxlines} ]
do
    thresholds=0
    threshold=${thresholdStart}
    while [ ${thresholds} -lt ${times} ]
    do
	    gradle cleanTest;
	    gradle test -P threshold=${threshold} -P lines=${lines} -P testData=${testData} -P cores=${cores}
	    threshold=`echo "$threshold + $step" | bc`
	    thresholds=thresholds+1;
	    chmod -R 777 ./benchmarks
	done;
	lines=`echo "$lines * 10" | bc`
done;