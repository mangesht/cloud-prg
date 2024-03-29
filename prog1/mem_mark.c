#include <pthread.h>
#include <stdio.h>
#include <unistd.h>
#include<time.h>
#include<stdlib.h> 
#include <string.h>
#include "strLib.h"
#include "simLibAdv.h"




pthread_mutex_t mutex= PTHREAD_MUTEX_INITIALIZER;
//pthread_barrier_t bar;
#define BILLION 1000000000L
#define MILLION 1000000L
#define NS pow((double)10,(double)-9)
#define MS pow((double)10,(double)-3)
int rand_seq = 0;
int bm = 0;
long long MAX_BLOCK_SIZE = 1000*1000;
long long MEM_ALLOCATED_SIZE = 1000*1000*100;
long long MAX_OPS = 1000*1000*1000;
int NUM_THREADS=1;
long long block_size = 1000 ;

long long max_operations[4];
char * my_memcpy(char *dest,char *src,long long size){
    // You dont have to anything just return 
    int i;
    return dest;
}

void *doops(){
    long long i;
    int rc;
    char *mem1;
    char *mem2;
    mem1 = (char *) malloc(MEM_ALLOCATED_SIZE);
    if(mem1 == NULL) {
        printf("Error memory allocation failed \n");
        //return (void*)NULL;
        return NULL;
    }
    mem2 = (char *) malloc(MEM_ALLOCATED_SIZE);
    if(mem2 == NULL) {
        printf("Error memory allocation failed \n");
        return NULL;
        //return (void*)NULL;
    }
    
    
    rc = pthread_mutex_lock(&mutex);
    rc = pthread_mutex_unlock(&mutex);
    long long locn=0;
    if(bm==1){
        for(i=0;i<MAX_OPS;i++){
            if(rand_seq == 0 ) { 
                locn=(locn+block_size)%(MEM_ALLOCATED_SIZE-block_size);
            } else { 
                locn = rand_d() *(MEM_ALLOCATED_SIZE-block_size); 
            }
            memcpy(mem1+locn,mem2+locn,block_size);
            //mem2[locn] = mem1[locn];
        }
    }else{
        for(i=0;i<MAX_OPS;i++){
            if(rand_seq == 0 ) { 
                locn=(locn+block_size)%(MEM_ALLOCATED_SIZE-block_size);
            } else { 
                locn = rand_d() *(MEM_ALLOCATED_SIZE-block_size); 
                //printf("Location = %ld \n",locn);
            }
            my_memcpy(mem1,mem2,block_size);
        }
    }
    free (mem1);
    free (mem2);
    return NULL;
} 
void display_help(){
    
    printf("Usage: mem_mark [-i num_iterations] [-m max_transactions] [-s] [-r] [-h] [-m1 max_transactions_1] [-m2 max_transactions_2] [-m3 max_transactions_3]\n");
    printf("\n");
    printf(" num_iterations -  number of iterations to be carried for each memory transaction set. A set consists of max_transactions accesses. \n");
    printf("Default value is 5. \n");
    printf("\n");
    printf("max_transactions -  number of memory accesses to be performed in an iteration for 1B block transfer. The 1KB  and 1MB block sizes the max_transactions are reduced to 1 percent. This is done to keep the run time for each block size comparable.  \n");
    printf("Default value is 1000000000 (1G) \n");
    printf("\n");
    printf("-s - The type of memory accesses as sequential. \n");
    printf("-r - Selects the type of memory accesses as random.\n");
    printf("-h - Displays help for the program \n");
    printf("\n");
    printf("Example : \n");
    printf("./mem_mark -m1  1000000000  m2 190900 -i 25 -r \n");
    printf("./mem_mark -s -m1 500000000 -i 50 \n");
    
}

struct run_info_s{
    double t_full; // Time required for complete run 
    double t_oh;  // Time required for overhead computations/processing 
    double t_op;  // Time required for actual memory copy 
    double throughput; // in Megabits/sec computation
    int num_threads;
    double max_throughput;
    double min_throughput;
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
    long long blk_size[3] = { 1, 1000,1000*1000};
    double thrpt_info[3*2];
    
