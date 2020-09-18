package com.vn.custom.activity.base

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vn.custom.util.Constant
import com.vn.custom.util.PreferenceUtils
import com.vn.eoffice.EOfficeApplication
import com.vn.eoffice.activity.login.LoginActivity
import com.vn.eoffice.base.Base
import com.vn.eoffice.enumApp.TypeLoadDataEnum
import com.vn.eoffice.extension.checkRequest
import com.vn.eoffice.extension.toJson
import com.vn.eoffice.model.base.BaseResponse
import com.vn.eoffice.model.database.EOBaseEntity
import com.vn.eoffice.model.file.DeleteFileRequestDTO
import com.vn.eoffice.model.login.LoginRequestDTO
import com.vn.eoffice.model.submission.DocumentAttachDTO
import com.vn.eoffice.service.ApiService
import com.vn.eoffice.service.ServiceRepository
import com.vn.eoffice.util.FileUtils
import com.vn.eoffice.util.FirebaseTokenHelper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.lang.reflect.Type

open class BaseViewModel(app: Application) : AndroidViewModel(app) {

    protected var mContext: Context = getApplication<Application>().applicationContext

    protected var mRequestCount = 0

    var showLoading: MutableLiveData<Boolean?> = MutableLiveData()
    var showDialogWarning: MutableLiveData<Throwable> = MutableLiveData()
    var showDialogThenAutoBack: MutableLiveData<String?> = MutableLiveData()
    var exceptionInvoke: MutableLiveData<Throwable?> = MutableLiveData()
    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    var preventShowError: Boolean = false
    var typeLoadDataEnum: TypeLoadDataEnum = TypeLoadDataEnum.NONE
        set(value) {
            field = value
            when (value) {
                TypeLoadDataEnum.LOAD_MORE -> {
                    basePageIndex++
                }
                TypeLoadDataEnum.REFRESH -> {
                    basePageIndex = 1
                    basePagingInfo = ""
                }
                TypeLoadDataEnum.NONE -> {
                    basePageIndex = 1
                    basePagingInfo = ""
                }
            }
        }
    var basePageIndex = 1
    var basePagingInfo = ""
    val loadingSubject: PublishSubject<Boolean> = PublishSubject.create()

    var countReLogin = 0

