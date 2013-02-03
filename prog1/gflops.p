#GNUplot script file for plotting 
# x0
unset log
unset label
set autoscale
set title "Mean Gflops "
set xlabel "Num of Threads "
set ylabel "Gfops(Giga Floating operations per secons)"
#set xrange [0:4]
set yrange [0.0:]
set boxwidth 0.5
set bars 4.0
set style fill solid 1.0 
##set style rectangle front fc rgb "white"
set terminal gif
set output "gflops.gif"
plot "gflops.dat" using 2 : xtic(1) title "Gflops"  with boxes fc rgb "#D99795" 

