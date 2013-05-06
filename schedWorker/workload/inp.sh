#!/bin/sh
echo "Are all 32 machines up?" 
echo "Wait for input " ;
while read inputline
do what="$inputline"
echo $what;
break;
done
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_1_Wnum_32 >  td_1_Wnum_32.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_2_Wnum_32 >  td_2_Wnum_32.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_4_Wnum_32 >  td_4_Wnum_32.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_8_Wnum_32 >  td_8_Wnum_32.log

echo "Kill 16 machine we need only 16 machines wait till all are killed , then press enter "
while read inputline
do what="$inputline"
echo $what;
break;
done

echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_1_Wnum_16 >  td_1_Wnum_16.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_2_Wnum_16 >  td_2_Wnum_16.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_4_Wnum_16 >  td_4_Wnum_16.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_8_Wnum_16 >  td_8_Wnum_16.log

echo "Kill 8 machine we need only 8 machines wait till all are killed , then press enter "
while read inputline
do what="$inputline"
echo $what;
break;
done

echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_1_Wnum_8 >  td_1_Wnum_8.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_2_Wnum_8 >  td_2_Wnum_8.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_4_Wnum_8 >  td_4_Wnum_8.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_8_Wnum_8 >  td_8_Wnum_8.log

echo "Kill 4 machine we need only 4 machines wait till all are killed , then press enter "
while read inputline
do what="$inputline"
echo $what;
break;
done

echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_1_Wnum_4 >  td_1_Wnum_4.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_2_Wnum_4 >  td_2_Wnum_4.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_4_Wnum_4 >  td_4_Wnum_4.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_8_Wnum_4 >  td_8_Wnum_4.log

echo "Kill 2 machine we need only 2 machines wait till all are killed , then press enter "
while read inputline
do what="$inputline"
echo $what;
break;
done

echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_1_Wnum_2 >  td_1_Wnum_2.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_2_Wnum_2 >  td_2_Wnum_2.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_4_Wnum_2 >  td_4_Wnum_2.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_8_Wnum_2 >  td_8_Wnum_2.log

echo "Kill 1 machine we need only 1 machines wait till all are killed , then press enter "
while read inputline
do what="$inputline"
echo $what;
break;
done

echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_1_Wnum_1 >  td_1_Wnum_1.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_2_Wnum_1 >  td_2_Wnum_1.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_4_Wnum_1 >  td_4_Wnum_1.log
echo "Starting another test"
java -cp ../bin client  -s 10.147.205.141:9100 -wl td_8_Wnum_1 >  td_8_Wnum_1.log

echo "test over " 
