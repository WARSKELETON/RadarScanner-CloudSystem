#!/bin/bash
# "baseline" "dynMcount" "alloc" "load_store" "branch" "DynICount" "ICount" "ICountPerR"
declare -a StringArray=("baseline" "ICount" "DynICount" "ICountPerR")

javac BIT/highBIT/*.java
javac BIT/lowBIT/*.java
javac BIT/*.java

for val in ${StringArray[@]}; do
  cp pt/ulisboa/tecnico/cnv/solver/original/*.class pt/ulisboa/tecnico/cnv/solver
  if [ "$val" = "baseline" ]; then
    echo $val
  elif [ "$val" = "dynMcount" ]; then
    echo $val
    java BIT.DynMCount pt/ulisboa/tecnico/cnv/solver/original/ pt/ulisboa/tecnico/cnv/solver/
  elif [ "$val" = "ICountPerR" ]; then
    echo $val
    java BIT.ICountPerR pt/ulisboa/tecnico/cnv/solver/original/ pt/ulisboa/tecnico/cnv/solver/
  elif [ "$val" = "DynICount" ]; then
    echo $val
    java BIT.DynICount pt/ulisboa/tecnico/cnv/solver/original/ pt/ulisboa/tecnico/cnv/solver/
  elif [ "$val" = "ICount" ]; then
    echo $val
    java BIT.ICount pt/ulisboa/tecnico/cnv/solver/original/ pt/ulisboa/tecnico/cnv/solver/
  elif [ "$val" = "alloc" ]; then
    echo $val
    java BIT.StatisticsTool -alloc pt/ulisboa/tecnico/cnv/solver/original/ pt/ulisboa/tecnico/cnv/solver/
  elif [ "$val" = "load_store" ]; then
    echo $val
    java BIT.StatisticsTool -load_store pt/ulisboa/tecnico/cnv/solver/original/ pt/ulisboa/tecnico/cnv/solver/
  else
    echo $val
    java BIT.StatisticsTool -branch pt/ulisboa/tecnico/cnv/solver/original/ pt/ulisboa/tecnico/cnv/solver/
  fi

  #128 256 512 1024 2048
  for view in 64 128 256 512 1024 2048; do
    echo $view

    n=10; shift
    for ((i = 0; i < n; i++)); do
        { time -p java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s GRID_SCAN -w 2048 -h 2048 -x0 0 -x1 $((view-1)) -y0 0 -y1 $((view-1)) -i 'datasets/SIMPLE_VORONOI_2048x2048_1.png' -yS $((view/2)) -xS $((view/2)) &>/dev/null; } 2>&1 # ignore the output of the command
    done | awk '
        /real/ { real = real + $2; nr++ }
        /user/ { user = user + $2; nu++ }
        /sys/  { sys  = sys  + $2; ns++}
        END    {
                 if (nr>0) printf("real %f\n", real/nr);
                 if (nu>0) printf("user %f\n", user/nu);
                 if (ns>0) printf("sys %f\n",  sys/ns)
               }'

        n=10; shift
    for ((i = 0; i < n; i++)); do
        { time -p java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s PROGRESSIVE_SCAN -w 2048 -h 2048 -x0 0 -x1 $((view-1)) -y0 0 -y1 $((view-1)) -i 'datasets/SIMPLE_VORONOI_2048x2048_1.png' -yS $((view/2)) -xS $((view/2)) &>/dev/null; } 2>&1 # ignore the output of the command
    done | awk '
        /real/ { real = real + $2; nr++ }
        /user/ { user = user + $2; nu++ }
        /sys/  { sys  = sys  + $2; ns++}
        END    {
                 if (nr>0) printf("real %f\n", real/nr);
                 if (nu>0) printf("user %f\n", user/nu);
                 if (ns>0) printf("sys %f\n",  sys/ns)
               }'
    n=10; shift
    for ((i = 0; i < n; i++)); do
        { time -p java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s GREEDY_RANGE_SCAN -w 2048 -h 2048 -x0 0 -x1 $((view-1)) -y0 0 -y1 $((view-1)) -i 'datasets/SIMPLE_VORONOI_2048x2048_1.png' -yS $((view/2)) -xS $((view/2)) &>/dev/null; } 2>&1 # ignore the output of the command
    done | awk '
        /real/ { real = real + $2; nr++ }
        /user/ { user = user + $2; nu++ }
        /sys/  { sys  = sys  + $2; ns++}
        END    {
                 if (nr>0) printf("real %f\n", real/nr);
                 if (nu>0) printf("user %f\n", user/nu);
                 if (ns>0) printf("sys %f\n",  sys/ns)
               }'
  done
done