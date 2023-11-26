package com.example.nightsky

data class PlanetJSON(
    val data: ObservationData
)

data class ObservationData(
    val dates: DateRange,
    val observer: Observer,
    val table: ObservationTable
)

data class DateRange(
    val from: String,
    val to: String
)

data class Observer(
    val location: Location
)

data class Location(
    val longitude: Double,
    val latitude: Double,
    val elevation: Double
)

data class ObservationTable(
    val header: List<String>,
    val rows: List<ObservationEntry>
)

data class ObservationEntry(
    val entry: EntryInfo,
    val cells: List<ObservationCell>
)

data class EntryInfo(
    val id: String,
    val name: String
)

data class ObservationCell(
    val date: String,
    val id: String,
    val name: String,
    val distance: DistanceInfo,
    val position: PositionInfo,
    val extraInfo: ExtraInfo
)

data class DistanceInfo(
    val fromEarth: EarthDistance
)

data class EarthDistance(
    val au: String,
    val km: String
)

data class PositionInfo(
    val horizontal: Coordinate,
    val horizonal: Coordinate,
    val equatorial: EquatorialCoordinate,
    val constellation: Constellation
)

data class Coordinate(
    val altitude: DegreeInfo,
    val azimuth: DegreeInfo
)

data class DegreeInfo(
    val degrees: String,
    val string: String
)

data class EquatorialCoordinate(
    val rightAscension: HourInfo,
    val declination: DegreeInfo
)

data class HourInfo(
    val hours: String,
    val string: String
)

data class Constellation(
    val id: String,
    val short: String,
    val name: String
)

data class ExtraInfo(
    val elongation: Double?,
    val magnitude: Double?
)
