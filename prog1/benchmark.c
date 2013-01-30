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
#define NUM_THREADS 16
#define BILLION 1000000000L

int bm = 0;

float k = 1000*1000*10;
void *doops(){
	int rc;
	float i;
	float a,b,c;
    float c0,c1,c2,c3,c4,c5,c6,c7,c8,c9;
    float m,n,o;
	a=1.1;b=0.9;c=1;
	rc = pthread_mutex_lock(&mutex);
	rc = pthread_mutex_unlock(&mutex);
  if(bm==1){
	for(i=0;i<k;i++){
		c0 = i * b ;
        c1 = i + a ;
		c2 = i * b ;
        c3 = i + a ;
	    c4 = i * b ;
        c5 = i + a ;
		c6 = i * b ;
        c7 = i + a ;
		c8 = i * b ;
        c9 = i + a ;
		c0 = i * b ;
        c1 = i + a ;
		c2 = i * b ;
        c3 = i + a ;
	    c4 = i * b ;
        c5 = i + a ;
		c6 = i * b ;
        c7 = i + a ;
		c8 = i * b ;
        c9 = i + a ;
		c0 = i * b ;
        c1 = i + a ;
		c2 = i * b ;
        c3 = i + a ;
	    c4 = i * b ;
        c5 = i + a ;
		c6 = i * b ;
        c7 = i + a ;
		c8 = i * b ;
        c9 = i + a ;
		c0 = i * b ;
        c1 = i + a ;
		c2 = i * b ;
        c3 = i + a ;
	    c4 = i * b ;
        c5 = i + a ;
		c6 = i * b ;
        c7 = i + a ;
		c8 = i * b ;
        c9 = i + a ;
	}
}else{
	for(i=0;i<k;i++){
		c0 = i  ;
        c1 = i  ;
		c2 = i  ;
        c3 = i  ;
	    c4 = i  ;
        c5 = i  ;
		c6 = i  ;
        c7 = i  ;
		c8 = i  ;
        c9 = i  ;
		c0 = i  ;
        c1 = i  ;
		c2 = i  ;
        c3 = i  ;
	    c4 = i  ;
        c5 = i  ;
		c6 = i  ;
        c7 = i  ;
		c8 = i  ;
        c9 = i  ;
		c0 = i  ;
        c1 = i  ;
		c2 = i  ;
        c3 = i  ;
	    c4 = i  ;
        c5 = i  ;
		c6 = i  ;
        c7 = i  ;
		c8 = i  ;
        c9 = i  ;
		c0 = i  ;
        c1 = i  ;
		c2 = i  ;
        c3 = i  ;
	    c4 = i  ;
        c5 = i  ;
		c6 = i  ;
        c7 = i  ;
		c8 = i  ;
        c9 = i  ;
	}

}
	return NULL;
}

int main(){
	pthread_t thread[NUM_THREADS];
	int i;
	int rc =0;
    struct timespec start,stop;
    double accum;
    double bm_accum;
    double oh_accum;
    int num_fop;
    num_fop = 40;

	rc = pthread_mutex_lock(&mutex);
    
    bm = 1 ; 
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

    bm_accum = (stop.tv_sec - start.tv_sec) 
            + (double)(stop.tv_nsec - start.tv_nsec) / BILLION; 

    // Calculate over heads 
    bm = 0 ; 
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

    oh_accum = (stop.tv_sec - start.tv_sec) 
            + (double)(stop.tv_nsec - start.tv_nsec) / BILLION; 

    accum = bm_accum - oh_accum;
    double flops;
    flops = (k * num_fop * NUM_THREADS) / accum ; 

    printf("Time float %f Time overhead %f Time taken = %f seconds\n",bm_accum,oh_accum,accum);
    printf("benchmark : flops = %f Gflops = %f \n",flops,flops/BILLION);

	return 0;
}
