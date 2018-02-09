![Kompass](https://github.com/sellmair/kompass/blob/develop/assets/Kompass_724.png?raw=true)

A boilerplate free router concept for android, written in Kotlin
<br>

[![Gitter](https://img.shields.io/gitter/room/nwjs/nw.js.svg)](https://gitter.im/kompass-android/)

#### Support
I am happy to help you with any problem on gitter, as fast as I can! <br>
The goal is to investigate any reported issue within 2 days.

# Why you should use Kompass
- Powerful router which works great with MVP, MVVM and almost any other architecture
- Boilerplate free routing: No more bundle.putInt(ARG_SOMETHING, something)
- Very simple and clean architecture for applying custom transitions
- Generic fragment transitions
- Routing with multiple screen (which are called ships)
- Kotlin

# Setup
##### Step 1: Enable Annotation Processing
Add this at the top of your build.gradle
```groovy
apply plugin: 'kotlin-kapt'


kapt {
    generateStubs = true
}

```

##### Step2: Add Kompass Dependencies
```groovy
dependencies {
    ...
    implementation 'io.sellmair:kompass:0.0.7'
    implementation 'io.sellmair:kompass-annotation:0.0.7'
    kapt 'io.sellmair:kompass-processor:0.0.7'
}
```


# Usage
## Example
I highly recommend having a look at the [example](https://github.com/sellmair/kompass/tree/develop/example) app built with Kompass

<br><br>
###### Gif
<img src="https://github.com/sellmair/kompass/blob/develop/assets/example.gif?raw=true" width="250">
<br><br>


## Basic
Kompass makes extensive use of ship-vocabulary for its API's. Here are the main analogies: 
- The _Kompass_ is the upper most object and contains ships
- A _Ship_ is the entity which can route to a certain _Destination_. This might represent a certain area of your 
activity where fragments can be loaded.
- A _Sail_ is the actual area where fragments can be placed in. Your activity therefore sets the 
sails for a certain ship, which then 'sails' to the destination. 
- A _Destination_ represents one certain 'scene' of your app. It also holds all necessary arguments for 
 the fragment/activity. For example: You might have a 
'LoginDestination', 'HomeDestination', 'SettingsDestination', ...  in your application. 
- A _Map_ knows how to display a certain _Destination_ (meaning which Fragment or Activity to load for it). 
A map (_AutoMap_) is automatically created for you
- A _Cran_ knows how to push a _Destination_ object into a _Bundle_. A Cran (_AutoCran_) is automatically
created for you
- A _Detour_ can implement custom transitions for certain routes.
- A _DetourPilot_ knows about a bunch of _Detours_ and knows when to apply which _Detour_


#### Create a Kompass
Creating the _Kompass_ is very simple using the provided builder: 

###### Create a Kompass: Trivial
This example is the most trivial Kompass that can be built. It accepts any object implementing
_KompassDestination_ as Destination. We will talk about the .autoMap() part later. 
It is easy, I promise :bowtie:
```kotlin
val trivialKompass = Kompass.builder<KompassDestination>(context)
                     .autoMap() // we will talk about this later
                     .build()
```

___
<br><br>
###### Create a Kompass: Real World Example
Here is a real-world example of Kompass, where _MyCustomDestinationType_ is just a basic
sealed class and 'autoMap', 'autoCrane' and 'autoPilot' are extension functions automatically 
generated by the _KompassCompiler_. But as you can see: It is very easy to create a Kompass object :blush:

```kotlin
val kompass = Kompass.builder<MyCustomDestinationType>(context)
                     .autoMap()
                     .autoCrane()
                     .autoPilot()
                     .build()
```


#### Create your _Destinations_
Destinations are simple classes or data classes which hold very simple data like 
- Int
- String
- List< Int >
- Parcelable 

(Everything that can be represented inside android.os.Bundle)

Destinations are typically annotated with 
```kotlin
@Destination(target = [MyFragmentOrActivity::class])
```
 OR implement the _KompassDestination_ Interface (if you do not want to use the Kompass-Compiler)
 
 
 I consider it a good idea implemented a sealed superclass for groups of _Destinations_ and restrict 
 the Kompass object to this superclass. 
 
 ###### Example: Annotated Destination
 ```kotlin
@Destination
class HomeDestination(val user: User)
```

###### Example: Implemented KompassDestination
```kotlin
class HomeDestination(val user: User): KompassDestination {
    override fun asBundle(): Bundle {
        val bundle = Bundle()
        // store user in bundle
                ...
    }


```
 
#### Set sails to a Ship
Once your activity has started, you have to provide a sail for the ship which should route to certain 
destinations. You can do this whenever you want to (even after you routed your ship to a certain
destination). The following example will show how the FrameLayout with id 'R.id.lisa_container' will 
be used for the ship called Lisa as _Sail_: 

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var sail: KompassSail

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Step 1: Get the kompass instance (Dagger, Kodein, Singleton?, ...)
        val kompass = ...
        
        // Step 2: Get the ship called 'lisa'
        val lisa = kompass["lisa"]
        
        // Step 3: Set the sail and store the reference
        this.sail = lisa.setSail(this, R.id.lisa_container)
       
    }

    
    override fun onDestroy() {
        super.onDestroy()
        
        // Step 4 Release the sail when no longer needed
        sail.release()
    }
}
```

#### Route to a Destination
Now it is time to route to a certain destination. The following example will show how the routing
for a login-screen could look like: 

```kotlin
    companion object {
        const val MAIN_SHIP = "main"
    }
    
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val user = getUser()

        sail = kompass[MAIN_SHIP].setSail(this, container.id)
        kompass[MAIN_SHIP].navigateTo(if(user!=null) HomeDestination(user) else LoginDestination())
        
    }
```


#### Recreate Destination from _Bundle_
One of the strongest parts of _Kompass_ is the elimination of hassle with bundles and arguments. 
You can easily recreate the original _Destination_ from an intent or bundle using the automatically
generated extension functions. 

##### Example: Fragment
If you routed to a certain fragment you can easily recreate the destination from the arguments _Bundle_
```kotlin
class HomeFragment: Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val homeDestination = arguments.asHomeDestination() // Function was automatically generated
        val user = homeDestination?.user
        // ... Do something with your user object
    }
}
```

##### Example: Activity
```kotlin
class HomeActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        val homeDestination = intent?.extras?.asHomeDestination()
        val user = homeDestination?.user
        // ... Do something with your user object
    }
}
```

## Advanced
#### The Map
Maps contain informations about how to display a certain _Destination_. This can be done by 
starting a new Activity or creating a new Fragment. If you want to use a custom Map element, add it to the KompassBuilder
```kotlin
Kompass.builder(context)
       .addMap(myCustomMap)
       ...
