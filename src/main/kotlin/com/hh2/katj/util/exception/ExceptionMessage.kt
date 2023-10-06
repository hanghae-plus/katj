package com.hh2.katj.util.exception

enum class ExceptionMessage(message: String, status: Int) {
    ID_DOES_NOT_EXIST("id does not exist", 400),
    DUPLICATED_DATA_ALREADY_EXISTS("duplicated data already exists", 400),
    USER_DOES_NOT_EXIST("user does not exist", 400),
}