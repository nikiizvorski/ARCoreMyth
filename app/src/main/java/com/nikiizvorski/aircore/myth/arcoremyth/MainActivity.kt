package com.nikiizvorski.aircore.myth.arcoremyth

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseArFragment
import com.google.ar.sceneform.ux.TransformableNode

class MainActivity : AppCompatActivity() {
    internal lateinit var arFragment: ArFragment
    internal var lampPostRenderable: ModelRenderable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkIsSupportedDeviceOrFinish(this)) {
            return
        }
        setContentView(R.layout.activity_main)
        arFragment = (supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?)!!

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            ModelRenderable.builder()
                    .setSource(this, Uri.parse("ArcticFox_Posed.sfb"))
                    .build()
                    .thenAccept { modelRenderable -> lampPostRenderable = modelRenderable }
                    .exceptionally {
                        val toast = Toast.makeText(this@MainActivity, "Unable to load andy renderable", Toast.LENGTH_LONG)
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                        null
                    }
        }

        arFragment.setOnTapArPlaneListener(BaseArFragment.OnTapArPlaneListener { hitResult, plane, motionEvent ->
            if (lampPostRenderable == null) {
                return@OnTapArPlaneListener
            }

            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            val lamp = TransformableNode(arFragment.transformationSystem)
            lamp.setParent(anchorNode)
            lamp.renderable = lampPostRenderable
            lamp.select()
        })


    }

    companion object {
        private val TAG = MainActivity::class.java!!.getSimpleName()
        private val MIN_OPENGL_VERSION = 3.0


        fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                Log.e(TAG, "Sceneform requires Android N or later")
                Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show()
                activity.finish()
                return false
            }
            val openGlVersionString = (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                    .deviceConfigurationInfo
                    .glEsVersion
            if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
                Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later")
                Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                        .show()
                activity.finish()
                return false
            }
            return true
        }
    }
}
