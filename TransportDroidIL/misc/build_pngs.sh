#!/bin/bash

set -e

cd $(dirname $0)

densities=(ldpi mdpi hdpi xhdpi)
density_res=(36 48 72 96)
num_densities=${#densities[*]}

for ((i = 0; i < num_densities; i++)); do
	density_name=${densities[$i]}
	res=${density_res[$i]}
	dest_dir=../res/drawable-${density_name}
	for icon_file in *.svg; do
		base_filename=${icon_file%.svg}
		dest_file="${dest_dir}/${base_filename}.png"
		echo "${icon_file}-[${res}]->${dest_file}"
		inkscape --export-png=$dest_file \
			--export-width=$res --export-height=$res \
			--export-background-opacity=0 --without-gui $icon_file
	done
done
