/*
 * man.c
 *
 *  Created on: 21-Jan-2013
 *      Author: mangesh
 */
#include <pthread.h>
#include <stdio.h>
#include <unistd.h>
#include<time.h>
#include<stdlib.h> 




pthread_mutex_t mutex= PTHREAD_MUTEX_INITIALIZER;
//pthread_barrier_t bar;
#define NUM_THREADS 15
#define BILLION 1000000000L

float k = 1000*1000*10;
void *doops(){
	int rc;
	float i;
	float a,b,c;
	a=1.1;b=0.9;c=1;
	rc = pthread_mutex_lock(&mutex);
	rc = pthread_mutex_unlock(&mutex);
	for(i=0;i<k;i++){
		c = a * b ;
	}
//	pthread_barrier_wait(&bar);
	//printf("Thread done\n");
	return NULL;
}

int main(){
	//printf("Hello World");
	pthread_t thread[NUM_THREADS];
	int i;
//	pthread_barrier_init(&bar,NULL,NUM_THREADS);
	int rc =0;
    struct timespec start,stop;
    double accum;
    int num_fop;
    num_fop = 3;

	rc = pthread_mutex_lock(&mutex);
    
	for(i=0;i<NUM_THREADS;i++){
		rc = pthread_create(&thread[i],NULL,doops,NULL);
	}
	sleep(2);
	pthread_mutex_unlock(&mutex);
    if(clock_gettime(CLOCK_REALTIME,&start) == -1) {
        perror("clock_gettime");
        exit(EXIT_FAILURE);
    }

	for(i=0;i<NUM_THREADS;i++){
		rc = pthread_join(thread[i],NULL);
	}
	//printf("All threads done\n");
    if(clock_gettime(CLOCK_REALTIME,&stop) == -1) {
        perror("clock_gettime");
        exit(EXIT_FAILURE);
    }

    accum = (stop.tv_sec - start.tv_sec) 
            + (double)(stop.tv_nsec - start.tv_nsec) / BILLION; 
    double flops;
    flops = (k * num_fop * NUM_THREADS) / accum ; 

    printf("Time taken = %f \n",accum);
    printf("benchmark flops = %f Gflops = %f \n",flops,flops/BILLION);

	return 0;
}
