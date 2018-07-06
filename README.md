**HotCorn** 

Kotlin wrapper tool to check for TV shows updates using the [Popcorntime api](https://popcornofficial.docs.apiary.io/#reference/show )

**Usage**

In order to be able to download and use the code you have to have `git` and `maven` installed 

**Downloading the code**

From within the folder you want to have the code installed run the following command

 `git clone https://github.com/llusx/HotCorn && cd HotCorn &&  mvn clean package`

This command will download the code and build it using `maven`. 

The resulting jar files will reside inside in `HotCorn/target` 

**Running HotCorn**

This requires you to have `JRE` installed on your machine

Within the target folder run:

`java -jar HotCorn-0.6-jar-with-dependencies.jar`

This will yield an error message because you have to provide arguments, more on this later

**Synopsis**

`java -jar Hotcorn.jar <option flags> <unix epoch> "<tv show name or imdb code>"[<tv show name or imdb code> ...]`







