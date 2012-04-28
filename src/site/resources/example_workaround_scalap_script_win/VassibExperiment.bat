call del /Q "VassibExperiment.jar"
call scalac -classpath *;./libs/* VassibExperiment.scala -d VassibExperiment.jar
call java -classpath *;./libs/*  VassibExperiment