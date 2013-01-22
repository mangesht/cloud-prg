#include<stdio.h>
#include<stdlib.h>
#include<pthread.h>


extern int sema;
    float i;
    double a;
    double b;
    double c;
    struct timespec start,stop;
    time_t cur_t;
    struct tm* loc_s;
    struct tm* loc_e;
    double accum; 
    double accum_wofl; 
    double accum_wfl; 
    float k=1000*1000*10;
    long j;
int float_op_en = 1;
#define NUM_THREADS 10
#define BILLION 1000000000L


void split();
int thread_no = 0;
int main(){ 
    int pid;
    int sema; 
    int status;
    int num_fop = 3;
    sema = 0; 
    a = 1.1;
    b = 1.1;
    c = 1;
/*
    pid = fork();
    if(pid== 0) { 
        // Child process 
        printf("Child started \n");
        thread_no++;
        split();
        //while(sema==0);
        printf("Child exited \n");

    } else { 
        // Parent process 
        int i;
        for( i=0;i<100;i++){
            printf(".");
        }
        printf("Sema 1 \n");
            sema = 1;
        waitpid(pid,&status,0); 
        printf("All Done");
    } 
  
*/

    thread_no = 0 ; 
    if(clock_gettime(CLOCK_REALTIME,&start) == -1) {
        perror("clock_gettime");
        exit(EXIT_FAILURE);
    }
    split();  
    if(clock_gettime(CLOCK_REALTIME,&stop) == -1) {
        perror("clock_gettime");
        exit(EXIT_FAILURE);
    }
    accum_wfl = (stop.tv_sec - start.tv_sec) 
            + (double)(stop.tv_nsec - start.tv_nsec) / BILLION; 
 
    thread_no = 0 ;
    float_op_en = 0;
    if(clock_gettime(CLOCK_REALTIME,&start) == -1) {
        perror("clock_gettime");
        exit(EXIT_FAILURE);
    }
    split();  
    if(clock_gettime(CLOCK_REALTIME,&stop) == -1) {
        perror("clock_gettime");
        exit(EXIT_FAILURE);
    }
    accum_wofl = (stop.tv_sec - start.tv_sec) 
            + (double)(stop.tv_nsec - start.tv_nsec) / BILLION; 
 
    double flops;
    accum = accum_wfl - accum_wofl;
    flops = (k * num_fop * NUM_THREADS) / accum ; 

    printf("Time taken = %f \n",accum);
    printf("benchmark flops = %f Gflops = %f \n",flops,flops/BILLION);


    return 0;
}

void split(){
    int p;
    int status;
    thread_no++;
    p = fork();
    if(p==0){
        //printf("Child thread no = %d \n",thread_no);
        if(thread_no < NUM_THREADS)
        split();
        exit(0);
    }else{
       // printf("In thread %d \n",thread_no);
        if(float_op_en == 1) { 
            for(i=0;i<k;i++){
                //c = c + b ;
                c = a * b ;
                //c = c * a + b ;
            }
        }
        waitpid(p,&status,0); 
    }

} 
