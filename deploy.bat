aws s3 rm s3://wba-csv2json-bucket --recursive

aws cloudformation delete-stack --stack-name Csv2JsonApp

timeout /t 15

sam deploy --stack-name  Csv2JsonApp --no-confirm-changeset  --force-upload