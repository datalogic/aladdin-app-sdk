# aladdin-app-sdk
[![Release](https://jitpack.io/v/datalogic/aladdin-app-sdk.svg)](https://jitpack.io/#datalogic/aladdin-app-sdk)

SDK repository for Android integration with the Aladdin application.

Follow the steps below to add to your Android project.

1. Add jitpack.io repository to dependency resolution:
   
   ```groovy
   dependencyResolutionManagement {
       repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
       repositories {
           google()
           mavenCentral()
           maven { url 'https://jitpack.io' } // add Jitpack
       }
   }
   ```
   
2. Add the dependency:
   
   ```groovy
   dependencies {
	     implementation 'com.github.datalogic:aladdin-app-sdk:{latest version}'
	 }
   ```
