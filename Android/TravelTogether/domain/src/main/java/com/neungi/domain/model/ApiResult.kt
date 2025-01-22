package com.neungi.domain.model

data class ApiResult<out T>(
    val status: ApiStatus,
    val data: @UnsafeVariance T?,
    val message: String?
) {

    companion object {

        fun <T> success(data: T?): ApiResult<T> {
            return ApiResult(ApiStatus.SUCCESS, data, null)
        }

        fun <T> error(message: String, data: T?): ApiResult<T> {
            return ApiResult(ApiStatus.ERROR, data, message)
        }

        fun <T> fail(): ApiResult<T> {
            return ApiResult(ApiStatus.FAIL, null, null)
        }

        fun <T> loading(data: T?): ApiResult<T> {
            return ApiResult(ApiStatus.LOADING, data, null)
        }
    }
}