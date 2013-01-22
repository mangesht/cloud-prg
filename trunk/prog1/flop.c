#include<stdio.h>
#include<time.h>
#include<unistd.h>
#include<stdlib.h> 



#define BILLION 1000000000L
int main(){
    long i;
    double a;
    double b;
    double c;
    struct timespec start,stop;
    time_t cur_t;
    struct tm* loc_s;
    struct tm* loc_e;
    double accum; 
    a = 1.1;
    b = 1.1;
    c = 1;
    long k=1000*1000*10;
    long j;
   // cur_t = time(NULL);
   // loc_s = localtime(&cur_t);
   // for(j=0;j<10;j++)
    if(clock_gettime(CLOCK_REALTIME,&start) == -1) {
        perror("clock_gettime");
        exit(EXIT_FAILURE);
    }
    for(i=0;i<k;i++){
        //c = c + b ;
        //c = a * b ;
        c = c * a + b ;
    }
   // cur_t = time(NULL);
   // loc_e = localtime(&cur_t);
     if(clock_gettime(CLOCK_REALTIME,&stop) == -1) {
        perror("clock_gettime");
        exit(EXIT_FAILURE);
    }
    accum = (stop.tv_sec - start.tv_sec) 
            + (double)(stop.tv_nsec - start.tv_nsec) / BILLION; 
    accum /= 2;
    //printf("ns = %ld \n", (stop.tv_nsec - start.tv_nsec));
    printf("Time taken by program = %f \n",accum);
    printf("Bench mark Flops = %lf Gflops = %lf \n",k/accum,(k/accum)/BILLION);
    return 0;
}
