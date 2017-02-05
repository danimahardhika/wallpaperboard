# Wallpaper Board <img src="https://raw.githubusercontent.com/danimahardhika/wallpaperboard/master/arts/icon.png" width="35">
[![](https://jitpack.io/v/danimahardhika/wallpaperboard.svg)](https://jitpack.io/#danimahardhika/wallpaperboard) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
<br>Android Wallpaper Dashboard
<p><a href='https://play.google.com/store/apps/details?id=com.dm.wallpaper.board.demo&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width="200"/></a></p>

# Gradle Dependency
**Requirements**
* Latest version of Android Studio</li>
* Android-SDK Build tools v25</li>
* API 25 SDK Platform</li>
* Latest version of Android Support Library</li>
* Java SE Development Kit 8</li>

<p>Take a look on this <a href="https://raw.githubusercontent.com/danimahardhika/candybar-library/698d102f504f5a843af4f5bc67a340a09b3c5889/screenshots/requirements.jpg">screenshot</a> for requirements
<p>The minimum API level supported by this library is API 15</p>
Add JitPack repository to root ```build.gradle```
```Gradle
allprojects {
    repositories {
        //...
        maven { url "https://jitpack.io" }
    }
}
```
Add the dependency
```Gradle
dependencies {
    //...
    compile 'com.github.danimahardhika:wallpaperboard:1.0.0'
}
```
