Program was created for parse all readme file to given path repositories. For ex. like https://github.com/spotify
For the start program via Docker you should run build image first and then create container and deploy this a image to container: 
1) docker build -t <your name image> .
2) docker run -d --name <your name container> -p 8080:8080 <your name image>
Endpoint for get most popular words from all readme files - 
GET - http://localhost:8080/file-service/api/v1/files?path=https://github.com/spotify&lengthWord=8&countWords=20
Where params: 
- path - path to repository
- lengthWord - amount characters in word which system need to find
- countWords - amount words for results
