chmod +x ./pacoder
chmod +x ./yacoder

for file in `find ./build/intermediates/assets -type f -name "*.??.plist"`
do
    echo $file
    ./pacoder -e $file ${file%.*}
    rm $file
done

for file in `find ./build/intermediates/assets -type f -name "*.ya.yaml"`
do
    echo $file
    ./yacoder -e $file ${file%.*}
    rm $file
done