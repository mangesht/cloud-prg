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
#include <string.h>




pthread_mutex_t mutex= PTHREAD_MUTEX_INITIALIZER;
//pthread_barrier_t bar;
#define BILLION 1000000000L

int bm = 0;

double MAX_OPS = 1000*1000*100;
int NUM_THREADS;
void *doops(){
    int rc;
    double i;
    double a,b,c;
    double c0,c1,c2,c3,c4,c5,c6,c7,c8,c9;
    double m,n,o;
    a=1.1;b=0.9;c=1;
    c1 = 1.8;
    rc = pthread_mutex_lock(&mutex);
    rc = pthread_mutex_unlock(&mutex);
    if(bm==1){
        for(i=0;i<MAX_OPS;i++){
            c0 = i * b ;
            c1 = c0 + i;
            c0 = i * b ;
            c1 = c0 + i;
            c0 = i * b ;
            c1 = c0 + i;
            c0 = i * b ;
            c1 = c0 + i;
            c0 = i * b ;
            c1 = c0 + i;
            c0 = i * b ;
            c1 = c0 + i;
            c0 = i * b ;
            c1 = c0 / i;
            //c1 = i + a ;
            //c2 = i / a ;
        }
    }else{
        for(i=0;i<MAX_OPS;i++){
            c0 = i  ;
            c1 = c0;
            c0 = i  ;
            c1 = c0 ;
            c0 = i  ;
            c1 = c0;
            c0 = i  ;
            c1 = c0 ;
            c0 = i  ;
            c1 = c0;
            c0 = i  ;
            c1 = c0 ;
            c0 = i  ;
            c1 = c0;
            c0 = i  ;
            c1 = c0 ;
        }
        
    }
    return NULL;
}

