# description
Take a photo and get similar adverts from youla:)

# before run
1) Run adapted <https://git.automobile.ru/ml/android/get_similar_imgs_by_a_vector_server>
2) Create file in com.example.myapplication.config
```
package com.example.myapplication.config;

public class Config {
    public static final String HOST = "http://192.168.43.134:9898";
    //    public static final String HOST = "http://172.27.43.17:8006";
    public static final String ADD_SERVER_FOR_UPLOAD = HOST + "/get_similar_img";
}

```