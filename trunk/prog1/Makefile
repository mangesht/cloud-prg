flop:flop.c
	gcc -o flop flop.c -lrt

gflops:gflops.c
	gcc -o gflops gflops.c -lrt -lpthread
iops:iops.c
	gcc -o iops iops.c -lrt -lpthread

mem_mark:mem_mark.c strLib.h strLib.h
	gcc -o mem_mark mem_mark.c -lrt -lpthread -lm 

disk_mark:disk_mark.c strLib.h strLib.h 
	gcc -o disk_mark disk_mark.c -lrt -lpthread -lm 

