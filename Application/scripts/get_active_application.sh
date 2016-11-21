#if [ "$#" -eq 1 ]
#	then
		active_window_pid=`xprop -root _NET_ACTIVE_WINDOW | sed 's/.* //'` &&
		common_window_pid_part=$(echo $active_window_pid | cut -c3- ) && 
		wmctrl -lp | grep $common_window_pid_part #> $1;
	#else
	#	echo "Please give the ouput text file at the command line parameter";

#fi

