#!/bin/bash

# set default values for parameters
website="example.com"
count=5

# parse command-line options
while getopts "w:c:" opt; do
  case $opt in
    w)
      website=$OPTARG
      ;;
    c)
      count=$OPTARG
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      exit 1
      ;;
  esac
done

# ping the website $count times
result=$(ping -c $count $website)

# compose output
if [ $? -eq 0 ]; then
  echo "$result"
  time=$(echo "$result" | grep "rtt min/avg/max/mdev" | awk -F'/' '{print $5}')
  echo "$time"
  exit 0
else
  echo "Error: The website $website is not reachable."
  exit 1
fi
