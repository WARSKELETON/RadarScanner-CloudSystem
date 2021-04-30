#!/bin/bash
# "baseline" "Mcount" "alloc" "load_store" "branch" "ICountPerBB" "ICountExecM" "ICountPerM"
declare -a StringArray=("baseline" "Mcount" "alloc" "load_store" "branch" "ICountPerBB" "ICountExecM" "ICountPerM")

javac BIT/highBIT/*.java
javac BIT/lowBIT/*.java
javac BIT/*.java

for val in ${StringArray[@]}; do
  cp pt/ulisboa/tecnico/cnv/solver/original/*.class pt/ulisboa/tecnico/cnv/solver
  if [ "$val" = "baseline" ]; then
    echo $val
  elif [ "$val" = "Mcount" ]; then
    echo $val
    java BIT.MCount pt/ulisboa/tecnico/cnv/solver/original/ pt/ulisboa/tecnico/cnv/solver/
  elif [ "$val" = "ICountPerBB" ]; then
    echo $val
    java BIT.ICountPerBB pt/ulisboa/tecnico/cnv/solver/original/ pt/ulisboa/tecnico/cnv/solver/
  elif [ "$val" = "ICountPerM" ]; then
    echo $val
    java BIT.ICountPerM pt/ulisboa/tecnico/cnv/solver/original/ pt/ulisboa/tecnico/cnv/solver/
  elif [ "$val" = "ICountExecM" ]; then
    echo $val
    java BIT.ICountExecM pt/ulisboa/tecnico/cnv/solver/original/ pt/ulisboa/tecnico/cnv/solver/
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
  for view in 64 128 256 512; do
    echo $view
    time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s GRID_SCAN -w 512 -h 512 -x0 0 -x1 $((view-1)) -y0 0 -y1 $((view-1)) -i 'datasets/SIMPLE_VORONOI_512x512_1.png' -yS $((view/2)) -xS $((view/2)) > results512/"$val""$view"_GS.txt
    time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s PROGRESSIVE_SCAN -w 512 -h 512 -x0 0 -x1 $((view-1)) -y0 0 -y1 $((view-1)) -i 'datasets/SIMPLE_VORONOI_512x512_1.png' -yS $((view/2)) -xS $((view/2)) > results512/"$val""$view"_PS.txt
    time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s GREEDY_RANGE_SCAN -w 512 -h 512 -x0 0 -x1 $((view-1)) -y0 0 -y1 $((view-1)) -i 'datasets/SIMPLE_VORONOI_512x512_1.png' -yS $((view/2)) -xS $((view/2)) > results512/"$val""$view"_GRS.txt
  done
done