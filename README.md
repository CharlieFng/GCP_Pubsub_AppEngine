export GOOGLE_APPLICATION_CREDENTIALS=/Users/charlie/Downloads/credential.json

#Run Locally
mvn compile exec:java \
    -Dexec.mainClass=charliefeng.club.WordCount \
    -Dexec.args="--output=./output/"

mvn -Pdataflow-runner compile exec:java \
    -Dexec.mainClass=charliefeng.club.WordCount \
    -Dexec.args="--project=charlie-feng-contino \
    --stagingLocation=gs://charlie-feng-contino-wordcount/staging/ \
    --output=gs://charlie-feng-contino-wordcount/output \
    --runner=DataflowRunner"

