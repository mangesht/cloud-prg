make benchmark
./benchmark -i 5 > gflop_i.dat
cat gflop_i.dat | grep Summary -A 10 | grep -e "^[0-9]" | awk '{print $1 "  "  $5}' > gflops.dat 
gnuplot gflops.p
