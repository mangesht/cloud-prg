make gflops
./gflops -i 5 | tee gflop_i.dat 
cat gflop_i.dat | grep Summary -A 10 | grep -e "^[0-9]" | awk '{print $1 "  "  $5}' > gflops.dat 
gnuplot gflops.p

make iops
./iops -i 5 | tee iops_i.dat
cat iops_i.dat | grep Summary -A 10 | grep -e "^[0-9]" | awk '{print $1 "  "  $5}' > iops.dat
gnuplot iops.p 
