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

- daon.spark.model.MakeModel
- daon.spark.EvaluateModel
- daon.spark.sentences.SejongSentences
- daon.spark.sentences.UserSentences
