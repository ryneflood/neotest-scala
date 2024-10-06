build-runner-native:
  #!/usr/bin/env bash
  docker build -t scala-test-runner -f runner/Dockerfile .
  
  # Create a container from the image
  container_id=$(docker create scala-test-runner:latest)
  # Copy the binary to local filesystem
  docker cp $container_id:/bin/app ./bin/scala-runner
  # Remove the container
  docker rm $container_id
