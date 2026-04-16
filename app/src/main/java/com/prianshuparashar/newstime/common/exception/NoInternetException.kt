package com.prianshuparashar.newstime.common.exception

import com.prianshuparashar.newstime.common.constant.Const
import java.io.IOException

class NoInternetException(message: String = Const.ERROR_NO_INTERNET) : IOException(message)