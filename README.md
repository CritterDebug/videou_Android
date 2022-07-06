# videouAndroid

Should be able to be run without any issues as long as the device is android 8.0 or newer. It was mostly tested on android 12 so I know that is stable. You will however need to have a real device plugged in to use the app. An emulator will run the app but I think emulators don't have bluetooth privilege, so most of the functionality will be lost.

Currently the fragment_home.xml page wont display the UI. To fix this comment the uncomment the following lines. I believe it has something to do with the stability of camerax library even though its the first release candidate. The previous version I was using did not have this problem.

<androidx.camera.view.PreviewView
        android:id="@+id/viewFinder"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintVertical_bias="0.0"
        app:flow_horizontalBias="0.5"
        app:layout_constraintDimensionRatio="W,16:9">
</androidx.camera.view.PreviewView>


To find the app on the google play store follow the link below, alternatively search "Videou" and rememeber to press "Search instead for Videou" as the indexing will try to search for "Videos" currently.
https://play.google.com/store/apps/details?id=com.secondShot.videou
