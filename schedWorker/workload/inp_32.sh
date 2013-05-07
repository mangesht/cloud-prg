#!/bin/sh
echo "Are all 32 machines up?" 
echo "Wait for input " ;
while read inputline
do what="$inputline"
echo $what;
break;
done
echo "Starting another test"
 java -cp ../bin client  -s 10.147.205.141:9800 -w td_1_Wnum_32 >  td_1_Wnum_32.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9800 -w td_2_Wnum_32 >  td_2_Wnum_32.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9800 -w td_4_Wnum_32 >  td_4_Wnum_32.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9800 -w td_8_Wnum_32 >  td_8_Wnum_32.log

