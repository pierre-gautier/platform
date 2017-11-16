if "%1"=="" exit /b
if "%2"=="" exit /b

sed -i s/exemple/%1/g pom.xml
sed -i s/exemple/%1/g .project
sed -i s/exemple/%1/g META-INF\MANIFEST.MF
sed -i s/Exemple/%2/g META-INF\MANIFEST.MF
sed -i s/exemple/%1/g src\platform\exemple\Activator.java

rename src\platform\exemple %1

rmdir /s/q target

del sed.exe
del rename.bat
