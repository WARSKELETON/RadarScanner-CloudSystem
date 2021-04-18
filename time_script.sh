#!/bin/bash
time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s GRID_SCAN -w 1024 -h 1024 -x0 0 -x1 1023 -y0 0 -y1 1023 -i 'datasets/SIMPLE_VORONOI_1024x1024_1.png' -yS 512 -xS 512

time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s PROGRESSIVE_SCAN -w 1024 -h 1024 -x0 0 -x1 1023 -y0 0 -y1 1023 -i 'datasets/SIMPLE_VORONOI_1024x1024_1.png' -yS 512 -xS 512

time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s GREEDY_RANGE_SCAN -w 1024 -h 1024 -x0 0 -x1 1023 -y0 0 -y1 1023 -i 'datasets/SIMPLE_VORONOI_1024x1024_1.png' -yS 512 -xS 512

