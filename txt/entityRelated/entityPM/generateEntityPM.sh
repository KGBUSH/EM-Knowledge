echo "hello " $1
cat $1  | sed 's/[ \t]*$//g' > TEMP.txt
cat TEMP.txt | while read LINE
do
	if [ -n "$LINE" ]; then
		echo $LINE >> shell/entityPM.txt
	fi
done
