package com.example.nightsky

class PictureOfDay(
    private var _copyright: String? = null,
    private var _date: String? = null,
    private var _explanation: String? = null,
    private var _hdurl: String? = null,
    private var _mediaType: String? = null,
    private var _serviceVersion: String? = null,
    private var _title: String? = null,
    private var _url: String? = null
) {
    var copyright: String?
        get() = _copyright
        set(value) {
            _copyright = value
        }

    var date: String?
        get() = _date
        set(value) {
            _date = value
        }

    var explanation: String?
        get() = _explanation
        set(value) {
            _explanation = value
        }

    var hdurl: String?
        get() = _hdurl
        set(value) {
            _hdurl = value
        }

    var mediaType: String?
        get() = _mediaType
        set(value) {
            _mediaType = value
        }

    var serviceVersion: String?
        get() = _serviceVersion
        set(value) {
            _serviceVersion = value
        }

    var title: String?
        get() = _title
        set(value) {
            _title = value
        }

    var url: String?
        get() = _url
        set(value) {
            _url = value
        }
}
