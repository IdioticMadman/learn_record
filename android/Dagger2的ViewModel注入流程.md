# Dagger2的ViewModel注入流程

## ViewModel介绍

### ViewModel的生命周期

![image](https://developer.android.google.cn/images/topic/libraries/architecture/viewmodel-lifecycle.png)

ViewModel的生命周期可以同当前的Activity和Fragment同生命周期。而且不会受当前activity配置变更导致生命周期进行变化而变化。简单的举个例子，横竖屏切换，不会影响viewMode的生命周期。

### ViewModel的使用

![image](https://developer.android.google.cn/images/topic/libraries/architecture/viewmodel-replace-loader.png)
使用示意图：

通过ViewModelProvider提供viewModel实例，然后在viewModel中向DataSource获取数据，更新LiveData。界面监听liveData的变化，从而响应界面的更新

`val viewModel = ViewModelProviders.of(this).get(UserProfileViewModel::class.java)` 

### ViewModel创建流程

* 调用`ViewModelProviders.of()`获取ViewModelProvider对象。需要传入被绑定的activity或fragemnt，可以传入自定义的ViewModelFactory，进行提供自定义的viewModel对象。否则使用自带的AndroidViewModelFactory。同时，也向ViewModelProvider中传入了当前fragemnt或activity中缓存的viewModelStore对象。以便下一步ViewModelProvider使用。

    ```java
    @NonNull
    @MainThread
    public static ViewModelProvider of(@NonNull Fragment fragment, @Nullable Factory factory) {
        Application application = checkApplication(checkActivity(fragment));
        if (factory == null) {
            //不传入使用系统默认的viewModelFactory
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application);
        }
        return new ViewModelProvider(fragment.getViewModelStore(), factory);
    }
    
    @NonNull
    @MainThread
    public static ViewModelProvider of(@NonNull FragmentActivity activity,
            @Nullable Factory factory) {
        Application application = checkApplication(activity);
        if (factory == null) {
            //不传入使用系统默认的viewModelFactory
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application);
        }
        return new ViewModelProvider(activity.getViewModelStore(), factory);
    }
    ```
* ViewModelProvider调用factory创建viewModel对象。其中会调用activity或者fragment中的viewModelStore进行缓存
    ```java
    @NonNull
    @MainThread
    public <T extends ViewModel> T get(@NonNull String key, @NonNull Class<T> modelClass) {
        //从缓存中读取
        ViewModel viewModel = mViewModelStore.get(key);
    
        if (modelClass.isInstance(viewModel)) {
            //noinspection unchecked
            return (T) viewModel;
        } else {
            //noinspection StatementWithEmptyBody
            if (viewModel != null) {
                // TODO: log a warning.
            }
        }
        //调用factory进行创建viewModel对象
        viewModel = mFactory.create(modelClass);
        //写入缓存
        mViewModelStore.put(key, viewModel);
        //noinspection unchecked
        return (T) viewModel;
    }
    ```
* ViewModelStore内部其实就是维护了一个HashMap进行缓存ViewModel对象.ViewModelStore对象由各自宿主(Fragment或activity)进行维护
    ```java
    public class ViewModelStore {
    
        private final HashMap<String, ViewModel> mMap = new HashMap<>();
    
        final void put(String key, ViewModel viewModel) {
            ViewModel oldViewModel = mMap.put(key, viewModel);
            if (oldViewModel != null) {
                oldViewModel.onCleared();
            }
        }
    
        final ViewModel get(String key) {
            return mMap.get(key);
        }
    
        /**
         *  Clears internal storage and notifies ViewModels that they are no longer used.
         */
        public final void clear() {
            for (ViewModel vm : mMap.values()) {
                vm.onCleared();
            }
            mMap.clear();
        }
    }
    ```
    **Note:** 

    * 可以通过同一个activity提供出来viewModel以达到fragment之间的通讯
    * 可以通过一个全局的ViewModel(也就是自己创建ViewModelStore进行管理ViewModel对象，不与Activity和Fragemnt进行关联)，以达到全局的缓存

### 怎么做到切换配置不清空数据

在Activity或者Fragment的onDestory的时候，进行判断是否是配置进行更新

~~~java
```
//FragmentActivity的onDestory()
/**
 * Destroy all fragments.
 */
@Override
protected void onDestroy() {
    super.onDestroy();

    if (mViewModelStore != null && !isChangingConfigurations()) {
        mViewModelStore.clear();
    }

    mFragments.dispatchDestroy();
}

//Fragment的onDestory()
/**
 * Called when the fragment is no longer in use.  This is called
 * after {@link #onStop()} and before {@link #onDetach()}.
 */
@CallSuper
public void onDestroy() {
    mCalled = true;
    FragmentActivity activity = getActivity();
    boolean isChangingConfigurations = activity != null && activity.isChangingConfigurations();
    if (mViewModelStore != null && !isChangingConfigurations) {
        mViewModelStore.clear();
    }
}

```
~~~
## Dagger2中使用

由上面我可知ViewModel对象，必须通过ViewModelProvider的get方法进行获取。流程是这样的

![image-20181228215104244](/Users/robert/Library/Application Support/typora-user-images/image-20181228215104244.png)

1. viewModelProvider的get方法
2. 判断当前宿主中是否保存有这个ViewModel对象
3. 如果存在，直接返回，如果不存在则调用ViewModelFactory进行创建

所以我们想要注入Dagger2注入的ViewModel对象也保持对当前宿主的生命周期有感应，也就必须通过ViewModelProvider进行获取。是不可以直接通过Dagger2进行注入ViewModel实例的。所以我们需要另寻他法。

灵光一闪，ViewModel是通过ViewModelFactory进行创建的。我们来控制这个ViewModelFactory不就行了，说搞就搞。题外话，刚开始我想的是全局用一个ViewModelFactory，这样缓存的ViewModel的Map集合会很大。

所以还是按照常规操作，每个Activity或者Fragment都注入一个ViewModelFactory。这个ViewModelFactory持有与之对应的ViewModel的map集合

### 将ViewModel对象与之ViewModel的class对象进行绑定

* 声明ViewModel对象，用@Inject进行注解。Dagger2则会生成Provider<LoginSecondViewModel>对象。以便需要viewModel的时候进行创建。同时，我们也可以在构造函数中注入我们想要注入到当前ViewModel的对象。列如当前的app对象，或者其他任何已经在Dagger2中提供的对象。

```java
class LoginSecondViewModel @Inject constructor(app: Application) : BaseViewModel(app) {
    ...
}
```

* 使用@Binds+@IntoMap+@ViewModelKey注解，生成一个Key 为 ViewModel的class，value为 ViewModel为泛型 Provider的map 准备提供给ViewModelFactory

```java
@Module
internal abstract class LoginSecondViewModule{
    
    @Binds
    @IntoMap
    @ViewModelKey(LoginSecondViewModel::class)
    abstract fun bindLoginSecondViewModel(viewModel: LoginSecondViewModel): ViewModel
}
```

### 创建注入的ViewModelFactory

这个ViewModelFactory会接受ViewModel的provider。作为创建Dagger2提供ViewModel对象的一个入口。然后我们在基类里面，注入这个ViewModelFactory对象。给ViewModelProvider就好了

```java
/**
 * 构造中需要传递一个 Key 为 ViewModel的class value为 ViewModel为泛型 Provider 的map
 * Provider为Dagger的接口。意为一个对象的提供者，可通过，@Provider @Binds @Inject提供
 */
class ViewModelFactory @Inject constructor(
        private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>)
    : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        var creator: Provider<ViewModel>? = creators[modelClass]
        if (creator == null) {
            for ((key, value) in creators) {
                if (modelClass.isAssignableFrom(key)) {
                    creator = value
                    break
                }
            }
        }
        if (creator == null) throw IllegalArgumentException("unknown model class " + modelClass)
        try {
            return creator.get() as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
```

### 使用注入的ViewModelFactory创建ViewModel

```java
@Inject
lateinit var viewModelFactory: ViewModelProvider.Factory

//这里可以简化为ViewModelProviders.of(<宿主>，<Factory>)，获取ViewModelProvider对象
val viewModelProvider: ViewModelProvider by lazy {
    createViewModelProvider()
}

protected open fun createViewModelProvider(): ViewModelProvider {
   return SuperViewModelProvider(this, viewModelFactory, appViewModelProvider)
}
```

但是我们这里自己定义了一个ViewModelProvider

```java
/**
 * ViewModel提供者
 *
 * ViewModelStore有两个
 * Activity级的ViewModelStore，存非AppViewModel之外所有
 * Application级的ViewModelStore,只能存AppViewModel
 * Created by hzz on 2018/8/18.
 */
class SuperViewModelProvider(private val lifecycleOwner: LifecycleOwner, factory: Factory,
                             private val appViewModelProvider: ViewModelProvider? = null) : ViewModelProvider(
        if (lifecycleOwner is FragmentActivity) {
            lifecycleOwner
        } else if (lifecycleOwner is Fragment && lifecycleOwner.activity != null) {
            lifecycleOwner.activity!!
        } else {//activity is null
            throw RuntimeException("Fragment中创建ViewModel必须在onAttach之后")
        }, factory) {

    private lateinit var activity: FragmentActivity
    //各个工程自定义风格的处理者
    private var mCustomObserverProvider: IObserverProvider? = null

    init {
        if (lifecycleOwner is FragmentActivity) {
            activity = lifecycleOwner
        } else if (lifecycleOwner is Fragment) {
            activity = lifecycleOwner.activity!!
        }
    }

    //置为全局是为了同个ViewModelStore作用于相同的对象
    private val defaultProgressObserver by lazy {
        VMSetup.getInstance().defaultObserverProvider?.providerProgressObserver(activity)
                ?: DefaultProgressObserver(activity)
    }
    private val defaultToastObserver by lazy {
        VMSetup.getInstance().defaultObserverProvider?.providerToastObserver(activity)
                ?: DefaultToastObserver(activity)
    }
    private val defaultErrorObserver by lazy {
        VMSetup.getInstance().defaultObserverProvider?.providerErrorObserver(activity)
                ?: DefaultErrorObserver(activity)
    }
    private val defaultActivityObserver by lazy { DefaultActivityObserver(activity) }


    override fun <T : ViewModel?> get(key: String, modelClass: Class<T>): T {
        //需要获取的类是AppViewModel时,如果有AppViewModelProvider存取到App#ViewModelProvider，没有就存到Activity#ViewModelProvider里
        appViewModelProvider?.let {
            if (AppViewModel::class.java.isAssignableFrom(modelClass)) {
                //get viewModel from ApplicationViewModelStore
                return appViewModelProvider.get(modelClass)
            }
        }

        //get viewModel from ownerViewModelStore
        val vm = super.get(key, modelClass)//从ViewModelStore中取出ViewModel
        if (vm is BaseViewModel) {
            //如果是BaseViewModel处理一些默认监听，因为是Activity内共享的，监听时要考虑多个VM可能会造成的冲突
            //如果自定义的Observer提供者中有提供自己的处理就使用提供者的，否则就用默认的
            vm.progress.observeEvent(lifecycleOwner, mCustomObserverProvider?.providerProgressObserver(activity)
                    ?: defaultProgressObserver)
            vm.toast.observeEvent(lifecycleOwner, mCustomObserverProvider?.providerToastObserver(activity)
                    ?: defaultToastObserver)
            vm.error.defaultObserver(mCustomObserverProvider?.providerErrorObserver(activity)
                    ?: defaultErrorObserver)
            vm.activity.observeEvent(lifecycleOwner, defaultActivityObserver)
        }
        return vm
    }

    fun setCustomObserverProvider(provider: IObserverProvider) {
        this.mCustomObserverProvider = provider
    }
}
```

