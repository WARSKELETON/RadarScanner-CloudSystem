#!/bin/bash
# "baseline" "dynMcount" "alloc" "load_store" "branch" "DynICount"
declare -a StringArray=("baseline" "dynMcount" "alloc" "load_store" "branch" "DynICount")

javac BIT/highBIT/*.java
javac BIT/lowBIT/*.java
javac BIT/*.java

for val in ${StringArray[@]}; do
  if [ "$val" = "baseline" ]; then
    echo $val
  elif [ "$val" = "dynMcount" ]; then
    echo $val
    java BIT.DynMCount pt/ulisboa/tecnico/cnv/solver/original/ pt/ulisboa/tecnico/cnv/solver/
  elif [ "$val" = "DynICount" ]; then
    echo $val
    java BIT.DynICount pt/ulisboa/tecnico/cnv/solver/original/ pt/ulisboa/tecnico/cnv/solver/
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
  for view in 64 128 256 512 1024; do
    echo $view
    time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s GRID_SCAN -w 1024 -h 1024 -x0 0 -x1 $((view-1)) -y0 0 -y1 $((view-1)) -i 'datasets/SIMPLE_VORONOI_1024x1024_1.png' -yS $((view/2)) -xS $((view/2)) > results1024/"$val""$view"_GS.txt
    time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s PROGRESSIVE_SCAN -w 1024 -h 1024 -x0 0 -x1 $((view-1)) -y0 0 -y1 $((view-1)) -i 'datasets/SIMPLE_VORONOI_1024x1024_1.png' -yS $((view/2)) -xS $((view/2)) > results1024/"$val""$view"_PS.txt
    time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s GREEDY_RANGE_SCAN -w 1024 -h 1024 -x0 0 -x1 $((view-1)) -y0 0 -y1 $((view-1)) -i 'datasets/SIMPLE_VORONOI_1024x1024_1.png' -yS $((view/2)) -xS $((view/2)) > results1024/"$val""$view"_GRS.txt
  done
done