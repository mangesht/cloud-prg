long long str2val(char *str){
    long long  res = 0 ; 
    int len;
    int k;
    
    len = strlen(str);
    if(len > 255 ){ 
        printf("Big Len \n");
        return -1;
    }
    for(k=0;k<len;k++){
       // printf("c = %c %d \t",str[k],str[k] - '0');
        res = res * 10 + str[k] - '0';
       // printf("res = %lf \n",(double)res);
    }
    //printf("Returning res = %lld for %s\n",res,str);
    return res;
}

