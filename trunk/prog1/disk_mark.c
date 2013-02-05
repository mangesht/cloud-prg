#include <pthread.h>
#include <stdio.h>
#include <unistd.h>
#include<time.h>
#include<stdlib.h> 
#include <string.h>
#include<sys/stat.h>
#include<fcntl.h>
#include<errno.h>
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
double MAX_OPS = 1000*1000*1000;
int NUM_THREADS=1;
long block_size = 1000 ;
long long fileSize = 2*1000*1000*1000;
unsigned char *outbuf;
int wfd;

int my_write(int dest,unsigned char *src,long int size){
    // You dont have to anything just return 
    int i;
    return dest;
}

void *dowrite(){
    long i;
    int rc;
     
    rc = pthread_mutex_lock(&mutex);
    rc = pthread_mutex_unlock(&mutex);
    long int locn=1;
    register a;
    if(bm==1){
        for(i=0;i<MAX_OPS;i++){
            if(rand_seq == 0 ) { 
                locn=(locn + block_size)%(MEM_ALLOCATED_SIZE-block_size);
            } else { 
                locn = rand_d() *(fileSize-block_size); 
                lseek(wfd,locn,SEEK_SET);
            }
            write(wfd,outbuf,block_size);
        }
    }else{
        // dry run 
        for(i=0;i<MAX_OPS;i++){
            if(rand_seq == 0 ) { 
                locn=(i%(MEM_ALLOCATED_SIZE-block_size));
            } else { 
                locn = rand_d() *(MEM_ALLOCATED_SIZE-block_size); 
            }
            my_write(wfd,outbuf,block_size);
        }
    }
    return NULL;
} 
void display_help(){
    printf("\n Usage mem_mark [-i iteration_num] [-m max_trans] \n");
    printf("iteration_num -> Number of iterations the operation needs to repeat. Default is 5 \n");
    printf("max_trans     -> Maximum number of transfers in each iteration. Default is 1000000000 (1 BILLION) \n");
    
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
    long blk_size[4] = { 1, 1000,1000*1000,1000*1000*1000};
    double thrpt_info[3*2];
    int ofd;
    int do_not_creat_file = 0;
    char ofname[] = "bigFile.scr";
    struct stat sb;
    
    p = (char *) malloc(256);
        num_fop = 16;
    randomize_seed(-1); 


    while(agcCount < argc){ 
        strcpy(p , argv[agcCount]);
        printf("Processing %s \n",p);
        if(p[0] == '-'){ 
            if(p[1]=='i'){
                NUM_ITERATIONS = str2val(argv[agcCount+1]);
                agcCount++;
            } else if(p[1]=='m'){
                MAX_OPS = str2val(argv[agcCount+1]);
                agcCount++;
            } else if(strstr(p,"fs")!=NULL){
                // fileSize in GB
                //printf("\t found %s \n",strstr(p,"fs"));
                fileSize = str2val(argv[agcCount+1]);
                //fileSize = fileSize * BILLION;
                agcCount++;
            }else if(strstr(p,"fname")!=NULL){
                strcpy(ofname,argv[agcCount+1]);
                do_not_creat_file = 1 ;
                agcCount++;
            }else if(p[1]=='r'){
                rand_seq = 1 ; 
            }else if(p[1]=='s'){
                rand_seq = 0 ; 
            }

        }
        agcCount++;
    }
    
    wfd = open(ofname,O_RDONLY , 0644);
    if(wfd <= 0) { 
        printf("Error opening file \n");
        return -1;
    }else{
        fstat(wfd,&sb);
        fileSize = sb.st_size;
        close(wfd);
    }
    printf("\n\nRunning test with \n");
    printf("Number of iterations = %d \t",NUM_ITERATIONS);
    printf("Filename = %s-----\n",ofname);
    printf("Filesize = %2.2f GB \t",(double)fileSize/BILLION);
    printf("AccessType = %s \n",rand_seq == 1 ? "Random " : "Sequential ");
    printf("----------------------------------------------------------------\n");
    int tidx;
    int blk_idx;

    // Create sufficiently big file 
    if(do_not_creat_file == 0) { 
    block_size = 4096; 
    long int nblocks = fileSize / block_size ;
    wfd = open(ofname,O_WRONLY | O_CREAT | O_TRUNC , 0644);
    if(wfd <= 0) {
        printf("Error opening file \n");
        return -1;
    }
    outbuf = (unsigned char *) malloc(sizeof(unsigned char) * block_size );
    for(blk_idx =0;blk_idx < nblocks;blk_idx++) {
        write(wfd,outbuf,block_size);
    }
    close(wfd);
    printf("File created  %s \n",ofname);
    // File creation done 
    }else{
         printf("File not created using existing file %s \n",ofname);
    }

    for(blk_idx = 0 ; blk_idx<4;blk_idx++) {    
        // Loop over all the block sizes one by one 
        block_size = blk_size[blk_idx];
        outbuf = (unsigned char *) malloc(sizeof(unsigned char) * block_size );
        printf("---------------------------------------------------------------------\n");
        printf("Checking for block size = %ld Number of transfers = %lf \n",block_size,MAX_OPS);
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
                wfd = open(ofname,O_WRONLY);
                if(wfd < 0){
                    printf("Could not open file %s \n",ofname);
                    perror("wfd_open");
                    return -1;
                }
                bm = 1 ; 
                // Create threads and ask them  do run flaot operations
                for(i=0;i<NUM_THREADS;i++){
                    rc = pthread_create(&thread[i],NULL,dowrite,NULL);
                }
                //sleep(1);
                // unlock all the threads 
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
                rc = pthread_mutex_lock(&mutex);
                for(i=0;i<NUM_THREADS;i++){
                    
                    rc = pthread_create(&thread[i],NULL,dowrite,NULL);
                }
                //sleep(1);
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
                close(wfd);
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
        free(outbuf);
    }

        printf("\n-------------------------------------------------------------------------------\n");
        printf("    Final Summary :  for %s acess \n",rand_seq == 1 ? "Random " : "Sequential ");
        printf("-------------------------------------------------------------------------------\n");
        printf("NumThreads\\ BlockSize       1B          1KB           1MB            1GB\n");
        printf("-------------------------------------------------------------------------------\n");
        for(tidx=0;tidx < 2 ;tidx++){
            printf("%15d          ",tidx+1);
            for(blk_idx=0;blk_idx<4;blk_idx++){ 
                printf("%5.4f      ",thrpt_info[tidx*3+blk_idx]);
            }
            printf("\n");
        }
        double latency;
        latency = (1 / (thrpt_info[0]*MILLION));
        if(rand_seq == 1) { 
            printf("\nLatency for Disk (random read) = %3.4f ms \n",latency / MS );
        }else{ 
            printf("\nLatency for Disk-Main Memory (sequential read ) = %3.4f ms \n",latency / MS );
        }

    return 0;
}

