# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

#blur kit
-keep class com.wonderkiln.blurkit.** { *; }

-dontwarn android.support.v8.renderscript.*
-keepclassmembers class android.support.v8.renderscript.RenderScript {
  native *** rsn*(...);
  native *** n*(...);
}


  # For easing lib
-keep public class com.daimajia.*
-keep class com.daimajia.easing.** { *; }
-keep interface com.daimajia.easing.* { *; }
    -keep class com.daimajia.** { *; }
           -dontwarn com.daimajia.**
           -keepnames class com.daimajia.**

  ## Joda Time 2.3

  -dontwarn org.joda.convert.**
  -dontwarn org.joda.time.**
  -keep class org.joda.time.** { *; }
  -keep interface org.joda.time.** { *; }

  -dontwarn android.databinding.**
  -keep class android.databinding.** { *; }

  # avoid obfuscate enums
  -keepclassmembers class * extends java.lang.Enum {
      <fields>;
      public static **[] values();
      public static ** valueOf(java.lang.String);
  }

  # Glide
  -keep public class * implements com.bumptech.glide.module.GlideModule
  -keep public class * extends com.bumptech.glide.module.AppGlideModule
  -keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
  }


# Mantem o nome original da classe e a linha em que houver um erro no stack quando em release
-keepattributes SourceFile,LineNumberTable
# sei la mas mandava fazer
-keep class com.bugsnag.android.NativeInterface { *; }
-keep class com.bugsnag.android.Breadcrumbs { *; }
-keep class com.bugsnag.android.Breadcrumbs$Breadcrumb { *; }
-keep class com.bugsnag.android.BreadcrumbType { *; }
-keep class com.bugsnag.android.Severity { *; }
-keep class com.bugsnag.android.ndk.BugsnagObserver { *; }


-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

# androidViewAnimations
 -keep class com.daimajia.androidanimations.** { *; }
 -keep interface com.daimajia.androidanimations.** { *; }


##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

##---------------End: proguard configuration for Gson  ----------

# Application classes that will be serialized/deserialized over Gson, keepclassmembers
-keep class gilianmarques.dev.musicplayer.utils.DontObfuscate
-keep @gilianmarques.dev.musicplayer.utils class * { *; }

# InAppBilling
-keep class com.android.vending.**

# remover  System.out.println
-assumenosideeffects class java.io.PrintStream {
     public void println(%);
     public void println(**);
 }



# Google
-keep class com.google.android.gms.common.GooglePlayServicesUtil {*;}
-keep class com.google.android.gms.ads.identifier.** { *; }
-dontwarn com.google.android.gms.**

# Legacy
-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**
-dontwarn android.net.http.**

# Google Play Services library
-keep class * extends java.util.ListResourceBundle {
  protected Object[][] getContents();
}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
  public static final *** NULL;
}
-keepnames class * implements android.os.Parcelable
-keepclassmembers class * implements android.os.Parcelable {
  public static final *** CREATOR;
}
-keep @interface android.support.annotation.Keep
-keep @android.support.annotation.Keep class *
-keepclasseswithmembers class * {
  @android.support.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
  @android.support.annotation.Keep <methods>;
}
-keep @interface com.google.android.gms.common.annotation.KeepName
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
  @com.google.android.gms.common.annotation.KeepName *;
}
-keep @interface com.google.android.gms.common.util.DynamiteApi
-keep public @com.google.android.gms.common.util.DynamiteApi class * {
  public <fields>;
  public <methods>;
}
-keep class com.google.android.gms.common.GooglePlayServicesNotAvailableException {*;}
-keep class com.google.android.gms.common.GooglePlayServicesRepairableException {*;}

# Google Play Services library 9.0.0 only
-dontwarn android.security.NetworkSecurityPolicy
-keep public @com.google.android.gms.common.util.DynamiteApi class * { *; }

# support-v4
-keep class android.support.v4.app.Fragment { *; }
-keep class android.support.v4.app.FragmentActivity { *; }
-keep class android.support.v4.app.FragmentManager { *; }
-keep class android.support.v4.app.FragmentTransaction { *; }
-keep class android.support.v4.content.LocalBroadcastManager { *; }
-keep class android.support.v4.util.LruCache { *; }
-keep class android.support.v4.view.PagerAdapter { *; }
-keep class android.support.v4.view.ViewPager { *; }
-keep class android.support.v4.content.ContextCompat { *; }

# support-v7-recyclerview
-keep class android.support.v7.widget.RecyclerView { *; }
-keep class android.support.v7.widget.LinearLayoutManager { *; }


  # Google Play Services library 9.0.0 only
  -dontwarn android.security.NetworkSecurityPolicy
  -keep public @com.google.android.gms.common.util.DynamiteApi class * { *; }

  # support-v4
  -keep class android.support.v4.app.Fragment { *; }
  -keep class android.support.v4.app.FragmentActivity { *; }
  -keep class android.support.v4.app.FragmentManager { *; }
  -keep class android.support.v4.app.FragmentTransaction { *; }
  -keep class android.support.v4.content.LocalBroadcastManager { *; }
  -keep class android.support.v4.util.LruCache { *; }
  -keep class android.support.v4.view.PagerAdapter { *; }
  -keep class android.support.v4.view.ViewPager { *; }
  -keep class android.support.v4.content.ContextCompat { *; }

  # support-v7-recyclerview
  -keep class android.support.v7.widget.RecyclerView { *; }
  -keep class android.support.v7.widget.LinearLayoutManager { *; }

  # Evita erros com textinputEditText
  -keepnames class android.support.design.widget.** { *; }

  #----------------------firebase Auth  COMEÇO
  -keepattributes Signature
  -keepattributes *Annotation*
  #----------------------firebase Auth FIM

  #----------------------PICASSO
-dontwarn com.squareup.okhttp.**

  #----------------------CRICULAR POGRESSBAR
-keep class net.futuredrama.** { *; }
-dontwarn net.futuredrama.**

  #----------------------FAB MENU
-dontwarn java.lang.invoke.*

  #----------------------Firebase auth
-keepattributes Signature
-keepattributes *Annotation*


 #----------------------Picasso and Libraries using OKHTTP

# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform

# desobfuscate my Gson objetcs to use with spotify
-keep public class gilianmarques.dev.musicplayer.spotify.objects.** {
  public protected private *;
}