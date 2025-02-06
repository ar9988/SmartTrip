import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime
import java.time.ZonedDateTime

@JsonClass(generateAdapter = true)
data class TripEntity(
    @Json(name = "tripId")
    val id: Int,

    @Json(name = "title")
    val title: String,

    @Json(name = "startDate")
    val startDate: ZonedDateTime,

    @Json(name = "endDate")
    val endDate: ZonedDateTime,

    @Json(name = "thumbnail")
    val thumbnail: String,

    @Json(name = "memberCount")
    val memberCount: Int,

    @Json(name = "status")
    val status: Boolean,

    @Json(name = "createdAt")
    val createdAt: ZonedDateTime
)