```
#### The Cran
A cran knows how to pack a _Destination_ object into a bundle. If you want to use a custom Cran, 
add it to the KompassBuilder 

```kotlin
Kompass.builder(context)
       .addCran(myCutomCran)
```
#### The Detour / The DetourPilot
It is a very common thing to apply transitions when one fragment is replaced by another fragment. 
A _Detour_ can very easily implement such a transition genericly. 

Consider we want every fragment to slide in, when entered and slide out, when exited. We just 
have to write a _Detour_ class like this: 

```kotlin
    @Detour
    class FragmentSlide: KompassDetour<Any, Fragment, Fragment>{
        override fun setup(destination: Any,
                           currentFragment: Fragment,
                           nextFragment: Fragment,
                           transaction: FragmentTransaction) {
            currentFragment.exitTransition = Slide(Gravity.RIGHT)
            nextFragment.enterTransition = Slide(Gravity.LEFT)
        }

    }
```

Every _Detour_ will automatically be applied if the types of 'destination', 'currentFragment' and 'nextFragment' 
can be assigned from the current route and 

```kotlin
Kompass.builder(context)
       .autoPilot() // <-- will be available if you have some class annotaged with @Detour
```
is used!

#### AutoMap, AutoCran, AutoPilot
The functions 
```kotlin
Kompass.builder(context)
       .autoMap()
       .autoCran()
       .autoPilot()
```
are automatically generated if possible. 

- .autoMap() will be available after you specified one target for at least one @Destination
- .autoCran() will be available after you annotated at least one class with @Destination
- .autoPilot() will be available after you annotated at least one class with @Detour
#### BackStack

Kompass comes with an own back-stack. You should override your Activities 'onBackPressed' like: 

```kotlin
    override fun onBackPressed() {
        if (!kompass.popBackImmediate())
            finish()
    }
```
