#!/bin/sh
###############################################
#   Run intensive benchmark tests from here   #
###############################################

#./gradlew build;

declare -i lines=100;
declare -i thresholds=1;
threshold=0.7;

while [ ${lines} -lt 1000000 ]
do
    while [ ${thresholds} -lt 30 ]
    do
	    ./gradlew clean;
	    ./gradlew test -P threshold=${threshold} -P lines=${lines}
	    threshold=`echo "$threshold + 0.01" | bc`
	    thresholds=thresholds+1;
	done;
	lines=lines*10
done;