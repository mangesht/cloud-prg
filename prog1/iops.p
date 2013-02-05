#GNUplot script file for plotting 
# x0
unset log
unset label
set autoscale
set title "Mean IOPS "
set xlabel "Num of Threads "
set ylabel "Gops(Giga Integer operations per secons)"
#set xrange [0:4]
set yrange [0.0:]
set boxwidth 0.5
set bars 4.0
set style fill solid 1.0 
##set style rectangle front fc rgb "white"
set terminal gif
set output "iops.gif"
plot "iops.dat" using 2 : xtic(1) title "iops"  with boxes fc rgb "#D99795" 

