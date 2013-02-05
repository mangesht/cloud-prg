long str2val(char *str){
    long res = 0 ; 
    int len;
    int k;
    
    len = strlen(str);
    if(len > 255 ){ 
        printf("Big Len \n");
        return -1;
    }
    for(k=0;k<len;k++){
        //        printf("c = %c \t",str[k]);
        res = res * 10 + str[k] - '0';
    }
     printf("Returning res = %ld for %s\n",res,str);
    return res;
}

