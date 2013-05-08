#!/usr/bin/perl
use strict;

my $td = 1 ;
my $perWorker=80*1000;
my $numWorker=1;
my $i;
my $j;
for ($i=0;$i<5;$i++){
     # For Task duration 1 , 2 4 , 8 Seconds 
	$numWorker = 1 ; 
	for ($j=0;$j<6;$j++){
		# For Num workers from 1 2 4 8 16 32 
		my $numTasks = (80*1000 * $numWorker) / $td ;
	    print "Td = $td NumWOkers = $numWorker tasNumber of tasks $numTasks \n";	
		my $fileName = "tdml_".$td."_Wnum_".$numWorker;
		print "FileName = $fileName \n";
		if(!open (MYFH,">$fileName")) { 
			die "Could not open $fileName \n";
		}
		my $tNum ; 
		my $slpTime = $td ;
		for ($tNum = 0 ; $tNum < $numTasks ; $tNum++) { 
			print MYFH "sleep $slpTime \n";
		}
		close MYFH;
		
		$numWorker = $numWorker * 2  ; 
	}
	$td = $td * 4 ; 

}
