# Spark Jar

## Required

java 1.8+

## Build

```bash
./gradlew :daon-spark:buildDaonSpark

cd distributions

# evaluate
java -cp daonSpark.jar daon.spark.EvaluateModel
```
## Job Classes

- daon.spark.MakeModel
- daon.spark.EvaluateModel
- daon.spark.write.SejongSentences
- daon.spark.write.UserSentences