    init {
        loadingSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                handleLoading(it)
            }.apply { compositeDisposable.add(this) }
    }

    private fun handleLoading(show: Boolean) {
        showLoading?.value = show
    }

    protected fun showLoading() {
        if (typeLoadDataEnum == TypeLoadDataEnum.NONE)
            handleLoading(true)
    }

    fun setContext(context: Context) {
        mContext = context
    }

    protected fun hideLoading() {
        handleLoading(false)
    }

    protected fun showDialog(error: Throwable) {
        showDialogWarning?.value = error
    }

    protected fun showDialog(mess: String) {
        showDialogWarning?.value = Throwable(mess)
    }

    protected fun showDialogThenAutoBack(message: String?) {
        showDialogThenAutoBack?.value = message
    }

    protected fun showDialogError(t: Throwable) {
        hideLoading()
        showDialog(t)
    }

    protected val mApiService =
        ServiceRepository.createService(EOfficeApplication.appContext, ApiService::class.java)


    fun <T> handleRequestServiceObject(
        observerOffline: Observable<BaseResponse<T?>>?,
        observerOnLine: Observable<BaseResponse<T?>>,
        key: String,
        onResult: (T?) -> Unit
    ): Disposable? {
        return handleRequestServiceObject(observerOffline, observerOnLine, {
            PreferenceUtils.writeString(mContext, key, it.toJson())
        }, onResult)
    }


    fun <T> handleRequestServiceObject(
        observerOffline: Observable<BaseResponse<T?>>?,
        observerOnLine: Observable<BaseResponse<T?>>,
        saveLocal: (BaseResponse<T?>?) -> Unit,
        onResult: (T?) -> Unit
    ): Disposable? {
        if (observerOffline == null) {
            return handleRequest(
                observer = observerOnLine,
                isHideLoading = true,
                onResult = onResult,
                onFail = null,
                doOnNext = saveLocal
            )
        } else {
            return Observable.concat(
                observerOffline?.checkRequest(mContext),
                observerOnLine?.checkRequest(mContext, doOnNext = {
                    if (it?.status?.code == 200) {
                        saveLocal?.invoke(it)
                    }
                })
            )?.subscribe({
                onResult?.invoke(it)
                if (typeLoadDataEnum == TypeLoadDataEnum.REFRESH) {
                    typeLoadDataEnum = TypeLoadDataEnum.NONE
                }
            }, {
                handleThrowable(it, observerOnLine, true, onResult, null)
            }, {
                hideLoading()
            }).apply { compositeDisposable.add(this) }
        }
    }


    fun <T> handleRequestServiceObject(
        observer: Observable<BaseResponse<T?>>,
        onResult: (T?) -> Unit
    ): Disposable? {
        return handleRequestServiceObject(observer, true, onResult)
    }

    fun <T> handleRequestServiceObject(
        observer: Observable<BaseResponse<T?>>,
        onResult: (T?) -> Unit,
        onFail: ((Throwable) -> Unit)? = null
    ): Disposable? {
        return handleRequestServiceObject(observer, true, onResult, onFail)
    }

    fun <T> handleRequestServiceObject(
        observer: Observable<BaseResponse<T?>>,
        isHideLoading: Boolean,
        onResult: (T?) -> Unit,
        onFail: ((Throwable) -> Unit)? = null
    ): Disposable? {
        return handleRequest(observer, isHideLoading, onResult, onFail, null)
    }

    private fun <T> handleRequest(
        observer: Observable<BaseResponse<T?>>,
        isHideLoading: Boolean,
        onResult: (T?) -> Unit,
        onFail: ((Throwable) -> Unit)? = null,
        doOnNext: ((BaseResponse<T?>?) -> Unit)?
    ): Disposable? {
        return observer.checkRequest(mContext, doOnNext)?.subscribe({
            onResult.invoke(it)
            if (typeLoadDataEnum == TypeLoadDataEnum.REFRESH) {
                typeLoadDataEnum = TypeLoadDataEnum.NONE
            }
        }, {
            handleThrowable(it, observer, isHideLoading, onResult, onFail)
        }, {
            if (isHideLoading)
                hideLoading()
        })?.apply { compositeDisposable.add(this) }
    }

    private fun <T> handleThrowable(
        it: Throwable,
        observable: Observable<BaseResponse<T?>>,
        isHideLoading: Boolean,
        onResult: (T?) -> Unit,
        onFail: ((Throwable) -> Unit)?
    ) {
        if (it.cause?.message == Constant.ERROR_RELOGIN) {
            if (countReLogin == 0) {
                // recall api login to refresh token
                countReLogin++
                mApiService?.login(LoginRequestDTO().apply {
                    this.email = Base.username?.value?.email
                    this.password = Base.username?.value?.pw
                }).checkRequest(mContext)?.subscribe({
                    Base.username.value?.tokenLogin = it?.token
                    handleRequestServiceObject(observable, true, onResult, null)
                }, {
                    logout()
                })
            } else {
                showDialogThenAutoBack(it.message ?: "")
            }
        } else {
            if (it.message?.contains(Constant.KEY_ITEM_NULL) == true) {
                onResult.invoke(null)
                hideLoading()
            } else {
                hideLoading()
                if (it?.message?.contains("Fragment") == true) {
                    return
                }
                if (!preventShowError) {
                    showDialogError(it)
                }
                onFail?.invoke(it)
                exceptionInvoke?.value = it
            }
        }
    }

    fun logout() {
        FirebaseTokenHelper().deleteToken(mContext) {
            PreferenceUtils.writeBoolean(mContext, PreferenceUtils.KEY_IS_LOGOUT, true)
            var nMgr =
                mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            nMgr.cancelAll();
            var intent = Intent(mContext, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            mContext.startActivity(intent)
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        compositeDisposable.dispose()
    }

    fun uploadFile(
        key: String?,
        file: File,
        finished: (DocumentAttachDTO?) -> Unit
    ) {
        uploadFile(key, file, null, null, null, null, finished)
    }

    fun uploadFile(
        key: String?,
        file: File,
        listName: String?,
        fieldName: String?,
        id: String?,
        webUrl: String?,
        finished: (DocumentAttachDTO?) -> Unit
    ) {
        var mediaType = FileUtils.getMimeType(file.absolutePath)

        var requestFile = RequestBody.create(MediaType.parse(mediaType), file)

        val body = MultipartBody.Part.createFormData("File", file.name, requestFile)

        val listName = RequestBody.create(
            MultipartBody.FORM, listName ?: ""
        )
        val fieldName = RequestBody.create(
            MultipartBody.FORM, fieldName ?: ""
        )
        val key = RequestBody.create(
            MultipartBody.FORM, key ?: ""
        )
        val id = RequestBody.create(
            MultipartBody.FORM, id ?: ""
        )
        val webUrl = RequestBody.create(
            MultipartBody.FORM, webUrl ?: ""
        )
        handleRequestServiceObject(
            mApiService?.uploadImage(
                body,
                listName,
                fieldName,
                key,
                id,
                webUrl
            )
        ) {
            hideLoading()
            finished?.invoke(it?.getOrNull(0))
        }
    }

    fun deleteFile(urlRelative: String?, finished: (Boolean?) -> Unit) {
        handleRequestServiceObject(mApiService?.deleteFile(DeleteFileRequestDTO().apply {
            this.filePath = urlRelative
        })) {
            finished?.invoke(it)
        }
    }

    fun deleteFile(rq: DeleteFileRequestDTO, finished: (Boolean?) -> Unit) {
        handleRequestServiceObject(mApiService?.deleteFile(rq)) {
            finished?.invoke(it)
        }
    }

    fun createTable() {
        Observable.create<Unit> {
            try {
                val db = EOfficeApplication.db?.openHelper?.writableDatabase
                val sql = EOBaseEntity.BASE_TABLE_CREATE_SQL.replace(
                    EOBaseEntity.BASE_TABLE_NAME_PLACEHOLDER,
                    this.javaClass.simpleName
                )
                db?.execSQL(sql)
                it.onComplete()
            } catch (ex: Exception) {
                it.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorResumeNext(Function {
                return@Function Observable.create<Unit> { it.onComplete() }
            })
            .subscribe({
//                observableEmitter.onComplete()
            }, {
//                observableEmitter.onComplete()
                it.printStackTrace()
            }, {
            }).apply {
                compositeDisposable.add(this)
            }
    }

    fun <T> getLocalData(
        iD: String,
        type: Type,
        showLoading: Boolean? = false
    ): Observable<BaseResponse<T?>> {
        val obv = Observable.create<BaseResponse<T?>> {
            try {
                if (typeLoadDataEnum == TypeLoadDataEnum.REFRESH) {
                    it.onComplete()
                    return@create
                }
                val db = EOfficeApplication.db?.openHelper?.writableDatabase
                val cursor = db?.query(
                    "SELECT * FROM ${javaClass.simpleName} WHERE ${EOBaseEntity.BASE_TABLE_COL_ID} = \'$iD\'",
                    null
                )
                var isHaveData = false
                if ((cursor?.count ?: 0) > 0 && cursor?.moveToLast() == true) {
                    val result = cursor.getString(1) ?: ""
                    val data = Gson().fromJson<BaseResponse<T?>>(result, type)
                    if (data.result is MutableList<*>) {
                        if ((data.result as MutableList<*>).size > 0) {
                            it.onNext(data)
                            isHaveData = true
                        }
                    } else {
                        it.onNext(data)
                        isHaveData = true
                    }
                }
                if (showLoading == true && !isHaveData) {
                    loadingSubject.onNext(true)
                } else {
//                    loadingSubject.onNext(false)
                }
                it.onComplete()
            } catch (ex: Exception) {
                createTable()
                it.onComplete()
            }
        }.onErrorResumeNext(Function {
            return@Function Observable.create<BaseResponse<T?>> { it.onComplete() }
        })


        return obv
    }


    fun <T> saveToDB(id: String, result: T) {
        val entity = EOBaseEntity().apply {
            this.iD = id
            value = Gson().toJson(result)
        }
        EOfficeApplication.db?.openHelper?.writableDatabase?.let {
            entity.insertRow(it, javaClass.simpleName)
        }
    }
}
