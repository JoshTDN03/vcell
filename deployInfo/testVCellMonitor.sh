#!/bin/bash

UNKNOWN=3                               ## Define nagios standard 'UNKNOWN' response code = 3, status of service undetermined

# main code
if [ "$#" != "3" ] ; then 
	echo "Usage: testVCellMonitor.sh testMonitorHost(e.g. vcellservice) testMonitorPort(e.g.33330) {statusConnect,statusFull}"
	exit $UNKNOWN
fi



port=/dev/tcp/${1}/${2}
testrequest=${3}


exec 3<>$port                           ## Try connect to VCell monitor server, get socket reference (3)
if [ $? != 0 ]; then                    ## Check connection succeeded
        echo "Failed connecting test port "$port        ## Status message for nagios
        exit $UNKNOWN                   ## Connection failed, respond with nagios 'UNKNOWN' code
fi

timeout 1 echo $testrequest >&3 		## Initiate monitor communication by sending 'hello'
if [ $? != 0 ]; then                    ## Check 'request' sent
        echo "Error, request "$testrequest" failed"               ## Status message for nagios
        exit $UNKNOWN                   ## 'request' failed, respond with nagios 'UNKNOWN' code
fi

read -t 1 var1 <&3                      ## get response as VCell status in nagios format (digit+text+|+performance)
if [ $? != 0 ]; then                    ## Check 'request' response
        echo "status Response Failed"   ## Status message for nagios
        exit $UNKNOWN                   ## 'request' response failed, respond with nagios 'UNKNOWN' code
fi

if [ "${#var1}" -lt 2 ]; then           ## Check response at least 2 characters (1 digit and some text)
        echo "Error, expecting 1 digit and some text" ## Status message for nagios
        exit $UNKNOWN                   ## too few characters, respond with nagios 'UNKNOWN' code
fi

re='^[0-9]+$'                           ## Regular expression to match integer digit
if ! [[ `cut -c1 <<<"$var1"` =~ $re ]]; then    ## Check first character is a digit
        echo "Error, expecting digit as first character"          ## Status message for nagios
        exit $UNKNOWN                   ## first char not a digit, respond with nagios 'UNKNOWN' code
fi

declare -i monitor_status_code          ## declare integer for script exit code (returned to nagios)
monitor_status_code=`cut -c1 <<<"$var1"`        ## Get VCell monitor standard nagios code

if [ "${#var1}" -gt 1 ]; then           ## Get VCell monitor status text if there is any
        strip_messg=`cut -c2- <<<"$var1"`	## Get characters after the first digit
        echo $strip_messg               ## Status message for nagios from VCell monitor
else
    	echo "No Message"
fi

exit $monitor_status_code				## exit with parsed nagios code (0-OK,1-Warning,2-Critical,3-Unknown)

