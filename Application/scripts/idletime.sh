#!/bin/sh
# Get MacOSX idletime
/usr/sbin/ioreg -c IOHIDSystem | /usr/bin/awk '/HIDIdleTime/ {print int($NF/1000000);  exit}'
