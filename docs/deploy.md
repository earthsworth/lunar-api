# Deploy lunar-api to production environment

## Helm Charts

```shell
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add strimzi https://strimzi.io/charts/
helm repo update

helm install lunar-mongo -f helm/mongo-values.yaml bitnami/mongodb
helm install lunar-redis bitnami/redis
helm install strimzi/strimzi-kafka-operator
```