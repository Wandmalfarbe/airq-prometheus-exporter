rm -f "airq-prometheus-exporter-arm64.tar"
rm -f "airq-prometheus-exporter-amd64.tar"

if ! docker buildx ls | grep -q "multiarch"; then
  docker buildx create --name multiarch --driver docker-container --use
else
  docker buildx use multiarch
fi
docker buildx inspect --bootstrap

docker buildx build \
  --platform linux/arm64 \
  -t airq-prometheus-exporter:arm64 \
  -t airq-prometheus-exporter:latest \
  --output type=docker,dest=airq-prometheus-exporter-arm64.tar .

docker buildx build \
  --platform linux/amd64 \
  -t airq-prometheus-exporter:amd64 \
  -t airq-prometheus-exporter:latest \
  --output type=docker,dest=airq-prometheus-exporter-amd64.tar .