int str2val(char *str){
    int res = 0 ; 
    int len;
    int k;
    len = strlen(str);
    if(len > 4 ){ 
        printf("Big Len \n");
        return -1;
    }
    for(k=0;k<len;k++){
        //        printf("c = %c \t",str[k]);
        res = res * 10 + str[k] - '0';
    }
    //    printf("Returning res = %d \n",res);
    return res;
}
void display_help(){
    printf("Usage: gflops [-i num_iterations] [-m max_ops] [-h]\n");
    printf("\n");
    printf("num_iterations- The number of iteration to be carried out for a set of max_ops floating point operations set. The default value is 5. \n");
    printf("\n");
    printf("max_ops - number of floating point operation sets to be run in an iteration. One set of floating point operations consists of addition, multiplication and division, total 16 operations. The default value is 100 Million. \n");
    printf("\n");
    printf("Thus total number of operations = num_iterations * max_ops * 16 \n");
    printf("\n");
    printf("-h displays help for the program \n");
    printf(" \n");
    printf("Example : ./gflops -i 20 -m 1000000000\n");
    
    
    
}
struct run_info_s{
    double t_full; // Time required for complete run 
    double t_oh;  // Time required for overhead computations/processing 
    double t_op;  // Time required for actual float computations  
    double gflops; // Gflops computation
    int num_threads;
    double max_gflops;
    double min_gflops;
};
int main(int argc,char *argv[]){
    pthread_t thread[8];
    int i;
    int rc =0;
    struct timespec start,stop;
    double accum;
    double bm_accum;
    double oh_accum;
    int num_fop;
    int NUM_ITERATIONS = 5;
    int ite_num;
    int agcCount =1 ;
    char *p;
    struct run_info_s run_info[4];
    
    p = (char *) malloc(256);
    num_fop = 16;
    
    while(agcCount < argc){ 
        strcpy(p , argv[agcCount]);
        if(p[0] == '-'){ 
            if(strchr(p,'i')!=NULL){
                NUM_ITERATIONS = str2val(argv[agcCount+1]);
                agcCount++;
                if(NUM_ITERATIONS == -1) { 
                    printf("ERROR:Invalid input \n");
                    display_help();
                    return -1;
                } else if(strchr(p,'m')!=NULL){
                    MAX_OPS = str2val(argv[agcCount+1]);
                    agcCount++;
                    if(NUM_ITERATIONS == -1) { 
                        printf("ERROR:Invalid input \n");
                        display_help();
                        return -1;
                    }
                } else if(p[1]=='h'){
                    display_help();
                    return 0;
                }
                
            }
        }
        agcCount++;
    }
    int tidx;
    
    for(tidx=0;tidx<4;tidx++) { 
        run_info[tidx].t_full = 0; // Time required for complete run 
        run_info[tidx].t_oh = 0 ;  // Time required for overhead computations/processing 
        run_info[tidx].t_op= 0;  // Time required for actual float computations  
        run_info[tidx].gflops= 0; // Gflops computation
        run_info[tidx].num_threads = 0 ;
        run_info[tidx].max_gflops = 0 ;
        run_info[tidx].min_gflops = 0 ;
        
    }
    
    for(NUM_THREADS=1,tidx=0;NUM_THREADS<=4;NUM_THREADS*=2,tidx++){
        printf("Evaluating for num_thread = %d \n",NUM_THREADS);
        
        printf("Total Time  overhead time  Operation Time    Gflops \n");
        printf("  (sec)         (sec)          (sec)                \n");
        //printf("benchmark : flops = %f Gflops = %f \n",flops,flops/BILLION);
        for(ite_num=0;ite_num<NUM_ITERATIONS;ite_num++){
            rc = pthread_mutex_lock(&mutex);
            
            bm = 1 ; 
            // Create threads and ask them  do run flaot operations
            for(i=0;i<NUM_THREADS;i++){
                rc = pthread_create(&thread[i],NULL,doops,NULL);
            }
            //sleep(1);
            if(clock_gettime(CLOCK_REALTIME,&start) == -1) {
                perror("clock_gettime");
                exit(EXIT_FAILURE);
            }
            // unlock all the threads 
            pthread_mutex_unlock(&mutex);
            
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
            rc = pthread_mutex_lock(&mutex);
            for(i=0;i<NUM_THREADS;i++){
                
                rc = pthread_create(&thread[i],NULL,doops,NULL);
            }
            //sleep(1);
            if(clock_gettime(CLOCK_REALTIME,&start) == -1) {
                perror("clock_gettime");
                exit(EXIT_FAILURE);
            }
            pthread_mutex_unlock(&mutex);
            
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
            float gflops;
            flops = (MAX_OPS * num_fop * NUM_THREADS) / accum ; 
            gflops = flops/BILLION;
            printf(" %4f     %4f       %4f       %4f \n",bm_accum,oh_accum,accum,gflops);
            
            run_info[tidx].t_full+=bm_accum;
            run_info[tidx].t_oh+=oh_accum;
            run_info[tidx].t_op+=accum;
            run_info[tidx].gflops+=gflops;
            run_info[tidx].num_threads = NUM_THREADS;
            if(ite_num == 0){
                run_info[tidx].max_gflops = gflops;
                run_info[tidx].min_gflops = gflops;
            }else{
                if(run_info[tidx].max_gflops < gflops) run_info[tidx].max_gflops = gflops ;
                if(run_info[tidx].min_gflops > gflops) run_info[tidx].min_gflops = gflops ;
                
            }
        }
        // for average divide it by number of iterations 
        run_info[tidx].t_full/=NUM_ITERATIONS;
        run_info[tidx].t_oh/=NUM_ITERATIONS;
        run_info[tidx].t_op/=NUM_ITERATIONS;
        run_info[tidx].gflops/=NUM_ITERATIONS;
        //    run_info[tidx].num_threads = NUM_THREADS;
        
    }
    printf("----------------------------------------------\n");
    printf("    Summary : Mean parameters\n");
    printf("----------------------------------------------\n");
    printf("Num of    Total Time  overhead time  Operation Time    Gflops    Gflops     Gflops \n");
    printf("Threads    (sec)         (sec)          (sec)         (mean)      Max        Min \n");
    for(tidx=0;tidx<3;tidx++){ 
        printf("%d      %4f     %4f       %4f           %3.4f     %3.4f     %3.4f\n",run_info[tidx].num_threads,run_info[tidx].t_full,run_info[tidx].t_oh,run_info[tidx].t_op,run_info[tidx].gflops,run_info[tidx].max_gflops,run_info[tidx].min_gflops);
    }
    
    
    return 0;
}
