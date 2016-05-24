echo "hello " $1
cat $1  | sed 's/[ \t]*$//g' > TEMP.txt
cat TEMP.txt | while read LINE
do
	if [ -n "$LINE" ]; then
		echo $1 $LINE >> shell/all.txt
	fi
done
