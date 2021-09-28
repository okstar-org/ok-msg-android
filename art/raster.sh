#!/bin/bash

SIZES="36 54 72 108 144"
EXPORT="../src/main/res/"
ICON="../Arcticons/scalable/apps"

for DIR in $(find -name "*.svg")
do
  FILE=${DIR##*/}
  NAME=${FILE%.*}
  cp ${FILE} ${FILE}.tmp
  rm ${FILE}.tmp
  #cp -f ${FILE} ${ICON}/${FILE}
  echo "Working on" ${FILE}
  for SIZE in ${SIZES}
  do
    svgexport ${NAME}.svg ${NAME}.png ${SIZE}:
    case ${SIZE} in
      8)
	mv ${NAME}.png ${EXPORT}/8x8/apps/
	;;

      16)
	mv ${NAME}.png ${EXPORT}/16x16/apps/
	;;
      24)
	mv ${NAME}.png ${EXPORT}/24x24/apps/
	;;
      32)
	mv ${NAME}.png ${EXPORT}/32x32/apps/
	;;
      36)
	mv ${NAME}.png ${EXPORT}/drawable-mdpi/
	;;
      48)
	mv ${NAME}.png ${EXPORT}/48x48/apps/
	;;
      54)
	mv ${NAME}.png ${EXPORT}/drawable-hdpi/
	;;
      72)
	mv ${NAME}.png ${EXPORT}/drawable-xhdpi/
	;;
      108)
	mv ${NAME}.png ${EXPORT}/drawable-xxhdpi/
	;;
      128)
	mv ${NAME}.png ${EXPORT}/128x128/apps/
	;;
      144)
	mv ${NAME}.png ${EXPORT}/drawable-xxxhdpi/
	;;
      256)
	mv ${NAME}.png ${EXPORT}/256x256/apps/
    esac
  done
done
