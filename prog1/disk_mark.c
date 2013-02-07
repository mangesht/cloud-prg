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
int rd_wr = 0;
int bm = 0;
long long MAX_BLOCK_SIZE = 1000*1000;
long long MEM_ALLOCATED_SIZE = 1000*1000*100;
long long MAX_OPS = 1000*1000*1000;
int NUM_THREADS=1;
long long block_size = 1000 ;
long long fileSize = 2*1000*1000*1000;
unsigned char *outbuf;
long long max_operations[4];
//int wfd;
int wfd_1;
int wfd_2;
int verbose = 1;
int my_write(int dest,unsigned char *src,long int size){
    // You dont have to anything just return 
    int i;
    return dest;
}

void *dowrite(void *wfdp){
    long long i;
    int rc;
    int wfd = *((int *) wfdp); 
    rc = pthread_mutex_lock(&mutex);
    rc = pthread_mutex_unlock(&mutex);
    long long locn=1;
    long long  read_bytes;
    //printf("File handler = %d \n",wfd);
    if(bm==1){
        for(i=0;i<MAX_OPS;i++){
            if(rand_seq == 0 ) { 
                locn=(locn + block_size)%(MEM_ALLOCATED_SIZE-block_size);
            } else { 
                locn = rand_d() *(fileSize-block_size); 
                lseek(wfd,locn,SEEK_SET);
            }
            if(rd_wr == 1) { 
                read_bytes =  read(wfd,outbuf,block_size);
                if(read_bytes != block_size) {
                    printf("ERROR : read_bytes = %lld block_size = %lld \n",read_bytes,block_size);
                    printf("Location = %lld \n",locn);
                    exit(-1);
                }
            }else{ 
                write(wfd,outbuf,block_size);
            }
        }
    }else{
        // dry run 
        for(i=0;i<MAX_OPS;i++){
            if(rand_seq == 0 ) { 
                locn=(i%(MEM_ALLOCATED_SIZE-block_size));
            } else { 
                locn = rand_d() *(MEM_ALLOCATED_SIZE-block_size); 
            }
            if(rd_wr == 1) { 
                my_write(wfd,outbuf,block_size);
                if(read_bytes != block_size) {
                    // Do dummy something 
                }
            }else{ 
                my_write(wfd,outbuf,block_size);
            }
        }
    }
    return NULL;
} 
void display_help(){
    printf("Usage: disk_mark [-s] [-r] [-h]  [-i num_iterations] [-fs file_size] [-fname1 filename1] [ -fname2 filename2] [-m1 max_transactions_1] [-m2 max_transactions_2] [-m3 max_transactions_3] [-m4 max_transactions_4] \n");
    printf("\n");
    printf(" num_iterations - The number of iterations to be carried for each memory transaction set. A set consists of max_transactions accesses. \n");
    printf("Default value is 5\n");
    printf("\n");
    printf("\t-s - Selects the type of disk accesses as sequential. \n");
    printf("\t-r - Selects the type of disk accesses as random.\n");
    printf("\t-h - Displays help for the program \n");
    printf("\n");
    printf("\tfilesize- This indicates the size of the file in bytes to be used in program. The program creates the files of this size for read / writes operations. \n");
    printf("Default value is 2000000000 (2G) \n");
    printf("\n");
    printf("\tfilename1/filename2: When the user does not want program to create the file, user can provide path of existing file. The program performs benchmarking operations on the user defined files. Filename1 is used one of the threads, while filename2 is used by the other thread.\n");
    printf("When user defines the filenames, option -fs filesize is ignored. Using this option saves 2 large files creation time. \n");
    printf("\n");
    printf("\tmax_transactions1 - The number of memory accesses to be performed in an iteration for 1B block transfer. \n");
    printf("Default value is 10000000 (10M)\n");
    printf("\n");
    printf("\tmax_transactions2 - The number of memory accesses to be performed in an iteration for 1KB block transfer.\n");
    printf("Default value is 100000 (100K) \n");
    printf("\n");
    printf("\tmax_transactions3 - The number of memory accesses to be performed in an iteration for 1MB block transfer. \n");
    printf("Default value is 1000 \n");
    printf("\n");
    printf("\tmax_transactions4 - The number of memory accesses to be performed in an iteration for 1B block transfer. \n");
    printf("Default value is 8\n");
    printf("\n");
    printf("The maximum transactions options can be used by user to keep the time taken for transfers almost same. \n");
    printf("\tExample : \n");
    printf("./disk_mark -s -i 10 -fs 8000000000 -m1 1000000 -m2 100000 -m3 1000 -m4 4 \n");
    printf("\n");
    printf("./disk_mark -r -i 10 -fname1 bigFile1.txt -fname2 bigFile2.txt  \n");
    
    
    
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
    long long blk_size[4] = { 1, 1000,1000*1000,1000*1000*1000};
    double thrpt_info[4*2];
    int ofd;
    int do_not_creat_file = 0;
    char ofname_1[] = "bigFile_1.scr";
    char ofname_2[] = "bigFile_2.scr";
    struct stat sb;
    max_operations[0] = 1000*1000*10;
    max_operations[1] = 1000*100;
    max_operations[2] = 1000*1;
    max_operations[3] = 8;
    p = (char *) malloc(256);
    num_fop = 16;
    randomize_seed(-1); 
    
    // Routine for getting input arguments  
    while(agcCount < argc){ 
        strcpy(p , argv[agcCount]);
        if(p[0] == '-'){ 
            if(p[1]=='i'){
                NUM_ITERATIONS = str2val(argv[agcCount+1]);
                agcCount++;
            } else if(p[1]=='v'){
                //verbose mode 
                verbose = 1;
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
            } else if(strstr(p,"fs")!=NULL){
                // fileSize in GB
                //printf("\t found %s \n",strstr(p,"fs"));
                fileSize = str2val(argv[agcCount+1]);
                //fileSize = fileSize * BILLION;
                agcCount++;
            }else if(strstr(p,"fname1")!=NULL){
                strcpy(ofname_1,argv[agcCount+1]);
                do_not_creat_file++ ;
                agcCount++;
            }else if(strstr(p,"fname2")!=NULL){
                strcpy(ofname_2,argv[agcCount+1]);
                do_not_creat_file++ ;
                agcCount++;
            }else if(p[1]=='r'){
                rand_seq = 1 ; 
            }else if(p[1]=='s'){
                rand_seq = 0 ; 
            }else if(p[1]=='h'){
                display_help();
                return 0;
            }
            
        }
        agcCount++;
    }
    
    printf("\n\nRunning test with \n");
    printf("Number of iterations = %d \t",NUM_ITERATIONS);
    printf("Filename = %s-----\n",ofname_1);
    printf("Filename = %s-----\n",ofname_2);
    printf("Filesize = %2.2f GB \t",(double)fileSize/BILLION);
    printf("\tAccessType = %s \n",rand_seq == 1 ? "Random " : "Sequential ");
    printf("----------------------------------------------------------------\n");
    int tidx;
    int blk_idx;
    
    // Create sufficiently big file 
    if(do_not_creat_file < 2 ) { 
        long long  nblocks ;
        block_size = 4096; 
        nblocks = fileSize / block_size ;
        wfd_1 = open(ofname_1,O_WRONLY | O_CREAT | O_TRUNC , 0644);
        if(wfd_1 <= 0) {
            printf("Error opening file %s \n",ofname_1);
            return -1;
        }
        outbuf = (unsigned char *) malloc(sizeof(unsigned char) * block_size );
        if(outbuf == NULL) {
            printf("Memory allocation failed for outbuf \n");
            perror("outbuf_alloc");
        }
        for(blk_idx =0;blk_idx < nblocks;blk_idx++) {
            write(wfd_1,outbuf,block_size);
        }
        close(wfd_1);
        // Create second file 
        wfd_2 = open(ofname_2,O_WRONLY | O_CREAT | O_TRUNC , 0644);
        if(wfd_2 <= 0) {
            printf("Error opening file \n");
            return -1;
        }
        outbuf = (unsigned char *) malloc(sizeof(unsigned char) * block_size );
        if(outbuf == NULL) {
            printf("Memory allocation failed for outbuf \n");
            perror("outbuf_alloc");
        }
        for(blk_idx =0;blk_idx < nblocks;blk_idx++) {
            write(wfd_2,outbuf,block_size);
        }
        close(wfd_2);
        
        if(verbose) printf("File created  %s \n",ofname_2);
        // File creation done 
    }else{
        wfd_1 = open(ofname_1,O_RDONLY , 0644);
        if(wfd_1 <= 0) { 
            printf("Error opening file %s \n",ofname_1);
            return -1;
        }else{
            fstat(wfd_1,&sb);
            fileSize = sb.st_size;
            close(wfd_1);
        }
        
        if(verbose) printf("File not created. Using existing file %s \n",ofname_1);
    }
    for(rd_wr = 0 ; rd_wr < 2 ; rd_wr++) {   
        for(blk_idx = 0 ; blk_idx<4;blk_idx++) {    
            // Loop over all the block sizes one by one 
            block_size = blk_size[blk_idx];
            MAX_OPS = max_operations[blk_idx];
            outbuf = (unsigned char *) malloc(sizeof(unsigned char) * block_size );
            if(verbose) printf("---------------------------------------------------------------------\n");
            if(verbose) printf("Checking for block size = %lld Number of transfers = %lld \n",block_size,MAX_OPS);
            if(verbose) printf("---------------------------------------------------------------------\n");
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
                if(verbose) printf("Evaluating for num of threads = %d \n",NUM_THREADS);
                
                if(verbose) printf("Total Time  overhead time  Operation Time    Throughput \n");
                if(verbose) printf("  (sec)         (sec)          (sec)            MB/s    \n");
                for(ite_num=0;ite_num<NUM_ITERATIONS;ite_num++){
                    rc = pthread_mutex_lock(&mutex);
                    wfd_1 = open(ofname_1,O_RDWR);
                    if(wfd_1 < 0){
                        printf("Could not open file %s \n",ofname_1);
                        perror("wfd_open");
                        return -1;
                    }
                    wfd_2 = open(ofname_2,O_RDWR);
                    if(wfd_2 < 0){
                        printf("Could not open file %s \n",ofname_2);
                        perror("wfd_open");
                        return -1;
                    }
                    
                    bm = 1 ; 
                    // Create threads and ask them  do run flaot operations
                    for(i=0;i<NUM_THREADS;i++){
                        if(i==0) 
                        rc = pthread_create(&thread[i],NULL,dowrite,(void *) &wfd_1); // Change this Mangesh 
                        else 
                        rc = pthread_create(&thread[i],NULL,dowrite,(void *) &wfd_2); // Change this Mangesh 
                    }
                    //sleep(1);
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
                        if(i==0) 
                        rc = pthread_create(&thread[i],NULL,dowrite,(void *)&wfd_1);
                        else
                        rc = pthread_create(&thread[i],NULL,dowrite,(void *)&wfd_2);
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
                    double bips;
                    double throughput;
                    bips = ((double)MAX_OPS * block_size * NUM_THREADS) / accum ; 
                    throughput = bips/MILLION;
                    if(verbose) printf(" %4f     %4f       %4f       %4f \n",bm_accum,oh_accum,accum,throughput);
                    
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
                    close(wfd_1);
                    close(wfd_2);
                }
                // for average divide it by number of iterations 
                run_info[tidx].t_full/=NUM_ITERATIONS;
                run_info[tidx].t_oh/=NUM_ITERATIONS;
                run_info[tidx].t_op/=NUM_ITERATIONS;
                run_info[tidx].throughput/=NUM_ITERATIONS;
                //    run_info[tidx].num_threads = NUM_THREADS;
                
            }
            if(verbose) printf("----------------------------------------------\n");
            if(verbose) printf("    Summary : Mean parameters\n");
            if(verbose) printf("----------------------------------------------\n");
            if(verbose) printf("Num of    Block Size Operation Time Throughput  Throughput  Throughput \n");
            if(verbose) printf("Threads     Bytes      (sec)         (mean)      Max        Min \n");
            for(tidx=0;tidx<2;tidx++){ 
                if(verbose) printf("%d      %9lld       %3.4f           %3.4f     %3.4f     %3.4f\n",run_info[tidx].num_threads,block_size,run_info[tidx].t_op,run_info[tidx].throughput,run_info[tidx].max_throughput,run_info[tidx].min_throughput);
                thrpt_info[tidx*4+blk_idx] = run_info[tidx].throughput;
            }
            MAX_OPS /= 10 ;     
            free(outbuf);
        }
        
        printf("\n-------------------------------------------------------------------------------\n");
        printf(" %s Throughput Summary :  for %s acess \n",rd_wr == 1 ? "Read " : "Write",rand_seq == 1 ? "Random " : "Sequential ");
        printf("-------------------------------------------------------------------------------\n");
        printf("NumThreads\\ BlockSize       1B          1KB           1MB            1GB\n");
        printf("-------------------------------------------------------------------------------\n");
        for(tidx=0;tidx < 2 ;tidx++){
            printf("%15d          ",tidx+1);
            for(blk_idx=0;blk_idx<4;blk_idx++){ 
                printf("%5.4f      ",thrpt_info[tidx*4+blk_idx]);
            }
            printf("\n");
        }
        double latency;
        latency = (1 / (thrpt_info[0]*MILLION));
        if(rand_seq == 1) { 
            printf("\nLatency for Disk (random read) = %3.6f ms \n",latency / MS );
        }else{ 
            printf("\nLatency for Disk-Main Memory (sequential %s ) = %3.6f ms \n",rd_wr == 1 ? "Read" : "Write", latency / MS  );
        }
    }    
    return 0;
}

