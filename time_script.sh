#!/bin/bash

time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s GRID_SCAN -w 2048 -h 2048 -x0 0 -x1 2047 -y0 0 -y1 2047 -i 'datasets/SIMPLE_VORONOI_2048x2048_1.png' -yS 1024 -xS 1024

time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s GRID_SCAN -w 2048 -h 2048 -x0 0 -x1 1023 -y0 0 -y1 1023 -i 'datasets/SIMPLE_VORONOI_2048x2048_1.png' -yS 511 -xS 511

time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s GRID_SCAN -w 2048 -h 2048 -x0 0 -x1 511 -y0 0 -y1 511 -i 'datasets/SIMPLE_VORONOI_2048x2048_1.png' -yS 256 -xS 256


time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s PROGRESSIVE_SCAN -w 2048 -h 2048 -x0 0 -x1 2047 -y0 0 -y1 2047 -i 'datasets/SIMPLE_VORONOI_2048x2048_1.png' -yS 1024 -xS 1024

time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s PROGRESSIVE_SCAN -w 2048 -h 2048 -x0 0 -x1 1023 -y0 0 -y1 1023 -i 'datasets/SIMPLE_VORONOI_2048x2048_1.png' -yS 511 -xS 511

time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s PROGRESSIVE_SCAN -w 2048 -h 2048 -x0 0 -x1 511 -y0 0 -y1 511 -i 'datasets/SIMPLE_VORONOI_2048x2048_1.png' -yS 256 -xS 256


time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s GREEDY_RANGE_SCAN -w 2048 -h 2048 -x0 0 -x1 2047 -y0 0 -y1 2047 -i 'datasets/SIMPLE_VORONOI_2048x2048_1.png' -yS 1024 -xS 1024

time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s GREEDY_RANGE_SCAN -w 2048 -h 2048 -x0 0 -x1 1023 -y0 0 -y1 1023 -i 'datasets/SIMPLE_VORONOI_2048x2048_1.png' -yS 511 -xS 511

time java pt.ulisboa.tecnico.cnv.solver.SolverMain -d -s GREEDY_RANGE_SCAN -w 2048 -h 2048 -x0 0 -x1 511 -y0 0 -y1 511 -i 'datasets/SIMPLE_VORONOI_2048x2048_1.png' -yS 256 -xS 256
