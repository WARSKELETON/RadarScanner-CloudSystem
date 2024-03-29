#!/bin/bash
# "baseline" "dynMcount" "alloc" "load_store" "branch" "DynICount" "ICount"
declare -a StringArray=("DynICountOpt")

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
  elif [ "$val" = "DynICountOpt" ]; then
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
    for ((xs=1;xs<view;xs=xs+10)); do
      echo $xs
      for ((ys=1;ys<view;ys=ys+10)); do
        echo $ys
        time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s GRID_SCAN -w 2048 -h 2048 -x0 0 -x1 $((view-1)) -y0 0 -y1 $((view-1)) -i 'datasets/SIMPLE_VORONOI_2048x2048_1.png' -yS $((ys)) -xS $((xs)) > results2048Start/"$val""$view"_"$xs"_"$ys"_GS.txt
        time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s PROGRESSIVE_SCAN -w 2048 -h 2048 -x0 0 -x1 $((view-1)) -y0 0 -y1 $((view-1)) -i 'datasets/SIMPLE_VORONOI_2048x2048_1.png' -yS $((ys)) -xS $((xs)) > results2048Start/"$val""$view"_"$xs"_"$ys"_PS.txt
        time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s GREEDY_RANGE_SCAN -w 2048 -h 2048 -x0 0 -x1 $((view-1)) -y0 0 -y1 $((view-1)) -i 'datasets/SIMPLE_VORONOI_2048x2048_1.png' -yS $((ys)) -xS $((xs)) > results2048Start/"$val""$view"_"$xs"_"$ys"_GRS.txt
      done
    done
  done
done