    max_operations[0] = 1000*1000*10;
    max_operations[1] = 1000*1000;
    max_operations[2] = 1000*100;
    max_operations[3] = 8;
    p = (char *) malloc(256);
    num_fop = 16;
    randomize_seed(-1); 
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
                }
            } else if(p[1]=='m'){
                if(p[2] == '1'){
                    max_operations[0] = str2val(argv[agcCount+1]);
                }else if (p[2] == '2'){
                    max_operations[1] = str2val(argv[agcCount+1]);
                }else if (p[2] == '3'){
                    max_operations[2] = str2val(argv[agcCount+1]);
                }else if (p[2] == '4'){
                    max_operations[3] = str2val(argv[agcCount+1]);
                }
                agcCount++;
            }else if(strchr(p,'r')!=NULL){
                rand_seq = 1 ; 
            }else if(strchr(p,'s')!=NULL){
                rand_seq = 0 ; 
            }else if(p[1] == 'h'){
                display_help();
                return 0;
            }
        }
        agcCount++;
    }
    int tidx;
    int blk_idx;
    for(blk_idx = 0 ; blk_idx<3;blk_idx++) {    
        // Loop over all the block sizes one by one 
        block_size = blk_size[blk_idx];
        MAX_OPS = max_operations[blk_idx];
        printf("---------------------------------------------------------------------\n");
        printf("Checking for block size = %lld Number of transfers = %lld \n",block_size,MAX_OPS);
        printf("---------------------------------------------------------------------\n");
        for(tidx=0;tidx<4;tidx++) { 
            run_info[tidx].t_full = 0; // Time required for complete run 
            run_info[tidx].t_oh = 0 ;  // Time required for overhead computations/processing 
            run_info[tidx].t_op= 0;  // Time required for actual float computations  
            run_info[tidx].throughput= 0; // Gflops computation
            run_info[tidx].num_threads = 0 ;
            run_info[tidx].max_throughput = 0 ;
            run_info[tidx].min_throughput = 0 ;
            
        }
        
        for(NUM_THREADS=1,tidx=0;NUM_THREADS<=2;NUM_THREADS*=2,tidx++){
            printf("Evaluating for num of threads = %d \n",NUM_THREADS);
            
            printf("Total Time  overhead time  Operation Time    Throughput \n");
            printf("  (sec)         (sec)          (sec)            MB/s    \n");
            for(ite_num=0;ite_num<NUM_ITERATIONS;ite_num++){
                rc = pthread_mutex_lock(&mutex);
                
                bm = 1 ; 
                // Create threads and ask them  do run flaot operations
                for(i=0;i<NUM_THREADS;i++){
                    rc = pthread_create(&thread[i],NULL,doops,NULL);
                }
                sleep(1);
                // unlock all the threads 
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
                
                bm_accum = (stop.tv_sec - start.tv_sec) 
                + (double)(stop.tv_nsec - start.tv_nsec) / BILLION; 
                
                // Calculate over heads 
                bm = 0 ; 
                rc = pthread_mutex_lock(&mutex);
                for(i=0;i<NUM_THREADS;i++){
                    
                    rc = pthread_create(&thread[i],NULL,doops,NULL);
                }
                sleep(1);
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
                double bips;
                double throughput;
                bips = ((double)MAX_OPS * block_size * NUM_THREADS) / accum ; 
                throughput = bips/MILLION;
                printf(" %4f     %4f       %4f       %4f \n",bm_accum,oh_accum,accum,throughput);
                
                run_info[tidx].t_full+=bm_accum;
                run_info[tidx].t_oh+=oh_accum;
                run_info[tidx].t_op+=accum;
                run_info[tidx].throughput+=throughput;
                run_info[tidx].num_threads = NUM_THREADS;
                if(ite_num == 0){
                    run_info[tidx].max_throughput = throughput;
                    run_info[tidx].min_throughput = throughput;
                }else{
                    if(run_info[tidx].max_throughput < throughput) run_info[tidx].max_throughput = throughput ;
                    if(run_info[tidx].min_throughput > throughput) run_info[tidx].min_throughput = throughput ;
                    
                }
            }
            // for average divide it by number of iterations 
            run_info[tidx].t_full/=NUM_ITERATIONS;
            run_info[tidx].t_oh/=NUM_ITERATIONS;
            run_info[tidx].t_op/=NUM_ITERATIONS;
            run_info[tidx].throughput/=NUM_ITERATIONS;
            //    run_info[tidx].num_threads = NUM_THREADS;
            
        }
        printf("----------------------------------------------\n");
        printf("    Summary : Mean parameters\n");
        printf("----------------------------------------------\n");
        printf("Num of    Block Size Operation Time Throughput  Throughput  Throughput \n");
        printf("Threads     Bytes      (sec)         (mean)      Max        Min \n");
        for(tidx=0;tidx<2;tidx++){ 
            printf("%d      %9ld       %3.4f           %3.4f     %3.4f     %3.4f\n",run_info[tidx].num_threads,block_size,run_info[tidx].t_op,run_info[tidx].throughput,run_info[tidx].max_throughput,run_info[tidx].min_throughput);
            thrpt_info[tidx*3+blk_idx] = run_info[tidx].throughput;
        }
        MAX_OPS /= 10 ;     
    }
    
    printf("-------------------------------------------------------------------------------\n");
    printf("    Final Summary :  for %s acess \n",rand_seq == 1 ? "Random " : "Sequential ");
    printf("-------------------------------------------------------------------------------\n");
    printf("NumThreads\\ BlockSize       1B          1KB           1MB\n");
    printf("-------------------------------------------------------------------------------\n");
    for(tidx=0;tidx < 2 ;tidx++){
        printf("%15d          ",tidx+1);
        for(blk_idx=0;blk_idx<3;blk_idx++){ 
            printf("%5.4f      ",thrpt_info[tidx*3+blk_idx]);
        }
        printf("\n");
    }
    double latency;
    latency = (1 / (thrpt_info[0]*MILLION));
    if(rand_seq == 1) { 
        printf("\nLatency for Main Memory = %3.4f ns \n",latency / NS );
    }else{ 
        printf("\nLatency for Cache Memory = %3.4f ns \n",latency / NS );
    }
    
    return 0;
}
