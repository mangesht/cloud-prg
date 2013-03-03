JOBID=`cat WordCountMR.log | grep "Job complete" | awk '{ print $7 }' | awk -F'_' '{ print $2 }'`
find ../hadoop/hadoop-1.0.4/logs/history/  -name "*$JOBID*WordCountMR" > joblogs

while read p; do
	echo filename $p
	cat $p | grep "TASKID" | awk '{ print $2 " " $3 " " $4 " " $5 }' | grep "^TASKID" >  jobtimes
done < joblogs

cat jobtimes | grep "SETUP" 
cat jobtimes | grep "CLEANUP" 

