### 旧项目支持compose

```groovy
android {
    ...
    kotlinOptions {
        jvmTarget = '1.8'
        useIR = true
    }
    buildFeatures {
        ...
        compose true
    }
    composeOptions {
        kotlinCompilerExtensionVersion rootProject.composeVersion
    }
}

dependencies {
    ...
    // Compose
    implementation "androidx.compose.runtime:runtime:$rootProject.composeVersion"
    implementation "androidx.compose.ui:ui:$rootProject.composeVersion"
    implementation "androidx.compose.foundation:foundation:$rootProject.composeVersion"
    implementation "androidx.compose.foundation:foundation-layout:$rootProject.composeVersion"
    implementation "androidx.compose.material:material:$rootProject.composeVersion"
    implementation "androidx.compose.runtime:runtime-livedata:$rootProject.composeVersion"
    implementation "androidx.compose.ui:ui-tooling:$rootProject.composeVersion"
    implementation "com.google.android.material:compose-theme-adapter:$rootProject.composeVersion"
    ...
}
```

ComposeView的容器

```xml
<androidx.compose.ui.platform.ComposeView
    android:id="@+id/compose_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

```kotlin
composeView.setContent {
    MaterialTheme {
        PlantDetailDescription()
    }
}
```



获取dimen和string资源

```kotlin
dimensionResource(id)
stringResource(id)
```



预览，用@Preview注解。可以套一个MaterialTheme给一个主题

```kotlin
@Preview
@Composable
fun PlantNamePreview() {
    MaterialTheme {
        PlantName("Apple")
    }
}
```